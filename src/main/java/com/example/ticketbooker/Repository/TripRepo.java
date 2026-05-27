package com.example.ticketbooker.Repository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List; // Dùng List cho chuẩn
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param; // Nhớ import cái này
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.example.ticketbooker.DTO.Trips.SearchTripRequest;
import com.example.ticketbooker.Entity.Routes;
import com.example.ticketbooker.Entity.Trips;

@Repository
public interface TripRepo extends JpaRepository<Trips, Integer> {
    ArrayList<Trips> findAll();
    Optional<Trips> findById(int id);
Page<Trips> findByTripStatus(Enum tripStatus, Pageable pageable);

    // Hàm cũ của bạn (Giữ nguyên hoặc dùng nếu tìm chính xác)
    @Query("SELECT t FROM Trips t WHERE " +
            "(t.route = :#{#route}) AND " +
            "(t.departureTime >= :#{#request.departureDate} OR :#{#request.departureDate} IS NULL) AND " +
            "(t.availableSeats >= :#{#request.ticketQuantity})")
    ArrayList<Trips> searchTrip(SearchTripRequest request, Routes route);

    // === HÀM MỚI CẦN THÊM: TÌM KIẾM LINH HOẠT (Departure có thể Null) ===
    @Query("SELECT t FROM Trips t JOIN t.route r WHERE " +
           "r.arrivalLocation = :arrival " +  // Điểm đến bắt buộc phải khớp
           "AND (:departure IS NULL OR :departure = '' OR r.departureLocation = :departure) " + // Điểm đi: Nếu null thì bỏ qua, nếu có thì phải khớp
           "AND t.departureTime >= :startDate " +
           "AND t.availableSeats >= :seats " +
           "ORDER BY t.departureTime ASC")
    List<Trips> findTripsFlexible(
            @Param("arrival") String arrival,
            @Param("departure") String departure,
            @Param("startDate") LocalDateTime startDate,
            @Param("seats") int seats
    );

    ArrayList<Trips> findAllById(int tripId);
    long countTripsByDepartureTimeBetween (LocalDateTime start, LocalDateTime end);
    @Modifying
    @Transactional
    @Query("UPDATE Trips t SET t.tripStatus = 'COMPLETED' " +
           "WHERE t.departureTime < :now AND t.tripStatus = 'SCHEDULED'")
    int updateCompletedTrips(@Param("now") LocalDateTime now);
    @Query("SELECT t FROM Trips t WHERE t.bus.id = :busId " +
           "AND t.tripStatus IN ('SCHEDULED', 'IN_PROGRESS')")
    List<Trips> findScheduledOrInProgressTripsForBus(@Param("busId") Integer busId);

    @Query("SELECT COUNT(t) FROM Trips t " +
           "WHERE t.driver.id = :driverId " +
           "AND t.id != :tripIdToExclude " +
           "AND t.tripStatus IN ('SCHEDULED', 'IN_PROGRESS') " +
           "AND ( " +
           // Điều kiện xung đột thời gian
           "    (t.departureTime < :newArrivalTime) AND (t.arrivalTime > :newDepartureTime) " +
           ")"
    )
    long countConflictingTripsForDriver(
        @Param("driverId") Integer driverId, 
        @Param("newDepartureTime") LocalDateTime newDepartureTime, 
        @Param("newArrivalTime") LocalDateTime newArrivalTime, 
        @Param("tripIdToExclude") Integer tripIdToExclude
    );

     // Đếm số chuyến xe đang ở trạng thái SCHEDULED của tài xế
    @Query("SELECT COUNT(t) FROM Trips t WHERE t.driver.driverId = :driverId AND t.tripStatus = 'SCHEDULED'")
    long countScheduledTripsByDriverId(@Param("driverId") Integer driverId);
    
    // Nếu bạn muốn chặt chẽ hơn (kiểm tra cả lịch sử để tránh lỗi Khóa Ngoại database):
    @Query("SELECT COUNT(t) FROM Trips t WHERE t.driver.driverId = :driverId")
    long countAllTripsByDriverId(@Param("driverId") Integer driverId);

}