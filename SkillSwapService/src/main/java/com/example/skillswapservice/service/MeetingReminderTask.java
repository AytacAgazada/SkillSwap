package com.example.skillswapservice.service;

import com.example.skillswapservice.entity.Swap;
import com.example.skillswapservice.repository.SwapRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class MeetingReminderTask {

    private final SwapRepository swapRepository;
    private final SwapEventPublisher eventPublisher;

    @Scheduled(cron = "0 0 5 * * *") // Runs every day at 9 AM
    public void sendMeetingReminders() {
        log.info("Running meeting reminder task.");
        LocalDate tomorrow = LocalDate.now().plusDays(1);

        List<Swap> upcomingSwaps = swapRepository.findSwapsForTomorrow(tomorrow);

        for (Swap swap : upcomingSwaps) {
            log.info("Sending reminder for swap: {}", swap.getId());
            eventPublisher.publishMeetingReminderEvent(swap.getId(), swap.getUser1Id(), swap.getUser2Id(), swap.getMeetingDateTime());
        }
    }
}
