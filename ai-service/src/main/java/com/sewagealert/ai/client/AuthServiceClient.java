package com.sewagealert.ai.client;

import com.sewagealert.ai.dto.ApiResponse;
import com.sewagealert.ai.dto.UserRoleResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * AuthServiceClient: Declarative OpenFeign client for AUTH-SERVICE.
 * Uses Eureka service discovery — no hardcoded URLs.
 * Calls the internal /api/v1/internal/auth/... endpoint which is NOT routed by the API Gateway.
 */
@FeignClient(name = "AUTH-SERVICE")
public interface AuthServiceClient {

    @GetMapping("/api/v1/internal/auth/users/{userId}/role")
    ApiResponse<UserRoleResponse> getUserRole(@PathVariable("userId") Long userId);
}
