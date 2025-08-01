package com.example.crowdfund.dto.request;


import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.web.multipart.MultipartFile;

import lombok.Data;

@Data
public class RegisterRequest {
    @NotBlank(message = "Username is required")
    @Size(min = 3, max = 50, message = "Username must be between 3 and 50 characters")
    private String username;

    @NotBlank(message = "Email is required")
    @Email(message = "Email should be valid")
    private String email;

    @NotBlank(message = "Password is required")
    @Size(min = 6, message = "Password must be at least 6 characters")
    private String password;

    @NotBlank(message = "firstName is required")
    @Size(max = 10, message = "firstName must be below 10 characters")
    private String firstName;

    @NotBlank(message = "lastName is required")
    @Size(max = 10, message = "lastName must be below 10 characters")
    private String lastName;

    private String bio;
    private MultipartFile avatar;

}

