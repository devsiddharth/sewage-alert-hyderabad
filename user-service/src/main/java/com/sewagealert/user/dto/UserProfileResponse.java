package com.sewagealert.user.dto;

import com.sewagealert.user.model.UserProfile;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Data
@NoArgsConstructor
@Schema(description = "User profile DTO returned to clients — never exposes the internal entity directly")
// UserProfileResponse: DTO returned to the client — never exposes the internal entity directly
public class UserProfileResponse {

    @Schema(description = "Internal profile id", example = "1")
    private Long id;

    @Schema(description = "Corresponding auth-service user id", example = "1")
    private Long authUserId;

    @Schema(description = "Display name", example = "Priya Sharma")
    private String name;

    @Schema(description = "Contact phone number", example = "9876543210")
    private Long phone;

    @Schema(description = "Profile image URL")
    private String profilePictureUrl;

    @Schema(description = "Residential address", example = "Banjara Hills, Hyderabad")
    private String address;

    @Schema(description = "Notification/language/theme preferences (JSON string)")
    private String preferences;

    @Schema(description = "Profile creation timestamp")
    private LocalDateTime createdAt;

    @Schema(description = "Last update timestamp")
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
