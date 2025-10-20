package com.example.authservice.kafka;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserRegisteredEventDTO {

    private String userId;
    private String email;
    private String verificationToken;
    private LocalDateTime registrationTime = LocalDateTime.now(); // Eventin yaranma vaxtı
}