package com.sewagealert.notification.client;

import com.sewagealert.notification.dto.ApiResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

// UserServiceClient: Declarative OpenFeign client for USER-SERVICE.
// Uses Eureka service discovery (lb://USER-SERVICE) — no hardcoded URLs.
//
// ⚠️ Currently dormant by design: this service only persists in-app notifications today.
// Future delivery channels (email/SMS/push workers) will use this client to fetch the
// recipient's contact details (email/phone) from USER-SERVICE before fanning out.
@FeignClient(name = "USER-SERVICE")
public interface UserServiceClient {

    // getUserProfile: Fetches the user profile for an auth user id from USER-SERVICE's
    // public endpoint. Returns 404 (FeignException.NotFound) if the profile does not exist.
    @GetMapping("/api/v1/users/auth/{authUserId}")
    ApiResponse<UserProfileResponse> getUserProfile(@PathVariable("authUserId") Long authUserId);
}
