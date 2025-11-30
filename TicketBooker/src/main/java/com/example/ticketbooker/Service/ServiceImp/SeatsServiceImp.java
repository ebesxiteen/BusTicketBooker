package com.example.ticketbooker.Service.ServiceImp;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.ticketbooker.DTO.Seats.AddSeatDTO;
import com.example.ticketbooker.Entity.Seats;
import com.example.ticketbooker.Entity.Trips;
import com.example.ticketbooker.Repository.SeatsRepo;
import com.example.ticketbooker.Service.SeatsService;
import com.example.ticketbooker.Service.TripService;
import com.example.ticketbooker.Util.Mapper.SeatsMapper;

@Service
public class SeatsServiceImp implements SeatsService {

    @Autowired
    private SeatsRepo seatsRepository;

    @Autowired
    private SeatsMapper seatsMapper;

    @Autowired
    private TripService tripService;

   @Override
public List<Integer> addSeats(AddSeatDTO addSeatDTO) {
    List<Integer> seatIds = new ArrayList<>();

    Trips trip = tripService.getTripById(addSeatDTO.getTripId());
    if (trip == null) {
        throw new IllegalArgumentException("Trip ID không tồn tại");
    }

    String raw = addSeatDTO.getSeatCode() != null ? addSeatDTO.getSeatCode().trim() : "";
    if (raw.isEmpty()) {
        throw new IllegalArgumentException("Chưa có ghế nào được chọn");
    }

    // hỗ trợ "A01 A02" hoặc "A01,A02"
    String[] seatCodes = raw.split("[,\\s]+");

    for (String seatCode : seatCodes) {
        if (seatCode.isBlank()) continue;

        if (seatsRepository.existsByTripIdAndSeatCode(trip.getId(), seatCode)) {
            throw new IllegalArgumentException("Ghế " + seatCode + " đã được đặt, vui lòng chọn ghế khác.");
        }

        Seats seat = Seats.builder()
                .trip(trip)
                .seatCode(seatCode)
                .build();
        Seats savedSeat = seatsRepository.save(seat);
        seatIds.add(savedSeat.getId());
    }

    return seatIds;
}


    @Override
    public List<String> getBookedSeatsForTrip(Integer tripId) {
        // Truy vấn ghế đã được đặt cho chuyến đi với tripId
        List<Seats> bookedSeats = seatsRepository.findByTripId(tripId);

        // Trả về danh sách mã ghế đã đặt
        return bookedSeats.stream()
                .map(Seats::getSeatCode)  // Lấy mã ghế từ đối tượng Seats
                .collect(Collectors.toList());
    }

    @Override
    public Seats getSeatById(int id) {
        return seatsRepository.findById(id).orElse(null);
    }

    @Override
    public void deleteSeat(int id) {
        try {
            seatsRepository.deleteById(id);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
