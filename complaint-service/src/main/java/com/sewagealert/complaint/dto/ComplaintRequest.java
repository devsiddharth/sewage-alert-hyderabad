package com.sewagealert.complaint.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
// ComplaintRequest: DTO for creating a new complaint — validated at the controller level using @Valid
public class ComplaintRequest {

    @NotBlank(message = "Title is required")
    private String title;

    @NotBlank(message = "Description is required")
    private String description;

    @NotNull(message = "Latitude is required")
    private Double latitude;

    @NotNull(message = "Longitude is required")
    private Double longitude;

    // imageUrls: Optional list of image URLs or base64-encoded images attached to the complaint
    private List<String> imageUrls;

}
