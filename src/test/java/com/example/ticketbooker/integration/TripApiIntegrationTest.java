package com.example.ticketbooker.integration;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.example.ticketbooker.Controller.Api.TripApi;
import com.example.ticketbooker.DTO.Trips.ResponseTripDTO;
import com.example.ticketbooker.Entity.Buses;
import com.example.ticketbooker.Entity.Routes;
import com.example.ticketbooker.Entity.Trips;
import com.example.ticketbooker.Service.TripService;
import com.example.ticketbooker.Util.Enum.BusStatus;
import com.example.ticketbooker.Util.Enum.BusType;
import com.example.ticketbooker.Util.Enum.RouteStatus;
import com.example.ticketbooker.Util.Enum.TripStatus;

@SpringBootTest(properties = {
        "spring.ai.openai.api-key=dummy-test-key",
        "spring.ai.openai.base-url=https://example.com",
        "spring.ai.openai.chat.options.model=llama3-8b-8192",
        "ZALO_APP_ID=demo-app-id",
        "ZALO_KEY1=demo-key-1",
        "ZALO_KEY2=demo-key-2",
        "ZALO_ENDPOINT=https://sandbox.zalopay.vn/v001/tpe/createorder"
})
@AutoConfigureMockMvc(addFilters = false)
class TripApiIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private TripService tripService;

    @Test
    void getTripByIdReturnsBookingDetails() throws Exception {
        Routes route = new Routes(1, "Ha Noi", "Da Nang", LocalTime.of(10, 30), RouteStatus.ACTIVE);
        Buses bus = new Buses(2, route, "30A-12345", BusType.SEAT, 40, BusStatus.ACTIVE);
        Trips trip = new Trips(10, route, bus, null, "A", "B",
                LocalDateTime.of(2026, 6, 1, 8, 0),
                LocalDateTime.of(2026, 6, 1, 18, 30),
                250000,
                35,
                TripStatus.SCHEDULED);
        when(tripService.getTripByIdpath(10)).thenReturn(trip);

        mockMvc.perform(get("/api/trips/10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.departureLocation").value("Ha Noi"))
                .andExpect(jsonPath("$.arrivalLocation").value("Da Nang"))
                .andExpect(jsonPath("$.departureTime").value("08:00 01/06/2026"))
                .andExpect(jsonPath("$.totalPrice").value("250000"))
                .andExpect(jsonPath("$.capacity").value(40));
    }

    @Test
    void searchTripReturnsTripResponse() throws Exception {
        ResponseTripDTO response = ResponseTripDTO.builder()
                .tripsCount(0)
                .listTrips(new ArrayList<>())
                .build();
        when(tripService.searchTrip(any())).thenReturn(response);

        mockMvc.perform(post("/api/trips/search-trip")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"departure": "Ha Noi", "arrival": "Da Nang", "ticketQuantity": 1}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tripsCount").value(0));
    }

    @Test
    void deleteTripReturnsBadRequestWhenServiceThrows() throws Exception {
        when(tripService.deleteTrip(any())).thenThrow(new RuntimeException("cannot delete"));

        mockMvc.perform(delete("/api/trips/delete")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"tripId": 10}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("cannot delete"));
    }
}
