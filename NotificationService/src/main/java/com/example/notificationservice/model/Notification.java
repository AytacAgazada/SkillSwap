package com.example.notificationservice.model;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

/**
 * Tətbiqdaxili (In-App) bildiriş tarixçəsini təmsil edir və MongoDB-də saxlanılır.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Document(collection = "notifications")
public class Notification {

    @Id
    private String id; // MongoDB tərəfindən yaradılan unikal ID
    private Long userId; // Bildirişin hədəfi olan istifadəçi ID
    private String type;
    private String title;
    private String message;
    private String payload;
    private boolean read; // Bildirişin oxunub-oxunmaması statusu
    private Instant createdAt; // Bildirişin yaradılma vaxtı
}