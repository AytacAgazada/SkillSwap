package com.example.chatservice.event;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class MatchEventListener {

    @EventListener
    public void handleMatchAcceptedEvent(MatchAcceptedEvent event) {
        log.info("🟢 New chat room created for SwapId: {}", event.getSwapId());
    }
}
