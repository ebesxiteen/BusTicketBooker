package com.example.ticketbooker.mapper;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;

import org.junit.jupiter.api.Test;

import com.example.ticketbooker.DTO.Driver.AddDriverDTO;
import com.example.ticketbooker.DTO.Driver.DriverDTO;
import com.example.ticketbooker.DTO.Driver.ResponseDriverDTO;
import com.example.ticketbooker.DTO.Driver.UpdateDriverDTO;
import com.example.ticketbooker.Entity.Driver;
import com.example.ticketbooker.Util.Enum.DriverStatus;
import com.example.ticketbooker.Util.Mapper.DriverMapper;

class DriverMapperTest {

    @Test
    void fromAddCopiesAllDriverFields() {
        AddDriverDTO dto = AddDriverDTO.builder()
                .name("John Doe")
                .licenseNumber("ABC123")
                .phone("0123456789")
                .address("123 Street")
                .driverStatus(DriverStatus.INACTIVE)
                .build();

        Driver driver = DriverMapper.fromAdd(dto);

        assertNull(driver.getDriverId());
        assertEquals("John Doe", driver.getName());
        assertEquals("ABC123", driver.getLicenseNumber());
        assertEquals("0123456789", driver.getPhone());
        assertEquals("123 Street", driver.getAddress());
        assertEquals(DriverStatus.INACTIVE, driver.getDriverStatus());
    }

    @Test
    void fromUpdatePreservesIdentifier() {
        UpdateDriverDTO dto = UpdateDriverDTO.builder()
                .driverId(10)
                .name("Jane Smith")
                .licenseNumber("XYZ789")
                .phone("0987654321")
                .address("456 Avenue")
                .driverStatus(DriverStatus.ACTIVE)
                .build();

        Driver driver = DriverMapper.fromUpdate(dto);

        assertEquals(10, driver.getDriverId());
        assertEquals("Jane Smith", driver.getName());
        assertEquals("XYZ789", driver.getLicenseNumber());
        assertEquals("0987654321", driver.getPhone());
        assertEquals("456 Avenue", driver.getAddress());
        assertEquals(DriverStatus.ACTIVE, driver.getDriverStatus());
    }

    @Test
    void toUpdateDTOMapsEntityFields() {
        Driver driver = Driver.builder()
                .driverId(5)
                .name("Driver Name")
                .licenseNumber("LIC123")
                .phone("0999888777")
                .address("Somewhere")
                .driverStatus(DriverStatus.INACTIVE)
                .build();

        UpdateDriverDTO dto = DriverMapper.toUpdateDTO(driver);

        assertEquals(5, dto.getDriverId());
        assertEquals("Driver Name", dto.getName());
        assertEquals("LIC123", dto.getLicenseNumber());
        assertEquals("0999888777", dto.getPhone());
        assertEquals("Somewhere", dto.getAddress());
        assertEquals(DriverStatus.INACTIVE, dto.getDriverStatus());
    }

    @Test
    void toResponseDTOProvidesCountAndList() {
        ArrayList<Driver> drivers = new ArrayList<>();
        drivers.add(Driver.builder().driverId(1).name("A").licenseNumber("L1").driverStatus(DriverStatus.ACTIVE).build());
        drivers.add(Driver.builder().driverId(2).name("B").licenseNumber("L2").driverStatus(DriverStatus.INACTIVE).build());

        ResponseDriverDTO response = DriverMapper.toResponseDTO(drivers);

        assertEquals(2, response.getDriverCount());
        assertEquals(drivers, response.getListDriver());
    }

    @Test
    void toDTOMapsAllFieldsIncludingStatus() {
        Driver driver = Driver.builder()
                .driverId(15)
                .name("Full Name")
                .licenseNumber("LIC999")
                .phone("0111222333")
                .address("Any Address")
                .driverStatus(DriverStatus.ACTIVE)
                .build();

        DriverDTO dto = DriverMapper.toDTO(driver);

        assertEquals(15, dto.getId());
        assertEquals("Full Name", dto.getName());
        assertEquals("LIC999", dto.getLicenseNumber());
        assertEquals("0111222333", dto.getPhone());
        assertEquals("Any Address", dto.getAddress());
        assertEquals(DriverStatus.ACTIVE, dto.getDriverStatus());
    }
}
