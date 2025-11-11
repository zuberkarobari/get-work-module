package com.getwork.user.service;

import com.getwork.user.dto.UserRequest;
import com.getwork.user.dto.UserResponse;
import com.getwork.user.entity.User;

public class UserMapper {

    public static User toEntity(UserRequest req) {
        User user = new User();
        user.setPhone(req.getPhone());
        user.setName(req.getName());
        user.setEmail(req.getEmail());
        user.setLanguagePref(req.getLanguagePref());
        user.setRole(
                req.getRole() != null ? User.Role.valueOf(req.getRole().toUpperCase()) : User.Role.RIDER
        );

        user.setStatus(User.Status.PENDING_VERIFICATION);
        user.setIsPhoneVerified(false);
        user.setIsEmailVerified(false);
        user.setIsActive(true);

        return user;
    }

    public static UserResponse toResponse(User user) {
        return new UserResponse(
                user.getId(),
                user.getPhone(),
                user.getName(),
                user.getEmail(),
                user.getRole().name(),
                user.getLanguagePref(),
                user.getStatus().name(),
                user.getIsPhoneVerified(),
                user.getIsEmailVerified(),
                user.getCreatedAt(),
                user.getUpdatedAt()
        );
    }
}
