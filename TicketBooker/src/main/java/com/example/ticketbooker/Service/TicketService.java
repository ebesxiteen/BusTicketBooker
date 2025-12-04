package com.example.ticketbooker.Service;

import java.io.ByteArrayInputStream;
import java.time.LocalDate;

import org.springframework.data.domain.Pageable;

import com.example.ticketbooker.DTO.Ticket.AddTicketRequest;
import com.example.ticketbooker.DTO.Ticket.PaymentInforRequest;
import com.example.ticketbooker.DTO.Ticket.PaymentInforResponse;
import com.example.ticketbooker.DTO.Ticket.TicketIdRequest;
import com.example.ticketbooker.DTO.Ticket.TicketResponse;
import com.example.ticketbooker.DTO.Ticket.TicketStatsDTO;
import com.example.ticketbooker.DTO.Ticket.UpdateTicketRequest;
import com.example.ticketbooker.Util.Enum.TicketStatus;

public interface TicketService {
    boolean addTicket(AddTicketRequest dto);
    boolean updateTicket(UpdateTicketRequest dto);
    
    TicketResponse getAllTickets();
    TicketResponse getTicketById(TicketIdRequest dto);
    PaymentInforResponse getPaymentInfo(PaymentInforRequest request);

    TicketResponse getTicketsByUserId(int userId); 

    TicketResponse searchTickets(int userId, Integer ticketId, LocalDate departureDate, String route, TicketStatus status);
    
    TicketStatsDTO getTicketStats(String period, LocalDate selectedDate);
    
    TicketResponse getAllTickets(Pageable pageable);
    TicketResponse getTicketsByTripId(int tripId, Pageable pageable);
    TicketResponse getTicketsByStatus(TicketStatus status, Pageable pageable);
    TicketResponse getTicketsByTripIdAndStatus(int tripId, TicketStatus status, Pageable pageable);
    
    ByteArrayInputStream exportTicketsToExcelByTripId(int tripId);
    ByteArrayInputStream exportAllTicketsToExcel();
}