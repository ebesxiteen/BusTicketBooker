package com.example.ticketbooker.util;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.example.ticketbooker.Repository.SeatsRepo;
import com.example.ticketbooker.Util.Utils.CleanupService;

@ExtendWith(MockitoExtension.class)
class CleanupServiceTest {

    @Mock
    private SeatsRepo seatsRepository;

    @InjectMocks
    private CleanupService cleanupService;

    @Test
    void cleanupZombieSeatsTaskDelegatesToRepositoryWhenNoSeatsDeleted() {
        when(seatsRepository.cleanupZombieSeats()).thenReturn(0);

        cleanupService.cleanupZombieSeatsTask();

        verify(seatsRepository).cleanupZombieSeats();
    }

    @Test
    void cleanupZombieSeatsTaskHandlesDeletedSeats() {
        when(seatsRepository.cleanupZombieSeats()).thenReturn(3);

        cleanupService.cleanupZombieSeatsTask();

        verify(seatsRepository).cleanupZombieSeats();
    }
}
