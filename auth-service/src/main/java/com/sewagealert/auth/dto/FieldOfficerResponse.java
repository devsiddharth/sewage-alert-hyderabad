package com.sewagealert.auth.dto;

import com.sewagealert.auth.model.User;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

// FieldOfficerResponse: Minimal, safe projection of a field officer user — the only
// information the assignment UI needs. Never exposes passwords or other credentials.
@Getter
@Setter
@Schema(description = "Minimal, safe projection of a field officer user — never exposes passwords or credentials")
public class FieldOfficerResponse {

    @Schema(description = "Auth-service user id", example = "5")
    private Long id;

    @Schema(description = "Field officer's name", example = "Ravi Kumar")
    private String name;

    @Schema(description = "Field officer's email", example = "ravi.officer@sewagealert.com")
    private String email;

    public static FieldOfficerResponse fromEntity(User user) {
        FieldOfficerResponse response = new FieldOfficerResponse();
        response.setId(user.getId());
        response.setName(user.getName());
        response.setEmail(user.getEmail());
        return response;
    }
}
