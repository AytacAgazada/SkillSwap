package com.example.notificationservice.kafka;

import com.example.notificationservice.dto.NotificationEventDTO;
import com.example.notificationservice.service.NotificationProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
@Component
public class NotificationKafkaListener {

    private static final Logger log = LoggerFactory.getLogger(NotificationKafkaListener.class);
    private final NotificationProcessor processor;

    public NotificationKafkaListener(NotificationProcessor processor) {
        this.processor = processor;
    }

    @KafkaListener(topics = "notifications", groupId = "notification-service-group", containerFactory = "kafkaListenerContainerFactory")
    public void listen(NotificationEventDTO event) {
        log.info("Received Kafka notification event: type={} userId={}", event.getType(), event.getUserId());
        try {
            processor.process(event);
        } catch (Exception e) {
            log.error("Error processing notification event: type={} userId={}", event.getType(), event.getUserId(), e);
            // Production-ready: DLQ veya retry mekanizmi buraya əlavə edilməlidir
        }
    }
}
