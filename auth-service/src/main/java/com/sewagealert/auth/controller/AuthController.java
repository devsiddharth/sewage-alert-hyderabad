package com.sewagealert.auth.controller;

import com.sewagealert.auth.dto.ApiResponse;
import com.sewagealert.auth.dto.AuthResponse;
import com.sewagealert.auth.dto.LoginRequest;
import com.sewagealert.auth.dto.RegisterRequest;
import com.sewagealert.auth.service.AuthService;
import com.sewagealert.auth.service.impl.AuthServiceImpl;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<AuthResponse>> register(@Valid @RequestBody RegisterRequest request) {
        AuthResponse authResponse = authService.register(request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("User registered successfully", authResponse));
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> login(@Valid @RequestBody LoginRequest request) {
        AuthResponse authResponse = authService.login(request);
        return ResponseEntity
                .ok(ApiResponse.success("Login successful", authResponse));
    }

    @GetMapping("/profile")
    public ResponseEntity<ApiResponse<AuthResponse>> profile(Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        AuthResponse authResponse = authService.getProfile(userId);
        return ResponseEntity
                .ok(ApiResponse.success("Profile retrieved successfully", authResponse));
    }
}
