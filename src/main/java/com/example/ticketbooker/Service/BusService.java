package com.example.ticketbooker.Service;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.example.ticketbooker.DTO.Bus.BusDTO;
import com.example.ticketbooker.Entity.Buses;

public interface BusService {
    List<BusDTO> getAllBuses();
    BusDTO getBusById(Integer id);
    BusDTO createBus(BusDTO busDTO);
    boolean updateBus(BusDTO busDTO);
    void deleteBus(Integer id);
    Page<BusDTO> getAllBuses(Pageable pageable);
    Optional<Integer> getBusIdByLicensePlate(String licensePlate);
    Page<BusDTO> getBusesByLicensePlateContaining(String licensePlate, Pageable pageable);
    Integer getBusCapacityById(Integer busId);
    Buses getBusEntityById(Integer id);
    Page<BusDTO> searchBuses(String keyword, String statusStr, String typeStr, Pageable pageable);
}
