package com.example.chatservice.dto;

import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.util.UUID;

@Data
@Builder
public class MessageResponseDTO {
    private String id;
    private String swapId;
    private UUID senderId;
    private UUID receiverId;
    private String senderName;
    private String receiverName;
    private String content;
    private Instant timestamp;
}
