package com.example.ticketbooker.Repository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List; // Dùng List cho chuẩn

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param; // Nhớ import cái này
import org.springframework.stereotype.Repository;

import com.example.ticketbooker.DTO.Trips.SearchTripRequest;
import com.example.ticketbooker.Entity.Routes;
import com.example.ticketbooker.Entity.Trips;

@Repository
public interface TripRepo extends JpaRepository<Trips, Integer> {
    ArrayList<Trips> findAll();
    Trips findById(int id);

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
}