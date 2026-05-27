package com.example.ticketbooker.Repository;

import java.util.ArrayList;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.ticketbooker.Entity.Driver;
import com.example.ticketbooker.Util.Enum.DriverStatus;
@SuppressWarnings({"SpringDataMethodInconsistencyInspection", "NullableProblems"})
@Repository
public interface DriverRepo  extends JpaRepository<Driver, Integer> {
    ArrayList<Driver> findAll();
    Page<Driver> findAll(Pageable pageable);
    ArrayList<Driver> findAllDriversByName(String name);
    ArrayList<Driver> findAllDriversByDriverStatus(DriverStatus status);
    ArrayList<Driver> findAllDriversByPhone(String phone);
    //  Kiểm tra trùng khi thêm mới
    boolean existsByPhone(String phone);
    boolean existsByLicenseNumber(String licenseNumber);
    // Kiểm tra trùng khi CẬP NHẬT (trừ chính nó ra)
    boolean existsByPhoneAndDriverIdNot(String phone, Integer driverId);
    boolean existsByLicenseNumberAndDriverIdNot(String licenseNumber, Integer driverId);
    @Query("SELECT d FROM Driver d WHERE " +
            "LOWER(d.name) LIKE LOWER(CONCAT('%', ?1, '%')) OR " +
            "LOWER(d.licenseNumber) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
            "LOWER(d.phone) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
            "LOWER(d.address) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
            "CAST(d.driverId AS string) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
            "LOWER(d.driverStatus) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    ArrayList<Driver> searchDrivers(String searchTerm);

    // Tìm kiếm nâng cao: Từ khóa + Trạng thái
    @Query("SELECT d FROM Driver d WHERE " +
           "(:keyword IS NULL OR :keyword = '' OR " +
           "LOWER(d.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(d.phone) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(d.licenseNumber) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
           "AND (:status IS NULL OR d.driverStatus = :status)")
    Page<Driver> findWithFilter(@Param("keyword") String keyword, 
                                @Param("status") DriverStatus status, 
                                Pageable pageable);
                            
   
}
