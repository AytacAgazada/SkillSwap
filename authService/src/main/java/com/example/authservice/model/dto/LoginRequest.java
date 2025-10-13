package com.example.authservice.model.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class LoginRequest {

    @NotBlank(message = "Identifier (username, FIN or email) cannot be blank")
    private String identifier;

    @NotBlank(message = "Password cannot be blank")
    @Size(min = 8, message = "Password must be at least 8 characters long")
    @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[!@#$%^&*()_+])[A-Za-z\\d!@#$%^&*()_+]{8,}$",
            message = "Password must contain at least one uppercase letter, one lowercase letter, one digit, and one special character (!@#$%^&*()_+) and be at least 8 characters long.")
    private String password;

    private Boolean rememberMe = false;



}