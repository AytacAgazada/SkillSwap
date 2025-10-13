package com.example.authservice.model.dto;

import com.example.authservice.validation.OneOfFieldsNotBlank;
import com.example.authservice.validation.PasswordMatches;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
@PasswordMatches
@OneOfFieldsNotBlank(fieldNames = {"email", "phone"}, message = "Email or Phone must be provided for communication.")
public class SignupRequest {
    @NotBlank(message = "Username cannot be blank")
    @Size(min = 3, max = 50, message = "Username must be between 3 and 50 characters")
    private String username;

    @NotBlank(message = "FIN cannot be blank")
    @Size(min = 7, max = 7, message = "FIN must be 7 characters")
    @Pattern(regexp = "^[0-9A-Z]{7}$", message = "FIN must consist of 7 uppercase letters or digits.")
    private String fin;

    @NotBlank(message = "Password cannot be blank")
    @Pattern(
            regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[!@#$%^&*()_+])[A-Za-z\\d!@#$%^&*()_+]{8,}$",
            message = "Password must be at least 8 characters long and contain an uppercase letter, a lowercase letter, a digit, and a special character (!@#$%^&*()_+)."
    )
    private String password;


    @NotBlank(message = "Password confirmation cannot be blank")
    private String confirmPassword;

    @Email(message = "Email must be a valid email address")
    private String email;

    @Size(min = 10, max = 20, message = "Phone number must be between 10 and 20 characters")
    private String phone;

    @Pattern(regexp = "^(USER|ADMIN|PROVIDER)$", message = "Role must be USER, ADMIN or PROVIDER")
    private String role;

}