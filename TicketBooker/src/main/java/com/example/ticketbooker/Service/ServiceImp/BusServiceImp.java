package com.example.ticketbooker.Service.ServiceImp;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.ticketbooker.DTO.Bus.BusDTO;
import com.example.ticketbooker.Entity.Buses;
import com.example.ticketbooker.Entity.Trips;
import com.example.ticketbooker.Repository.BusRepo;
import com.example.ticketbooker.Repository.SeatsRepo;
import com.example.ticketbooker.Repository.TicketRepo;
import com.example.ticketbooker.Repository.TripRepo;
import com.example.ticketbooker.Service.BusService;
import com.example.ticketbooker.Util.Enum.BusStatus;
import com.example.ticketbooker.Util.Enum.BusType;
import com.example.ticketbooker.Util.Mapper.BusMapper;

@Service
public class BusServiceImp implements BusService {

    @Autowired
    private BusRepo busRepository;

    @Autowired
    private TripRepo tripRepo; 
    
    @Autowired
    private SeatsRepo seatsRepo;

    @Autowired
    private TicketRepo ticketRepo;

    @Override
    public List<BusDTO> getAllBuses() {
        List<Buses> buses = busRepository.findAll();
        List<BusDTO> dtos = new ArrayList<>();
        buses.forEach(bus -> dtos.add(BusMapper.toDTO(bus)));
        return dtos;
    }

    @Override
    public Page<BusDTO> getAllBuses(Pageable pageable) {
        Page<Buses> buses = busRepository.findAll(pageable);
        return buses.map(BusMapper::toDTO);
    }

    @Override
    public BusDTO getBusById(Integer id) {
        return busRepository.findById(id)
                      .map(BusMapper::toDTO) 
                      .orElse(null);
    }

    @Override
    public BusDTO createBus(BusDTO busDTO) {
        Buses bus = BusMapper.toEntity(busDTO);
        Buses savedBus = busRepository.save(bus);
        return BusMapper.toDTO(busRepository.save(bus));
    }

    @Override
    @Transactional // Quan trọng để đảm bảo tính toàn vẹn
    public boolean updateBus(BusDTO busDTO) {
        try {
            // 1. Lấy thông tin xe cũ từ DB
            Buses existingBus = busRepository.findById(busDTO.getId())
                    .orElseThrow(() -> new RuntimeException("Bus not found"));

            int oldCapacity = existingBus.getCapacity();
            int newCapacity = busDTO.getCapacity();

            // 2. NẾU SỨC CHỨA GIẢM ĐI -> Cần kiểm tra xung đột
            if (newCapacity < oldCapacity) {
                
                // a. Tìm tất cả các chuyến xe (SCHEDULED) đang dùng xe này
                List<Trips> futureTrips = tripRepo.findScheduledOrInProgressTripsForBus(existingBus.getId());

                // b. Tạo tập hợp các ghế hợp lệ cho sức chứa MỚI
                Set<String> allowedSeatCodes = this.generateExpectedSeatCodes(newCapacity);

                // c. Duyệt qua từng chuyến xe để kiểm tra vé đã đặt
                for (Trips trip : futureTrips) {
                    // Lấy danh sách ghế đã được đặt (Booked) của chuyến này
                     List<String> bookedSeatCodes = ticketRepo.findBookedSeatCodesByTripId(trip.getId());
                    
                    // Kiểm tra từng ghế đã đặt xem có nằm trong phạm vi cho phép mới không
                    List<String> invalidSeats = new ArrayList<>();
                    for (String bookedSeat : bookedSeatCodes) {
                        if (!allowedSeatCodes.contains(bookedSeat)) {
                            invalidSeats.add(bookedSeat);
                        }
                    }

                    // Nếu có ghế vi phạm -> Báo lỗi và CHẶN cập nhật ngay lập tức
                    if (!invalidSeats.isEmpty()) {
                        throw new RuntimeException("Không thể giảm số ghế xuống " + newCapacity + 
                            "! Xe này đang chạy chuyến #" + trip.getId() + 
                            " có các ghế đã bán nằm ngoài phạm vi mới: " + String.join(", ", invalidSeats));
                    }
                }
            }

            // 3. Nếu mọi thứ ổn (hoặc sức chứa tăng lên), cập nhật DB
            Buses busToUpdate = BusMapper.toEntity(busDTO);
            
            // Lưu ý: Mapper tạo object mới nên có thể mất các quan hệ cũ nếu không cẩn thận.
            // Tốt nhất là update từng field của existingBus
            existingBus.setLicensePlate(busDTO.getLicensePlate());
            existingBus.setBusType(busDTO.getBusType());
            existingBus.setCapacity(busDTO.getCapacity());
            existingBus.setBusStatus(busDTO.getBusStatus());
            // existingBus.setRoute(...) // Nếu có sửa route

            this.busRepository.save(existingBus);
            
            // 4. (Tùy chọn) Nếu sức chứa thay đổi, có thể cần update lại availableSeats của các chuyến xe tương lai?
            // Logic này tùy thuộc vào bạn:
            // - Nếu tăng ghế: Có nên tự động tăng availableSeats cho các chuyến chưa chạy không?
            // - Nếu giảm ghế (hợp lệ): Có nên tự động giảm availableSeats không?
            // -> Thường là CÓ để dữ liệu đồng bộ.
            if (newCapacity != oldCapacity) {
                updateAvailableSeatsForFutureTrips(existingBus.getId(), newCapacity);
            }

        } catch (Exception e) {
            // Ném lỗi ra để Controller bắt được và hiện lên màn hình
            throw new RuntimeException(e.getMessage());
        }
        return true;
    }

