package com.sewagealert.auth.dto;

import com.sewagealert.auth.model.User;
import lombok.Getter;
import lombok.Setter;

// FieldOfficerResponse: Minimal, safe projection of a field officer user — the only
// information the assignment UI needs. Never exposes passwords or other credentials.
@Getter
@Setter
public class FieldOfficerResponse {

    private Long id;
    private String name;
    private String email;

    public static FieldOfficerResponse fromEntity(User user) {
        FieldOfficerResponse response = new FieldOfficerResponse();
        response.setId(user.getId());
        response.setName(user.getName());
        response.setEmail(user.getEmail());
        return response;
    }
}
