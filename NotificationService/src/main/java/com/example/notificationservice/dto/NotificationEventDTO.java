package com.example.notificationservice.dto;

import lombok.*;

/**
 * Kafka üzərindən digər servislərdən gələn bildiriş event-lərini təmsil edir.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificationEventDTO {

    // Bildirişin növü (məs: SWAP_REQUEST, SWAP_COMPLETED, BADGE_GAINED)
    private String type;

    // Bildirişi alacaq istifadəçinin ID-si (Recipent)
    private Long userId;

    // Bildirişin başlığı (Email mövzusu və ya In-App başlıq)
    private String title;

    // Bildirişin əsas mətni
    private String message;

    // Əlavə, xüsusi məlumatları (məs: email adresi, telefon) JSON string kimi daşıyır.
    private String payload;
}