package com.sewagealert.auth.controller;

import com.sewagealert.auth.dto.ApiResponse;
import com.sewagealert.auth.dto.UserRoleResponse;
import com.sewagealert.auth.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

// AuthInternalController: Inter-service endpoints for role verification. These endpoints
// are NOT routed by the API Gateway (the gateway routes /api/v1/auth/** and /api/v1/users/**
// but NOT /api/v1/internal/**), so they are only reachable via service-to-service Feign
// calls using Eureka service discovery. No JWT is required — the caller is trusted because
// the call never passes through the gateway's public surface.
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/internal/auth")
public class AuthInternalController {

    private final AuthService authService;

    @GetMapping("/users/{userId}/role")
    // GET /api/v1/internal/auth/users/{userId}/role: Returns id/name/email/role for a user.
    // 404 if the user does not exist. Never exposes passwords or credentials.
    public ResponseEntity<ApiResponse<UserRoleResponse>> getUserRole(@PathVariable Long userId) {
        UserRoleResponse response = authService.getUserRoleInfo(userId);
        return ResponseEntity
                .ok(ApiResponse.success("User role retrieved successfully", response));
    }
}