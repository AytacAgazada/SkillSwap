package com.example.skilluserservice.dto;

import com.example.skilluserservice.entity.enumeration.SkillLevel;
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