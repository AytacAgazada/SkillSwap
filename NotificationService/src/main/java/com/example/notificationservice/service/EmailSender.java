package com.example.notificationservice.service;

/**
 * Xarici Email göndərmə xidmətləri üçün əsas interfeys (Abstraksiya).
 * Bu, gələcəkdə JavaMail-dan SendGrid və ya AWS SES kimi başqa bir xidmətə keçidi asanlaşdırır.
 */
public interface EmailSender {
    /**
     * Verilmiş ünvana HTML formatında email göndərir.
     * @param to Göndəriləcək ünvan
     * @param subject Emailin mövzusu
     * @param htmlBody Emailin HTML mətni
     */
    void sendEmail(String to, String subject, String htmlBody);
}
