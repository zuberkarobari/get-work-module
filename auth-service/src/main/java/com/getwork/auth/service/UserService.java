package com.getwork.auth.service;


import com.getwork.auth.entity.User;
import com.getwork.auth.repo.UserRepository;
import jakarta.mail.MessagingException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final EmailService emailService;

    public UserService(UserRepository userRepository, EmailService emailService) {
        this.userRepository = userRepository;
        this.emailService = emailService;
    }

    /**
     * Fetch all users from DB
     */
    public List<User> allUsers() {
        return new ArrayList<>(userRepository.findAll());
    }

    /**
     * Fetch a single user by ID
     */
    public Optional<User> getUserById(long id) {
        return userRepository.findById(id);
    }

    /**
     * Create new user and send verification email
     */
    @Transactional
    public User createUser(User user) {
        // Generate verification code and set expiration
        String verificationCode = UUID.randomUUID().toString().substring(0, 6).toUpperCase();
        user.setVerificationCode(verificationCode);
        user.setVerificationExpiration(LocalDateTime.now().plusMinutes(15));
        user.setEnabled(false);

        User savedUser = userRepository.save(user);

        // Send verification email asynchronously
        try {
            emailService.sendVerificationEmail(
                    savedUser.getEmail(),
                    "Account Verification for Get-Work App",
                    savedUser.getUsername()
            );
        } catch (Exception e) {
            throw new RuntimeException("Failed to send verification email: " + e.getMessage());
        }

        return savedUser;
    }

    /**
     * Delete user by ID
     */
    public void deleteUser(long id) {
        userRepository.deleteById(id);
    }

    /**
     * Verify user's email using verification code
     */
    public boolean verifyUser(String email, String code) {
        Optional<User> optionalUser = userRepository.findByEmail(email);

        if (optionalUser.isPresent()) {
            User user = optionalUser.get();

            if (user.getVerificationCode().equals(code)
                    && user.getVerificationExpiration().isAfter(LocalDateTime.now())) {
                user.setEnabled(true);
                user.setIsEmailVerified(true);
                user.setVerificationCode(null);
                userRepository.save(user);
                return true;
            }
        }

        return false;
    }
}
