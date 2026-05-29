package com.example.ticketbooker.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.example.ticketbooker.DTO.Driver.AddDriverDTO;
import com.example.ticketbooker.DTO.Driver.UpdateDriverDTO;
import com.example.ticketbooker.Entity.Driver;
import com.example.ticketbooker.Repository.DriverRepo;
import com.example.ticketbooker.Repository.TripRepo;
import com.example.ticketbooker.Service.ServiceImp.DriverServiceImp;
import com.example.ticketbooker.Util.Enum.DriverStatus;

@ExtendWith(MockitoExtension.class)
class DriverServiceImpTest {

    @Mock
    private DriverRepo driverRepo;

    @Mock
    private TripRepo tripRepo;

    @InjectMocks
    private DriverServiceImp driverServiceImp;

    @Test
    void addDriverThrowsWhenPhoneAlreadyExists() {
        AddDriverDTO request = new AddDriverDTO("Nguyen Van A", "GPLX-001", "Ha Noi", "0912345678", DriverStatus.ACTIVE);
        when(driverRepo.existsByPhone("0912345678")).thenReturn(true);

        RuntimeException exception = assertThrows(RuntimeException.class, () -> driverServiceImp.addDriver(request));

        assertTrue(exception.getMessage().contains("0912345678"));
        verify(driverRepo, never()).save(any(Driver.class));
    }

    @Test
    void addDriverDefaultsStatusToActiveWhenRequestStatusIsNull() {
        AddDriverDTO request = new AddDriverDTO("Nguyen Van A", "GPLX-001", "Ha Noi", "0912345678", null);
        ArgumentCaptor<Driver> driverCaptor = ArgumentCaptor.forClass(Driver.class);

        boolean result = driverServiceImp.addDriver(request);

        assertTrue(result);
        verify(driverRepo).save(driverCaptor.capture());
        Driver savedDriver = driverCaptor.getValue();
        assertEquals("Nguyen Van A", savedDriver.getName());
        assertEquals("GPLX-001", savedDriver.getLicenseNumber());
        assertEquals(DriverStatus.ACTIVE, savedDriver.getDriverStatus());
    }

    @Test
    void updateDriverMutatesExistingEntityAndSavesIt() {
        Driver existingDriver = new Driver(7, "Old Name", "OLD", "0900000000", "Old Address", DriverStatus.ACTIVE);
        UpdateDriverDTO request = new UpdateDriverDTO(
                7,
                "New Name",
                "NEW",
                "0911111111",
                "New Address",
                DriverStatus.INACTIVE);

        when(driverRepo.findById(7)).thenReturn(Optional.of(existingDriver));

        boolean result = driverServiceImp.updateDriver(request);

        assertTrue(result);
        assertEquals("New Name", existingDriver.getName());
        assertEquals("NEW", existingDriver.getLicenseNumber());
        assertEquals("0911111111", existingDriver.getPhone());
        assertEquals("New Address", existingDriver.getAddress());
        assertEquals(DriverStatus.INACTIVE, existingDriver.getDriverStatus());
        verify(driverRepo).save(existingDriver);
    }

    @Test
    void updateDriverThrowsWhenPhoneBelongsToAnotherDriver() {
        UpdateDriverDTO request = new UpdateDriverDTO(7, "New Name", "NEW", "0911111111", "New Address", DriverStatus.ACTIVE);
        when(driverRepo.existsByPhoneAndDriverIdNot("0911111111", 7)).thenReturn(true);

        RuntimeException exception = assertThrows(RuntimeException.class, () -> driverServiceImp.updateDriver(request));

        assertTrue(exception.getMessage().contains("0911111111"));
        verify(driverRepo, never()).findById(any());
        verify(driverRepo, never()).save(any(Driver.class));
    }

    @Test
    void deleteDriverThrowsWhenDriverHasScheduledTrips() {
        when(tripRepo.countScheduledTripsByDriverId(7)).thenReturn(2L);

        RuntimeException exception = assertThrows(RuntimeException.class, () -> driverServiceImp.deleteDriver(7));

        assertTrue(exception.getMessage().contains("2"));
        verify(driverRepo, never()).deleteById(any());
    }

    @Test
    void deleteDriverDeletesWhenDriverHasNoTrips() {
        when(tripRepo.countScheduledTripsByDriverId(7)).thenReturn(0L);
        when(tripRepo.countAllTripsByDriverId(7)).thenReturn(0L);

        boolean result = driverServiceImp.deleteDriver(7);

        assertTrue(result);
        verify(driverRepo).deleteById(7);
    }

    @Test
    void getDriverReturnsRepositoryResult() {
        Driver driver = new Driver(7, "Nguyen Van A", "GPLX-001", "0912345678", "Ha Noi", DriverStatus.ACTIVE);
        when(driverRepo.findById(7)).thenReturn(Optional.of(driver));

        Driver result = driverServiceImp.getDriver(7);

        assertSame(driver, result);
        verify(driverRepo).findById(eq(7));
    }
}
