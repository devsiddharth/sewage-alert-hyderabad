package com.sewagealert.ai.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * AiChatRequest: DTO for the unified AI chat endpoint.
 * Accepts a natural-language message from any authenticated user.
 */
@Data
@Schema(description = "AI chat request")
public class AiChatRequest {

    @NotBlank(message = "Message cannot be empty")
    @Size(max = 2000, message = "Message must be at most 2000 characters")
    @Schema(description = "User's natural-language message", example = "What NGO drives are happening near Miyapur?")
    private String message;
}
