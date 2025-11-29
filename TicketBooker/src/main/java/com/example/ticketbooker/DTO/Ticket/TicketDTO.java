package com.example.ticketbooker.DTO.Ticket;

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
    
    // Dùng UserDTO thay vì Users Entity để che giấu password
    private UserDTO booker;
    
    // Nếu chăm chỉ, bạn nên tạo cả TripDTO, SeatDTO. 
    // Tạm thời dùng Entity cho Trip/Seat/Invoice cũng được nếu chúng không chứa dữ liệu nhạy cảm.
    private Trips trip;
    private String seatCodes;
    private Invoices invoice;

    private String customerName;
    private String customerPhone;
    private String qrCode;
    private TicketStatus ticketStatus;
}