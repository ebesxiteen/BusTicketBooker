package com.example.ticketbooker.integration;

import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.example.ticketbooker.Controller.Api.BusApi;
import com.example.ticketbooker.DTO.Bus.BusDTO;
import com.example.ticketbooker.Entity.Buses;
import com.example.ticketbooker.Repository.BusRepo;
import com.example.ticketbooker.Service.BusService;
import com.example.ticketbooker.Util.Enum.BusStatus;
import com.example.ticketbooker.Util.Enum.BusType;

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
class BusApiIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private BusService busService;

    @MockBean
    private BusRepo busRepository;

    @Test
    void deleteBusReturnsOkWhenBusExists() throws Exception {
        when(busService.getBusById(5)).thenReturn(new BusDTO(5, null, "30A-12345", BusType.SEAT, 40, BusStatus.ACTIVE));

        mockMvc.perform(delete("/api/buses/5"))
                .andExpect(status().isOk());

        verify(busService).deleteBus(5);
    }

    @Test
    void deleteBusReturnsNotFoundWhenBusMissing() throws Exception {
        when(busService.getBusById(5)).thenReturn(null);

        mockMvc.perform(delete("/api/buses/5"))
                .andExpect(status().isNotFound());

        verify(busService, never()).deleteBus(5);
    }

    @Test
    void updateBusStatusPatchesExistingBus() throws Exception {
        BusDTO existing = new BusDTO(5, null, "30A-12345", BusType.SEAT, 40, BusStatus.ACTIVE);
        when(busService.getBusById(5)).thenReturn(existing);

        mockMvc.perform(patch("/api/buses/5/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"busStatus": "MAINTENANCE"}
                                """))
                .andExpect(status().isOk());

        verify(busService).updateBus(existing);
    }

    @Test
    void getBusIdByLicensePlateReturnsIdWhenFound() throws Exception {
        Buses bus = new Buses();
        bus.setId(5);
        when(busRepository.findByLicensePlate("30A-12345")).thenReturn(Optional.of(bus));

        mockMvc.perform(get("/api/buses/byLicensePlate/30A-12345"))
                .andExpect(status().isOk())
                .andExpect(content().string("5"));
    }
}
