package com.example.notificationservice.dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class MeetingReminderEvent {
    private Long swapId;
    private UUID user1Id;
    private UUID user2Id;
    private LocalDateTime meetingDateTime;
}
