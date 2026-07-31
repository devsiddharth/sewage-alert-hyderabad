package com.sewagealert.complaint.client;

import com.sewagealert.complaint.dto.ApiResponse;
import com.sewagealert.complaint.dto.UserProfileResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

// UserServiceClient: Declarative OpenFeign client for USER-SERVICE.
// Uses Eureka service discovery (lb://USER-SERVICE) — no hardcoded URLs.
@FeignClient(name = "USER-SERVICE")
public interface UserServiceClient {

    // getUserProfile: Fetches the user profile for an auth user id from USER-SERVICE's internal endpoint.
    // Returns 404 (FeignException.NotFound) if the profile does not exist.
    @GetMapping("/api/v1/internal/users/{authUserId}")
    ApiResponse<UserProfileResponse> getUserProfile(@PathVariable("authUserId") Long authUserId);
}
