package com.example.crowdfund.controller;

import com.example.crowdfund.dto.request.RegisterRequest;
import com.example.crowdfund.entity.User;
import com.example.crowdfund.service.user.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/v1/api/users")
@RequiredArgsConstructor
public class UserController {
    
    private final UserService userService;

    @PutMapping(value = "/profile", consumes = {"multipart/form-data"})
    public ResponseEntity<?> updateProfile(@ModelAttribute RegisterRequest profileRequest, Authentication authentication) throws IOException {
        
        User currentUser = userService.findByUsername(authentication.getName());
        User updatedUser = userService.updateProfile(profileRequest, currentUser);
        
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Profile updated successfully");
        response.put("user", updatedUser);
        
        return ResponseEntity.ok(response);
    }

    @GetMapping("/profile")
    public ResponseEntity<?> getProfile(Authentication authentication) {
        User currentUser = userService.findByUsername(authentication.getName());
        
        Map<String, Object> response = new HashMap<>();
        response.put("user", currentUser);
        
        return ResponseEntity.ok(response);
    }
}
