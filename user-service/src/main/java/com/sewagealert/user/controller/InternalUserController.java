package com.sewagealert.user.controller;

import com.sewagealert.user.dto.ApiResponse;
import com.sewagealert.user.dto.UserProfileResponse;
import com.sewagealert.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/internal/users")
@RequiredArgsConstructor
@Tag(name = "Internal (service-to-service)", description = "Endpoints for inter-service communication only — not routed through the API Gateway")
// InternalUserController: Endpoints for inter-service communication ONLY (not routed through the API Gateway).
// Called directly by other microservices via OpenFeign + Eureka.
public class InternalUserController {

    private final UserService userService;

    @GetMapping("/{authUserId}")
    @Operation(
            summary = "Get a user profile by auth user id (internal)",
            description = "Returns the user profile for an auth user id. Used by the Complaint Service to "
                    + "confirm a user profile exists before creating a complaint."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Profile retrieved successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Profile not found")
    })
    // GET /api/v1/internal/users/{authUserId}: Returns the user profile for an auth user id.
    // Used by Complaint Service to confirm a user profile exists before creating a complaint.
    public ResponseEntity<ApiResponse<UserProfileResponse>> getUserProfileByAuthUserId(
            @Parameter(description = "Auth-service user id", example = "1") @PathVariable Long authUserId) {
        UserProfileResponse response = userService.getProfileByAuthUserId(authUserId);
        return ResponseEntity
                .ok(ApiResponse.success("Profile retrieved successfully", response));
    }
}
