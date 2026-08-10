package com.sewagealert.complaint.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

// UserRoleResponse: Complaint Service's local copy of the auth-service user role DTO.
// Deserialized from AUTH-SERVICE's internal endpoint over OpenFeign — never imported
// from another microservice's codebase (same convention as UserProfileResponse).
@Data
@NoArgsConstructor
public class UserRoleResponse {

    private Long id;
    private String name;
    private String email;
    private String role;
}
