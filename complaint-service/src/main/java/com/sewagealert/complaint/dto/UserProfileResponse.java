package com.sewagealert.complaint.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

// UserProfileResponse: Complaint Service's own copy of the user profile DTO.
// Owned locally (never imported from another microservice) — deserialized from the JSON
// returned by USER-SERVICE's internal endpoint over OpenFeign.
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
