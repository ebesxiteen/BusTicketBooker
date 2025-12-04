package com.example.ticketbooker.Service.ServiceImp;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.ticketbooker.DTO.Trips.AddTripDTO;
import com.example.ticketbooker.DTO.Trips.RequestIdTripDTO;
import com.example.ticketbooker.DTO.Trips.ResponseTripDTO;
import com.example.ticketbooker.DTO.Trips.SearchTripRequest;
import com.example.ticketbooker.DTO.Trips.TripDTO;
import com.example.ticketbooker.DTO.Trips.TripStatsDTO;
import com.example.ticketbooker.DTO.Trips.UpdateTripDTO;
import com.example.ticketbooker.Entity.Buses;
import com.example.ticketbooker.Entity.Routes;
import com.example.ticketbooker.Entity.Trips;
import com.example.ticketbooker.Repository.InvoiceRepo;
import com.example.ticketbooker.Repository.SeatsRepo;
import com.example.ticketbooker.Repository.TicketRepo;
import com.example.ticketbooker.Repository.TripRepo;
import com.example.ticketbooker.Service.BusService;
import com.example.ticketbooker.Service.DriverService;
import com.example.ticketbooker.Service.RouteService;
import com.example.ticketbooker.Service.TripService;
import com.example.ticketbooker.Util.Enum.TripStatus;
import com.example.ticketbooker.Util.Mapper.TripMapper;

@Service
public class TripServiceImp implements TripService {
    @Autowired
    private TripRepo tripRepo;

    @Autowired
    private RouteService routeService;

    @Autowired 
    private BusService busService;

    @Autowired
    private DriverService driverService; 

    @Autowired
    private TicketRepo ticketRepo;

    @Autowired
    private SeatsRepo seatRepo;

    @Autowired
    private InvoiceRepo invoiceRepo;

    private boolean checkDriverConflict(Integer driverId, LocalDateTime newDepartureTime, LocalDateTime newArrivalTime, Integer tripIdToExclude) {
        long conflictCount = tripRepo.countConflictingTripsForDriver(
            driverId, 
            newDepartureTime,
            newArrivalTime, 
            tripIdToExclude
        );
        return conflictCount > 0;
    }

    // --- CÁC PHƯƠNG THỨC TRUY VẤN (ĐÃ FIX LOGIC FIND VÀ OPTIONAL) ---

