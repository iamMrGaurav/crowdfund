package com.example.crowdfund.service.user;


import com.example.crowdfund.dto.request.RegisterRequest;
import com.example.crowdfund.GloablExceptionHandler.ResourceAlreadyExistsException;
import com.example.crowdfund.entity.User;
import com.example.crowdfund.repository.UserRepository;
import com.example.crowdfund.service.document.ImageService;
import com.example.crowdfund.service.payment.PaymentStrategy;
import com.example.crowdfund.service.payment.StripeStrategy;
import com.stripe.exception.StripeException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Optional;


@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final StripeStrategy stripeStrategy;


    @Autowired
    private ImageService imageService;

    @Transactional
    public User createUser(RegisterRequest request) throws IOException, StripeException {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new ResourceAlreadyExistsException("Username is already taken");
        }

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new ResourceAlreadyExistsException("Email is already registered");
        }

        String avatarUrl = null;
        if (request.getAvatar() != null && !request.getAvatar().isEmpty()) {
            avatarUrl = imageService.uploadAvatar(request.getAvatar());
        }

        User user = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .bio(request.getBio())
                .avatarUrl(avatarUrl)
                .build();

        String accountId = stripeStrategy.createStripeAccount(user.getEmail());
        user.setStripeAccountId(accountId);

        return userRepository.save(user);
    }

    public User findByUsername(String username) {
        Optional<User> user = userRepository.findByUsername(username);
        return user.orElseThrow(() -> 
            new ResourceAlreadyExistsException("User not found with username: " + username)
        );
    }

    public User findByStripeAccountId(String stripeAccountId) {
        Optional<User> user = userRepository.findByStripeAccountId(stripeAccountId);
        return user.orElse(null);
    }

    public User findById(Long id) {
        Optional<User> user = userRepository.findById(id);
        return user.orElse(null);
    }

    public User save(User user) {
        return userRepository.save(user);
    }

    @Transactional
    public User updateProfile(RegisterRequest request, User currentUser) throws IOException {
        if (request.getFirstName() != null && !request.getFirstName().trim().isEmpty()) {
            currentUser.setFirstName(request.getFirstName());
        }
        if (request.getLastName() != null && !request.getLastName().trim().isEmpty()) {
            currentUser.setLastName(request.getLastName());
        }
        if (request.getBio() != null) {
            currentUser.setBio(request.getBio());
        }

        if (request.getAvatar() != null && !request.getAvatar().isEmpty()) {
            String avatarUrl = imageService.uploadAvatar(request.getAvatar());
            if (avatarUrl != null) {
                currentUser.setAvatarUrl(avatarUrl);
            }
        }

        return userRepository.save(currentUser);
    }

    public User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();

        return userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }
}
