package com.sewagealert.ai.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * UserRoleResponse: Local copy of the auth-service user role DTO.
 * Deserialized from AUTH-SERVICE's internal endpoint over OpenFeign.
 */
@Data
@NoArgsConstructor
public class UserRoleResponse {

    private Long id;
    private String name;
    private String email;
    private String role;
}
