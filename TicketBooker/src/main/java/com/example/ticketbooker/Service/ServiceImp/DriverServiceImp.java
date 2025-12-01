package com.example.ticketbooker.Service.ServiceImp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.example.ticketbooker.DTO.Driver.AddDriverDTO;
import com.example.ticketbooker.DTO.Driver.DriverDTO;
import com.example.ticketbooker.DTO.Driver.ResponseDriverDTO;
import com.example.ticketbooker.DTO.Driver.UpdateDriverDTO;
import com.example.ticketbooker.Entity.Driver;
import com.example.ticketbooker.Repository.DriverRepo;
import com.example.ticketbooker.Service.DriverService;
import com.example.ticketbooker.Util.Enum.DriverStatus;
import com.example.ticketbooker.Util.Mapper.DriverMapper;

@Service
public class DriverServiceImp implements DriverService {
    @Autowired
    private DriverRepo driverRepo;
    @Override
    public List<DriverDTO> getAllDrivers() {
        List<Driver> drivers = driverRepo.findAll();
        List<DriverDTO> dtos = new ArrayList<>();
        drivers.forEach(driver -> dtos.add(DriverMapper.toDTO(driver)));
        return dtos;
    }
    @Override
    public boolean addDriver(AddDriverDTO dto) {
        try{
            Driver driver = DriverMapper.fromAdd(dto);
            this.driverRepo.save(driver);
        }catch (Exception e){
            System.out.println(e.getMessage());
            return false;
        }
        return true;
    }

    @Override
    public boolean updateDriver(UpdateDriverDTO dto) {
        try{
            Driver driver = DriverMapper.fromUpdate(dto);
            this.driverRepo.save(driver);
        }catch (Exception e){
            System.out.println(e.getMessage());
            return false;
        }
        return true;
    }

    @Override
    public boolean deleteDriver(Integer id) {
        try{
            this.driverRepo.deleteById(id);
        }catch (Exception e){
            System.out.println(e.getMessage());
            return false;
        }
        return true;
    }

    @Override
    public Driver getDriver(Integer id) {
        Driver result;
        try{
            result = driverRepo.findById(id).orElse(null);

        }catch (Exception e){
            System.out.println(e.getMessage());
            return null;
        }
        return result;
    }

    @Override
    public ResponseDriverDTO findAll() {
        ResponseDriverDTO response;
        try{
            ArrayList<Driver> drivers = this.driverRepo.findAll();
            response = DriverMapper.toResponseDTO(drivers);
        }catch (Exception e){
            System.out.println(e.getMessage());
            return null;
        }
        return response;
    }

    @Override
    public Page<DriverDTO> findAll(Pageable pageable) {
        try{
            Page<Driver> drivers = this.driverRepo.findAll(pageable);
            return drivers.map(DriverMapper::toDTO);
        }catch (Exception e){
            System.out.println(e.getMessage());
            return null;
        }
    }

    @Override
    public ResponseDriverDTO findAllField(String searchTerm) {
        ResponseDriverDTO response;
        try{
            response = DriverMapper.toResponseDTO(this.driverRepo.searchDrivers(searchTerm));
        }catch (Exception e){
            System.out.println(e.getMessage());
            return null;
        }
        return response;
    }

    @Override
    public ResponseDriverDTO findDriverByName(String driverName) {
        ResponseDriverDTO response;
        try{
            response = DriverMapper.toResponseDTO( this.driverRepo.findAllDriversByName(driverName));
        }catch (Exception e){
            System.out.println(e.getMessage());
            return null;
        }
        return response;
    }

    @Override
    public ResponseDriverDTO findDriverByStatus(DriverStatus status) {
        ResponseDriverDTO response;
        try{
            response = DriverMapper.toResponseDTO(this.driverRepo.findAllDriversByDriverStatus(status));
        }catch (Exception e){
            System.out.println(e.getMessage());
            return null;
        }
        return response;
    }

    @Override
    public ResponseDriverDTO findDriverByPhone(String phone) {
        return null;
    }

    private boolean checkDriverConflict(Integer driverId, LocalDateTime departureTime, Integer tripIdToExclude) {
        // [CẦN] Triển khai trong DriverRepo/DriverService
        // Đếm số chuyến xe khác (trừ tripIdToExclude) mà driverId này đang SCHEDULED hoặc IN_PROGRESS
        // và thời gian trùng lặp.
        // Tạm thời trả về false nếu chưa có implementation
        return false; 
    }
}
