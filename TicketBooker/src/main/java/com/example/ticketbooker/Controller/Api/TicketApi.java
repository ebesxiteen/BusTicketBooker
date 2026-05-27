package com.example.ticketbooker.Controller.Api;

import java.time.format.DateTimeFormatter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/tickets")
public class TicketApi {
    private static final Logger log = LoggerFactory.getLogger(TicketApi.class);

    private final TicketService ticketsService;
    private final EmailService emailService;

    public TicketApi(TicketService ticketsService, EmailService emailService) {
        this.ticketsService = ticketsService;
        this.emailService = emailService;
    }

    @DeleteMapping("/cancel-ticket")
    public ResponseEntity<Boolean> cancelTicket(@RequestBody TicketIdRequest id) {
        try {
            if (id == null || id.getId() <= 0) {
                return ResponseEntity.badRequest().body(false);
            }

            TicketResponse response = ticketsService.getTicketById(id);
            if (response.getTicketsCount() <= 0) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(false);
            }

            TicketDTO ticketDTO = response.getListTickets().get(0);
            if (ticketDTO.getTicketStatus() == TicketStatus.USED) {
                return ResponseEntity.status(HttpStatus.CONFLICT).body(false);
            }

            UpdateTicketRequest updated = TicketMapper.toUpdateDTO(ticketDTO);
            updated.setTicketStatus(TicketStatus.CANCELLED);
            boolean result = ticketsService.updateTicket(updated);

            if (result) {
                sendCancelEmail(ticketDTO);
                return ResponseEntity.ok(true);
            }

            return ResponseEntity.status(HttpStatus.CONFLICT).body(false);
        } catch (Exception e) {
            log.error("Failed to cancel ticket", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(false);
        }
    }

    @PostMapping("/create-ticket")
    public ResponseEntity<Boolean> createTicket(@Valid @RequestBody AddTicketRequest request) {
        try {
            boolean result = ticketsService.addTicket(request);
            if (!result) {
                return ResponseEntity.badRequest().body(false);
            }
            return ResponseEntity.status(HttpStatus.CREATED).body(true);
        } catch (Exception e) {
            log.error("Failed to create ticket", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(false);
        }
    }

    @PostMapping("/payment-infor")
    public ResponseEntity<PaymentInforResponse> paymentInfor(@RequestBody PaymentInforRequest request) {
        try {
            PaymentInforResponse response = ticketsService.getPaymentInfo(request);
            if (response == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
            }
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Failed to get payment information", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    private void sendCancelEmail(TicketDTO ticketDTO) {
        String email = ticketDTO.getBooker() != null ? ticketDTO.getBooker().getEmail() : null;
        if (email == null || email.isBlank()) {
            log.info("Skip cancel email because booker email is missing for ticket {}", ticketDTO.getId());
            return;
        }

        String customerName = ticketDTO.getBooker().getFullName() != null
                ? ticketDTO.getBooker().getFullName()
                : "Quy khach";
        String route = ticketDTO.getTrip().getRoute().getDepartureLocation()
                + " -> "
                + ticketDTO.getTrip().getRoute().getArrivalLocation();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
        String formattedDeparture = ticketDTO.getTrip().getDepartureTime().format(formatter);
        String seatList = ticketDTO.getSeatCodes() != null ? ticketDTO.getSeatCodes() : "Khong xac dinh";
        String licensePlate = ticketDTO.getTrip().getBus() != null
                ? ticketDTO.getTrip().getBus().getLicensePlate()
                : "Dang cap nhat";

        String html = ""
                + "<html><body style='font-family:Arial, sans-serif; line-height:1.6;'>"
                + "<p>Xin chao <b>" + customerName + "</b>,</p>"
                + "<p>Ve cua ban da duoc <b style='color:red;'>huy thanh cong</b>.</p>"
                + "<ul>"
                + "<li><b>Ma ve:</b> " + ticketDTO.getId() + "</li>"
                + "<li><b>Tuyen duong:</b> " + route + "</li>"
                + "<li><b>Thoi gian khoi hanh:</b> " + formattedDeparture + "</li>"
                + "<li><b>Bien so xe:</b> " + licensePlate + "</li>"
                + "<li><b>So ghe:</b> " + seatList + "</li>"
                + "</ul>"
                + "<p>Neu day khong phai yeu cau cua ban, vui long lien he tong dai <b>1900 1990</b>.</p>"
                + "<p>Tran trong,<br/>GreenBus Line</p>"
                + "</body></html>";

        emailService.sendHtmlContent(email, "Huy ve thanh cong - GreenBus", html);
    }
}
