package com.sewagealert.user.controller;

import com.sewagealert.user.dto.ApiResponse;
import com.sewagealert.user.dto.UserProfileResponse;
import com.sewagealert.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/internal/users")
@RequiredArgsConstructor
// InternalUserController: Endpoints for inter-service communication ONLY (not routed through the API Gateway).
// Called directly by other microservices via OpenFeign + Eureka.
public class InternalUserController {

    private final UserService userService;

    @GetMapping("/{authUserId}")
    // GET /api/v1/internal/users/{authUserId}: Returns the user profile for an auth user id.
    // Used by Complaint Service to confirm a user profile exists before creating a complaint.
    public ResponseEntity<ApiResponse<UserProfileResponse>> getUserProfileByAuthUserId(@PathVariable Long authUserId) {
        UserProfileResponse response = userService.getProfileByAuthUserId(authUserId);
        return ResponseEntity
                .ok(ApiResponse.success("Profile retrieved successfully", response));
    }
}
