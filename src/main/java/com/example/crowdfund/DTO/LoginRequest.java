package com.example.crowdfund.DTO;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class LoginRequest {

    @NotBlank(message = "Username or Email cannot be empty")
    private String usernameOrEmail;

    @NotBlank(message = "Password is required")
    private String password;
}
