package com.sewagealert.user.controller;

import com.sewagealert.user.dto.ApiResponse;
import com.sewagealert.user.dto.CreateUserProfileRequest;
import com.sewagealert.user.dto.UserProfileRequest;
import com.sewagealert.user.dto.UserProfileResponse;
import com.sewagealert.user.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
// UserController: REST controller exposing user profile CRUD endpoints — all routes start with /api/v1/users
public class UserController {

    private final UserService userService;


    @PostMapping
    // POST /api/v1/users: Creates a new user profile. The authUserId is passed as a header from the API Gateway
    // after JWT validation — this ensures only authenticated users can create profiles
    public ResponseEntity<ApiResponse<UserProfileResponse>> createProfile(
            @RequestHeader("X-Auth-User-Id") Long authUserId,
            @Valid @RequestBody UserProfileRequest request) {
        UserProfileResponse response = userService.createProfile(authUserId, request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("Profile created successfully", response));
    }

    @PostMapping("/internal/profile")
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
    // GET /api/v1/users/{id}: Retrieves a user profile by its profile ID
    public ResponseEntity<ApiResponse<UserProfileResponse>> getProfile(@PathVariable Long id) {
        UserProfileResponse response = userService.getProfile(id);
        return ResponseEntity
                .ok(ApiResponse.success("Profile retrieved successfully", response));
    }

    @GetMapping("/auth/{authUserId}")
    // GET /api/v1/users/auth/{authUserId}: Retrieves a profile by the auth-service user ID
    // Used for inter-service communication (e.g., complaint service needs user name)
    public ResponseEntity<ApiResponse<UserProfileResponse>> getProfileByAuthUserId(@PathVariable Long authUserId) {
        UserProfileResponse response = userService.getProfileByAuthUserId(authUserId);
        return ResponseEntity
                .ok(ApiResponse.success("Profile retrieved successfully", response));
    }

    @PutMapping("/{id}")
    // PUT /api/v1/users/{id}: Updates a user profile by its profile ID
    public ResponseEntity<ApiResponse<UserProfileResponse>> updateProfile(
            @PathVariable Long id,
            @Valid @RequestBody UserProfileRequest request) {
        UserProfileResponse response = userService.updateProfile(id, request);
        return ResponseEntity
                .ok(ApiResponse.success("Profile updated successfully", response));
    }

    @DeleteMapping("/{id}")
    // DELETE /api/v1/users/{id}: Deletes a user profile by its profile ID
    public ResponseEntity<ApiResponse<Void>> deleteProfile(@PathVariable Long id) {
        userService.deleteProfile(id);
        return ResponseEntity
                .ok(ApiResponse.success("Profile deleted successfully", null));
    }
}
