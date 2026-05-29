package com.example.ticketbooker.integration;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.example.ticketbooker.Controller.Api.TicketApi;
import com.example.ticketbooker.DTO.Ticket.PaymentInforRequest;
import com.example.ticketbooker.DTO.Ticket.PaymentInforResponse;
import com.example.ticketbooker.DTO.Ticket.TicketDTO;
import com.example.ticketbooker.DTO.Ticket.TicketIdRequest;
import com.example.ticketbooker.DTO.Ticket.TicketResponse;
import com.example.ticketbooker.DTO.Ticket.UpdateTicketRequest;
import com.example.ticketbooker.DTO.Users.UserDTO;
import com.example.ticketbooker.Entity.Buses;
import com.example.ticketbooker.Entity.Invoices;
import com.example.ticketbooker.Entity.Routes;
import com.example.ticketbooker.Entity.Trips;
import com.example.ticketbooker.Service.TicketService;
import com.example.ticketbooker.Service.OutSource.EmailService;
import com.example.ticketbooker.Util.Enum.PaymentMethod;
import com.example.ticketbooker.Util.Enum.PaymentStatus;
import com.example.ticketbooker.Util.Enum.RouteStatus;
import com.example.ticketbooker.Util.Enum.TicketStatus;
import com.example.ticketbooker.Util.Enum.TripStatus;
import com.fasterxml.jackson.databind.ObjectMapper;

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
class TicketApiIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private TicketService ticketService;

    @MockBean
    private EmailService emailService;

    @Test
    void createTicketReturnsCreatedWhenServiceSucceeds() throws Exception {
        when(ticketService.addTicket(any())).thenReturn(true);

        mockMvc.perform(post("/api/tickets/create-ticket")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "tripId": 10,
                                  "customerName": "Nguyen Van A",
                                  "customerPhone": "0912345678",
                                  "seat": [1, 2],
                                  "invoices": {"id": 20}
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(content().string("true"));

        verify(ticketService).addTicket(any());
    }

    @Test
    void paymentInforReturnsNotFoundWhenServiceReturnsNull() throws Exception {
        when(ticketService.getPaymentInfo(any(PaymentInforRequest.class))).thenReturn(null);

        mockMvc.perform(post("/api/tickets/payment-infor")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"ticketId": 1, "customerPhone": "0912345678"}
                                """))
                .andExpect(status().isNotFound())
                .andExpect(content().string(""));
    }

    @Test
    void paymentInforReturnsPaymentDetails() throws Exception {
        PaymentInforResponse response = PaymentInforResponse.builder()
                .id(1)
                .customerName("Nguyen Van A")
                .customerPhone("0912345678")
                .totalAmount(250000)
                .departureLocation("Ha Noi")
                .arrivalLocation("Da Nang")
                .build();
        when(ticketService.getPaymentInfo(any(PaymentInforRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/tickets/payment-infor")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"ticketId": 1, "customerPhone": "0912345678"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.totalAmount").value(250000))
                .andExpect(jsonPath("$.arrivalLocation").value("Da Nang"));
    }

    @Test
    void cancelTicketReturnsConflictWhenTicketAlreadyUsed() throws Exception {
        TicketDTO ticket = sampleTicket(TicketStatus.USED);
        when(ticketService.getTicketById(any(TicketIdRequest.class)))
                .thenReturn(TicketResponse.builder().ticketsCount(1).listTickets(List.of(ticket)).build());

        mockMvc.perform(delete("/api/tickets/cancel-ticket")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"id\": 1}"))
                .andExpect(status().isConflict())
                .andExpect(content().string("false"));

        verify(ticketService, never()).updateTicket(any(UpdateTicketRequest.class));
    }

    @Test
    void cancelTicketUpdatesTicketAndSendsEmail() throws Exception {
        TicketDTO ticket = sampleTicket(TicketStatus.BOOKED);
        when(ticketService.getTicketById(any(TicketIdRequest.class)))
                .thenReturn(TicketResponse.builder().ticketsCount(1).listTickets(List.of(ticket)).build());
        when(ticketService.updateTicket(any(UpdateTicketRequest.class))).thenReturn(true);

        mockMvc.perform(delete("/api/tickets/cancel-ticket")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"id\": 1}"))
                .andExpect(status().isOk())
                .andExpect(content().string("true"));

        verify(ticketService).updateTicket(any(UpdateTicketRequest.class));
        verify(emailService).sendHtmlContent(any(), any(), any());
    }

    private TicketDTO sampleTicket(TicketStatus status) {
        Routes route = new Routes(1, "Ha Noi", "Da Nang", LocalTime.of(10, 30), RouteStatus.ACTIVE);
        Buses bus = new Buses();
        bus.setId(2);
        bus.setLicensePlate("30A-12345");
        Trips trip = new Trips(10, route, bus, null, "A", "B",
                LocalDateTime.of(2026, 6, 1, 8, 0),
                LocalDateTime.of(2026, 6, 1, 18, 30),
                250000,
                40,
                TripStatus.SCHEDULED);
        return TicketDTO.builder()
                .id(1)
                .booker(UserDTO.builder().userId(7).fullName("Nguyen Van A").email("a@example.com").build())
                .trip(trip)
                .seatIds(List.of(1))
                .seatCodes("A01")
                .invoice(new Invoices(20, 250000, PaymentStatus.PAID, LocalDateTime.now(), PaymentMethod.VNPAY))
                .customerName("Nguyen Van A")
                .customerPhone("0912345678")
                .ticketStatus(status)
                .build();
    }
}
