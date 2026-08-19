package com.sewagealert.community.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Schema(description = "NGO expense record create request")
public class NgoExpenseRequest {

    @Schema(description = "Fund record ID to allocate against")
    @NotNull(message = "Fund record ID is required")
    private Long fundRecordId;

    @Schema(description = "Expense category", example = "Equipment")
    @NotBlank(message = "Category is required")
    private String category;

    @Schema(description = "Amount spent")
    @NotNull(message = "Amount is required")
    @PositiveOrZero(message = "Amount cannot be negative")
    private BigDecimal amount;

    @Schema(description = "Description")
    private String description;

    @Schema(description = "Expense date")
    @NotNull(message = "Expense date is required")
    private LocalDate expenseDate;
}
