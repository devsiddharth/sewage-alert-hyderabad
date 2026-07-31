package com.sewagealert.auth.client;


import com.sewagealert.auth.dto.CreateUserProfileRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "USER-SERVICE")
public interface UserServiceClient {

    @PostMapping("/api/v1/users/internal/profile")
    void createProfile(@RequestBody CreateUserProfileRequest request);
}