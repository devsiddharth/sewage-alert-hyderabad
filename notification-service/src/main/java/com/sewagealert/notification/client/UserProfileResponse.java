package com.sewagealert.notification.client;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

// UserProfileResponse: Mirror of the USER-SERVICE profile DTO — identical JSON contract.
// Kept in the client package because it only exists to support inter-service calls.
@Data
@NoArgsConstructor
public class UserProfileResponse {

    private Long id;
    private Long authUserId;
    private String name;
    private Long phone;
    private String profilePictureUrl;
    private String address;
    private String preferences;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
