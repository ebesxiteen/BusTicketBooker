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
public class AddTicketRequest {
    private Integer tripId;              // ID chuyến
    private Integer bookerId;
    
    private String customerName;
    private String customerPhone;
    @Builder.Default
    private List<Integer> seat = new java.util.ArrayList<>();
    private TicketStatus ticketStatus;
    private Invoices invoices;
}