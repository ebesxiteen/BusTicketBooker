package com.example.ticketbooker.Util.Utils;

import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.example.ticketbooker.Repository.TicketRepo;
import com.example.ticketbooker.Repository.TripRepo;

@Component
public class StatusScheduler {

    @Autowired
    private TripRepo tripRepo;

    @Autowired
    private TicketRepo ticketRepo;

    // Chạy mỗi 1 gi giây (60000ms)
    @Scheduled(fixedRate = 60000) 
    public void autoUpdateStatus() {
        LocalDateTime now = LocalDateTime.now();

        // 1. Chuyển vé sang USED trước (logic: xe chạy rồi thì vé thành USED)
        // Lưu ý: Dùng t.trip.departureTime nên update vé trước hay sau Trip không quá quan trọng 
        // miễn là dựa vào thời gian. Nhưng nên update vé trước khi update trạng thái Trip nếu logic yêu cầu chặt chẽ.
        // Ở đây ta update độc lập dựa vào thời gian thực.
        
        int ticketsUpdated = ticketRepo.updateUsedTickets(now);
        
        // 2. Chuyển chuyến xe sang COMPLETED
        int tripsUpdated = tripRepo.updateCompletedTrips(now);

        if (ticketsUpdated > 0 || tripsUpdated > 0) {
            System.out.println("--- AUTO UPDATE ---");
            System.out.println("Time: " + now);
            System.out.println("Tickets set to USED: " + ticketsUpdated);
            System.out.println("Trips set to COMPLETED: " + tripsUpdated);
        }
    }
}