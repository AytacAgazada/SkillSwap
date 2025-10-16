package com.example.chatservice.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

import java.util.UUID;

@Data
@Document(collection = "messages")
@CompoundIndex(name = "swap_timestamp_idx", def = "{'swapId': 1, 'timestamp': 1}")
public class Message {

    @Id
    private String id;

    private String swapId;
    private UUID senderId;
    private UUID receiverId;
    private String content;
    private Instant timestamp;
}
