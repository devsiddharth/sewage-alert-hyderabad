package com.sewagealert.community.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;

@Data
@Schema(description = "NGO drive creation/update request")
public class NgoDriveRequest {

    @Schema(description = "Drive title")
    @NotBlank(message = "Title is required")
    private String title;

    @Schema(description = "Drive description")
    @NotBlank(message = "Description is required")
    private String description;

    @Schema(description = "Drive type", example = "Cleanliness")
    private String driveType;

    @Schema(description = "Drive location")
    @NotBlank(message = "Location is required")
    private String location;

    @Schema(description = "Start date")
    @NotNull(message = "Start date is required")
    private LocalDate startDate;

    @Schema(description = "End date")
    private LocalDate endDate;

    @Schema(description = "Target number of volunteers")
    private Integer totalTarget;
}
