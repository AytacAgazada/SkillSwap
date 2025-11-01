package com.example.gamificationservice.entity;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Data
@Document(collection = "xp_transactions")
public class XpTransaction {
    @Id
    private String id;
    private String userId;
    private String eventType;
    private int xpGained;
    private LocalDateTime createdAt;
}
