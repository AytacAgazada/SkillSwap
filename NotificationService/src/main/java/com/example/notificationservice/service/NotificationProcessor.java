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

import java.time.Instant;

// @SuppressWarnings qaldırıldı, çünki bütün dependency-lər konstruktorda var.
@SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
@Service
public class NotificationProcessor {

    private static final Logger log = LoggerFactory.getLogger(NotificationProcessor.class);

    private final NotificationRepository repository;
    private final EmailSender emailSender;
    private final SimpMessagingTemplate webSocketTemplate;
    private final ObjectMapper mapper;

    // Konstruktorda bütün zəruri dependency-lər daxil edilir
    public NotificationProcessor(NotificationRepository repository, EmailSender emailSender, SimpMessagingTemplate webSocketTemplate, ObjectMapper mapper) {
        this.repository = repository;
        this.emailSender = emailSender;
        this.webSocketTemplate = webSocketTemplate;
        this.mapper = mapper;
    }

    /**
     * Kafka-dan gələn event-i emal edir: DB-yə yazır, WebSocket üzərindən yayır, Email göndərir.
     * @param event Kafka-dan gələn Notification Event DTO
     */
    public void process(NotificationEventDTO event) {
        // 1. In-App Bildirişin Yaddaşa yazılması (Tarixçə)
        Notification savedNotification = saveInAppNotification(event);

        // 2. Real-Time In-App Bildirişin göndərilməsi (WebSocket)
        sendRealTimeNotification(savedNotification);

        // 3. Xarici Kanallar (Yalnız Email qalır)
        handleExternalChannels(event);
    }

    // --- Daxili Metodlar ---

    private Notification saveInAppNotification(NotificationEventDTO event) {
        // MongoDB Modelini DTO-dan yaradır
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
        // Bildirişi istifadəçinin xüsusi WebSocket mövzusuna göndərir (/topic/user/{userId})
        String destination = "/topic/user/" + notification.getUserId();
        webSocketTemplate.convertAndSend(destination, notification);
        log.info("WebSocket notification sent to user: {}", notification.getUserId());
    }

    private void handleExternalChannels(NotificationEventDTO event) {
        String userEmail = extractEmailFromPayload(event.getPayload());

        // Qərar məntiqi: Yalnız yüksək prioritetli hadisələr üçün Email göndərilir.
        if (("EMAIL_VERIFICATION".equals(event.getType()) || "SWAP_REQUEST".equals(event.getType())) && userEmail != null) {
            emailSender.sendEmail(userEmail, event.getTitle(), event.getMessage());
            log.info("Email notification sent for event type: {}", event.getType());
        }
    }

    private String extractEmailFromPayload(String payload) {
        if (payload == null) return null;
        try {
            JsonNode node = mapper.readTree(payload);
            if (node.has("email")) {
                return node.get("email").asText();
            }
        } catch (Exception e) {
            // JSON parsing xətasını loglayırıq.
            log.error("Failed to parse payload for email extraction: {}", payload, e);
        }
        return null;
    }
}
