package com.sewagealert.ai.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * AiChatResponse: Response from the AI assistant.
 * Includes the AI-generated response text, detected intent, and whether platform data was used.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "AI chat response")
public class AiChatResponse {

    @Schema(description = "AI-generated response text", example = "There are 3 upcoming cleanup drives near Miyapur.")
    private String response;

    @Schema(description = "Detected intent of the user's query", example = "DRIVE_DISCOVERY")
    private AiIntent intent;

    @Schema(description = "Whether platform data was used to ground the response", example = "true")
    private boolean dataUsed;

    @Schema(description = "Optional additional context or suggestions", example = "You can register for a drive from the Events page.")
    private String suggestion;
}
