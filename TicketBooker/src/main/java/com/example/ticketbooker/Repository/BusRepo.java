package com.example.ticketbooker.Repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.ticketbooker.Entity.Buses;
import com.example.ticketbooker.Util.Enum.BusStatus;
import com.example.ticketbooker.Util.Enum.BusType;

@Repository
public interface BusRepo extends JpaRepository<Buses, Integer> {
    Buses findById(int id);
    List<Buses> findAll();
    Optional<Buses> findByLicensePlate(String licensePlate); // New method
    Page<Buses> findByLicensePlateContainingIgnoreCase(String licensePlate, Pageable pageable);

    @Query("SELECT b FROM Buses b WHERE " +
           "(:keyword IS NULL OR :keyword = '' OR LOWER(b.licensePlate) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
           "AND (:status IS NULL OR b.busStatus = :status) " +
           "AND (:type IS NULL OR b.busType = :type)")
    Page<Buses> findWithFilter(@Param("keyword") String keyword, 
                               @Param("status") BusStatus status,
                               @Param("type") BusType type,
                               Pageable pageable);
}

