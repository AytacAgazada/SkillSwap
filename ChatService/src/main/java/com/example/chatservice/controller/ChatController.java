package com.example.chatservice.controller;

import com.example.chatservice.dto.MessageResponseDTO;
import com.example.chatservice.dto.MessageRequestDTO;
import com.example.chatservice.service.MessageService;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.util.UUID;

@SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
@Controller
@RequiredArgsConstructor
public class ChatController {

    private final SimpMessagingTemplate messagingTemplate;
    private final MessageService messageService;

    @MessageMapping("/chat.sendMessage")
    public void sendMessage(@Payload MessageRequestDTO dto, SimpMessageHeaderAccessor headerAccessor) {
        String authUserId = (String) headerAccessor.getSessionAttributes().get("X-Auth-User-Id");
        MessageResponseDTO saved = messageService.saveMessage(dto, UUID.fromString(authUserId));
        messagingTemplate.convertAndSend("/topic/swap/" + saved.getSwapId(), saved);
    }
}
