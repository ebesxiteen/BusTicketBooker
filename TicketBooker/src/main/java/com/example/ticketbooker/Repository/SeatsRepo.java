package com.example.ticketbooker.Repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.example.ticketbooker.Entity.Seats;

@Repository
public interface SeatsRepo extends JpaRepository<Seats, Integer> {
    List<Seats> findByTripId(Integer tripId);

    boolean existsByTripIdAndSeatCode(Integer id, String seatCode);
    
    @Modifying
    @Transactional // Bắt buộc phải có
    @Query(value = "DELETE FROM Seats " +
                   "WHERE seatId NOT IN (SELECT seatId FROM ticket_seats)", 
           nativeQuery = true)
    int cleanupZombieSeats();
           
}

