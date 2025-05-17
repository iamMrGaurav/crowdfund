package com.example.crowdfund.controller;


import com.example.crowdfund.DTO.LoginRequest;
import com.example.crowdfund.DTO.RegisterRequest;
import com.example.crowdfund.entity.User;
import com.example.crowdfund.service.AuthenticationService;
import com.example.crowdfund.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RequestMapping("/api/auth")
@RestController
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationService authenticationService;
    private final UserService userService;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest loginRequest) {

        // Authenticate user and generate token
        String token = authenticationService.authenticateAndGenerateToken(
                loginRequest.getUsernameOrEmail(),
                loginRequest.getPassword()
        );

        if (token == null) {
            return ResponseEntity.status(401).body(Map.of("error", "Invalid credentials"));
        }

        // Return token in response
        Map<String, Object> response = new HashMap<>();
        response.put("token", token);
        response.put("username", loginRequest.getUsernameOrEmail());

        return ResponseEntity.ok(response);
    }

    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@RequestBody RegisterRequest registerRequest){

        Map<String, Object> response = new HashMap<>();
        User user = userService.createUser(registerRequest);
        response.put("User", user);
        response.put("isCreated", true);

        return ResponseEntity.ok(response);
    }

}
