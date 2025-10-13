package com.example.skilluserservice.dto;

import com.example.skilluserservice.entity.enumeration.SkillLevel;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SkillCreateDto {

    @NotBlank(message = "Skill name cannot be empty")
    @Size(max = 100, message = "Skill name cannot exceed 100 characters")
    private String name;

    private String description;

    @NotNull(message = "Skill level cannot be null")
    private SkillLevel level;
}