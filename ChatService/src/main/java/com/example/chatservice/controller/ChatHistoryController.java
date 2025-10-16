package com.example.chatservice.controller;

import com.example.chatservice.dto.MessageResponseDTO;
import com.example.chatservice.service.MessageService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
@RestController
@RequestMapping("/api/chat/history")
@RequiredArgsConstructor
public class ChatHistoryController {

    private final MessageService messageService;

    @GetMapping("/{swapId}")
    public List<MessageResponseDTO> getChatHistory(@PathVariable String swapId) {
        return messageService.getChatHistory(swapId);
    }
}
