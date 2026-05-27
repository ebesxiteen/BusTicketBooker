package com.example.ticketbooker.Util.Mapper;


import java.time.LocalDateTime;
import java.util.ArrayList;

import com.example.ticketbooker.DTO.Trips.AddTripDTO;
import com.example.ticketbooker.DTO.Trips.ResponseTripDTO;
import com.example.ticketbooker.DTO.Trips.TripDTO;
import com.example.ticketbooker.DTO.Trips.UpdateTripDTO;
import com.example.ticketbooker.Entity.Buses;
import com.example.ticketbooker.Entity.Driver;
import com.example.ticketbooker.Entity.Routes;
import com.example.ticketbooker.Entity.Trips;
import com.example.ticketbooker.Util.Enum.TripStatus;

public class TripMapper {
    public static Trips fromAdd(AddTripDTO dto) {
        return Trips.builder()
                .route(dto.getRoute())
                .bus(dto.getBus())
                .driver(dto.getDriver())
                .departureStation(dto.getDepartureStation())
                .arrivalStation(dto.getArrivalStation())
                .departureTime(LocalDateTime.from(dto.getDepartureTime()))
                .price(dto.getPrice())
                .availableSeats(dto.getAvailableSeats())
                .tripStatus(dto.getTripStatus() != null ? dto.getTripStatus() : TripStatus.SCHEDULED)
                .build();
    }

    public static Trips fromUpdate(UpdateTripDTO dto) {
        Trips.TripsBuilder tripBuilder = Trips.builder()
                .id(dto.getTripId())
                .route(dto.getRoute())
                .bus(dto.getBus())
                .driver(dto.getDriver())
                .departureStation(dto.getDepartureStation())
                .arrivalStation(dto.getArrivalStation())
                .departureTime(LocalDateTime.from(dto.getDepartureTime()))
                .price(dto.getPrice())
                .availableSeats(dto.getAvailableSeats())
                .tripStatus(dto.getTripStatus());

        if (dto.getArrivalTime() != null) {
            tripBuilder.arrivalTime(LocalDateTime.from(dto.getArrivalTime()));
        } else {
            tripBuilder.arrivalTime(null);
        }

        return tripBuilder.build();
    }

    public static ResponseTripDTO toResponseDTO(ArrayList<Trips> trips) {
        return ResponseTripDTO.builder()
                .listTrips(trips)
                .tripsCount(trips.size())
                .build();
    }

    public static TripDTO toDTO(Trips trip) {
        if (trip == null) return null;

        TripDTO tripDTO = new TripDTO();
        tripDTO.setId(trip.getId());
        tripDTO.setRoute(trip.getRoute());
        tripDTO.setBus(trip.getBus());
        tripDTO.setDriver(trip.getDriver());
        tripDTO.setDepartureStation(trip.getDepartureStation());
        tripDTO.setArrivalStation(trip.getArrivalStation());
        tripDTO.setDepartureTime(trip.getDepartureTime());
        tripDTO.setArrivalTime(trip.getArrivalTime());
        tripDTO.setPrice(trip.getPrice());
        tripDTO.setAvailableSeats(trip.getAvailableSeats());
        tripDTO.setTripStatus(trip.getTripStatus());
        return tripDTO;
    }
public static UpdateTripDTO toUpdateDTO(Trips trip) {
    UpdateTripDTO dto = new UpdateTripDTO();
    
    // Gán các trường cơ bản
    dto.setTripId(trip.getId());
    dto.setDepartureTime(trip.getDepartureTime());
    dto.setArrivalTime(trip.getArrivalTime());
    dto.setDepartureStation(trip.getDepartureStation());
    dto.setArrivalStation(trip.getArrivalStation());
    dto.setPrice(trip.getPrice());
    dto.setAvailableSeats(trip.getAvailableSeats());
    dto.setTripStatus(trip.getTripStatus());
    
    // -----------------------------------------------------------------
    // KHẮC PHỤC LỖI TYPE MISMATCH VÀ NULL POINTER
    // -----------------------------------------------------------------
    
    // 1. Gán Driver (Entity)
    // Nếu trip.getDriver() là null, tạo một Entity rỗng để Thymeleaf không bị lỗi
    if (trip.getDriver() != null) {
        dto.setDriver(trip.getDriver()); // TRUYỀN THẲNG ENTITY
    } else {
        // Khởi tạo Entity rỗng để tránh lỗi driver.driverId trong HTML
        dto.setDriver(new Driver()); 
    }

    // 2. Gán Bus (Entity)
    if (trip.getBus() != null) {
        dto.setBus(trip.getBus()); // TRUYỀN THẲNG ENTITY
    } else {
        dto.setBus(new Buses());
    }

    // 3. Gán Route (Entity)
    if (trip.getRoute() != null) {
        dto.setRoute(trip.getRoute()); // TRUYỀN THẲNG ENTITY
    } else {
        dto.setRoute(new Routes());
    }
    
    // -----------------------------------------------------------------
    
    return dto;
}
// Trong com.example.ticketbooker.Util.Mapper.TripMapper

public static Trips toEntity(AddTripDTO dto) {
    Trips trip = new Trips();
    
    // --- 1. MAPPING KHÓA NGOẠI (Foreign Keys) ---
    // Chỉ cần set ID cho các Entity liên kết
    
    Routes route = new Routes();
    route.setRouteId(dto.getRoute().getRouteId());
    trip.setRoute(route); 
    
    Buses bus = new Buses();
    bus.setId(dto.getBus().getId());
    trip.setBus(bus);
    
    Driver driver = new Driver();
    driver.setDriverId(dto.getDriver().getDriverId());
    trip.setDriver(driver);

    // --- 2. MAPPING ĐIỂM ĐI, ĐIỂM ĐẾN (Vấn đề hiện tại) ---
    
    // !!! BỔ SUNG HAI DÒNG NÀY (hoặc kiểm tra xem chúng có bị lỗi không)
    trip.setDepartureStation(dto.getDepartureStation()); 
    trip.setArrivalStation(dto.getArrivalStation());   
    
    // --- 3. MAPPING CÁC TRƯỜNG CÒN LẠI ---

    trip.setDepartureTime(dto.getDepartureTime());
    // arrivalTime: KHÔNG CẦN MAPPING ở đây, vì nó được tính toán và set trong TripServiceImp.java
    
    trip.setPrice(dto.getPrice());

    // TripStatus (trạng thái, ví dụ: SCHEDULED)
    trip.setTripStatus(dto.getTripStatus());

    return trip;
}
}
