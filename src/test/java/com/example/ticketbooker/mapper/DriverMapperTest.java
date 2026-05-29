package com.example.ticketbooker.mapper;

import static org.junit.jupiter.api.Assertions.assertEquals;

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
    void fromAddMapsAllFields() {
        AddDriverDTO dto = new AddDriverDTO("Nguyen Van A", "GPLX-001", "Ha Noi", "0912345678", DriverStatus.ACTIVE);

        Driver driver = DriverMapper.fromAdd(dto);

        assertEquals("Nguyen Van A", driver.getName());
        assertEquals("GPLX-001", driver.getLicenseNumber());
        assertEquals("0912345678", driver.getPhone());
        assertEquals("Ha Noi", driver.getAddress());
        assertEquals(DriverStatus.ACTIVE, driver.getDriverStatus());
    }

    @Test
    void fromUpdateAndToUpdateDtoMapAllFields() {
        UpdateDriverDTO dto = new UpdateDriverDTO(5, "Tran Van B", "GPLX-002", "0987654321", "Da Nang", DriverStatus.INACTIVE);

        Driver driver = DriverMapper.fromUpdate(dto);
        UpdateDriverDTO result = DriverMapper.toUpdateDTO(driver);

        assertEquals(5, result.getDriverId());
        assertEquals("Tran Van B", result.getName());
        assertEquals("GPLX-002", result.getLicenseNumber());
        assertEquals("0987654321", result.getPhone());
        assertEquals("Da Nang", result.getAddress());
        assertEquals(DriverStatus.INACTIVE, result.getDriverStatus());
    }

    @Test
    void toDtoAndResponseDtoMapDriverList() {
        Driver driver = new Driver(5, "Tran Van B", "GPLX-002", "0987654321", "Da Nang", DriverStatus.INACTIVE);
        ArrayList<Driver> drivers = new ArrayList<>();
        drivers.add(driver);

        DriverDTO dto = DriverMapper.toDTO(driver);
        ResponseDriverDTO response = DriverMapper.toResponseDTO(drivers);

        assertEquals(5, dto.getId());
        assertEquals("Tran Van B", dto.getName());
        assertEquals(1, response.getDriverCount());
        assertEquals(driver, response.getListDriver().get(0));
    }
}
