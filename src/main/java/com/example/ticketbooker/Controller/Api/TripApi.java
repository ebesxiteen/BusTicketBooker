package com.example.ticketbooker.Controller.Api;

import java.time.format.DateTimeFormatter;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.ticketbooker.DTO.Trips.RequestIdTripDTO;
import com.example.ticketbooker.DTO.Trips.ResponseTripByIdDTO;
import com.example.ticketbooker.DTO.Trips.ResponseTripDTO;
import com.example.ticketbooker.DTO.Trips.SearchTripRequest;
import com.example.ticketbooker.Entity.Buses;
import com.example.ticketbooker.Entity.Routes;
import com.example.ticketbooker.Entity.Trips;
import com.example.ticketbooker.Service.TripService;

@RestController
@RequestMapping("/api/trips")
public class TripApi {
    
    @Autowired
    private TripService tripService;

    // Xóa chuyến xe (Dành cho Admin)
@DeleteMapping("/delete")
public ResponseEntity<?> deleteTrip(@RequestBody RequestIdTripDTO request) {
    try {
        // Gọi service
        tripService.deleteTrip(request);
        
        // Nếu thành công (không có lỗi ném ra)
        return ResponseEntity.ok("Xóa chuyến xe thành công!");
        
    } catch (RuntimeException e) {
        return ResponseEntity.badRequest().body(e.getMessage());
        
    } catch (Exception e) {
        // Lỗi không mong muốn khác
        return ResponseEntity.internalServerError().body("Lỗi hệ thống: " + e.getMessage());
    }
}

    @PostMapping("/search-trip")
    public ResponseTripDTO searchTrip(@RequestBody SearchTripRequest request) {
        ResponseTripDTO result = new ResponseTripDTO();
        try {
            result = tripService.searchTrip(request);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    // API lấy chi tiết chuyến xe cho trang Booking (Quan trọng)
    @GetMapping("/{tripId}")
    public ResponseTripByIdDTO getTripById(@PathVariable int tripId) {
        try {
            // Gọi Service lấy Entity Trips
            Trips trip = tripService.getTripByIdpath(tripId); 
            
            if (trip == null) return null;

            Routes route = trip.getRoute();
            
            
            ResponseTripByIdDTO response = new ResponseTripByIdDTO();
            Buses bus = trip.getBus();
            if (bus != null && bus.getCapacity() != null) {
                response.setCapacity(bus.getCapacity());
            }

            response.setDepartureLocation(route.getDepartureLocation());
            response.setArrivalLocation(route.getArrivalLocation());
            
            // Format ngày giờ đẹp (HH:mm dd/MM/yyyy) để hiển thị lên Booking
            if (trip.getDepartureTime() != null) {
                String timeStr = trip.getDepartureTime().format(DateTimeFormatter.ofPattern("HH:mm dd/MM/yyyy"));
                response.setDepartureTime(timeStr);
            }
            
            // Format giá tiền (Chuyển sang String để JS xử lý hiển thị)
            if (trip.getPrice() != null) {
                response.setTotalPrice(String.valueOf(trip.getPrice())); 
            } else {
                response.setTotalPrice("0");
            }

            return response;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}