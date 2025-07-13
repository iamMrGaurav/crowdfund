package com.example.crowdfund.controller;

import com.example.crowdfund.DTO.LoginRequest;
import com.example.crowdfund.DTO.RegisterRequest;
import com.example.crowdfund.entity.User;
import com.example.crowdfund.service.AuthenticationService;
import com.example.crowdfund.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@RequestMapping("v1/api/auth")
@RestController
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationService authenticationService;
    private final UserService userService;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest loginRequest) {

        String token = authenticationService.authenticateAndGenerateToken(
                loginRequest.getUsernameOrEmail(),
                loginRequest.getPassword()
        );

        if (token == null) {
            return ResponseEntity.status(401).body(Map.of("error", "Invalid credentials"));
        }

        Map<String, Object> response = new HashMap<>();
        response.put("token", token);
        response.put("username", loginRequest.getUsernameOrEmail());

        return ResponseEntity.ok(response);
    }

    @PostMapping(value = "/register", consumes = {"application/json", "multipart/form-data"})
    public ResponseEntity<?> registerUser(@ModelAttribute RegisterRequest registerRequest) throws IOException {

        Map<String, Object> response = new HashMap<>();
        User user = userService.createUser(registerRequest);

        response.put("User", user);

        return ResponseEntity.ok(response);
    }

}
