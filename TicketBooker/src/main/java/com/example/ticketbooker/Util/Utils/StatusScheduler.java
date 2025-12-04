package com.example.ticketbooker.Util.Utils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.example.ticketbooker.Entity.Tickets;
import com.example.ticketbooker.Repository.InvoiceRepo;
import com.example.ticketbooker.Repository.TicketRepo;
import com.example.ticketbooker.Repository.TripRepo;
import com.example.ticketbooker.Service.OutSource.EmailService;
import com.example.ticketbooker.Util.Enum.PaymentStatus;
import com.example.ticketbooker.Util.Enum.TicketStatus;

@Component
public class StatusScheduler {

    @Autowired
    private TripRepo tripRepo;

    @Autowired
    private TicketRepo ticketRepo;

    @Autowired
    private InvoiceRepo invoiceRepo;

    @Autowired
    private EmailService emailService;

    // Chạy mỗi 1 gi giây (60000ms)
    @Scheduled(fixedRate = 3600000) // 1 hour
    @Transactional
    public void autoUpdateStatus() {
        LocalDateTime now = LocalDateTime.now();
        int ticketsUpdated = 0;
        for (Tickets ticket : ticketRepo.findAllByTrip_DepartureTimeBeforeAndTicketStatus(now, TicketStatus.BOOKED)) {
            ticket.setTicketStatus(TicketStatus.USED);
            if (ticket.getInvoice() != null && ticket.getInvoice().getPaymentStatus() != PaymentStatus.CANCELLED) {
                if (ticket.getInvoice().getPaymentStatus() != PaymentStatus.PAID) {
                    ticket.getInvoice().setPaymentStatus(PaymentStatus.PAID);
                    if (ticket.getInvoice().getPaymentTime() == null) {
                        ticket.getInvoice().setPaymentTime(now);
                    }
                    invoiceRepo.save(ticket.getInvoice());
                }
                sendCompletionEmail(ticket);
            }
            ticketRepo.save(ticket);
            ticketsUpdated++;
        }
        
        // 2. Chuyển chuyến xe sang COMPLETED
        int tripsUpdated = tripRepo.updateCompletedTrips(now);

        if (ticketsUpdated > 0 || tripsUpdated > 0) {
            System.out.println("--- AUTO UPDATE ---");
            System.out.println("Time: " + now);
            System.out.println("Tickets set to USED: " + ticketsUpdated);
            System.out.println("Trips set to COMPLETED: " + tripsUpdated);
        }
    }

    private void sendCompletionEmail(Tickets ticket) {
        if (ticket.getBooker() == null || ticket.getBooker().getEmail() == null) {
            return;
        }

        String customerName = ticket.getBooker().getFullName() != null ? ticket.getBooker().getFullName() : "Quý khách";
        String route = ticket.getTrip().getRoute().getDepartureLocation() + " → " + ticket.getTrip().getRoute().getArrivalLocation();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
        String formattedDeparture = ticket.getTrip().getDepartureTime().format(formatter);
        String seatList = ticket.getSeats() != null ? ticket.getSeats().stream()
                .map(seat -> seat.getSeatCode())
                .collect(Collectors.joining(", ")) : "";

        String html = "<html><body style='font-family:Arial, sans-serif; line-height:1.6;'>"
                + "<p>Xin chào <b>" + customerName + "</b>,</p>"
                + "<p>Cảm ơn bạn đã lựa chọn GreenBus. Chúng tôi ghi nhận bạn đã hoàn thành chuyến đi vừa qua.</p>"
                + "<p>Thông tin chuyến đi:</p>"
                + "<ul>"
                + "<li><b>Tuyến đường:</b> " + route + "</li>"
                + "<li><b>Thời gian khởi hành:</b> " + formattedDeparture + "</li>"
                + "<li><b>Ghế:</b> " + seatList + "</li>"
                + "</ul>"
                + "<p>Hóa đơn của bạn đã được xác nhận thanh toán. Chúc bạn có những trải nghiệm vui vẻ cùng GreenBus!</p>"
                + "<p>Trân trọng,<br/>GreenBus Line</p>"
                + "</body></html>";

        emailService.sendHtmlContent(
                ticket.getBooker().getEmail(),
                "Cảm ơn bạn đã đồng hành cùng GreenBus",
                html
        );
    }
}