package com.sewagealert.user.dto;

import com.sewagealert.user.model.UserProfile;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Data
@NoArgsConstructor
// UserProfileResponse: DTO returned to the client — never exposes the internal entity directly
public class UserProfileResponse {

    private Long id;
    private Long authUserId;
    private String name;
    private Long phone;
    private String profilePictureUrl;
    private String address;
    private String preferences;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // fromEntity: Static factory method that converts the JPA entity into a clean response DTO
    // This prevents entity internals (like JPA lazy-loading proxies) from leaking to the API layer
    public static UserProfileResponse fromEntity(UserProfile profile) {
        UserProfileResponse response = new UserProfileResponse();
        response.setId(profile.getId());
        response.setAuthUserId(profile.getAuthUserId());
        response.setName(profile.getName());
        response.setPhone(profile.getPhone());
        response.setProfilePictureUrl(profile.getProfilePictureUrl());
        response.setAddress(profile.getAddress());
        response.setPreferences(profile.getPreferences());
        response.setCreatedAt(profile.getCreatedAt());
        response.setUpdatedAt(profile.getUpdatedAt());
        return response;
    }

}
