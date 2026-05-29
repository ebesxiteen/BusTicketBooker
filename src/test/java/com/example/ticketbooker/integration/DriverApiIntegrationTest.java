package com.example.ticketbooker.integration;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.ArrayList;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.example.ticketbooker.DTO.Driver.ResponseDriverDTO;
import com.example.ticketbooker.Entity.Driver;
import com.example.ticketbooker.Service.DriverService;
import com.example.ticketbooker.Util.Enum.DriverStatus;

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
class DriverApiIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private DriverService driverService;

    @Test
    void deleteDriverReturnsOkWhenServiceDeletes() throws Exception {
        when(driverService.deleteDriver(5)).thenReturn(true);

        mockMvc.perform(delete("/api/drivers/5"))
                .andExpect(status().isOk());
    }

    @Test
    void deleteDriverReturnsBadRequestWhenServiceThrows() throws Exception {
        when(driverService.deleteDriver(5)).thenThrow(new RuntimeException("driver has trips"));

        mockMvc.perform(delete("/api/drivers/5"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$").value("driver has trips"));
    }

    @Test
    void getAllDriversReturnsResponse() throws Exception {
        ResponseDriverDTO response = ResponseDriverDTO.builder()
                .driverCount(1)
                .listDriver(new ArrayList<>())
                .build();
        response.getListDriver().add(new Driver(5, "Driver A", "GPLX-1", "0912345678", "Ha Noi", DriverStatus.ACTIVE));
        when(driverService.findAll()).thenReturn(response);

        mockMvc.perform(get("/api/drivers/getAll"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.driverCount").value(1))
                .andExpect(jsonPath("$.listDriver[0].name").value("Driver A"));
    }

    @Test
    void searchDriversReturnsResponse() throws Exception {
        ResponseDriverDTO response = ResponseDriverDTO.builder()
                .driverCount(0)
                .listDriver(new ArrayList<>())
                .build();
        when(driverService.findAllField("Driver")).thenReturn(response);

        mockMvc.perform(post("/api/drivers/search")
                        .contentType(MediaType.TEXT_PLAIN)
                        .content("Driver"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.driverCount").value(0));
    }

    @Test
    void updateDriverStatusUpdatesExistingDriver() throws Exception {
        when(driverService.getDriver(5))
                .thenReturn(new Driver(5, "Driver A", "GPLX-1", "0912345678", "Ha Noi", DriverStatus.ACTIVE));
        when(driverService.updateDriver(any())).thenReturn(true);

        mockMvc.perform(patch("/api/drivers/5/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"driverStatus\":\"INACTIVE\"}"))
                .andExpect(status().isOk());

        verify(driverService).updateDriver(any());
    }
}
