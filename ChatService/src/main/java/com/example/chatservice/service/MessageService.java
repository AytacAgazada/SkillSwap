package com.example.chatservice.service;

import com.example.chatservice.client.SkillUserClient;
import com.example.chatservice.dto.MessageRequestDTO;
import com.example.chatservice.dto.MessageResponseDTO;
import com.example.chatservice.dto.UserBioResponseDTO;
import com.example.chatservice.mapper.MessageMapper;
import com.example.chatservice.model.Message;
import com.example.chatservice.repository.MessageRepository;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class MessageService {

    private final MessageRepository messageRepository;
    private final MessageMapper messageMapper;
    private final SkillUserClient skillUserClient;

    /**
     * Saves a message after validating sender and receiver exist in User Service.
     * Also enriches the message with sender and receiver names.
     */
    @Transactional
    public MessageResponseDTO saveMessage(MessageRequestDTO dto) {
        // Validate sender and receiver exist
        UserBioResponseDTO sender = fetchUser(dto.getSenderId());
        UserBioResponseDTO receiver = fetchUser(dto.getReceiverId());

        // Map DTO to entity and save
        Message message = messageMapper.toEntity(dto);
        Message saved = messageRepository.save(message);
        log.info("Saved message id={} from {} to {}", saved.getId(), dto.getSenderId(), dto.getReceiverId());

        // Map entity to response DTO with sender/receiver names
        MessageResponseDTO responseDTO = messageMapper.toResponseDTO(saved);
        responseDTO.setSenderName(sender.getFirstName());
        responseDTO.setReceiverName(receiver.getFirstName());

        return responseDTO;
    }

    public List<MessageResponseDTO> getChatHistory(String swapId) {
        List<Message> messages = messageRepository.findBySwapIdOrderByTimestampAsc(swapId);

        // Extract unique authUserIds
        Set<UUID> userIds = messages.stream()
                .flatMap(msg -> Arrays.stream(new UUID[]{msg.getSenderId(), msg.getReceiverId()}))
                .collect(Collectors.toSet());

        // Fetch all users from User Service once
        Map<UUID, UserBioResponseDTO> userMap = new HashMap<>();
        userIds.forEach(id -> {
            try {
                UserBioResponseDTO user = fetchUser(id);
                userMap.put(id, user);
            } catch (FeignException.NotFound e) {
                log.warn("User with authUserId={} not found", id);
            }
        });

        // Map messages to response DTOs with names
        return messages.stream()
                .map(msg -> {
                    MessageResponseDTO dto = messageMapper.toResponseDTO(msg);
                    UserBioResponseDTO sender = userMap.get(msg.getSenderId());
                    UserBioResponseDTO receiver = userMap.get(msg.getReceiverId());

                    dto.setSenderName(sender != null ? sender.getFirstName() : "Unknown");
                    dto.setReceiverName(receiver != null ? receiver.getFirstName() : "Unknown");
                    return dto;
                })
                .collect(Collectors.toList());
    }


    private UserBioResponseDTO fetchUser(UUID authUserId) {
        try {
            return skillUserClient.getUserBioByAuthUserId(authUserId).getBody();
        } catch (FeignException.NotFound e) {
            throw new IllegalArgumentException("User with authUserId=" + authUserId + " does not exist.");
        }
    }
}
