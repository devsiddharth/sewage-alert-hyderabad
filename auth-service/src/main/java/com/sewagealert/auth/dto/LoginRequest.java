package com.sewagealert.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(description = "User login request — returns a Bearer JWT on success")
public class LoginRequest {

    @Schema(description = "Registered user's email address", example = "priya@example.com")
    @NotBlank(message = "Email is required")
    @Email(message = "Email must be valid")
    private String email;

    @Schema(description = "Account password — never echoed back")
    @NotBlank(message = "Password is required")
    private String password;

}
