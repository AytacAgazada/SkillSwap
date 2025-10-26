package com.example.authservice.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final JavaMailSender mailSender;

    @Async("taskExecutor") // `AsyncConfig`də təyin olunmuş `taskExecutor` bean-ini istifadə edir
    public void sendEmail(String to, String subject, String body) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8"); // true for multipart, UTF-8 for encoding
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(body, true); // true for HTML content
            mailSender.send(message);
            log.info("Email sent successfully to: {}", to);
        } catch (MessagingException e) {
            log.error("Failed to send email to {}: {}", to, e.getMessage());
            // Real tətbiqlərdə bu xətanı istifadəçiyə qaytarmaq və ya Retry mexanizmi tətbiq etmək olar.
            throw new RuntimeException("Email göndərilərkən xəta baş verdi: " + e.getMessage());
        }
    }

    @Async("taskExecutor")
    public void sendTestEmail(String to) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setTo(to);
            helper.setSubject("Test Email from SkillSwap");
            helper.setText("This is a test email from SkillSwap.", false);
            mailSender.send(message);
            log.info("Test email sent successfully to: {}", to);
        } catch (MessagingException e) {
            log.error("Failed to send test email to {}: {}", to, e.getMessage());
            throw new RuntimeException("Test email göndərilərkən xəta baş verdi: " + e.getMessage());
        }
    }
}