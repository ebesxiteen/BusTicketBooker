package com.example.ticketbooker.Util.Utils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.ticketbooker.Repository.SeatsRepo;

@Service
public class CleanupService {

    @Autowired
    private SeatsRepo seatsRepository;
    @Scheduled(fixedRate = 20000)
    @Transactional
    public void cleanupZombieSeatsTask() {
        int deletedCount = seatsRepository.cleanupZombieSeats();
        if (deletedCount > 0) {
            System.out.println(" Đã dọn dẹp thành công " + deletedCount + " Ghế Zombie.");
        }
    }
}