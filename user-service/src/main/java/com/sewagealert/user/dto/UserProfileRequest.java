package com.sewagealert.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;


@Getter
@Setter
@Data
@Schema(description = "User profile create/update request")
public class UserProfileRequest {

    @Schema(description = "Display name of the user", example = "Priya Sharma", minLength = 2, maxLength = 100)
    @NotBlank(message = "Name is required")
    @Size(min = 2, max = 100, message = "Name must be between 2 and 100 characters")
    // name: Display name of the user, validated for min/max length
    private String name;

    @Schema(description = "Optional contact phone number", example = "9876543210")
    private Long phone;

    @Schema(description = "Optional profile image URL")
    // profilePictureUrl: Optional URL or base64 string for the uploaded profile image
    private String profilePictureUrl;

    @Schema(description = "Optional residential address", example = "Banjara Hills, Hyderabad")
    // address: Optional residential address field
    private String address;

    @Schema(description = "Optional JSON string for notification preferences, language, theme, etc.")
    // preferences: Optional JSON string for storing notification preferences, language, theme, etc.
    private String preferences;

}
