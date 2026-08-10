package com.sewagealert.complaint.client;

import com.sewagealert.complaint.dto.ApiResponse;
import com.sewagealert.complaint.dto.UserRoleResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

// AuthServiceClient: Declarative OpenFeign client for AUTH-SERVICE — the single source of
// truth for users and roles. Used to verify callers' roles server-side (an admin assigning,
// a field officer fetching their complaints). Returns 404 (FeignException.NotFound) when the
// user does not exist.
@FeignClient(name = "AUTH-SERVICE")
public interface AuthServiceClient {

    // getUserRole: Fetches a user's identity + role from AUTH-SERVICE's internal endpoint
    // at /api/v1/internal/auth/... — this path is NOT routed by the API Gateway, so it is
    // only reachable via service-to-service Feign calls over Eureka service discovery.
    @GetMapping("/api/v1/internal/auth/users/{userId}/role")
    ApiResponse<UserRoleResponse> getUserRole(@PathVariable("userId") Long userId);
}
