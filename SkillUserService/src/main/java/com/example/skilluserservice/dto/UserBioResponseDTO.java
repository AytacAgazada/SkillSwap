package com.example.skilluserservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserBioResponseDTO {
    private Long id;
    private Long authUserId;
    private String firstName;
    private String lastName;
    private String education;
    private Set<SkillResponseDto> skills;
    private String phone;
    private String jobTitle;
    private Integer yearsOfExperience;
    private String linkedInProfileUrl;
    private String bio;
}