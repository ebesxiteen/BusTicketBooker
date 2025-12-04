package com.example.ticketbooker.Service;

import java.util.List;

import com.example.ticketbooker.DTO.Seats.AddSeatDTO;
import com.example.ticketbooker.Entity.Seats;

public interface SeatsService {
    List<Integer> addSeats(AddSeatDTO addSeatDTO);
    List<String> getBookedSeatsForTrip(Integer tripId);
    Seats getSeatById(int id);

    Seats getSeatByTripIdAndSeatCode(Integer tripId, String seatCode);

    void deleteSeat(int i);
}
