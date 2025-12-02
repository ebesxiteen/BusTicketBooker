package com.example.ticketbooker.Repository;

import java.util.ArrayList;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.ticketbooker.Entity.Routes;
import com.example.ticketbooker.Util.Enum.RouteStatus;

@Repository
public interface RouteRepo extends JpaRepository<Routes, Integer> {
    Routes findById(int id);
    ArrayList<Routes> findAll();
    Page<Routes> findAll(Pageable pageable);
    ArrayList<Routes> findByStatus(RouteStatus status);
    ArrayList<Routes> findByDepartureLocation(String departureLocation);
    ArrayList<Routes> findByArrivalLocation(String arrivalLocation);
    ArrayList<Routes> findByDepartureLocationAndArrivalLocation(String departureLocation, String arrivalLocation);

    @Query("SELECT r FROM Routes r WHERE " +
           "LOWER(r.departureLocation) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(r.arrivalLocation) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    Page<Routes> searchByKeyword(@Param("keyword") String keyword, Pageable pageable);

    @Query("SELECT r FROM Routes r WHERE " +
           "(:keyword IS NULL OR :keyword = '' OR " +
           "LOWER(r.departureLocation) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(r.arrivalLocation) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
           "AND (:status IS NULL OR r.status = :status)")
    Page<Routes> findWithFilter(@Param("keyword") String keyword, 
                                @Param("status") RouteStatus status, 
                                Pageable pageable);
}
