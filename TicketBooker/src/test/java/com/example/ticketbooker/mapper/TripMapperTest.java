package com.example.ticketbooker.mapper;

import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDateTime;
import java.util.ArrayList;

import org.junit.jupiter.api.Test;

import com.example.ticketbooker.DTO.Trips.AddTripDTO;
import com.example.ticketbooker.DTO.Trips.UpdateTripDTO;
import com.example.ticketbooker.Entity.Buses;
import com.example.ticketbooker.Entity.Driver;
import com.example.ticketbooker.Entity.Routes;
import com.example.ticketbooker.Entity.Trips;
import com.example.ticketbooker.Util.Enum.TripStatus;
import com.example.ticketbooker.Util.Mapper.TripMapper;

class TripMapperTest {

    @Test
    void fromAddSetsDefaultStatusWhenMissing() {
        Routes route = new Routes();
        route.setRouteId(1);
        Buses bus = new Buses();
        bus.setId(2);
        Driver driver = new Driver();
        driver.setDriverId(3);

        LocalDateTime departureTime = LocalDateTime.of(2024, 1, 1, 10, 0);

        AddTripDTO addTripDTO = AddTripDTO.builder()
                .route(route)
                .bus(bus)
                .driver(driver)
                .departureStation("Station A")
                .arrivalStation("Station B")
                .departureTime(departureTime)
                .price(150)
                .availableSeats(25)
                .tripStatus(null)
                .build();

        Trips trip = TripMapper.fromAdd(addTripDTO);

        assertEquals(route, trip.getRoute());
        assertEquals(bus, trip.getBus());
        assertEquals(driver, trip.getDriver());
        assertEquals("Station A", trip.getDepartureStation());
        assertEquals("Station B", trip.getArrivalStation());
        assertEquals(departureTime, trip.getDepartureTime());
        assertEquals(150, trip.getPrice());
        assertEquals(25, trip.getAvailableSeats());
        assertEquals(TripStatus.SCHEDULED, trip.getTripStatus());
    }

    @Test
    void fromUpdateCopiesArrivalTimeWhenProvided() {
        Routes route = new Routes();
        route.setRouteId(10);
        Buses bus = new Buses();
        bus.setId(20);
        Driver driver = new Driver();
        driver.setDriverId(30);

        LocalDateTime departure = LocalDateTime.of(2024, 5, 10, 12, 15);
        LocalDateTime arrival = LocalDateTime.of(2024, 5, 10, 15, 45);

        UpdateTripDTO updateTripDTO = UpdateTripDTO.builder()
                .tripId(5)
                .route(route)
                .bus(bus)
                .driver(driver)
                .departureStation("Origin")
                .arrivalStation("Destination")
                .departureTime(departure)
                .arrivalTime(arrival)
                .price(200)
                .availableSeats(40)
                .tripStatus(TripStatus.CANCELLED)
                .build();

        Trips trip = TripMapper.fromUpdate(updateTripDTO);

        assertEquals(5, trip.getId());
        assertEquals(route, trip.getRoute());
        assertEquals(bus, trip.getBus());
        assertEquals(driver, trip.getDriver());
        assertEquals("Origin", trip.getDepartureStation());
        assertEquals("Destination", trip.getArrivalStation());
        assertEquals(departure, trip.getDepartureTime());
        assertEquals(arrival, trip.getArrivalTime());
        assertEquals(200, trip.getPrice());
        assertEquals(40, trip.getAvailableSeats());
        assertEquals(TripStatus.CANCELLED, trip.getTripStatus());
    }

    @Test
    void toUpdateDTOProvidesEmptyRelationsWhenMissing() {
        LocalDateTime departure = LocalDateTime.of(2023, 12, 25, 6, 30);

        Trips trip = Trips.builder()
                .id(8)
                .route(null)
                .bus(null)
                .driver(null)
                .departureStation("Terminal 1")
                .arrivalStation("Terminal 2")
                .departureTime(departure)
                .arrivalTime(null)
                .price(90)
                .availableSeats(10)
                .tripStatus(TripStatus.SCHEDULED)
                .build();

        UpdateTripDTO dto = TripMapper.toUpdateDTO(trip);

        assertEquals(8, dto.getTripId());
        assertEquals("Terminal 1", dto.getDepartureStation());
        assertEquals("Terminal 2", dto.getArrivalStation());
        assertEquals(departure, dto.getDepartureTime());
        assertNull(dto.getArrivalTime());
        assertEquals(90, dto.getPrice());
        assertEquals(10, dto.getAvailableSeats());
        assertEquals(TripStatus.SCHEDULED, dto.getTripStatus());
        assertNotNull(dto.getDriver());
        assertNotNull(dto.getBus());
        assertNotNull(dto.getRoute());
        assertNull(dto.getDriver().getDriverId());
        assertNull(dto.getBus().getId());
        assertNull(dto.getRoute().getRouteId());
    }

    @Test
    void toEntityMapsIdentifiersAndFields() {
        Routes route = new Routes();
        route.setRouteId(100);
        Buses bus = new Buses();
        bus.setId(200);
        Driver driver = new Driver();
        driver.setDriverId(300);

        LocalDateTime departure = LocalDateTime.of(2024, 2, 20, 9, 0);

        AddTripDTO addTripDTO = AddTripDTO.builder()
                .route(route)
                .bus(bus)
                .driver(driver)
                .departureStation("Start")
                .arrivalStation("Finish")
                .departureTime(departure)
                .price(500)
                .availableSeats(5)
                .tripStatus(TripStatus.CANCELLED)
                .build();

        Trips trip = TripMapper.toEntity(addTripDTO);

        assertEquals(100, trip.getRoute().getRouteId());
        assertEquals(200, trip.getBus().getId());
        assertEquals(300, trip.getDriver().getDriverId());
        assertEquals("Start", trip.getDepartureStation());
        assertEquals("Finish", trip.getArrivalStation());
        assertEquals(departure, trip.getDepartureTime());
        assertEquals(500, trip.getPrice());
        assertEquals(TripStatus.CANCELLED, trip.getTripStatus());
    }

    @Test
    void toResponseDTOCountsTrips() {
        Trips first = Trips.builder().id(1).build();
        Trips second = Trips.builder().id(2).build();

        ArrayList<Trips> trips = new ArrayList<>();
        trips.add(first);
        trips.add(second);

        var response = TripMapper.toResponseDTO(trips);

        assertEquals(trips, response.getListTrips());
        assertEquals(2, response.getTripsCount());
    }
}
