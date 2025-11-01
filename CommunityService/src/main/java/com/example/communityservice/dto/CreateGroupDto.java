package com.example.communityservice.dto;

import lombok.Data;

@Data
public class CreateGroupDto {
    private String name;
    private String description;
    private String category;
    private String createdByUserId;
}
