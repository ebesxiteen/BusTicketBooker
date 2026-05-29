package com.example.ticketbooker.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import com.example.ticketbooker.DTO.Trips.AddTripDTO;
import com.example.ticketbooker.DTO.Trips.RequestIdTripDTO;
import com.example.ticketbooker.DTO.Trips.SearchTripRequest;
import com.example.ticketbooker.DTO.Trips.TripDTO;
import com.example.ticketbooker.DTO.Trips.TripStatsDTO;
import com.example.ticketbooker.Entity.Buses;
import com.example.ticketbooker.Entity.Driver;
import com.example.ticketbooker.Entity.Routes;
import com.example.ticketbooker.Entity.Trips;
import com.example.ticketbooker.Repository.InvoiceRepo;
import com.example.ticketbooker.Repository.SeatsRepo;
import com.example.ticketbooker.Repository.TicketRepo;
import com.example.ticketbooker.Repository.TripRepo;
import com.example.ticketbooker.Service.BusService;
import com.example.ticketbooker.Service.DriverService;
import com.example.ticketbooker.Service.RouteService;
import com.example.ticketbooker.Service.ServiceImp.TripServiceImp;
import com.example.ticketbooker.Util.Enum.BusStatus;
import com.example.ticketbooker.Util.Enum.BusType;
import com.example.ticketbooker.Util.Enum.DriverStatus;
import com.example.ticketbooker.Util.Enum.RouteStatus;
import com.example.ticketbooker.Util.Enum.TripStatus;

@ExtendWith(MockitoExtension.class)
class TripServiceImpTest {

    @Mock
    private TripRepo tripRepo;

    @Mock
    private RouteService routeService;

    @Mock
    private BusService busService;

    @Mock
    private DriverService driverService;

    @Mock
    private TicketRepo ticketRepo;

    @Mock
    private SeatsRepo seatRepo;

    @Mock
    private InvoiceRepo invoiceRepo;

    @InjectMocks
    private TripServiceImp tripServiceImp;

    @Test
    void addTripCalculatesArrivalAndAvailableSeats() {
        Routes route = new Routes(1, "Ha Noi", "Da Nang", LocalTime.of(10, 30), RouteStatus.ACTIVE);
        Buses bus = new Buses(2, route, "30A-12345", BusType.SEAT, 40, BusStatus.ACTIVE);
        Driver driver = new Driver(3, "Driver", "GPLX", "0912345678", "Ha Noi", DriverStatus.ACTIVE);
        LocalDateTime departure = LocalDateTime.of(2026, 6, 1, 8, 0);
        AddTripDTO request = new AddTripDTO(route, bus, driver, "A", "B", departure, 150000, null, TripStatus.SCHEDULED);

        when(routeService.getRoute(1)).thenReturn(route);
        when(busService.getBusCapacityById(2)).thenReturn(40);
        when(tripRepo.findScheduledOrInProgressTripsForBus(2)).thenReturn(List.of());

        boolean result = tripServiceImp.addTrip(request);

        assertTrue(result);
        verify(tripRepo).save(any(Trips.class));
    }

    @Test
    void addTripThrowsWhenBusScheduleOverlaps() {
        Routes route = new Routes(1, "Ha Noi", "Da Nang", LocalTime.of(10, 30), RouteStatus.ACTIVE);
        Buses bus = new Buses(2, route, "30A-12345", BusType.SEAT, 40, BusStatus.ACTIVE);
        Driver driver = new Driver(3, "Driver", "GPLX", "0912345678", "Ha Noi", DriverStatus.ACTIVE);
        LocalDateTime departure = LocalDateTime.of(2026, 6, 1, 8, 0);
        AddTripDTO request = new AddTripDTO(route, bus, driver, "A", "B", departure, 150000, null, TripStatus.SCHEDULED);
        Trips existingTrip = new Trips(99, route, bus, driver, "C", "D",
                LocalDateTime.of(2026, 6, 1, 9, 0),
                LocalDateTime.of(2026, 6, 1, 12, 0),
                150000,
                40,
                TripStatus.SCHEDULED);

        when(routeService.getRoute(1)).thenReturn(route);
        when(busService.getBusCapacityById(2)).thenReturn(40);
        when(tripRepo.findScheduledOrInProgressTripsForBus(2)).thenReturn(List.of(existingTrip));

        assertThrows(RuntimeException.class, () -> tripServiceImp.addTrip(request));
        verify(tripRepo, never()).save(any());
    }

