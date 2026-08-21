package com.sewagealert.community.client;

import com.sewagealert.community.dto.ApiResponse;
import com.sewagealert.community.dto.CreateNgoUserRequest;
import com.sewagealert.community.dto.CreateNgoUserResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * AuthServiceClient: Feign client for calling auth-service internal endpoints.
 * Uses Eureka service discovery (lb://AUTH-SERVICE).
 */
@FeignClient(name = "AUTH-SERVICE")
public interface AuthServiceClient {

    @PostMapping("/api/v1/internal/auth/ngo-users")
    ApiResponse<CreateNgoUserResponse> createNgoUser(@RequestBody CreateNgoUserRequest request);
}
