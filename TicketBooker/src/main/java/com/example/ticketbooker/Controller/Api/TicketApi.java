package com.example.ticketbooker.Controller.Api;

import java.time.format.DateTimeFormatter;

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
import com.example.ticketbooker.Service.OutSource.EmailService;
import com.example.ticketbooker.Util.Enum.TicketStatus;
import com.example.ticketbooker.Util.Mapper.TicketMapper;

@RestController
@RequestMapping("/api/tickets")
public class TicketApi {

    @Autowired
    private TicketService ticketsService;

    @Autowired
    private EmailService emailService;

    @DeleteMapping("/delete")
    public boolean deleteTicket(@RequestBody TicketIdRequest id) {
        try {
            return ticketsService.deleteTicket(id);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    // 1. Hủy vé (SỬA LOGIC)
    @DeleteMapping("/cancel-ticket")
    public boolean cancelTicket(@RequestBody TicketIdRequest id) {
        
        System.out.println("Vao ham cancelTicket voi id: " + id.getId());
        try {
            // Bước 1: Lấy thông tin vé hiện tại (Trả về Response chứa List DTO)
            TicketResponse response = this.ticketsService.getTicketById(id);
            System.out.println("Response khi lấy vé: " + response);

            // Bước 2: Kiểm tra xem có vé không
            if (response.getTicketsCount() > 0) {
                // Lấy DTO đầu tiên
                TicketDTO ticketDTO = response.getListTickets().get(0);

                // Bước 3: Convert DTO -> UpdateRequest (Dùng hàm mới viết trong Mapper)
                UpdateTicketRequest updated = TicketMapper.toUpdateDTO(ticketDTO);
                
                // Bước 4: Đổi trạng thái và cập nhật
                updated.setTicketStatus(TicketStatus.CANCELLED);
                boolean result = this.ticketsService.updateTicket(updated);

 if (result) {
                    // ===== GỬI EMAIL HỦY VÉ Ở ĐÂY =====

                    // Lấy info người đặt
                    String customerName = ticketDTO.getBooker() != null
                            ? ticketDTO.getBooker().getFullName()
                            : "Quý khách";

                    String email = ticketDTO.getBooker() != null
                            ? ticketDTO.getBooker().getEmail()
                            : null;

                    if (email != null) {

                        // Tuyến đường
                        String route = ticketDTO.getTrip().getRoute().getDepartureLocation()
                                + " → "
                                + ticketDTO.getTrip().getRoute().getArrivalLocation();

                        // Format thời gian khởi hành cho đẹp
                        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
                        String formattedDeparture = ticketDTO.getTrip().getDepartureTime().format(formatter);

                        // Danh sách số ghế (chuỗi như: A01, A02)
                        String seatList = (ticketDTO.getSeatCodes() != null
                                ? ticketDTO.getSeatCodes()
                                : "Không xác định");

                        // Biển số xe (nếu cần, có thể bỏ nếu chưa map Bus trong Trip)
                        String licensePlate = ticketDTO.getTrip().getBus() != null
                                ? ticketDTO.getTrip().getBus().getLicensePlate()
                                : "Đang cập nhật";

                        String html = ""
                                + "<html><body style='font-family:Arial, sans-serif; line-height:1.6;'>"
                                + "<p>Xin chào <b>" + customerName + "</b>,</p>"
                                + "<p>Vé của bạn đã được "
                                + "<span style='color:red;font-weight:bold;'>HỦY THÀNH CÔNG</span>.</p>"
                                + "<p>Thông tin vé đã hủy:</p>"
                                + "<ul>"
                                + "<li><b>Mã vé:</b> " + ticketDTO.getId() + "</li>"
                                + "<li><b>Tuyến đường:</b> " + route + "</li>"
                                + "<li><b>Thời gian khởi hành:</b> " + formattedDeparture + "</li>"
                                + "<li><b>Biển số xe:</b> " + licensePlate + "</li>"
                                + "<li><b>Số ghế:</b> " + seatList + "</li>"
                                + "</ul>"
                                + "<p>Nếu đây không phải là yêu cầu của bạn, vui lòng liên hệ tổng đài "
                                + "<b>1900 1990</b>.</p>"
                                + "<p>Trân trọng,<br/>GreenBus Line</p>"
                                + "</body></html>";

                        emailService.sendHtmlContent(
                                email,
                                "Hủy vé thành công - GreenBus",
                                html
                        );
                    } else {
                        System.out.println("Không tìm thấy email booker, bỏ qua gửi mail hủy vé.");
                    }
                }

                return result;
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
            boolean result = this.ticketsService.addTicket(request);
            return result;
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
                System.out.println("Khong tim thay thong tin thanh toan");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
            }
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            System.out.println(e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }
}