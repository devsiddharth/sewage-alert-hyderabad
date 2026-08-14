package com.sewagealert.auth.dto;

import com.sewagealert.auth.model.Role;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;


@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Authentication response containing the JWT and the logged-in user's summary")
public class AuthResponse {

    @Schema(description = "JWT access token — send as 'Authorization: Bearer <token>'", example = "eyJhbGciOiJIUzI1NiJ9...")
    private String token;

    @Schema(description = "Token type", example = "Bearer")
    private String type = "Bearer";

    @Schema(description = "Auth-service user id", example = "1")
    private Long id;

    @Schema(description = "User's full name", example = "Priya Sharma")
    private String name;

    @Schema(description = "User's email address", example = "priya@example.com")
    private String email;

    @Schema(description = "User's role", example = "CITIZEN")
    private Role role;

}