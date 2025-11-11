package com.getwork.user.controller;

import com.getwork.user.dto.*;
import com.getwork.user.service.UserService;
import com.getwork.user.util.ApiResponse;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/users")
public class UserController {

    private final UserService service;

    public UserController(UserService service) {
        this.service = service;
    }

    @PostMapping("/signup")
    public ApiResponse<UserResponse> signup(@Valid @RequestBody UserRequest request) {
        return service.createUser(request);
    }

    @GetMapping
    public ApiResponse<Page<UserResponse>> getAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String role) {
        return service.getAll(page, size, role);
    }

    @DeleteMapping("/{id}")
    public ApiResponse<UserResponse> deactivateUser(@PathVariable UUID id) {
        return service.deactivate(id);
    }
}