    @Override
    public ResponseTripDTO getTripById(int tripId) {
        ResponseTripDTO result = new ResponseTripDTO();
        try {
            // Sửa lỗi: findAllById(int) không đúng, dùng findById và chuyển thành List 1 phần tử
            Trips trip = this.tripRepo.findById(tripId).orElse(null);
            if (trip != null) {
                result = TripMapper.toResponseDTO(new ArrayList<>(List.of(trip)));
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        return result;
    }

    @Override
    public ResponseTripDTO getAllTrips() {
        ResponseTripDTO result = new ResponseTripDTO();
        try {
            List<Trips> trips = this.tripRepo.findAll();
            result = TripMapper.toResponseDTO(new ArrayList<>(trips));
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        return result;
    }
    
    @Override
    @Transactional 
    public boolean addTrip(AddTripDTO dto) {
        try {
            Integer busId = dto.getBus().getId();
            LocalDateTime newDepartureTime = dto.getDepartureTime();
            
            Routes route = routeService.getRoute(dto.getRoute().getRouteId());
            Integer busCapacity = busService.getBusCapacityById(busId); 
            
            if (route == null || busCapacity == null) {
                throw new RuntimeException("Route hoặc Bus ID không tồn tại.");
            }
            
            LocalTime estimatedTime = route.getEstimatedTime(); 
            
            LocalDateTime newEndTime = newDepartureTime
                                                 .plusHours(estimatedTime.getHour())
                                                 .plusMinutes(estimatedTime.getMinute());

            List<Trips> existingTrips = tripRepo.findScheduledOrInProgressTripsForBus(busId);
            
            for (Trips existingTrip : existingTrips) {
                // Sửa lỗi: Nếu ArrivalTime chưa được tính và lưu, cần tính lại
                LocalDateTime existingEndTime = existingTrip.getArrivalTime();
                if (existingEndTime == null) {
                    LocalTime existingEstimatedTime = existingTrip.getRoute().getEstimatedTime();
                    existingEndTime = existingTrip.getDepartureTime()
                        .plusHours(existingEstimatedTime.getHour())
                        .plusMinutes(existingEstimatedTime.getMinute());
                }
                LocalDateTime existingStartTime = existingTrip.getDepartureTime();

                boolean isOverlapping = 
                    newDepartureTime.isBefore(existingEndTime) && 
                    newEndTime.isAfter(existingStartTime);

                if (isOverlapping) {
                    throw new RuntimeException("Xe (ID " + busId + ") đã có lịch chạy trùng: " + existingTrip.getId() +
                                               " (Thời gian: " + existingStartTime + " đến " + existingEndTime + ").");
                }
            }

            Trips newTrip = TripMapper.toEntity(dto);
            
            newTrip.setArrivalTime(newEndTime); 
            newTrip.setAvailableSeats(busCapacity); 
            
            this.tripRepo.save(newTrip); 
            
        } catch (Exception e) {
            System.out.println("Error adding trip: " + e.getMessage());
            throw new RuntimeException(e.getMessage()); 
        }
        return true; 
    }

    @Override
    @Transactional
    public boolean updateTrip(UpdateTripDTO updateTripDTO) {
        // 1. Lấy thông tin chuyến xe hiện tại
        Trips existingTrip = tripRepo.findById(updateTripDTO.getTripId())
                .orElseThrow(() -> new RuntimeException("Trip not found"));

        Integer oldBusId = existingTrip.getBus().getId();
        Integer newBusId = updateTripDTO.getBus().getId();
        Integer newDriverId = updateTripDTO.getDriver().getDriverId();
        LocalDateTime newDepartureTime = updateTripDTO.getDepartureTime();
        
        // --- BẮT ĐẦU FIX: Xử lý ArrivalTime NULL ---
        LocalDateTime newArrivalTime = updateTripDTO.getArrivalTime(); 
        
        // Nếu ArrivalTime mới (từ DTO) là NULL, giữ lại ArrivalTime cũ (từ DB)
        if (newArrivalTime == null) {
             newArrivalTime = existingTrip.getArrivalTime();
        }
        
        // Nếu ArrivalTime cũ cũng NULL, đây là lỗi dữ liệu hoặc logic
        if (newArrivalTime == null) {
             throw new RuntimeException("Lỗi: Arrival Time không hợp lệ (NULL). Vui lòng cung cấp Arrival Time hợp lệ hoặc tính toán dựa trên Route.");
        }
        // --- KẾT THÚC FIX ---
        
        // --- VALIDATION LOGIC 1: KIỂM TRA TÀI XẾ TRÙNG LỊCH ---
        if (!existingTrip.getDriver().getDriverId().equals(newDriverId) || 
            !existingTrip.getDepartureTime().equals(newDepartureTime) || 
            !existingTrip.getArrivalTime().equals(newArrivalTime)) 
        {
             if (this.checkDriverConflict(newDriverId, newDepartureTime, newArrivalTime, updateTripDTO.getTripId())) {
                 throw new RuntimeException("Lỗi: Tài xế đã có lịch trình khác trùng với thời gian chuyến đi này!");
             }
        }
        
       // --- VALIDATION ĐỔI XE ---
        if (!oldBusId.equals(newBusId)) {
            // A. Lấy thông tin xe mới
            Buses newBus = busService.getBusEntityById(newBusId);
            if (newBus == null) throw new RuntimeException("Xe mới không tồn tại");
            
            // B. Lấy danh sách mã ghế khách CŨ đã đặt
            List<String> bookedSeatCodes = ticketRepo.findBookedSeatCodesByTripId(updateTripDTO.getTripId());

            if (!bookedSeatCodes.isEmpty()) {
                // C. VALIDATE: Kiểm tra xem các ghế đã đặt có tồn tại trên xe mới không?
                Set<String> allowedNewSeats = this.generateExpectedSeatCodes(newBus.getCapacity());

                List<String> conflictSeats = new ArrayList<>();
                for (String code : bookedSeatCodes) {
                    if (!allowedNewSeats.contains(code)) {
                        conflictSeats.add(code);
                    }
                }

                if (!conflictSeats.isEmpty()) {
                    throw new RuntimeException("Không thể đổi xe! Các ghế sau đã bán (" + conflictSeats.size() + 
                        " ghế) nhưng không tồn tại trên cấu trúc xe mới: " + String.join(", ", conflictSeats));
                }
            }
            
            //UPDATE DB
            existingTrip.setBus(newBus);
            
            // Tính lại số ghế trống: Tổng ghế xe mới - Số ghế đang bị Book
            int currentBookedCount = bookedSeatCodes.size();
            existingTrip.setAvailableSeats(newBus.getCapacity() - currentBookedCount);
            
        }
            
        // 3. Cập nhật các trường thông thường khác
        existingTrip.setRoute(routeService.getRoute(updateTripDTO.getRoute().getRouteId()));
        existingTrip.setDriver(driverService.getDriver(newDriverId));
        existingTrip.setDepartureStation(updateTripDTO.getDepartureStation());
        existingTrip.setArrivalStation(updateTripDTO.getArrivalStation());
        existingTrip.setDepartureTime(newDepartureTime);
        existingTrip.setArrivalTime(newArrivalTime); // Đã là giá trị không NULL
        existingTrip.setPrice(updateTripDTO.getPrice());
        if(updateTripDTO.getTripStatus()==TripStatus.CANCELLED&&existingTrip.getTripStatus() != TripStatus.CANCELLED &&existingTrip.getTripStatus() != TripStatus.COMPLETED){
            this.cancelTrip(updateTripDTO.getTripId());
        }else if(updateTripDTO.getTripStatus()==TripStatus.CANCELLED){
            throw new RuntimeException("Chuyến xe này không được phép Hủy");
        }
        existingTrip.setTripStatus(updateTripDTO.getTripStatus());

        // 4. Lưu thay đổi
        tripRepo.save(existingTrip);
        return true;
    }

    @Override
    public boolean deleteTrip(RequestIdTripDTO dto) {
        try {
            Integer tripId = dto.getTripId();

            // BƯỚC 1: KIỂM TRA "CÓ VÉ KHÔNG?"
            long count = ticketRepo.countBookedOrUsedTicketsByTripId(tripId);
            
            if (count > 0) {
                // NẾU CÓ VÉ -> CHẶN NGAY LẬP TỨC
                throw new RuntimeException("Không thể xóa! Chuyến xe này đang có " + count + " vé đã bán hoặc đang giữ chỗ. Vui lòng sử dụng chức năng 'Hủy chuyến' hoặc liên hệ quản trị viên.");
            }

            this.cancelTrip(tripId);

            // 2. LƯU LẠI ID INVOICE CẦN XÓA (Bước quan trọng)
            // Vì quan hệ 1-1: 10 vé hủy sẽ ra 10 ID invoice khác nhau
            List<Integer> invoiceIds = invoiceRepo.findInvoiceIdsByTripId(tripId);

            // 3. XÓA VÉ (Giải phóng khóa ngoại)
            ticketRepo.deleteAllByTripId(tripId);

            // 4. XÓA INVOICE
            // Bây giờ vé đã mất, nhưng ta đã kịp lưu ID invoice ở bước 2 rồi
            if (!invoiceIds.isEmpty()) {
                invoiceRepo.deleteAllById(invoiceIds);
            }

            // 5. XÓA GHẾ VÀ CHUYẾN XE
            seatRepo.deleteAllByTripId(tripId);
            tripRepo.deleteById(tripId);
            
            return true;
            
        } catch (Exception e) {
            throw new RuntimeException("Lỗi xóa chuyến xe: " + e.getMessage());
        }
    }

    @Override
    public Trips getTripById(Integer tripId) {
        return tripRepo.findById(tripId).orElse(null);
    }


    public Trips getTripByIdpath(int tripId) {
        return tripRepo.findById(tripId).orElse(null);
    }

    @Override
    public ResponseTripDTO getTripByIds(int tripId) {
        ResponseTripDTO result = new ResponseTripDTO();
        try {
            Trips trip = this.tripRepo.findById(tripId).orElse(null);
            if (trip != null) {
                result = TripMapper.toResponseDTO(new ArrayList<>(List.of(trip)));
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        return result;
    }

    @Override
    @Transactional(readOnly = true)
    public ResponseTripDTO searchTrip(SearchTripRequest dto) {
        ResponseTripDTO result = new ResponseTripDTO();
        try {
            int seats = dto.getTicketQuantity() > 0 ? dto.getTicketQuantity() : 1;
            LocalDateTime startDate = dto.getDepartureDate() != null ? dto.getDepartureDate() : LocalDateTime.now();

            List<Trips> trips;

            if (dto.getKeyword() != null && !dto.getKeyword().trim().isEmpty()) {
                trips = tripRepo.findTripsByKeyword(
                        dto.getKeyword().trim(),
                        startDate,
                        seats
                );
            } else {
                trips = tripRepo.findTripsFlexible(
                        dto.getArrival(),
                        dto.getDeparture(),
                        startDate,
                        seats
                );
            }
            
            for (Trips t : trips) {
                 if (t.getRoute() != null) {
                     t.getRoute().getEstimatedTime(); 
                 }
            }

            result = TripMapper.toResponseDTO(new ArrayList<>(trips));

        } catch (Exception e) {
            System.out.println("Error searching trip: " + e.getMessage());
        }
        return result;
    }

    @Override
    public TripStatsDTO getTripStats(String period, LocalDate selectedDate) {
        LocalDateTime startDate = selectedDate.atStartOfDay();
        LocalDateTime endDate;

        LocalDateTime prevStartDate;
        LocalDateTime prevEndDate;

        switch (period) {
            case "Day":
                endDate = startDate.plusDays(1);
                prevStartDate = startDate.minusDays(1);
                prevEndDate = endDate.minusDays(1);
                break;
            case "Month":
                endDate = startDate.plusMonths(1);
                prevStartDate = startDate.minusMonths(1);
                prevEndDate = endDate.minusMonths(1);
                break;
            case "Year":
                startDate = selectedDate.withDayOfYear(1).atStartOfDay(); 
                endDate = startDate.plusYears(1); 
                prevStartDate = startDate.minusYears(1); 
                prevEndDate = endDate.minusYears(1); 
                break;
            default:
                throw new IllegalArgumentException("Invalid period: " + period);
        }

        long currentPeriodCount = tripRepo.countTripsByDepartureTimeBetween(startDate, endDate);
        long previousPeriodCount = tripRepo.countTripsByDepartureTimeBetween(prevStartDate, prevEndDate);

        return TripStatsDTO.builder()
                .period(period)
                .selectedDate(selectedDate)
                .currentPeriodTripCount(currentPeriodCount)
                .previousPeriodTripCount(previousPeriodCount)
                .build();
    }

    @Override
    @Transactional
    public void updateAvailableSeats(Integer tripId, int delta) {
        Trips trip = tripRepo.findById(tripId)
                .orElseThrow(() -> new RuntimeException("Trip not found: " + tripId));

        Integer current = trip.getAvailableSeats();
        if (current == null) current = 0;

        int updated = current + delta; 
        if (updated < 0) updated = 0; 

        trip.setAvailableSeats(updated);
        tripRepo.save(trip);
    }

 @Override
public Page<TripDTO> getAllTrips(String status, Pageable pageable) {
    Page<Trips> tripsPage;

    if (status != null && !status.isEmpty() && !status.equalsIgnoreCase("ALL")) {
        try {
            TripStatus statusEnum = TripStatus.valueOf(status); 
            tripsPage = tripRepo.findByTripStatus(statusEnum, pageable);
            
        } catch (IllegalArgumentException e) {
            tripsPage = tripRepo.findAll(pageable);
        }
    } else {
        // Trường hợp ALL hoặc null
        tripsPage = tripRepo.findAll(pageable);
    }
    
    return tripsPage.map(TripMapper::toDTO);
}

@Override
public Page<TripDTO> getAllTrips(Pageable pageable) {
    return getAllTrips("ALL", pageable);
}

// Hàm này copy từ TripServiceImp qua (để dùng chung logic sinh ghế)
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
@Override
    @Transactional
    public boolean cancelTrip(Integer tripId) {
        try {
            // 1. Tìm chuyến xe
            Trips trip = tripRepo.findById(tripId)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy chuyến xe ID: " + tripId));

            // 2. Hủy các Hóa đơn (Invoices) liên quan đến chuyến xe
            invoiceRepo.cancelInvoicesByTripId(tripId);

            // 3. Chuyển trạng thái vé (Tickets) -> CANCELLED
            ticketRepo.cancelBookedTicketsByTripId(tripId);

            // 4. Xóa ghế (Seats) để giải phóng dữ liệu (hoặc giữ lại tùy logic, nhưng bạn muốn xóa)
            // Nếu có bảng trung gian ticket_seats, nhớ xử lý như lưu ý ở bài trước
            seatRepo.deleteAllByTripId(tripId);

            // 5. Cuối cùng: Cập nhật trạng thái Chuyến xe -> CANCELLED
            trip.setTripStatus(TripStatus.CANCELLED);
            trip.setAvailableSeats(0); // Set về 0 cho chắc chắn
            tripRepo.save(trip);

            return true;
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Lỗi khi hủy chuyến: " + e.getMessage());
        }
    }
}