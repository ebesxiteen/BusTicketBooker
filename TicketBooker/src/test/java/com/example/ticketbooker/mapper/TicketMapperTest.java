package com.example.ticketbooker.mapper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.List;

import org.junit.jupiter.api.Test;

import com.example.ticketbooker.DTO.Ticket.TicketResponse;
import com.example.ticketbooker.Entity.Invoices;
import com.example.ticketbooker.Entity.Routes;
import com.example.ticketbooker.Entity.Seats;
import com.example.ticketbooker.Entity.Tickets;
import com.example.ticketbooker.Entity.Trips;
import com.example.ticketbooker.Util.Enum.TicketStatus;
import com.example.ticketbooker.Util.Mapper.TicketMapper;

class TicketMapperTest {

    @Test
    void toDTOAggregatesSeatCodesAndBooker() {
        Seats seatA = new Seats(1, null, "A01");
        Seats seatB = new Seats(2, null, "B02");

        Invoices invoice = new Invoices();
        invoice.setTotalAmount(200000);

        Routes route = new Routes();
        route.setDepartureLocation("A");
        route.setArrivalLocation("B");

        Trips trip = new Trips();
        trip.setId(9);
        trip.setRoute(route);

        var user = new com.example.ticketbooker.Entity.Users();
        user.setId(3);
        user.setFullName("Customer");
        user.setEmail("customer@example.com");

        Tickets ticket = new Tickets();
        ticket.setId(20);
        ticket.setTrip(trip);
        ticket.setInvoice(invoice);
        ticket.setCustomerName("Customer");
        ticket.setCustomerPhone("0909");
        ticket.setQrCode("QR");
        ticket.setTicketStatus(TicketStatus.BOOKED);
        ticket.setSeats(List.of(seatB, seatA));
        ticket.setBooker(user);

        var dto = TicketMapper.toDTO(ticket);

        assertEquals("A01, B02", dto.getSeatCodes());
        assertEquals(List.of(1, 2), dto.getSeatIds());
        assertNotNull(dto.getBooker());
        assertEquals(3, dto.getBooker().getUserId());
    }

    @Test
    void toResponseDTOWrapsList() {
        Tickets ticket = new Tickets();
        ticket.setId(1);
        ticket.setTicketStatus(TicketStatus.BOOKED);

        TicketResponse response = TicketMapper.toResponseDTO(List.of(ticket));

        assertEquals(1, response.getTicketsCount());
        assertEquals(1, response.getListTickets().size());
        assertEquals(TicketStatus.BOOKED, response.getListTickets().get(0).getTicketStatus());
    }
}
