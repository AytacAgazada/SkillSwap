package com.example.communityservice.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ProblemDto {
    private Long id;
    private String title;
    private String description;
    private String createdByUserId;
    private Long groupId;
    private boolean solved;
    private String solvedByUserId;
    private LocalDateTime createdAt;
    private LocalDateTime solvedAt;
}
