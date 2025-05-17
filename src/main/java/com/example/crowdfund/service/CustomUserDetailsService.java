package com.example.crowdfund.service;

import com.example.crowdfund.entity.User;
import com.example.crowdfund.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String usernameOrEmail) throws UsernameNotFoundException {

        return userRepository.findByUsername(usernameOrEmail)
                // If not found, try by email
                .orElseGet(() -> userRepository.findByEmail(usernameOrEmail)
                        // If still not found, throw exception
                        .orElseThrow(() -> new UsernameNotFoundException("User not found with username or email: " + usernameOrEmail)));
    }
}