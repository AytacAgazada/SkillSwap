package com.example.chatservice.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MatchAcceptedEvent {
    private String swapId;
    private String userId;
}
