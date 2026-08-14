package com.sewagealert.auth.controller;

import com.sewagealert.auth.dto.ApiResponse;
import com.sewagealert.auth.dto.AuthResponse;
import com.sewagealert.auth.dto.FieldOfficerResponse;
import com.sewagealert.auth.dto.LoginRequest;
import com.sewagealert.auth.dto.RegisterRequest;
import com.sewagealert.auth.dto.ResendVerificationRequest;
import com.sewagealert.auth.dto.VerifyCodeRequest;
import com.sewagealert.auth.exception.ForbiddenException;
import com.sewagealert.auth.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "Auth", description = "Registration, login, email verification and profile APIs (JWT issuance)")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    @Operation(
            summary = "Register a new citizen",
            description = "Creates an unverified account, generates a 6-digit verification code and "
                    + "publishes USER_REGISTERED so the Notification Service emails the code. The account "
                    + "must be verified via POST /api/v1/auth/verify-code before it can log in."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Account created — verification email queued"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Validation failed (name/email/password)"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "Email already registered")
    })
    public ResponseEntity<ApiResponse<AuthResponse>> register(@Valid @RequestBody RegisterRequest request) {
        AuthResponse authResponse = authService.register(request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("User registered successfully", authResponse));
    }

    @PostMapping("/login")
    @Operation(
            summary = "Log in and receive a JWT",
            description = "Validates credentials and returns a Bearer JWT (plus user id/name/email/role). "
                    + "Unverified accounts are rejected with code EMAIL_NOT_VERIFIED."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Login successful — JWT returned"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Validation failed (email/password)"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Invalid credentials or email not verified (EMAIL_NOT_VERIFIED)")
    })
    public ResponseEntity<ApiResponse<AuthResponse>> login(@Valid @RequestBody LoginRequest request) {
        AuthResponse authResponse = authService.login(request);
        return ResponseEntity
                .ok(ApiResponse.success("Login successful", authResponse));
    }

    @PostMapping("/verify-code")
    @Operation(
            summary = "Verify the email with the 6-digit code",
            description = "Validates the OTP the customer types into the registration flow — the only "
                    + "verification mechanism (OTP-only, no emailed link). Marks the account verified and "
                    + "publishes EMAIL_VERIFIED. Throttled server-side (max 5 wrong attempts, then a 60s lockout)."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Email verified successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid/expired/used code (INVALID_VERIFICATION_TOKEN) or validation failure")
    })
    // POST /api/v1/auth/verify-code: Validates the 6-digit code the customer types into the
    // registration flow — the only verification mechanism (OTP-only, no emailed link).
    // Public. Throttled server-side (max 5 wrong attempts, then a short lockout).
    public ResponseEntity<ApiResponse<Void>> verifyCode(@Valid @RequestBody VerifyCodeRequest request) {
        authService.verifyEmailWithCode(request.getEmail(), request.getCode());
        return ResponseEntity
                .ok(ApiResponse.success("Email verified successfully.", null));
    }

    @PostMapping("/resend-verification")
    @Operation(
            summary = "Resend the verification code",
            description = "Re-issues a fresh verification code for an unverified account (throttled to one "
                    + "email per account per minute). The response is intentionally generic to prevent account enumeration."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Generic confirmation — email sent when the account exists and needs verification"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Validation failed (email format)")
    })
    // POST /api/v1/auth/resend-verification: Re-issues a verification code for an unverified
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
    @Operation(
            summary = "Get the authenticated user's profile",
            description = "Returns the profile of the currently authenticated user (derived from the JWT)."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Profile retrieved successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Missing or invalid JWT")
    })
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<ApiResponse<AuthResponse>> profile(Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        AuthResponse authResponse = authService.getProfile(userId);
        return ResponseEntity
                .ok(ApiResponse.success("Profile retrieved successfully", authResponse));
    }

    @GetMapping("/admin/field-officers")
    @Operation(
            summary = "List field officers (admin)",
            description = "Lists assignable field officers (id/name/email only) for the complaint assignment UI. "
                    + "Authorization is enforced server-side — the JWT must carry the ROLE_ADMIN authority."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Field officers retrieved successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Missing or invalid JWT"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Authenticated user is not an ADMIN")
    })
    @SecurityRequirement(name = "bearerAuth")
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