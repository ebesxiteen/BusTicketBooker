package com.example.ticketbooker.Service.ServiceImp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.ticketbooker.DTO.Driver.AddDriverDTO;
import com.example.ticketbooker.DTO.Driver.DriverDTO;
import com.example.ticketbooker.DTO.Driver.ResponseDriverDTO;
import com.example.ticketbooker.DTO.Driver.UpdateDriverDTO;
import com.example.ticketbooker.Entity.Driver;
import com.example.ticketbooker.Repository.DriverRepo;
import com.example.ticketbooker.Repository.TripRepo;
import com.example.ticketbooker.Service.DriverService;
import com.example.ticketbooker.Util.Enum.DriverStatus;
import com.example.ticketbooker.Util.Mapper.DriverMapper;

@Service
public class DriverServiceImp implements DriverService {
    @Autowired
    private DriverRepo driverRepo;

    @Autowired
    private TripRepo tripRepo;

    @Override
    public List<DriverDTO> getAllDrivers() {
        List<Driver> drivers = driverRepo.findAll();
        List<DriverDTO> dtos = new ArrayList<>();
        drivers.forEach(driver -> dtos.add(DriverMapper.toDTO(driver)));
        return dtos;
    }
    @Override
    @Transactional
    public boolean addDriver(AddDriverDTO dto) {
        // 1. Validate Số điện thoại
        if (driverRepo.existsByPhone(dto.getPhone())) {
            throw new RuntimeException("Số điện thoại " + dto.getPhone() + " đã tồn tại!");
        }
        // 2. Validate Bằng lái
        if (driverRepo.existsByLicenseNumber(dto.getLicenseNumber())) {
            throw new RuntimeException("Số bằng lái " + dto.getLicenseNumber() + " đã tồn tại!");
        }

        try {
            Driver driver = DriverMapper.fromAdd(dto);
            // Mặc định Active khi tạo mới nếu null
            if(driver.getDriverStatus() == null) driver.setDriverStatus(DriverStatus.ACTIVE);
            driverRepo.save(driver);
        } catch (Exception e) {
            throw new RuntimeException("Lỗi hệ thống: " + e.getMessage());
        }
        return true;
    }

    @Override
    @Transactional
    public boolean updateDriver(UpdateDriverDTO dto) {
        // 1. Validate Số điện thoại (trừ chính nó)
        if (driverRepo.existsByPhoneAndDriverIdNot(dto.getPhone(), dto.getDriverId())) {
            throw new RuntimeException("Số điện thoại " + dto.getPhone() + " đã thuộc về tài xế khác!");
        }
        // 2. Validate Bằng lái (trừ chính nó)
        if (driverRepo.existsByLicenseNumberAndDriverIdNot(dto.getLicenseNumber(), dto.getDriverId())) {
            throw new RuntimeException("Số bằng lái " + dto.getLicenseNumber() + " đã tồn tại ở tài xế khác!");
        }

        try {
            // Lấy entity cũ để giữ các trường không sửa (nếu có)
            Driver existingDriver = driverRepo.findById(dto.getDriverId())
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy tài xế!"));
            
            existingDriver.setName(dto.getName());
            existingDriver.setPhone(dto.getPhone());
            existingDriver.setLicenseNumber(dto.getLicenseNumber());
            existingDriver.setAddress(dto.getAddress());
            existingDriver.setDriverStatus(dto.getDriverStatus());
            
            driverRepo.save(existingDriver);
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
        return true;
    }

  @Override
    @Transactional
    public boolean deleteDriver(Integer id) {
        try {
            // 1. KIỂM TRA RÀNG BUỘC NGHIỆP VỤ (Chuyến sắp chạy)
            long scheduledCount = tripRepo.countScheduledTripsByDriverId(id);
            if (scheduledCount > 0) {
                throw new RuntimeException("Không thể xóa! Tài xế này đang được phân công cho " + scheduledCount + " chuyến xe sắp khởi hành.");
            }

            
            long totalCount = tripRepo.countAllTripsByDriverId(id);
            if (totalCount > 0) {
                 throw new RuntimeException("Không thể xóa! Tài xế này đã có lịch sử chạy " + totalCount + " chuyến xe. Vui lòng chuyển trạng thái sang 'Ngừng hoạt động' thay vì xóa.");
            }

            // 3. XÓA NẾU SẠCH SẼ
            this.driverRepo.deleteById(id);
            return true;

        } catch (Exception e) {
            // Ném lỗi ra để Controller bắt và hiển thị
            throw new RuntimeException(e.getMessage());
        }
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
        return driverRepo.findAll(pageable).map(DriverMapper::toDTO);
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

    public Page<DriverDTO> searchDrivers(String keyword, String statusStr, Pageable pageable) {
        DriverStatus statusEnum = null;
        if (statusStr != null && !statusStr.equals("ALL") && !statusStr.isEmpty()) {
            try { statusEnum = DriverStatus.valueOf(statusStr); } catch (Exception e) {}
        }
        Page<Driver> drivers = driverRepo.findWithFilter(keyword, statusEnum, pageable);
        return drivers.map(DriverMapper::toDTO);
    }

    
}
