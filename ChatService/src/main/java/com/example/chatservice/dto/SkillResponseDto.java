package com.example.chatservice.dto;

import com.example.chatservice.model.SkillLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SkillResponseDto {
    private Long id;
    private String name;
    private String description;
    private SkillLevel level;
}
