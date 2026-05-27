package com.example.ticketbooker.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.example.ticketbooker.Entity.Invoices;
import com.example.ticketbooker.Entity.Routes;
import com.example.ticketbooker.Entity.Seats;
import com.example.ticketbooker.Entity.Tickets;
import com.example.ticketbooker.Entity.Trips;
import com.example.ticketbooker.Entity.Users;
import com.example.ticketbooker.Repository.InvoiceRepo;
import com.example.ticketbooker.Repository.TicketRepo;
import com.example.ticketbooker.Repository.TripRepo;
import com.example.ticketbooker.Service.OutSource.EmailService;
import com.example.ticketbooker.Util.Enum.PaymentMethod;
import com.example.ticketbooker.Util.Enum.PaymentStatus;
import com.example.ticketbooker.Util.Enum.TicketStatus;
import com.example.ticketbooker.Util.Utils.StatusScheduler;

@ExtendWith(MockitoExtension.class)
class StatusSchedulerTest {

    @Mock
    private TripRepo tripRepo;

    @Mock
    private TicketRepo ticketRepo;

    @Mock
    private InvoiceRepo invoiceRepo;

    @Mock
    private EmailService emailService;

    @InjectMocks
    private StatusScheduler statusScheduler;

    @Test
    void autoUpdateStatusUpdatesTicketsAndInvoices() {
        Routes route = new Routes();
        route.setDepartureLocation("HCM");
        route.setArrivalLocation("HN");

        Trips trip = new Trips();
        trip.setId(7);
        trip.setRoute(route);
        trip.setDepartureTime(LocalDateTime.now().minusHours(2));
        trip.setAvailableSeats(5);

        Invoices invoice = new Invoices();
        invoice.setPaymentStatus(PaymentStatus.PENDING);
        invoice.setPaymentMethod(PaymentMethod.CASH);

        Users booker = new Users();
        booker.setEmail("customer@example.com");
        booker.setFullName("Customer Name");

        Seats seat = new Seats();
        seat.setSeatCode("A01");

        Tickets ticket = new Tickets();
        ticket.setTrip(trip);
        ticket.setInvoice(invoice);
        ticket.setBooker(booker);
        ticket.setSeats(List.of(seat));
        ticket.setTicketStatus(TicketStatus.BOOKED);

        when(ticketRepo.findAllByTrip_DepartureTimeBeforeAndTicketStatus(any(LocalDateTime.class), eq(TicketStatus.BOOKED)))
                .thenReturn(List.of(ticket));
        when(invoiceRepo.save(any(Invoices.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(ticketRepo.save(any(Tickets.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(tripRepo.updateCompletedTrips(any(LocalDateTime.class))).thenReturn(1);
        when(emailService.sendHtmlContent(eq("customer@example.com"), any(String.class), any(String.class))).thenReturn(true);

        statusScheduler.autoUpdateStatus();

        assertEquals(TicketStatus.USED, ticket.getTicketStatus());
        assertEquals(PaymentStatus.PAID, invoice.getPaymentStatus());
        assertNotNull(invoice.getPaymentTime());
        verify(ticketRepo).save(ticket);
        verify(invoiceRepo).save(invoice);
        verify(emailService).sendHtmlContent(eq("customer@example.com"), any(String.class), any(String.class));
    }
}
