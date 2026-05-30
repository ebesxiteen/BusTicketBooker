package com.example.ticketbooker.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.example.ticketbooker.DTO.Seats.AddSeatDTO;
import com.example.ticketbooker.Entity.Seats;
import com.example.ticketbooker.Entity.Trips;
import com.example.ticketbooker.Repository.SeatsRepo;
import com.example.ticketbooker.Service.TripService;
import com.example.ticketbooker.Service.ServiceImp.SeatsServiceImp;

@ExtendWith(MockitoExtension.class)
class SeatsServiceImpTest {

    @Mock
    private SeatsRepo seatsRepository;

    @Mock
    private TripService tripService;

    @InjectMocks
    private SeatsServiceImp seatsServiceImp;

    @Test
    void addSeatsSplitsSeatCodesAndReturnsSavedIds() {
        Trips trip = new Trips();
        trip.setId(10);
        when(tripService.getTripById(Integer.valueOf(10))).thenReturn(trip);
        when(seatsRepository.saveAndFlush(any(Seats.class))).thenAnswer(invocation -> {
            Seats seat = invocation.getArgument(0);
            seat.setId("A01".equals(seat.getSeatCode()) ? 1 : 2);
            return seat;
        });

        List<Integer> ids = seatsServiceImp.addSeats(new AddSeatDTO(10, "A01, A02"));

        assertEquals(List.of(1, 2), ids);
    }

    @Test
    void addSeatsThrowsWhenTripDoesNotExist() {
        when(tripService.getTripById(Integer.valueOf(10))).thenReturn(null);

        assertThrows(IllegalArgumentException.class, () -> seatsServiceImp.addSeats(new AddSeatDTO(10, "A01")));
    }

    @Test
    void addSeatsThrowsWhenSeatAlreadyBooked() {
        Trips trip = new Trips();
        trip.setId(10);
        when(tripService.getTripById(Integer.valueOf(10))).thenReturn(trip);
        when(seatsRepository.existsByTripIdAndSeatCode(10, "A01")).thenReturn(true);

        assertThrows(IllegalArgumentException.class, () -> seatsServiceImp.addSeats(new AddSeatDTO(10, "A01")));
    }

    @Test
    void getBookedSeatsForTripMapsSeatCodes() {
        when(seatsRepository.findUnavailableSeatCodesByTripId(10)).thenReturn(List.of("A01", "A02"));

        List<String> result = seatsServiceImp.getBookedSeatsForTrip(10);

        assertEquals(List.of("A01", "A02"), result);
    }

    @Test
    void holdSeatsMarksExistingSeatsWithExpiryAndReturnsIds() {
        Trips trip = new Trips();
        trip.setId(10);
        Seats seat = new Seats(1, trip, "A01");
        when(tripService.getTripById(Integer.valueOf(10))).thenReturn(trip);
        when(seatsRepository.findOptionalByTripIdAndSeatCode(10, "A01")).thenReturn(Optional.of(seat));
        when(seatsRepository.saveAndFlush(seat)).thenReturn(seat);

        List<Integer> result = seatsServiceImp.holdSeats(new AddSeatDTO(10, "A01"), 300);

        assertEquals(List.of(1), result);
        org.junit.jupiter.api.Assertions.assertNotNull(seat.getHoldExpiresAt());
    }

    @Test
    void holdSeatsRejectsUnavailableSeats() {
        Trips trip = new Trips();
        trip.setId(10);
        when(tripService.getTripById(Integer.valueOf(10))).thenReturn(trip);
        when(seatsRepository.countUnavailableSeatByTripIdAndSeatCode(10, "A01")).thenReturn(1);

        assertThrows(IllegalArgumentException.class, () -> seatsServiceImp.holdSeats(new AddSeatDTO(10, "A01"), 300));
    }

    @Test
    void getSeatByIdAndByTripIdAndSeatCodeDelegateToRepository() {
        Seats seat = new Seats(1, new Trips(), "A01");
        when(seatsRepository.findById(1)).thenReturn(Optional.of(seat));
        when(seatsRepository.findByTripIdAndSeatCode(10, "A01")).thenReturn(seat);

        assertSame(seat, seatsServiceImp.getSeatById(1));
        assertSame(seat, seatsServiceImp.getSeatByTripIdAndSeatCode(10, "A01"));
    }

    @Test
    void deleteSeatDelegatesToRepository() {
        seatsServiceImp.deleteSeat(1);

        verify(seatsRepository).deleteById(1);
    }

    @Test
    void releaseHeldSeatsDelegatesToRepository() {
        when(seatsRepository.releaseHeldSeatsByIds(List.of(1, 2))).thenReturn(2);

        assertEquals(2, seatsServiceImp.releaseHeldSeats(List.of(1, 2)));
    }

    @Test
    void releaseHeldSeatsIgnoresEmptyInput() {
        assertEquals(0, seatsServiceImp.releaseHeldSeats(List.of()));
    }
}
