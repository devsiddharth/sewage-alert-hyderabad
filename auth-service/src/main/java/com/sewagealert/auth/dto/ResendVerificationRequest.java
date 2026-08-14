package com.sewagealert.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(description = "Resend-verification request — issues a fresh code for an unverified account (throttled)")
public class ResendVerificationRequest {

    @Schema(description = "Email address of the account to re-verify", example = "priya@example.com")
    @NotBlank(message = "Email is required")
    @Email(message = "Email must be valid")
    private String email;
}
