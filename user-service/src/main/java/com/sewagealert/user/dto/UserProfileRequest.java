package com.sewagealert.user.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;


@Getter
@Setter
@Data
public class UserProfileRequest {

    @NotBlank(message = "Name is required")
    @Size(min = 2, max = 100, message = "Name must be between 2 and 100 characters")
    // name: Display name of the user, validated for min/max length
    private String name;

    private Long phone;

    // profilePictureUrl: Optional URL or base64 string for the uploaded profile image
    private String profilePictureUrl;

    // address: Optional residential address field
    private String address;

    // preferences: Optional JSON string for storing notification preferences, language, theme, etc.
    private String preferences;

}
