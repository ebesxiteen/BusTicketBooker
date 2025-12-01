package com.example.ticketbooker.Repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.example.ticketbooker.Entity.Seats;

@Repository
public interface SeatsRepo extends JpaRepository<Seats, Integer> {
    List<Seats> findByTripId(Integer tripId);

    boolean existsByTripIdAndSeatCode(Integer id, String seatCode);
    
    @Modifying
    @Transactional
    @Query(value = "DELETE FROM Seats " +
                   "WHERE seatId NOT IN (SELECT seatId FROM ticket_seats)", 
           nativeQuery = true)
    int cleanupZombieSeats();
    
    @Modifying
    @Transactional
    @Query("DELETE FROM Seats s WHERE s.trip.id = :tripId")
    void deleteAllByTripId(@Param("tripId") Integer tripId);
}

