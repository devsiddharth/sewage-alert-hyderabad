package com.sewagealert.auth.controller;

import com.sewagealert.auth.dto.ApiResponse;
import com.sewagealert.auth.dto.AuthResponse;
import com.sewagealert.auth.dto.FieldOfficerResponse;
import com.sewagealert.auth.dto.LoginRequest;
import com.sewagealert.auth.dto.RegisterRequest;
import com.sewagealert.auth.dto.ResendVerificationRequest;
import com.sewagealert.auth.exception.ForbiddenException;
import com.sewagealert.auth.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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

    @GetMapping("/verify-email")
    // GET /api/v1/auth/verify-email?token=...: Validates the one-time verification token,
    // marks the account verified and the token used. Permitted without authentication.
    public ResponseEntity<ApiResponse<Void>> verifyEmail(@RequestParam("token") String token) {
        authService.verifyEmail(token);
        return ResponseEntity
                .ok(ApiResponse.success("Email verified successfully.", null));
    }

    @PostMapping("/resend-verification")
    // POST /api/v1/auth/resend-verification: Re-issues a verification token for an unverified
    // account. The response is intentionally generic to prevent account enumeration, and the
    // endpoint is throttled server-side (one email per account per minute).
    public ResponseEntity<ApiResponse<Void>> resendVerification(
            @Valid @RequestBody ResendVerificationRequest request) {
        authService.resendVerification(request.getEmail());
        return ResponseEntity.ok(ApiResponse.success(
                "If the account exists and requires verification, a verification email has been sent.",
                null));
    }

    @GetMapping("/profile")
    public ResponseEntity<ApiResponse<AuthResponse>> profile(Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        AuthResponse authResponse = authService.getProfile(userId);
        return ResponseEntity
                .ok(ApiResponse.success("Profile retrieved successfully", authResponse));
    }

    @GetMapping("/admin/field-officers")
    // GET /api/v1/auth/admin/field-officers: Lists assignable field officers (id/name/email only).
    // Authorization is enforced server-side — the JWT must carry the ROLE_ADMIN authority.
    public ResponseEntity<ApiResponse<List<FieldOfficerResponse>>> getFieldOfficers(Authentication authentication) {
        requireAdmin(authentication);
        List<FieldOfficerResponse> officers = authService.getFieldOfficers();
        return ResponseEntity
                .ok(ApiResponse.success("Field officers retrieved successfully", officers));
    }

    // requireAdmin: Rejects the call unless the authenticated principal has the ADMIN role.
    private void requireAdmin(Authentication authentication) {
        if (authentication == null
                || authentication.getAuthorities().stream()
                        .noneMatch(a -> "ROLE_ADMIN".equals(a.getAuthority()))) {
            throw new ForbiddenException("Only administrators can perform this action");
        }
    }
}