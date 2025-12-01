package com.example.ticketbooker.Service.ServiceImp;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.example.ticketbooker.DTO.Bus.BusDTO;
import com.example.ticketbooker.Entity.Buses;
import com.example.ticketbooker.Repository.BusRepo;
import com.example.ticketbooker.Service.BusService;
import com.example.ticketbooker.Util.Mapper.BusMapper;

@Service
public class BusServiceImp implements BusService {

    @Autowired
    private BusRepo busRepository;

    @Override
    public List<BusDTO> getAllBuses() {
        List<Buses> buses = busRepository.findAll();
        List<BusDTO> dtos = new ArrayList<>();
        buses.forEach(bus -> dtos.add(BusMapper.toDTO(bus)));
        return dtos;
    }

    @Override
    public Page<BusDTO> getAllBuses(Pageable pageable) {
        Page<Buses> buses = busRepository.findAll(pageable);
        return buses.map(BusMapper::toDTO);
    }

    @Override
    public BusDTO getBusById(Integer id) {
        return busRepository.findById(id)
                      .map(BusMapper::toDTO) 
                      .orElse(null);
    }

    @Override
    public BusDTO createBus(BusDTO busDTO) {
        Buses bus = BusMapper.toEntity(busDTO);
        Buses savedBus = busRepository.save(bus);
        return BusMapper.toDTO(busRepository.save(bus));
    }

    @Override
    public boolean updateBus(BusDTO busDTO) {
        try {
            Buses bus = BusMapper.toEntity(busDTO);
            this.busRepository.save(bus);
        } catch (Exception e) {
            System.out.println(e.getMessage());
            return false;
        }
        return true;

    }

    @Override
    public void deleteBus(Integer id) {
        busRepository.deleteById(id);
    }

    @Override
    public Optional<Integer> getBusIdByLicensePlate(String licensePlate) {
        return busRepository.findByLicensePlate(licensePlate).map(Buses::getId); // Use map to extract the ID directly
    }

    @Override
    public Page<BusDTO> getBusesByLicensePlateContaining(String licensePlate, Pageable pageable) {
        Page<Buses> buses = busRepository.findByLicensePlateContainingIgnoreCase(licensePlate, pageable);
        return buses.map(BusMapper::toDTO);
    }

    @Override
    public Integer getBusCapacityById(Integer busId) {
        // Sử dụng findById và trả về capacity (hoặc null nếu không tìm thấy)
        Optional<Buses> bus = busRepository.findById(busId);
        return bus.map(Buses::getCapacity).orElse(null);
    }
    @Override
    public Buses getBusEntityById(Integer id) {
        return busRepository.findById(id).orElse(null);
    }
}
