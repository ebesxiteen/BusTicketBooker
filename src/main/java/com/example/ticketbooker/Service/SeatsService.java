package com.example.ticketbooker.Service;

import java.util.List;

import com.example.ticketbooker.DTO.Seats.AddSeatDTO;
import com.example.ticketbooker.Entity.Seats;

public interface SeatsService {
    List<Integer> addSeats(AddSeatDTO addSeatDTO);
    List<Integer> holdSeats(AddSeatDTO addSeatDTO, int holdSeconds);
    List<String> getBookedSeatsForTrip(Integer tripId);
    Seats getSeatById(int id);

    Seats getSeatByTripIdAndSeatCode(Integer tripId, String seatCode);

    void deleteSeat(int i);

    int releaseHeldSeats(List<Integer> seatIds);
}
