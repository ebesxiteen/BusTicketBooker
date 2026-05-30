package com.example.ticketbooker.Service.ServiceImp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
    @Transactional
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

        String[] seatCodes = raw.split("[,\\s]+");

        for (String seatCode : seatCodes) {
            if (seatCode.isBlank()) continue;

            if (seatsRepository.existsByTripIdAndSeatCode(trip.getId(), seatCode)) {
                throw new IllegalArgumentException("Ghế " + seatCode + " đã được đặt, vui lòng chọn ghế khác.");
            }

            try {
                Seats seat = Seats.builder()
                        .trip(trip)
                        .seatCode(seatCode)
                        .build();
                Seats savedSeat = seatsRepository.saveAndFlush(seat);
                seatIds.add(savedSeat.getId());
            } catch (DataIntegrityViolationException e) {
                throw new IllegalArgumentException("Seat " + seatCode + " is already booked.", e);
            }
        }

        return seatIds;
    }

    @Override
    @Transactional
    public List<Integer> holdSeats(AddSeatDTO addSeatDTO, int holdSeconds) {
        List<Integer> seatIds = new ArrayList<>();

        Trips trip = tripService.getTripById(addSeatDTO.getTripId());
        if (trip == null) {
            throw new IllegalArgumentException("Trip ID không tồn tại");
        }

        String raw = addSeatDTO.getSeatCode() != null ? addSeatDTO.getSeatCode().trim() : "";
        if (raw.isEmpty()) {
            throw new IllegalArgumentException("Chưa có ghế nào được chọn");
        }

        seatsRepository.cleanupZombieSeats();
        LocalDateTime holdExpiresAt = LocalDateTime.now().plusSeconds(holdSeconds);
        String[] seatCodes = raw.split("[,\\s]+");

        for (String seatCode : seatCodes) {
            if (seatCode.isBlank()) continue;

            if (seatsRepository.countUnavailableSeatByTripIdAndSeatCode(trip.getId(), seatCode) > 0) {
                throw new IllegalArgumentException("Ghế " + seatCode + " đã được đặt hoặc đang được giữ, vui lòng chọn ghế khác.");
            }

            Seats seat = seatsRepository.findOptionalByTripIdAndSeatCode(trip.getId(), seatCode)
                    .orElseGet(() -> Seats.builder()
                            .trip(trip)
                            .seatCode(seatCode)
                            .build());
            seat.setHoldExpiresAt(holdExpiresAt);

            Seats savedSeat = seatsRepository.saveAndFlush(seat);
            seatIds.add(savedSeat.getId());
        }

        return seatIds;
    }

    @Override
    public List<String> getBookedSeatsForTrip(Integer tripId) {
        seatsRepository.cleanupZombieSeats();
        return seatsRepository.findUnavailableSeatCodesByTripId(tripId);
    }

    @Override
    public Seats getSeatById(int id) {
        return seatsRepository.findById(id).orElse(null);
    }

    @Override
    public Seats getSeatByTripIdAndSeatCode(Integer tripId, String seatCode) {
        return seatsRepository.findByTripIdAndSeatCode(tripId, seatCode);
    }

    @Override
    public void deleteSeat(int id) {
        try {
            seatsRepository.deleteById(id);
        } catch (Exception e) {
            throw new IllegalArgumentException("Seat not found with id: " + id, e);
        }
    }

    @Override
    @Transactional
    public int releaseHeldSeats(List<Integer> seatIds) {
        if (seatIds == null || seatIds.isEmpty()) {
            return 0;
        }
        return seatsRepository.releaseHeldSeatsByIds(seatIds);
    }
}
