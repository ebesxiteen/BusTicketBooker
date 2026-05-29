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

import java.time.LocalTime;
import java.util.ArrayList;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.example.ticketbooker.DTO.Routes.RequestRouteIdDTO;
import com.example.ticketbooker.DTO.Routes.ResponseRouteDTO;
import com.example.ticketbooker.Entity.Routes;
import com.example.ticketbooker.Service.RouteService;
import com.example.ticketbooker.Util.Enum.RouteStatus;

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
class RouteApiIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private RouteService routeService;

    @Test
    void deleteRouteReturnsBooleanResult() throws Exception {
        when(routeService.deleteRoute(any(RequestRouteIdDTO.class))).thenReturn(true);

        mockMvc.perform(delete("/api/routes/delete")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"routeId\": 1}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value(true));
    }

    @Test
    void updateRouteStatusUpdatesExistingRoute() throws Exception {
        Routes route = new Routes(1, "Ha Noi", "Da Nang", LocalTime.of(10, 30), RouteStatus.ACTIVE);
        when(routeService.getRoute(1)).thenReturn(route);
        when(routeService.updateRoute(any())).thenReturn(true);

        mockMvc.perform(patch("/api/routes/1/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"INACTIVE\"}"))
                .andExpect(status().isOk());

        verify(routeService).updateRoute(any());
    }

    @Test
    void getDepartureLocationReturnsAllDepartureLocations() throws Exception {
        ResponseRouteDTO response = new ResponseRouteDTO();
        response.setRouteCount(2);
        response.setList(new ArrayList<>());
        response.getList().add(new Routes(1, "Ha Noi", "Da Nang", LocalTime.of(10, 30), RouteStatus.ACTIVE));
        response.getList().add(new Routes(2, "Sai Gon", "Da Lat", LocalTime.of(6, 0), RouteStatus.ACTIVE));
        when(routeService.findAllRoutes()).thenReturn(response);

        mockMvc.perform(get("/api/routes/getDepartureLocation"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0]").value("Ha Noi"))
                .andExpect(jsonPath("$[1]").value("Sai Gon"));
    }

    @Test
    void getArrivalLocationReturnsRoutesForDeparture() throws Exception {
        ResponseRouteDTO response = new ResponseRouteDTO();
        response.setRouteCount(1);
        response.setList(new ArrayList<>());
        response.getList().add(new Routes(1, "Ha Noi", "Da Nang", LocalTime.of(10, 30), RouteStatus.ACTIVE));
        when(routeService.findByDepartureLocation("Ha Noi")).thenReturn(response);

        mockMvc.perform(get("/api/routes/getArrivalLocation").param("departureLocation", "Ha Noi"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].arrivalLocation").value("Da Nang"));
    }

    @Test
    void getRoutesReturnsAllRoutes() throws Exception {
        ResponseRouteDTO response = new ResponseRouteDTO();
        response.setRouteCount(0);
        response.setList(new ArrayList<>());
        when(routeService.findAllRoutes()).thenReturn(response);

        mockMvc.perform(post("/api/routes/get-routes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.routeCount").value(0));
    }
}
