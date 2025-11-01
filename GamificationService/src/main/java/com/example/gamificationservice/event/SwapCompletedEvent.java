package com.example.gamificationservice.event;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class SwapCompletedEvent {
    private Long swapId;
    private String userId;
    private LocalDateTime timestamp;
}
