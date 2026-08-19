package com.sewagealert.community.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.time.LocalDate;

@Data
@Schema(description = "NGO achievement create/update request")
public class NgoAchievementRequest {

    @Schema(description = "Achievement title")
    @NotBlank(message = "Title is required")
    private String title;

    @Schema(description = "Description")
    private String description;

    @Schema(description = "Achievement date")
    private LocalDate date;

    @Schema(description = "Evidence / proof description")
    private String evidence;
}
