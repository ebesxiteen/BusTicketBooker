package com.example.ticketbooker.DTO.Ticket;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;

import com.example.ticketbooker.Entity.Tickets;
import com.example.ticketbooker.Util.Mapper.TicketMapper; // Nhớ import Mapper

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class TicketResponse {
    private int ticketsCount;
    private List<TicketDTO> listTickets; 
    
    private int currentPage;
    private int totalPages;

    // Constructor hỗ trợ phân trang (Tự động convert Entity -> DTO)
    public TicketResponse(Page<Tickets> ticketPage) {
        this.ticketsCount = (int) ticketPage.getTotalElements();
        
        // Logic chuyển đổi: Lấy list Entity -> Dùng Mapper -> Ra list DTO
        this.listTickets = ticketPage.getContent().stream()
                .map(TicketMapper::toDTO)
                .collect(Collectors.toList());
                
        this.currentPage = ticketPage.getNumber() + 1;
        this.totalPages = ticketPage.getTotalPages();
    }

    // Constructor hỗ trợ list thường
    public static TicketResponse fromList(List<Tickets> tickets) {
        // Cũng phải convert sang DTO
        List<TicketDTO> dtos = tickets.stream()
                .map(TicketMapper::toDTO)
                .collect(Collectors.toList());
                
        return new TicketResponse(tickets.size(), dtos, 0, 0);
    }
}