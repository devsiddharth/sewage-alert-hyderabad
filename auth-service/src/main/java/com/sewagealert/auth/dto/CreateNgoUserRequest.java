package com.sewagealert.auth.dto;

import lombok.Data;

/**
 * CreateNgoUserRequest: DTO for creating an NGO user account when an NGO application is approved.
 */
@Data
public class CreateNgoUserRequest {
    private String name;
    private String email;
    private String phone;
    private String password;  // Pre-set password from application (BCrypt-hashed)
}
