package com.sewagealert.community.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateNgoUserRequest {
    private String name;
    private String email;
    private String phone;
    private String password;  // BCrypt-hashed password from application
}
