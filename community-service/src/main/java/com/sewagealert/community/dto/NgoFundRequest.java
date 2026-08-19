package com.sewagealert.community.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Schema(description = "NGO fund record create request")
public class NgoFundRequest {

    @Schema(description = "Fund source name", example = "Municipal Corporation Grant")
    @NotBlank(message = "Source is required")
    private String source;

    @Schema(description = "Amount received")
    @NotNull(message = "Amount is required")
    @PositiveOrZero(message = "Amount cannot be negative")
    private BigDecimal amount;

    @Schema(description = "Amount allocated for spending")
    @PositiveOrZero(message = "Allocated amount cannot be negative")
    private BigDecimal allocatedAmount;

    @Schema(description = "Project name")
    private String projectName;

    @Schema(description = "Description")
    private String description;

    @Schema(description = "Date funds were received")
    @NotNull(message = "Received date is required")
    private LocalDate receivedDate;
}
