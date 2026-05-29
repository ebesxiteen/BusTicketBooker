package com.example.ticketbooker.Util.Utils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.example.ticketbooker.Repository.SeatsRepo;

@Service
public class CleanupService {
    private static final Logger log = LoggerFactory.getLogger(CleanupService.class);

    @Autowired
    private SeatsRepo seatsRepository;
    @Scheduled(fixedRate = 20000)
    @Transactional
    public void cleanupZombieSeatsTask() {
        int deletedCount = seatsRepository.cleanupZombieSeats();
        if (deletedCount > 0) {
            log.info("Cleaned up {} temporary seats", deletedCount);
        }
    }
}
