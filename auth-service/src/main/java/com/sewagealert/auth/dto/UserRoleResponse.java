package com.sewagealert.auth.dto;

import com.sewagealert.auth.model.User;
import lombok.Getter;
import lombok.Setter;

// UserRoleResponse: Internal inter-service contract used by other microservices
// (e.g. Complaint Service) to verify a user's role server-side. Roles are owned by
// this service — consumers must never be allowed to trust client-supplied roles.
@Getter
@Setter
public class UserRoleResponse {

    private Long id;
    private String name;
    private String email;
    private String role;

    public static UserRoleResponse fromEntity(User user) {
        UserRoleResponse response = new UserRoleResponse();
        response.setId(user.getId());
        response.setName(user.getName());
        response.setEmail(user.getEmail());
        response.setRole(user.getRole() != null ? user.getRole().name() : null);
        return response;
    }
}
