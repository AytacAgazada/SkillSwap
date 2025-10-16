package com.example.chatservice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.UUID;

@Data
public class MessageRequestDTO {

    @NotBlank(message = "Swap ID cannot be empty")
    private String swapId;

    @NotNull(message = "Sender ID cannot be null")
    private UUID senderId;

    @NotNull(message = "Receiver ID cannot be null")
    private UUID receiverId;

    @NotBlank(message = "Content cannot be empty")
    private String content;
}
