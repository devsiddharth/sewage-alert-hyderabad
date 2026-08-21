package com.sewagealert.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * CreateNgoUserResponse: Returns the created NGO user details including the generated password.
 * The password is returned ONCE — it is not stored in plain text.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateNgoUserResponse {
    private Long userId;
    private String email;
    private String name;
    private String temporaryPassword;
}
