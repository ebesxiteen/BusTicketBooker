package com.example.ticketbooker.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.ByteArrayInputStream;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.example.ticketbooker.DTO.Ticket.AddTicketRequest;
import com.example.ticketbooker.DTO.Ticket.PaymentInforRequest;
import com.example.ticketbooker.DTO.Ticket.PaymentInforResponse;
import com.example.ticketbooker.DTO.Ticket.TicketStatsDTO;
import com.example.ticketbooker.DTO.Ticket.UpdateTicketRequest;
import com.example.ticketbooker.Entity.Buses;
import com.example.ticketbooker.Entity.Driver;
import com.example.ticketbooker.Entity.Invoices;
import com.example.ticketbooker.Entity.Routes;
import com.example.ticketbooker.Entity.Seats;
import com.example.ticketbooker.Entity.Tickets;
import com.example.ticketbooker.Entity.Trips;
import com.example.ticketbooker.Repository.InvoiceRepo;
import com.example.ticketbooker.Repository.SeatsRepo;
import com.example.ticketbooker.Repository.TicketRepo;
import com.example.ticketbooker.Repository.TripRepo;
import com.example.ticketbooker.Repository.UserRepo;
import com.example.ticketbooker.Service.OutSource.EmailService;
import com.example.ticketbooker.Service.ServiceImp.TicketServiceImp;
import com.example.ticketbooker.Util.Enum.PaymentMethod;
import com.example.ticketbooker.Util.Enum.PaymentStatus;
import com.example.ticketbooker.Util.Enum.RouteStatus;
import com.example.ticketbooker.Util.Enum.TicketStatus;
import com.example.ticketbooker.Util.Enum.TripStatus;

@ExtendWith(MockitoExtension.class)
class TicketServiceImpTest {

    @Mock
    private TicketRepo ticketRepository;

    @Mock
    private SeatsRepo seatsRepo;

    @Mock
    private EmailService emailService;

    @Mock
    private UserRepo userRepository;

    @Mock
    private TripRepo tripRepos;

    @Mock
    private InvoiceRepo invoiceRepo;

    @InjectMocks
    private TicketServiceImp ticketServiceImp;

    @Test
    void addTicketReturnsFalseForInvalidRequest() {
        assertFalse(ticketServiceImp.addTicket(null));
        verify(ticketRepository, never()).save(any());
    }

    @Test
    void addTicketCreatesBookedTicketWithSeatsAndInvoice() {
        Trips trip = sampleTrip();
        Invoices invoice = new Invoices(20, 250000, PaymentStatus.PENDING, null, PaymentMethod.VNPAY);
        Seats seat = new Seats(1, trip, "A01");
        AddTicketRequest request = AddTicketRequest.builder()
                .tripId(10)
                .customerName("Nguyen Van A")
                .customerPhone("0912345678")
                .seat(List.of(1))
                .invoices(invoice)
                .build();
        ArgumentCaptor<Tickets> ticketCaptor = ArgumentCaptor.forClass(Tickets.class);
        when(tripRepos.findById(Integer.valueOf(10))).thenReturn(Optional.of(trip));
        when(invoiceRepo.findById(Integer.valueOf(20))).thenReturn(Optional.of(invoice));
        when(seatsRepo.findById(Integer.valueOf(1))).thenReturn(Optional.of(seat));

        boolean result = ticketServiceImp.addTicket(request);

        assertTrue(result);
        verify(ticketRepository).save(ticketCaptor.capture());
        Tickets saved = ticketCaptor.getValue();
        assertEquals(trip, saved.getTrip());
        assertEquals(invoice, saved.getInvoice());
        assertEquals(TicketStatus.BOOKED, saved.getTicketStatus());
        assertEquals(List.of(seat), saved.getSeats());
    }

    @Test
    void updateTicketRejectsChangingUsedTicketBackToAnotherStatus() {
        Tickets ticket = sampleTicket(TicketStatus.USED);
        UpdateTicketRequest request = UpdateTicketRequest.builder()
                .id(1)
                .ticketStatus(TicketStatus.BOOKED)
                .customerName("Name")
                .customerPhone("Phone")
                .build();
        when(ticketRepository.findById(1)).thenReturn(Optional.of(ticket));

        assertFalse(ticketServiceImp.updateTicket(request));
        verify(ticketRepository, never()).save(any());
    }

