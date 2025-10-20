
package com.example.authservice.kafka;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class KafkaAuthProducer {

    private final KafkaTemplate<String, UserRegisteredEventDTO> kafkaTemplate;

    // application.yml-dən topic adını inject edirik
    @Value("${kafka.topic.user-registration-topic}")
    private String registrationTopic;

    public KafkaAuthProducer(KafkaTemplate<String, UserRegisteredEventDTO> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    /**
     * Yeni istifadəçi qeydiyyatı eventini Kafka-ya göndərir.
     * Mesaj key olaraq userId istifadə edilir.
     */
    public void sendUserRegistrationEvent(UserRegisteredEventDTO event) {
        String key = event.getUserId(); // Key, mesajların sıralanması və yerləşdirilməsi üçün faydalıdır.

        kafkaTemplate.send(registrationTopic, key, event)
                .whenComplete((result, ex) -> {
                    if (ex == null) {
                        // Mesaj uğurla göndərildi, loglama (opsional)
                        System.out.println("✅ Event sent: User registered. Topic: " + registrationTopic +
                                ", Partition: " + result.getRecordMetadata().partition());
                    } else {
                        // Göndərmə uğursuz olduqda xəta idarəsi
                        System.err.println("❌ Failed to send User Registration Event: " + ex.getMessage());
                    }
                });
    }
}