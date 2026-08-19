package com.sewagealert.community.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;

@Data
@Schema(description = "NGO event creation/update request")
public class NgoEventRequest {

    @Schema(description = "Event title")
    @NotBlank(message = "Title is required")
    private String title;

    @Schema(description = "Event description")
    @NotBlank(message = "Description is required")
    private String description;

    @Schema(description = "Event location")
    @NotBlank(message = "Location is required")
    private String location;

    @Schema(description = "Event date (yyyy-MM-dd)")
    @NotNull(message = "Event date is required")
    private LocalDate eventDate;

    @Schema(description = "End date (if multi-day)")
    private LocalDate endDate;

    @Schema(description = "Event time", example = "10:00 AM - 2:00 PM")
    private String eventTime;

    @Schema(description = "Maximum capacity")
    private Integer capacity;

    @Schema(description = "Event category", example = "Awareness")
    private String category;
}