    @Test
    void deleteTripThrowsWhenBookedTicketsExist() {
        when(ticketRepo.countBookedOrUsedTicketsByTripId(5)).thenReturn(2L);

        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> tripServiceImp.deleteTrip(new RequestIdTripDTO(5)));

        assertTrue(exception.getMessage().contains("2"));
        verify(tripRepo, never()).deleteById(any());
    }

    @Test
    void cancelTripCancelsInvoicesTicketsSeatsAndTrip() {
        Trips trip = new Trips();
        trip.setId(5);
        trip.setTripStatus(TripStatus.SCHEDULED);
        trip.setAvailableSeats(10);
        when(tripRepo.findById(Integer.valueOf(5))).thenReturn(Optional.of(trip));

        boolean result = tripServiceImp.cancelTrip(5);

        assertTrue(result);
        assertEquals(TripStatus.CANCELLED, trip.getTripStatus());
        assertEquals(0, trip.getAvailableSeats());
        verify(invoiceRepo).cancelInvoicesByTripId(5);
        verify(ticketRepo).cancelBookedTicketsByTripId(5);
        verify(seatRepo).deleteAllByTripId(5);
        verify(tripRepo).save(trip);
    }

    @Test
    void updateAvailableSeatsNeverDropsBelowZero() {
        Trips trip = new Trips();
        trip.setId(5);
        trip.setAvailableSeats(1);
        when(tripRepo.findById(Integer.valueOf(5))).thenReturn(Optional.of(trip));

        tripServiceImp.updateAvailableSeats(5, -10);

        assertEquals(0, trip.getAvailableSeats());
        verify(tripRepo).save(trip);
    }

    @Test
    void getAllTripsUsesStatusFilterWhenValidAndFindAllWhenInvalid() {
        Pageable pageable = PageRequest.of(0, 10);
        Trips trip = new Trips();
        when(tripRepo.findByTripStatus(TripStatus.SCHEDULED, pageable)).thenReturn(new PageImpl<>(List.of(trip), pageable, 1));
        when(tripRepo.findAll(pageable)).thenReturn(new PageImpl<>(List.of(trip), pageable, 1));

        Page<TripDTO> scheduled = tripServiceImp.getAllTrips("SCHEDULED", pageable);
        Page<TripDTO> invalid = tripServiceImp.getAllTrips("BAD_STATUS", pageable);

        assertEquals(1, scheduled.getTotalElements());
        assertEquals(1, invalid.getTotalElements());
        verify(tripRepo).findByTripStatus(TripStatus.SCHEDULED, pageable);
        verify(tripRepo).findAll(pageable);
    }

    @Test
    void searchTripDefaultsTicketQuantityToOne() {
        SearchTripRequest request = new SearchTripRequest();
        request.setArrival("Da Nang");
        request.setDeparture("");
        request.setDepartureDate(LocalDateTime.of(2026, 6, 1, 0, 0));
        request.setTicketQuantity(0);
        when(tripRepo.findTripsFlexible(eq("Da Nang"), eq(""), eq(request.getDepartureDate()), eq(1)))
                .thenReturn(List.of(new Trips()));

        tripServiceImp.searchTrip(request);

        verify(tripRepo).findTripsFlexible("Da Nang", "", request.getDepartureDate(), 1);
    }

    @Test
    void tripStatsCountsCurrentAndPreviousDay() {
        LocalDate selectedDate = LocalDate.of(2026, 5, 29);
        when(tripRepo.countTripsByDepartureTimeBetween(any(), any())).thenReturn(4L).thenReturn(2L);

        TripStatsDTO stats = tripServiceImp.getTripStats("Day", selectedDate);

        assertEquals(4L, stats.getCurrentPeriodTripCount());
        assertEquals(2L, stats.getPreviousPeriodTripCount());
    }
}
