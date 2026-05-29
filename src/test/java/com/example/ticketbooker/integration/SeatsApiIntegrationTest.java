package com.example.ticketbooker.integration;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.cookie;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import jakarta.servlet.http.Cookie;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.example.ticketbooker.Controller.Api.SeatsApi;
import com.example.ticketbooker.DTO.Seats.AddSeatDTO;
import com.example.ticketbooker.Service.SeatsService;

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
class SeatsApiIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private SeatsService seatsService;

    @Test
    void addSeatsReturnsCreatedSeatIds() throws Exception {
        when(seatsService.addSeats(new AddSeatDTO(10, "A01 A02"))).thenReturn(List.of(1, 2));

        mockMvc.perform(post("/api/seats/add")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"tripId": 10, "seatCode": "A01 A02"}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$[0]").value(1))
                .andExpect(jsonPath("$[1]").value(2));
    }

    @Test
    void preBookingSeatReadsCookiesAndWritesSeatIdsCookie() throws Exception {
        when(seatsService.addSeats(new AddSeatDTO(10, "A01 A02"))).thenReturn(List.of(1, 2));

        mockMvc.perform(post("/api/seats/prebooking-seat")
                        .cookie(new Cookie("tripId", "10"))
                        .cookie(new Cookie("selectedSeats", "A01+A02")))
                .andExpect(status().isOk())
                .andExpect(content().string("Seats pre-booked successfully."))
                .andExpect(cookie().exists("seatIds"));
    }

    @Test
    void deleteSeatsDeletesEveryId() throws Exception {
        mockMvc.perform(post("/api/seats/delete")
                        .contentType(MediaType.TEXT_PLAIN)
                        .content("1 2"))
                .andExpect(status().isNoContent());

        verify(seatsService).deleteSeat(1);
        verify(seatsService).deleteSeat(2);
    }

    @Test
    void bookedSeatsReturnsList() throws Exception {
        when(seatsService.getBookedSeatsForTrip(10)).thenReturn(List.of("A01", "A02"));

        mockMvc.perform(get("/api/seats/10/booked"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0]").value("A01"))
                .andExpect(jsonPath("$[1]").value("A02"));
    }
}
