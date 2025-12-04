package com.example.ticketbooker.Util.Mapper;


import java.time.LocalDateTime;
import java.util.ArrayList;

import com.example.ticketbooker.DTO.Trips.AddTripDTO;
import com.example.ticketbooker.DTO.Trips.ResponseTripDTO;
import com.example.ticketbooker.DTO.Trips.TripDTO;
import com.example.ticketbooker.DTO.Trips.UpdateTripDTO;
import com.example.ticketbooker.Entity.Buses;
import com.example.ticketbooker.Entity.Driver;
import com.example.ticketbooker.Entity.Routes;
import com.example.ticketbooker.Entity.Trips;
import com.example.ticketbooker.Util.Enum.TripStatus;

public class TripMapper {
    public static Trips fromAdd(AddTripDTO dto) {
        LocalDateTime departureTime = dto.getDepartureTime();

        return Trips.builder()
                .route(dto.getRoute())
                .bus(dto.getBus())
                .driver(dto.getDriver())
                .departureStation(dto.getDepartureStation())
                .arrivalStation(dto.getArrivalStation())
                .departureTime(departureTime != null ? LocalDateTime.from(departureTime) : null)
                .price(dto.getPrice())
                .availableSeats(dto.getAvailableSeats())
                .tripStatus(dto.getTripStatus() != null ? dto.getTripStatus() : TripStatus.SCHEDULED)
                .build();
    }

    public static Trips fromUpdate(UpdateTripDTO dto) {
        LocalDateTime departureTime = dto.getDepartureTime();

        Trips.TripsBuilder tripBuilder = Trips.builder()
                .id(dto.getTripId())
                .route(dto.getRoute())
                .bus(dto.getBus())
                .driver(dto.getDriver())
                .departureStation(dto.getDepartureStation())
                .arrivalStation(dto.getArrivalStation())
                .departureTime(departureTime != null ? LocalDateTime.from(departureTime) : null)
                .price(dto.getPrice())
                .availableSeats(dto.getAvailableSeats())
                .tripStatus(dto.getTripStatus());

        if (dto.getArrivalTime() != null) {
            tripBuilder.arrivalTime(LocalDateTime.from(dto.getArrivalTime()));
        } else {
            tripBuilder.arrivalTime(null);
        }

        return tripBuilder.build();
    }

    public static ResponseTripDTO toResponseDTO(ArrayList<Trips> trips) {
        return ResponseTripDTO.builder()
                .listTrips(trips)
                .tripsCount(trips.size())
                .build();
    }

    public static TripDTO toDTO(Trips trip) {
        if (trip == null) return null;

        TripDTO tripDTO = new TripDTO();
        tripDTO.setId(trip.getId());
        tripDTO.setRoute(trip.getRoute());
        tripDTO.setBus(trip.getBus());
        tripDTO.setDriver(trip.getDriver());
        tripDTO.setDepartureStation(trip.getDepartureStation());
        tripDTO.setArrivalStation(trip.getArrivalStation());
        tripDTO.setDepartureTime(trip.getDepartureTime());
        tripDTO.setArrivalTime(trip.getArrivalTime());
        tripDTO.setPrice(trip.getPrice());
        tripDTO.setAvailableSeats(trip.getAvailableSeats());
        tripDTO.setTripStatus(trip.getTripStatus());
        return tripDTO;
    }

    public static UpdateTripDTO toUpdateDTO(Trips trip) {
        UpdateTripDTO dto = new UpdateTripDTO();

        dto.setTripId(trip.getId());
        dto.setDepartureTime(trip.getDepartureTime());
        dto.setArrivalTime(trip.getArrivalTime());
        dto.setDepartureStation(trip.getDepartureStation());
        dto.setArrivalStation(trip.getArrivalStation());
        dto.setPrice(trip.getPrice());
        dto.setAvailableSeats(trip.getAvailableSeats());
        dto.setTripStatus(trip.getTripStatus());

        if (trip.getDriver() != null) {
            dto.setDriver(trip.getDriver());
        } else {
            dto.setDriver(new Driver());
        }

        if (trip.getBus() != null) {
            dto.setBus(trip.getBus());
        } else {
            dto.setBus(new Buses());
        }

        if (trip.getRoute() != null) {
            dto.setRoute(trip.getRoute());
        } else {
            dto.setRoute(new Routes());
        }

        return dto;
    }

    public static Trips toEntity(AddTripDTO dto) {
        Trips trip = new Trips();

        Routes route = new Routes();
        route.setRouteId(dto.getRoute().getRouteId());
        trip.setRoute(route);

        Buses bus = new Buses();
        bus.setId(dto.getBus().getId());
        trip.setBus(bus);

        Driver driver = new Driver();
        driver.setDriverId(dto.getDriver().getDriverId());
        trip.setDriver(driver);

        trip.setDepartureStation(dto.getDepartureStation());
        trip.setArrivalStation(dto.getArrivalStation());

        trip.setDepartureTime(dto.getDepartureTime());
        trip.setPrice(dto.getPrice());
        trip.setAvailableSeats(dto.getAvailableSeats());

        TripStatus status = dto.getTripStatus() != null ? dto.getTripStatus() : TripStatus.SCHEDULED;
        trip.setTripStatus(status);

        return trip;
    }
}
