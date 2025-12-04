package com.example.ticketbooker.DTO.Ticket;

import java.util.List;

import com.example.ticketbooker.DTO.Users.UserDTO;
import com.example.ticketbooker.Entity.Invoices;
import com.example.ticketbooker.Entity.Trips;
import com.example.ticketbooker.Util.Enum.TicketStatus;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TicketDTO {
    private Integer id;
    
    private UserDTO booker;
    
    private Trips trip;
    private List<Integer> seatIds;
    private String seatCodes;
    private Invoices invoice;

    private String customerName;
    private String customerPhone;
    private String qrCode;
    private TicketStatus ticketStatus;
}