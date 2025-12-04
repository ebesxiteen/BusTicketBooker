package com.example.ticketbooker.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.example.ticketbooker.DTO.Bus.BusDTO;
import com.example.ticketbooker.Entity.Buses;
import com.example.ticketbooker.Entity.Trips;
import com.example.ticketbooker.Repository.BusRepo;
import com.example.ticketbooker.Repository.SeatsRepo;
import com.example.ticketbooker.Repository.TicketRepo;
import com.example.ticketbooker.Repository.TripRepo;
import com.example.ticketbooker.Service.ServiceImp.BusServiceImp;
import com.example.ticketbooker.Util.Enum.BusStatus;
import com.example.ticketbooker.Util.Enum.BusType;

@ExtendWith(MockitoExtension.class)
class BusServiceImpTest {

    @Mock
    private BusRepo busRepository;

    @Mock
    private TripRepo tripRepo;

    @Mock
    private SeatsRepo seatsRepo;

    @Mock
    private TicketRepo ticketRepo;

    @InjectMocks
    private BusServiceImp busServiceImp;

    @Test
    void updateBusThrowsWhenNewCapacityInvalidatesExistingSeats() {
        Buses existingBus = new Buses(1, null, "79A-88888", BusType.SEAT, 4, BusStatus.ACTIVE);
        BusDTO updateRequest = new BusDTO(1, null, "79A-88888", BusType.SEAT, 2, BusStatus.ACTIVE);

        Trips futureTrip = new Trips();
        futureTrip.setId(10);

        when(busRepository.findById(1)).thenReturn(Optional.of(existingBus));
        when(tripRepo.findScheduledOrInProgressTripsForBus(1)).thenReturn(List.of(futureTrip));
        when(ticketRepo.findBookedSeatCodesByTripId(10)).thenReturn(List.of("B02"));

        RuntimeException exception = assertThrows(RuntimeException.class, () -> busServiceImp.updateBus(updateRequest));

        assertEquals(
                "Không thể giảm số ghế xuống 2! Xe này đang chạy chuyến #10 có các ghế đã bán nằm ngoài phạm vi mới: B02",
                exception.getMessage());
    }

    @Test
    void updateBusExpandsCapacityAndRecalculatesAvailableSeats() {
        Buses existingBus = new Buses(1, null, "79A-99999", BusType.SLEEPER, 2, BusStatus.ACTIVE);
        BusDTO updateRequest = new BusDTO(1, null, "79A-99999", BusType.SLEEPER, 4, BusStatus.MAINTENANCE);

        Trips futureTrip = new Trips();
        futureTrip.setId(5);
        futureTrip.setAvailableSeats(1);

        when(busRepository.findById(1)).thenReturn(Optional.of(existingBus));
        when(busRepository.save(any(Buses.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(tripRepo.findScheduledOrInProgressTripsForBus(1)).thenReturn(Collections.singletonList(futureTrip));
        when(seatsRepo.countBookedSeatsByTripId(5)).thenReturn(1L);
        when(tripRepo.save(any(Trips.class))).thenAnswer(invocation -> invocation.getArgument(0));

        busServiceImp.updateBus(updateRequest);

        verify(busRepository).save(existingBus);
        verify(tripRepo, times(1)).save(futureTrip);
        assertEquals(3, futureTrip.getAvailableSeats());
        assertEquals(BusStatus.MAINTENANCE, existingBus.getBusStatus());
        assertEquals(4, existingBus.getCapacity());
    }
}
