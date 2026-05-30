package com.example.ticketbooker.Controller.Api;

import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
    private static final Logger log = LoggerFactory.getLogger(SeatsApi.class);
    private static final int SEAT_HOLD_SECONDS = 300;

    private final SeatsService seatsService;

    public SeatsApi(SeatsService seatsService) {
        this.seatsService = seatsService;
    }

    @PostMapping("/add")
    public ResponseEntity<List<Integer>> addSeats(@RequestBody AddSeatDTO addSeatDTO) {
        List<Integer> seatIds = seatsService.addSeats(addSeatDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(seatIds);
    }

    @PostMapping("/prebooking-seat")
    public ResponseEntity<String> preBookingSeat(HttpServletRequest request, HttpServletResponse response) {
        try {
            int tripId = Integer.parseInt(CookieUtils.getCookieValue(request, "tripId", "0"));
            String selectedSeatsRaw = CookieUtils.getCookieValue(request, "selectedSeats", "");
            String selectedSeats = URLDecoder.decode(selectedSeatsRaw, StandardCharsets.UTF_8);

            if (tripId <= 0) {
                return ResponseEntity.badRequest().body("Trip ID is required.");
            }
            if (selectedSeats == null || selectedSeats.trim().isEmpty()) {
                return ResponseEntity.badRequest().body("No seats were selected.");
            }

            AddSeatDTO addSeatDTO = new AddSeatDTO();
            addSeatDTO.setTripId(tripId);
            addSeatDTO.setSeatCode(selectedSeats);

            List<Integer> seatIds = seatsService.holdSeats(addSeatDTO, SEAT_HOLD_SECONDS);
            if (seatIds == null || seatIds.isEmpty()) {
                return ResponseEntity.badRequest().body("No seats were created.");
            }

            String seatIdsString = seatIds.stream()
                    .map(String::valueOf)
                    .collect(Collectors.joining(" "));

            Cookie seatIdsCookie = new Cookie("seatIds", URLEncoder.encode(seatIdsString, StandardCharsets.UTF_8));
            seatIdsCookie.setPath("/");
            seatIdsCookie.setMaxAge(SEAT_HOLD_SECONDS);
            response.addCookie(seatIdsCookie);

            return ResponseEntity.ok()
                    .header("X-Seat-Hold-Seconds", String.valueOf(SEAT_HOLD_SECONDS))
                    .body("Seats pre-booked successfully.");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            log.error("Failed to pre-book seats", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error occurred while pre-booking seats.");
        }
    }

    @PostMapping("/delete")
    public ResponseEntity<Void> deleteSeats(@RequestBody String seatIds) {
        if (seatIds == null || seatIds.isBlank()) {
            return ResponseEntity.badRequest().build();
        }

        String[] seats = seatIds.trim().split("\\s+");
        for (String seat : seats) {
            seatsService.deleteSeat(Integer.parseInt(seat));
        }

        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{tripId}/booked")
    public List<String> getBookedSeats(@PathVariable Integer tripId) {
        return seatsService.getBookedSeatsForTrip(tripId);
    }
}
