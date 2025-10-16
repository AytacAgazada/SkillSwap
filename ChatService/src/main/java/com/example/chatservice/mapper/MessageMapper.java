package com.example.chatservice.mapper;


import com.example.chatservice.dto.MessageResponseDTO;
import com.example.chatservice.model.Message;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Component
public class MessageMapper {

        public Message toEntity(com.example.chatservice.dto.MessageRequestDTO dto) {
        Message message = new Message();
        message.setSwapId(dto.getSwapId());
        message.setSenderId(dto.getSenderId());
        message.setReceiverId(dto.getReceiverId());
        message.setContent(dto.getContent());
        message.setTimestamp(Instant.now());
        return message;
    }

    public MessageResponseDTO toResponseDTO(Message entity) {
        return MessageResponseDTO.builder()
                .id(entity.getId())
                .swapId(entity.getSwapId())
                .senderId(entity.getSenderId())
                .receiverId(entity.getReceiverId())
                .content(entity.getContent())
                .timestamp(entity.getTimestamp())
                .build();
    }
}
