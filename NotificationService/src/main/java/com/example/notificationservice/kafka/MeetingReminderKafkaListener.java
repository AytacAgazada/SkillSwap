package com.example.notificationservice.kafka;

import com.example.notificationservice.dto.MeetingReminderEvent;
import com.example.notificationservice.service.NotificationProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class MeetingReminderKafkaListener {

    private static final Logger log = LoggerFactory.getLogger(MeetingReminderKafkaListener.class);
    private final NotificationProcessor processor;

    public MeetingReminderKafkaListener(NotificationProcessor processor) {
        this.processor = processor;
    }

    @KafkaListener(topics = "meeting-reminder-events", groupId = "notification-service-group", containerFactory = "kafkaListenerContainerFactory")
    public void listen(MeetingReminderEvent event) {
        log.info("Received Kafka meeting reminder event for swap: {}", event.getSwapId());
        try {
            processor.processMeetingReminder(event);
        } catch (Exception e) {
            log.error("Error processing meeting reminder event for swap: {}", event.getSwapId(), e);
        }
    }
}
