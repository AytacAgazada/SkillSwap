package com.example.communityservice.dto;

import lombok.Data;

@Data
public class CreateProblemDto {
    private String title;
    private String description;
    private String createdByUserId;
    private Long groupId;
}
