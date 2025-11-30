package com.example.ticketbooker.Controller.Api;

import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.ticketbooker.DTO.Seats.AddSeatDTO;
import com.example.ticketbooker.Service.SeatsService;
import com.example.ticketbooker.Util.Utils.CookieUtils;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@RestController
@RequestMapping("/api/seats")
public class SeatsApi {

    @Autowired
    private SeatsService seatsService;

    @PostMapping("/add")
    public ResponseEntity<List<Integer>> addSeats(@RequestBody AddSeatDTO addSeatDTO) {
        try {
            // Gọi service để thêm ghế và nhận danh sách seatId
            List<Integer> seatIds = seatsService.addSeats(addSeatDTO);

            // Trả về danh sách seatId sau khi ghế được thêm thành công
            return ResponseEntity.ok(seatIds);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(null);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(null);
        }
    }

    @PostMapping("/prebooking-seat")
    public ResponseEntity<String> preBookingSeat(HttpServletRequest request, HttpServletResponse response) {
        try {
            // Lấy dữ liệu từ cookie
            int tripId = Integer.parseInt(CookieUtils.getCookieValue(request, "tripId", "0"));
            // Lấy raw từ cookie
            String selectedSeatsRaw = CookieUtils.getCookieValue(request, "selectedSeats", "");

        // ✅ GIẢI MÃ %20 → space
            String selectedSeats = URLDecoder.decode(selectedSeatsRaw, StandardCharsets.UTF_8);

            System.out.println("selectedSeats raw  = " + selectedSeatsRaw);
            System.out.println("selectedSeats deco = " + selectedSeats);

            if (selectedSeats == null || selectedSeats.trim().isEmpty()) {
            return ResponseEntity.badRequest().body("No seats were selected.");
            }
          AddSeatDTO addSeatDTO = new AddSeatDTO();
        addSeatDTO.setTripId(tripId);
        // ví dụ: "A04 A05"
        addSeatDTO.setSeatCode(selectedSeats);

        List<Integer> seatIds = seatsService.addSeats(addSeatDTO);
        System.out.println("Danh sách seatIds được tạo: " + seatIds);
        
        if (seatIds == null || seatIds.isEmpty()) {
            // trường hợp service không ném exception nhưng lại không tạo được ghế
            return ResponseEntity
                    .badRequest()
                    .body("Không tạo được ghế, vui lòng thử lại.");
        }
         String seatIdsString = seatIds.stream()
                .map(String::valueOf)
                .collect(Collectors.joining(" "));

        String encodedSeatIds = URLEncoder.encode(seatIdsString, StandardCharsets.UTF_8);
        Cookie seatIdsCookie = new Cookie("seatIds", encodedSeatIds);
        seatIdsCookie.setPath("/");
        seatIdsCookie.setMaxAge(900);
        response.addCookie(seatIdsCookie);

        return ResponseEntity.ok("Đặt chỗ ghế tạm thời thành công.");

    } catch (Exception e) {
        e.printStackTrace();
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Error occurred while pre-booking seats.");
    }
}

    @PostMapping("/delete")
    public void deleteSeats(@RequestBody String seatIds) {
        String[] seats = seatIds.split(" ");
        for(int i = 0; i < seats.length; i++) {
            seatsService.deleteSeat(Integer.parseInt(seats[i]));
        }
    }

    @GetMapping("/{tripId}/booked")
    public List<String> getBookedSeats(@PathVariable Integer tripId) {
        // Lấy danh sách các ghế đã được đặt cho chuyến đi theo tripId
        return seatsService.getBookedSeatsForTrip(tripId);
    }

}
