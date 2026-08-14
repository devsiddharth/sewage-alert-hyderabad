package com.sewagealert.complaint.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(description = "Complaint creation request — bound from multipart/form-data fields (images arrive as separate file parts)")
// ComplaintRequest: DTO for creating a new complaint — bound from multipart form fields
// (title, description, latitude, longitude) and validated at the controller level using @Valid.
// Images are NOT part of this DTO: they arrive as MultipartFile parts and are uploaded to
// object storage (Cloudinary); only the returned URLs are persisted.
public class ComplaintRequest {

    @Schema(description = "Short title of the complaint", example = "Blocked sewer near Jubilee Hills")
    @NotBlank(message = "Title is required")
    private String title;

    @Schema(description = "Detailed description of the issue", example = "Sewage water is overflowing onto the road")
    @NotBlank(message = "Description is required")
    private String description;

    @Schema(description = "GPS latitude of the incident", example = "17.4319")
    @NotNull(message = "Latitude is required")
    private Double latitude;

    @Schema(description = "GPS longitude of the incident", example = "78.4481")
    @NotNull(message = "Longitude is required")
    private Double longitude;

}
