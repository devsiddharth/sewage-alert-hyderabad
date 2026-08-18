package com.sewagealert.auth.dto;

import com.sewagealert.auth.model.Role;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
@Getter
@Setter
@Schema(description = "Citizen registration request — creates an unverified account that must be email-verified before login")
public class RegisterRequest {

    @Schema(description = "Full name of the citizen", example = "Priya Sharma", minLength = 2, maxLength = 100)
    @NotBlank(message = "Name is required")
    @Size(min = 2, max = 100, message = "Name must be between 2 and 100 characters")
    private String name;

    @Schema(description = "Registered user's email address (verification code is sent here)", example = "priya@example.com")
    @NotBlank(message = "Email is required")
    @Email(message = "Email must be valid")
    private String email;

    @Schema(description = "Account password (min 6 characters) — never echoed back", minLength = 6, maxLength = 100)
    @NotBlank(message = "Password is required")
    @Size(min = 6, max = 100, message = "Password must be between 6 and 100 characters")
    private String password;

    @Schema(description = "Optional contact phone number", example = "9876543210")
    private Long phone;

    @Schema(description = "Account role (defaults to CITIZEN when omitted)", example = "CITIZEN")
    private Role role;

}
