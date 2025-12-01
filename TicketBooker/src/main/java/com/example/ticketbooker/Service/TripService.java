package com.example.ticketbooker.Service;

import java.time.LocalDate;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.example.ticketbooker.DTO.Trips.AddTripDTO;
import com.example.ticketbooker.DTO.Trips.RequestIdTripDTO;
import com.example.ticketbooker.DTO.Trips.ResponseTripDTO;
import com.example.ticketbooker.DTO.Trips.SearchTripRequest;
import com.example.ticketbooker.DTO.Trips.TripDTO;
import com.example.ticketbooker.DTO.Trips.TripStatsDTO;
import com.example.ticketbooker.DTO.Trips.UpdateTripDTO;
import com.example.ticketbooker.Entity.Trips;

public interface TripService {
    public ResponseTripDTO getTripById(int id);
    Page<TripDTO> getAllTrips(Pageable pageable);
    public ResponseTripDTO getAllTrips();
    public boolean addTrip(AddTripDTO dto);
    public boolean updateTrip(UpdateTripDTO updateTripDTO);
    public boolean deleteTrip(RequestIdTripDTO dto); // Thêm phương thức xóa chuyến xe
    public ResponseTripDTO searchTrip(SearchTripRequest dto);
    Trips getTripById(Integer tripId);
    public Trips getTripByIdpath(int tripId);
    ResponseTripDTO getTripByIds(int tripId);
    TripStatsDTO getTripStats(String period, LocalDate selectedDate);
    void updateAvailableSeats(Integer tripId, int delta);
    Page<TripDTO> getAllTrips(String status, Pageable pageable);
    boolean cancelTrip(Integer tripId);


}