    // Hàm phụ để cập nhật lại availableSeats cho các chuyến xe
    private void updateAvailableSeatsForFutureTrips(Integer busId, int newCapacity) {
        List<Trips> futureTrips = tripRepo.findScheduledOrInProgressTripsForBus(busId);
        for (Trips trip : futureTrips) {
            // Tính số vé đã bán
            long soldCount = seatsRepo.countBookedSeatsByTripId(trip.getId()); // Cần hàm đếm
            
            // Cập nhật lại ghế trống
            int newAvailable = newCapacity - (int) soldCount;
            if (newAvailable < 0) newAvailable = 0; // Just in case
            
            trip.setAvailableSeats(newAvailable);
            tripRepo.save(trip);
        }
    }

    @Override
    public void deleteBus(Integer id) {
        busRepository.deleteById(id);
    }

    @Override
    public Optional<Integer> getBusIdByLicensePlate(String licensePlate) {
        return busRepository.findByLicensePlate(licensePlate).map(Buses::getId); // Use map to extract the ID directly
    }

    @Override
    public Page<BusDTO> getBusesByLicensePlateContaining(String licensePlate, Pageable pageable) {
        Page<Buses> buses = busRepository.findByLicensePlateContainingIgnoreCase(licensePlate, pageable);
        return buses.map(BusMapper::toDTO);
    }

    @Override
    public Integer getBusCapacityById(Integer busId) {
        // Sử dụng findById và trả về capacity (hoặc null nếu không tìm thấy)
        Optional<Buses> bus = busRepository.findById(busId);
        return bus.map(Buses::getCapacity).orElse(null);
    }
    @Override
    public Buses getBusEntityById(Integer id) {
        return busRepository.findById(id).orElse(null);
    }

    @Override
    public Page<BusDTO> searchBuses(String keyword, String statusStr, String typeStr, Pageable pageable) {
        BusStatus statusEnum = null;
        BusType typeEnum = null;

        // Convert String -> Enum
        if (statusStr != null && !statusStr.equals("ALL") && !statusStr.isEmpty()) {
            try { statusEnum = BusStatus.valueOf(statusStr); } catch (Exception e) {}
        }
        
        if (typeStr != null && !typeStr.equals("ALL") && !typeStr.isEmpty()) {
            try { typeEnum = BusType.valueOf(typeStr); } catch (Exception e) {}
        }

        // Gọi Repo
        Page<Buses> buses = busRepository.findWithFilter(keyword, statusEnum, typeEnum, pageable);
        return buses.map(BusMapper::toDTO);
    }
    private Set<String> generateExpectedSeatCodes(int capacity) {
        Set<String> validSeats = new HashSet<>();
        int seatsPerFloor = capacity / 2;
        
        // Tầng A
        for (int i = 1; i <= seatsPerFloor; i++) {
            validSeats.add(String.format("A%02d", i)); // A01...
        }
        // Tầng B (nếu còn)
        for (int i = 1; i <= (capacity - seatsPerFloor); i++) {
            validSeats.add(String.format("B%02d", i)); // B01...
        }
        return validSeats;
    }
}
