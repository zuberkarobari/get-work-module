package com.getwork.user.service;

import com.getwork.user.dto.*;
import com.getwork.user.entity.User;
import com.getwork.user.repository.UserRepository;
import com.getwork.user.util.ApiResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import java.time.Instant;
import java.util.UUID;

@Service
public class UserService {

    @Autowired
    private OtpGatewayService otpGatewayService;

    private final UserRepository repo;

    public UserService(UserRepository repo) {
        this.repo = repo;
    }

    public ApiResponse<UserResponse> createUser(UserRequest req) {
        if (repo.findByPhone(req.getPhone()).isPresent())
            throw new IllegalArgumentException("Phone number already registered");

        User user = UserMapper.toEntity(req);
        user.setStatus(User.Status.PENDING_VERIFICATION);
        user.setIsPhoneVerified(false);
        user = repo.save(user);

        otpGatewayService.sendOtp(user.getPhone(), "SMS");

        return ApiResponse.success(
                String.format("User created. OTP sent to %s", user.getPhone()),
                UserMapper.toResponse(user)
        );
    }

    public ApiResponse<Page<UserResponse>> getAll(int page, int size, String role) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<User> users = (role == null)
                ? repo.findAll(pageable)
                : repo.findByRole(User.Role.valueOf(role.toUpperCase()), pageable);

        return ApiResponse.success("Fetched users successfully", users.map(UserMapper::toResponse));
    }

    public ApiResponse<UserResponse> deactivate(UUID userId) {
        User user = repo.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        user.setIsActive(false);
        user.setDeletedAt(Instant.now());
        repo.save(user);
        return ApiResponse.success("User deactivated", UserMapper.toResponse(user));
    }

    // ✅ Added this method for OTP verification flow
    public User findOrCreateByPhone(String phone) {
        return repo.findByPhone(phone).orElseGet(() -> {
            User newUser = new User();
            newUser.setPhone(phone);
            newUser.setStatus(User.Status.PENDING_VERIFICATION);
            newUser.setIsActive(true);
            newUser.setIsPhoneVerified(false);
            newUser.setLanguagePref("en");
            newUser.setRole(User.Role.RIDER);
            return repo.save(newUser);
        });
    }

    // ✅ Added for saving updates (like phone verification)
    public User save(User user) {
        user.setUpdatedAt(Instant.now());
        return repo.save(user);
    }
}

