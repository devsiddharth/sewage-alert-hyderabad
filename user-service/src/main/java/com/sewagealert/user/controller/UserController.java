package com.sewagealert.user.controller;

import com.sewagealert.user.dto.ApiResponse;
import com.sewagealert.user.dto.CreateUserProfileRequest;
import com.sewagealert.user.dto.UserProfileRequest;
import com.sewagealert.user.dto.UserProfileResponse;
import com.sewagealert.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
@Tag(name = "User Profiles", description = "Citizen profile CRUD endpoints")
// UserController: REST controller exposing user profile CRUD endpoints — all routes start with /api/v1/users
public class UserController {

    private final UserService userService;


    @PostMapping
    @Operation(
            summary = "Create a user profile",
            description = "Creates a profile for the authenticated user. The user id is passed as the "
                    + "X-Auth-User-Id header (set by the API Gateway after JWT validation) — it is never "
                    + "trusted from the request body."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Profile created successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Validation failed (name/phone)")
    })
    @SecurityRequirement(name = "bearerAuth")
    // POST /api/v1/users: Creates a new user profile. The authUserId is passed as a header from the API Gateway
    // after JWT validation — this ensures only authenticated users can create profiles
    public ResponseEntity<ApiResponse<UserProfileResponse>> createProfile(
            @Parameter(description = "Authenticated user's auth-service id (set by the gateway)", example = "1")
            @RequestHeader("X-Auth-User-Id") Long authUserId,
            @Valid @RequestBody UserProfileRequest request) {
        UserProfileResponse response = userService.createProfile(authUserId, request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("Profile created successfully", response));
    }

    @PostMapping("/internal/profile")
    @Operation(
            summary = "Create a user profile (internal)",
            description = "Internal endpoint used by the Auth Service right after registration (service-to-service "
                    + "via Feign, not routed through the API Gateway)."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Profile created successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Validation failed")
    })
    public ResponseEntity<ApiResponse<UserProfileResponse>> createProfileInternal(
            @RequestBody CreateUserProfileRequest request) {

        UserProfileRequest profileRequest = new UserProfileRequest();
        profileRequest.setName(request.getName());
        profileRequest.setPhone(request.getPhone());

        UserProfileResponse response =
                userService.createProfile(
                        request.getAuthUserId(),
                        profileRequest
                );

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Profile created successfully", response));
    }

    @GetMapping("/{id}")
    @Operation(
            summary = "Get a user profile by profile id",
            description = "Retrieves a user profile by its internal profile id."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Profile retrieved successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Profile not found")
    })
    // GET /api/v1/users/{id}: Retrieves a user profile by its profile ID
    public ResponseEntity<ApiResponse<UserProfileResponse>> getProfile(
            @Parameter(description = "Profile id", example = "1") @PathVariable Long id) {
        UserProfileResponse response = userService.getProfile(id);
        return ResponseEntity
                .ok(ApiResponse.success("Profile retrieved successfully", response));
    }

    @GetMapping("/auth/{authUserId}")
    @Operation(
            summary = "Get a user profile by auth-service user id",
            description = "Retrieves a profile by the auth-service user id. Also used for inter-service communication "
                    + "(e.g., the Complaint Service needs the user name)."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Profile retrieved successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Profile not found")
    })
    // GET /api/v1/users/auth/{authUserId}: Retrieves a profile by the auth-service user ID
    // Used for inter-service communication (e.g., complaint service needs user name)
    public ResponseEntity<ApiResponse<UserProfileResponse>> getProfileByAuthUserId(
            @Parameter(description = "Auth-service user id", example = "1") @PathVariable Long authUserId) {
        UserProfileResponse response = userService.getProfileByAuthUserId(authUserId);
        return ResponseEntity
                .ok(ApiResponse.success("Profile retrieved successfully", response));
    }

    @PutMapping("/{id}")
    @Operation(
            summary = "Update a user profile",
            description = "Updates a user profile by its profile id."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Profile updated successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Validation failed"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Profile not found")
    })
    // PUT /api/v1/users/{id}: Updates a user profile by its profile ID
    public ResponseEntity<ApiResponse<UserProfileResponse>> updateProfile(
            @Parameter(description = "Profile id", example = "1") @PathVariable Long id,
            @Valid @RequestBody UserProfileRequest request) {
        UserProfileResponse response = userService.updateProfile(id, request);
        return ResponseEntity
                .ok(ApiResponse.success("Profile updated successfully", response));
    }

    @DeleteMapping("/{id}")
    @Operation(
            summary = "Delete a user profile",
            description = "Deletes a user profile by its profile id."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Profile deleted successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Profile not found")
    })
    // DELETE /api/v1/users/{id}: Deletes a user profile by its profile ID
    public ResponseEntity<ApiResponse<Void>> deleteProfile(
            @Parameter(description = "Profile id", example = "1") @PathVariable Long id) {
        userService.deleteProfile(id);
        return ResponseEntity
                .ok(ApiResponse.success("Profile deleted successfully", null));
    }
}
