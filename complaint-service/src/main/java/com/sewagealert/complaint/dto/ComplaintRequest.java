package com.sewagealert.complaint.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
// ComplaintRequest: DTO for creating a new complaint — bound from multipart form fields
// (title, description, latitude, longitude) and validated at the controller level using @Valid.
// Images are NOT part of this DTO: they arrive as MultipartFile parts and are uploaded to
// object storage (Cloudinary); only the returned URLs are persisted.
public class ComplaintRequest {

    @NotBlank(message = "Title is required")
    private String title;

    @NotBlank(message = "Description is required")
    private String description;

    @NotNull(message = "Latitude is required")
    private Double latitude;

    @NotNull(message = "Longitude is required")
    private Double longitude;

}
