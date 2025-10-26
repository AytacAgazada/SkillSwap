package com.example.authservice.model.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ResetPasswordRequest {
    @NotBlank(message = "Identifier (username, FIN or email) cannot be blank")
    private String identifier;
    @NotBlank(message = "OTP code cannot be blank")
    private String otpCode;

    @NotBlank(message = "New password cannot be blank")
    @Size(min = 6, max = 40, message = "New password must be between 6 and 40 characters")
    @Pattern(
            regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[!@#$%^&*()_+])[A-Za-z\\d!@#$%^&*()_+]{8,}$",
            message = "Password must be at least 8 characters long and contain an uppercase letter, a lowercase letter, a digit, and a special character (!@#$%^&*()_+)."
    )
    private String newPassword;
    @NotBlank(message = "New password confirmation cannot be blank")
    private String newPasswordConfirmation;
}