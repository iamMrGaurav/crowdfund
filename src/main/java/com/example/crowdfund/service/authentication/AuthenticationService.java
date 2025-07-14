package com.example.crowdfund.service.authentication;

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

    /**
     * Authenticate a user and generate a JWT token
     * @param username The username
     * @param password The password
     * @return JWT token if authentication successful, null otherwise
     */
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
            System.out.println("Exception" + e);
            throw e;
        }
    }
}