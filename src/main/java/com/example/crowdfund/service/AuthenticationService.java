package com.example.crowdfund.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthenticationService {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    public String authenticateAndGenerateToken(String username, String password) {
        try {
            // Authenticate the user with Spring Security
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(username, password)
            );

            // If authentication successful, get user details and generate token
            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            return jwtService.genToken(userDetails);

        } catch (AuthenticationException e) {
            // Authentication failed
            return null;
        }
    }
}