package com.example.communityservice.dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class GroupDto {
    private Long id;
    private String name;
    private String description;
    private String category;
    private String createdByUserId;
    private List<String> members;
    private LocalDateTime createdAt;
}
