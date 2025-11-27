package com.example.ticketbooker.Controller.Api;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.ticketbooker.DTO.Ticket.AddTicketRequest;
import com.example.ticketbooker.DTO.Ticket.PaymentInforRequest;
import com.example.ticketbooker.DTO.Ticket.PaymentInforResponse;
import com.example.ticketbooker.DTO.Ticket.TicketDTO;
import com.example.ticketbooker.DTO.Ticket.TicketIdRequest;
import com.example.ticketbooker.DTO.Ticket.TicketResponse;
import com.example.ticketbooker.DTO.Ticket.UpdateTicketRequest;
import com.example.ticketbooker.Service.TicketService;
import com.example.ticketbooker.Util.Enum.TicketStatus;
import com.example.ticketbooker.Util.Mapper.TicketMapper;

@RestController
@RequestMapping("/api/tickets")
public class TicketApi {

    @Autowired
    private TicketService ticketsService;

    // 1. Hủy vé (SỬA LOGIC)
    @DeleteMapping("/cancel-ticket")
    public boolean cancelTicket(@RequestBody TicketIdRequest id) {
        try {
            // Bước 1: Lấy thông tin vé hiện tại (Trả về Response chứa List DTO)
            TicketResponse response = this.ticketsService.getTicketById(id);

            // Bước 2: Kiểm tra xem có vé không
            if (response.getTicketsCount() > 0) {
                // Lấy DTO đầu tiên
                TicketDTO ticketDTO = response.getListTickets().get(0);

                // Bước 3: Convert DTO -> UpdateRequest (Dùng hàm mới viết trong Mapper)
                UpdateTicketRequest updated = TicketMapper.toUpdateDTO(ticketDTO);
                
                // Bước 4: Đổi trạng thái và cập nhật
                updated.setTicketStatus(TicketStatus.CANCELLED);
                return this.ticketsService.updateTicket(updated);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    // 2. Tạo vé mới
    @PostMapping("/create-ticket")
    public boolean createTicket(@RequestBody AddTicketRequest request) {
        try {
            return this.ticketsService.addTicket(request);
        } catch (Exception e) {
            System.out.println(e.getMessage());
            return false;
        }
    }

    // 3. Lấy thông tin thanh toán
    @PostMapping("/payment-infor")
    public ResponseEntity<PaymentInforResponse> paymentInfor(@RequestBody PaymentInforRequest request) {
        try {
            PaymentInforResponse response = ticketsService.getPaymentInfo(request);
            if (response == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
            }
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            System.out.println(e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }
}