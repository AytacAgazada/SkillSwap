package com.example.notificationservice.service;

import com.example.notificationservice.dto.NotificationEventDTO;
import com.example.notificationservice.model.Notification;
import com.example.notificationservice.repository.NotificationRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
public class NotificationProcessor {

    private static final Logger log = LoggerFactory.getLogger(NotificationProcessor.class);

    private final NotificationRepository repository;
    private final EmailSender emailSender;
    private final SimpMessagingTemplate webSocketTemplate;
    private final ObjectMapper mapper;
    private final com.example.notificationservice.client.UserClient userClient;

    public NotificationProcessor(NotificationRepository repository,
                                 EmailSender emailSender,
                                 SimpMessagingTemplate webSocketTemplate,
                                 ObjectMapper mapper,
                                 com.example.notificationservice.client.UserClient userClient) {
        this.repository = repository;
        this.emailSender = emailSender;
        this.webSocketTemplate = webSocketTemplate;
        this.mapper = mapper;
        this.userClient = userClient;
    }

    public void processMeetingReminder(com.example.notificationservice.dto.MeetingReminderEvent event) {
        com.example.notificationservice.dto.UserDTO user1 = userClient.getUserById(event.getUser1Id());
        com.example.notificationservice.dto.UserDTO user2 = userClient.getUserById(event.getUser2Id());

        String subject = "Meeting Reminder";
        String body = String.format("Hi, this is a reminder for your meeting tomorrow at %s.", event.getMeetingDateTime().toString());

        emailSender.sendEmail(user1.getEmail(), subject, body);
        emailSender.sendEmail(user2.getEmail(), subject, body);
    }

    @Transactional
    public void process(NotificationEventDTO event) {
        Notification savedNotification = saveInAppNotification(event);
        sendRealTimeNotification(savedNotification);
        handleExternalChannels(event);
    }

    private Notification saveInAppNotification(NotificationEventDTO event) {
        Notification n = Notification.builder()
                .userId(event.getUserId())
                .type(event.getType())
                .title(event.getTitle())
                .message(event.getMessage())
                .payload(event.getPayload())
                .read(false)
                .createdAt(Instant.now())
                .build();
        return repository.save(n);
    }

    private void sendRealTimeNotification(Notification notification) {
        String destination = "/topic/user/" + notification.getUserId();
        webSocketTemplate.convertAndSend(destination, notification);
        log.info("WebSocket notification sent to userId={}", notification.getUserId());
    }

    private void handleExternalChannels(NotificationEventDTO event) {
        String userEmail = extractEmailFromPayload(event.getPayload());

        // Professional pattern: only high-priority events
        if (userEmail != null && ("USER_REGISTERED".equals(event.getType())
                || "SWAP_REQUEST".equals(event.getType()))) {
            try {
                emailSender.sendEmail(userEmail, event.getTitle(), event.getMessage());
                log.info("Email notification sent for userId={} type={}", event.getUserId(), event.getType());
            } catch (Exception e) {
                log.error("Failed to send email for userId={} type={}", event.getUserId(), event.getType(), e);
                // TODO: Consider retry or DLQ mechanism
            }
        } else if (userEmail == null) {
            log.warn("No email in payload for event type={} userId={}", event.getType(), event.getUserId());
        }
    }

    private String extractEmailFromPayload(String payload) {
        if (payload == null) return null;
        try {
            JsonNode node = mapper.readTree(payload);
            if (node.has("email")) return node.get("email").asText();
        } catch (Exception e) {
            log.error("Failed to parse payload for email extraction: {}", payload, e);
        }
        return null;
    }
}
