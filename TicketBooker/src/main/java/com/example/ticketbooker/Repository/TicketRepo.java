package com.example.ticketbooker.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.example.ticketbooker.Entity.Tickets;
import com.example.ticketbooker.Util.Enum.TicketStatus;

@Repository
public interface TicketRepo extends JpaRepository<Tickets, Integer> {

    List<Tickets> findAllByBookerId(int bookerId);

    // --- CÂU QUERY ĐÃ ĐƯỢC FIX LỖI ---
    @Query("SELECT t FROM Tickets t WHERE t.booker.id = :bookerId " +
           "AND (:ticketId IS NULL OR t.id = :ticketId) " +
           // Sửa lỗi ngày tháng: Dùng hàm DATE() của MySQL
           "AND (:departureDate IS NULL OR FUNCTION('DATE', t.trip.departureTime) = :departureDate) " +
           // Sửa lỗi cú pháp LIKE: Phải dùng CONCAT để nối chuỗi '%'
           "AND (:route IS NULL OR CONCAT(t.trip.route.departureLocation, ' - ', t.trip.route.arrivalLocation) LIKE CONCAT('%', :route, '%')) " +
           "AND (:status IS NULL OR t.ticketStatus = :status) " +
           "ORDER BY t.id DESC")
    List<Tickets> searchTickets(@Param("bookerId") int bookerId,
                                @Param("ticketId") Integer ticketId,
                                @Param("departureDate") LocalDate departureDate,
                                @Param("route") String route,
                                @Param("status") TicketStatus status);

    @Query("SELECT COUNT(t) FROM Tickets t WHERE t.invoice.paymentTime BETWEEN :start AND :end")
    int countTicketsByPaymentTimeBetween(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    Page<Tickets> findAllByTripId(int tripId, Pageable pageable);
    
    List<Tickets> findAllByTripId(int tripId);
    
    @Modifying
    @Transactional
    @Query("UPDATE Tickets t SET t.ticketStatus = 'USED' " +
           "WHERE t.trip.departureTime < :now AND t.ticketStatus = 'BOOKED'")
    int updateUsedTickets(@Param("now") LocalDateTime now);

    @Query("SELECT COUNT(t) FROM Tickets t WHERE t.trip.id = :tripId AND t.ticketStatus IN ('BOOKED', 'USED')")
    long countBookedOrUsedTicketsByTripId(@Param("tripId") Integer tripId);

    @Query("SELECT s.seatCode " + 
           "FROM Tickets t " + 
           "JOIN t.seats s " + // Join bảng ticket_seats rồi sang bảng Seats
           "WHERE t.trip.id = :tripId " +
           "AND (t.ticketStatus = 'BOOKED')")
    List<String> findBookedSeatCodesByTripId(@Param("tripId") Integer tripId);

    @Modifying
    @Transactional
    @Query("UPDATE Tickets t SET t.ticketStatus = 'CANCELLED' WHERE t.trip.id = :tripId AND t.ticketStatus = 'BOOKED'")
    void cancelBookedTicketsByTripId(@Param("tripId") Integer tripId);

    @Modifying // Bắt buộc vì đây là lệnh DELETE/UPDATE
    @Transactional // Bắt buộc để quản lý giao dịch
    @Query("DELETE FROM Tickets t WHERE t.trip.id = :tripId")
    void deleteAllByTripId(@Param("tripId") Integer tripId);
}