    @Test
    void updateTicketCancelsTicketAndInvoiceAndDeletesSeats() {
        Tickets ticket = sampleTicket(TicketStatus.BOOKED);
        ticket.setInvoice(new Invoices(20, 250000, PaymentStatus.PENDING, null, PaymentMethod.VNPAY));
        Seats seat = new Seats(1, ticket.getTrip(), "A01");
        ticket.setSeats(new java.util.ArrayList<>(List.of(seat)));
        UpdateTicketRequest request = UpdateTicketRequest.builder()
                .id(1)
                .ticketStatus(TicketStatus.CANCELLED)
                .customerName("Name")
                .customerPhone("Phone")
                .build();
        when(ticketRepository.findById(1)).thenReturn(Optional.of(ticket));

        boolean result = ticketServiceImp.updateTicket(request);

        assertTrue(result);
        assertEquals(TicketStatus.CANCELLED, ticket.getTicketStatus());
        assertEquals(PaymentStatus.CANCELLED, ticket.getInvoice().getPaymentStatus());
        assertTrue(ticket.getSeats().isEmpty());
        verify(seatsRepo).deleteAll(List.of(seat));
        verify(invoiceRepo).save(ticket.getInvoice());
        verify(ticketRepository).save(ticket);
    }

    @Test
    void getPaymentInfoReturnsInfoOnlyForBookedTicketAndMatchingPhone() {
        Tickets ticket = sampleTicket(TicketStatus.BOOKED);
        ticket.setCustomerPhone("0912345678");
        when(ticketRepository.findById(1)).thenReturn(Optional.of(ticket));

        PaymentInforResponse result = ticketServiceImp.getPaymentInfo(new PaymentInforRequest(1, "0912345678"));
        PaymentInforResponse mismatch = ticketServiceImp.getPaymentInfo(new PaymentInforRequest(1, "000"));

        assertNotNull(result);
        assertNull(mismatch);
    }

    @Test
    void ticketStatsCountsCurrentAndPreviousMonth() {
        LocalDate selectedDate = LocalDate.of(2026, 5, 29);
        when(ticketRepository.countTicketsByPaymentTimeBetween(any(), any())).thenReturn(8).thenReturn(3);

        TicketStatsDTO result = ticketServiceImp.getTicketStats("Month", selectedDate);

        assertEquals(8, result.getCurrentPeriodTicketCount());
        assertEquals(3, result.getPreviousPeriodTicketCount());
    }

    @Test
    void exportAllTicketsToExcelReturnsStream() {
        Tickets ticket = sampleTicket(TicketStatus.BOOKED);
        ticket.setSeats(List.of(new Seats(1, ticket.getTrip(), "A01")));
        when(ticketRepository.findAll()).thenReturn(List.of(ticket));

        ByteArrayInputStream stream = ticketServiceImp.exportAllTicketsToExcel();

        assertNotNull(stream);
    }

    private Tickets sampleTicket(TicketStatus status) {
        return Tickets.builder()
                .id(1)
                .trip(sampleTrip())
                .invoice(new Invoices(20, 250000, PaymentStatus.PENDING, LocalDateTime.of(2026, 5, 29, 12, 0), PaymentMethod.VNPAY))
                .customerName("Nguyen Van A")
                .customerPhone("0912345678")
                .seats(new java.util.ArrayList<>())
                .ticketStatus(status)
                .build();
    }

    private Trips sampleTrip() {
        Routes route = new Routes(1, "Ha Noi", "Da Nang", LocalTime.of(10, 30), RouteStatus.ACTIVE);
        return new Trips(
                10,
                route,
                new Buses(),
                new Driver(),
                "A",
                "B",
                LocalDateTime.of(2026, 6, 1, 8, 0),
                LocalDateTime.of(2026, 6, 1, 18, 30),
                150000,
                40,
                TripStatus.SCHEDULED);
    }
}
