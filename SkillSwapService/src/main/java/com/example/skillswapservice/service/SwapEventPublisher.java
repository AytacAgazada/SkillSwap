package com.example.skillswapservice.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class SwapEventPublisher {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public static final String SWAP_COMPLETED_TOPIC = "swap-completed-events";
    public static final String MEETING_REMINDER_TOPIC = "meeting-reminder-events";
    public static final String MATCH_REQUESTED_TOPIC = "match-requested-events";

    public record SwapCompletedEvent(Long swapId, UUID user1Id, UUID user2Id) {}
    public record MeetingReminderEvent(Long swapId, UUID user1Id, UUID user2Id, java.time.LocalDateTime meetingDateTime) {}
    public record MatchRequestedEvent(UUID offererId, UUID requestedId, Long offerId) {}

    public void publishSwapCompletedEvent(Long swapId, UUID user1Id, UUID user2Id) {
        SwapCompletedEvent event = new SwapCompletedEvent(swapId, user1Id, user2Id);
        kafkaTemplate.send(SWAP_COMPLETED_TOPIC, event);
        log.info("Published swap completed event for swapId: {}", swapId);
    }

    public void publishMeetingReminderEvent(Long swapId, UUID user1Id, UUID user2Id, java.time.LocalDateTime meetingDateTime) {
        MeetingReminderEvent event = new MeetingReminderEvent(swapId, user1Id, user2Id, meetingDateTime);
        kafkaTemplate.send(MEETING_REMINDER_TOPIC, event);
        log.info("Published meeting reminder event for swapId: {}", swapId);
    }

    public void publishMatchRequestedEvent(UUID offererId, UUID requestedId, Long offerId) {
        MatchRequestedEvent event = new MatchRequestedEvent(offererId, requestedId, offerId);
        kafkaTemplate.send(MATCH_REQUESTED_TOPIC, event);
    }
}