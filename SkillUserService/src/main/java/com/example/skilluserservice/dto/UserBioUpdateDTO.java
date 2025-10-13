package com.example.skilluserservice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserBioUpdateDTO {

    @NotNull(message = "User Bio ID cannot be null")
    private Long id;

    @NotNull(message = "Auth User ID cannot be null")
    private Long authUserId;

    @NotBlank(message = "First name cannot be empty")
    @Size(max = 50, message = "First name cannot exceed 50 characters")
    private String firstName;

    @NotBlank(message = "Last name cannot be empty")
    @Size(max = 50, message = "Last name cannot exceed 50 characters")
    private String lastName;

    private String education;

    private Set<Long> skillIds;

    @Size(max = 20, message = "Phone number cannot exceed 20 characters")
    private String phone;

    @Size(max = 255, message = "Job title cannot exceed 255 characters")
    private String jobTitle;

    private Integer yearsOfExperience;

    @Size(max = 255, message = "LinkedIn profile URL cannot exceed 255 characters")
    private String linkedInProfileUrl;

    private String bio;
}