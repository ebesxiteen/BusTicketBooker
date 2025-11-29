package com.example.ticketbooker.DTO.Ticket;

import java.util.List;

import com.example.ticketbooker.Entity.Invoices;
import com.example.ticketbooker.Util.Enum.TicketStatus;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class UpdateTicketRequest {
    private int id;
    private int tripId;
    private int bookerId; 
    
    private Invoices invoice;
    private String customerName;
    private String customerPhone;
    private List<Integer> seat = new java.util.ArrayList<>();
    private String qrCode;
    private TicketStatus ticketStatus;
}