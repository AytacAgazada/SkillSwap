package com.example.notificationservice.service;

import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

@Service
public class SmtpEmailSenderImpl implements EmailSender {

    private final JavaMailSender mailSender;

    public SmtpEmailSenderImpl(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    @Override
    public void sendEmail(String to, String subject, String htmlBody) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            // true = multi-part message (fayl əlavələri və ya HTML üçün)
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlBody, true); // true = HTML mətni

            mailSender.send(message);

            System.out.println("Email successfully sent to: " + to);

        } catch (MessagingException e) {
            System.err.println("Error sending email to " + to + ": " + e.getMessage());
            // Zəifliklərdən qaçmaq üçün mail şifrəsini loglamaqdan çəkinin
            throw new RuntimeException("Failed to send email", e);
        }
    }
}