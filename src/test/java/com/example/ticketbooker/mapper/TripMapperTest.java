package com.example.ticketbooker.mapper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;

import org.junit.jupiter.api.Test;

import com.example.ticketbooker.DTO.Trips.AddTripDTO;
import com.example.ticketbooker.DTO.Trips.ResponseTripDTO;
import com.example.ticketbooker.DTO.Trips.TripDTO;
import com.example.ticketbooker.DTO.Trips.UpdateTripDTO;
import com.example.ticketbooker.Entity.Buses;
import com.example.ticketbooker.Entity.Driver;
import com.example.ticketbooker.Entity.Routes;
import com.example.ticketbooker.Entity.Trips;
import com.example.ticketbooker.Util.Enum.BusStatus;
import com.example.ticketbooker.Util.Enum.BusType;
import com.example.ticketbooker.Util.Enum.DriverStatus;
import com.example.ticketbooker.Util.Enum.RouteStatus;
import com.example.ticketbooker.Util.Enum.TripStatus;
import com.example.ticketbooker.Util.Mapper.TripMapper;

class TripMapperTest {

    @Test
    void fromAddDefaultsStatusToScheduledWhenStatusIsNull() {
        Routes route = new Routes(1, "Ha Noi", "Da Nang", LocalTime.of(10, 30), RouteStatus.ACTIVE);
        Buses bus = new Buses(2, route, "30A-12345", BusType.SEAT, 40, BusStatus.ACTIVE);
        Driver driver = new Driver(3, "Driver", "GPLX", "0912345678", "Ha Noi", DriverStatus.ACTIVE);
        LocalDateTime departure = LocalDateTime.of(2026, 6, 1, 8, 0);
        AddTripDTO dto = new AddTripDTO(route, bus, driver, "Station A", "Station B", departure, 150000, 40, null);

        Trips trip = TripMapper.fromAdd(dto);

        assertEquals(route, trip.getRoute());
        assertEquals(bus, trip.getBus());
        assertEquals(driver, trip.getDriver());
        assertEquals("Station A", trip.getDepartureStation());
        assertEquals(TripStatus.SCHEDULED, trip.getTripStatus());
    }

    @Test
    void toDtoAndToUpdateDtoMapTripFieldsAndCreateEmptyNestedObjectsWhenNull() {
        LocalDateTime departure = LocalDateTime.of(2026, 6, 1, 8, 0);
        LocalDateTime arrival = LocalDateTime.of(2026, 6, 1, 18, 30);
        Trips trip = new Trips(4, null, null, null, "A", "B", departure, arrival, 150000, 20, TripStatus.SCHEDULED);

        TripDTO dto = TripMapper.toDTO(trip);
        UpdateTripDTO update = TripMapper.toUpdateDTO(trip);

        assertEquals(4, dto.getId());
        assertEquals(arrival, dto.getArrivalTime());
        assertNotNull(update.getRoute());
        assertNotNull(update.getBus());
        assertNotNull(update.getDriver());
    }

    @Test
    void toEntityMapsOnlyForeignKeysForAddTrip() {
        Routes route = new Routes();
        route.setRouteId(1);
        Buses bus = new Buses();
        bus.setId(2);
        Driver driver = new Driver();
        driver.setDriverId(3);
        AddTripDTO dto = new AddTripDTO(route, bus, driver, "A", "B", LocalDateTime.of(2026, 6, 1, 8, 0), 150000, 40, TripStatus.SCHEDULED);

        Trips trip = TripMapper.toEntity(dto);

        assertEquals(1, trip.getRoute().getRouteId());
        assertEquals(2, trip.getBus().getId());
        assertEquals(3, trip.getDriver().getDriverId());
        assertNull(trip.getArrivalTime());
    }

    @Test
    void responseDtoCountsTrips() {
        ArrayList<Trips> trips = new ArrayList<>();
        trips.add(new Trips());

        ResponseTripDTO response = TripMapper.toResponseDTO(trips);

        assertEquals(1, response.getTripsCount());
    }
}
