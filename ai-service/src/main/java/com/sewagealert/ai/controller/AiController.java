package com.sewagealert.ai.controller;

import com.sewagealert.ai.dto.AiChatRequest;
import com.sewagealert.ai.dto.AiChatResponse;
import com.sewagealert.ai.dto.ApiResponse;
import com.sewagealert.ai.service.AiOrchestrationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * AiController: REST endpoints for the AI assistant.
 * All endpoints require authentication (X-Auth-User-Id header from the gateway).
 * Role-based authorization is enforced per endpoint.
 */
@RestController
@RequestMapping("/api/v1/ai")
@Tag(name = "AI Assistant", description = "AI-powered assistant for platform users, NGOs, and administrators")
public class AiController {

    private final AiOrchestrationService orchestrationService;

    public AiController(AiOrchestrationService orchestrationService) {
        this.orchestrationService = orchestrationService;
    }

    // ---- Unified chat endpoint (any authenticated user) ----

    @PostMapping("/chat")
    @Operation(
            summary = "Chat with the AI assistant",
            description = "Sends a natural-language question and receives a grounded, AI-generated response. "
                    + "The AI retrieves relevant platform data before answering to prevent hallucination."
    )
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<ApiResponse<AiChatResponse>> chat(
            @RequestHeader("X-Auth-User-Id") Long userId,
            @Valid @RequestBody AiChatRequest request) {
        AiChatResponse response = orchestrationService.processQuery(request.getMessage(), userId);
        return ResponseEntity.ok(ApiResponse.success("AI response generated", response));
    }

    // ---- Role-specific endpoints ----

    @PostMapping("/user/query")
    @Operation(
            summary = "User AI assistant",
            description = "AI assistant endpoint specifically for citizen users. "
                    + "Can answer questions about events, drives, NGOs, articles, and platform usage."
    )
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<ApiResponse<AiChatResponse>> userQuery(
            @RequestHeader("X-Auth-User-Id") Long userId,
            @Valid @RequestBody AiChatRequest request) {
        AiChatResponse response = orchestrationService.processQuery(request.getMessage(), userId);
        return ResponseEntity.ok(ApiResponse.success("AI response generated", response));
    }

    @PostMapping("/ngo/query")
    @Operation(
            summary = "NGO AI assistant",
            description = "AI assistant endpoint for verified NGO representatives. "
                    + "Can access NGO-specific data for insights, summaries, and analytics."
    )
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<ApiResponse<AiChatResponse>> ngoQuery(
            @RequestHeader("X-Auth-User-Id") Long userId,
            @Valid @RequestBody AiChatRequest request) {
        AiChatResponse response = orchestrationService.processQueryWithRole(request.getMessage(), userId, "NGO_REPRESENTATIVE");
        return ResponseEntity.ok(ApiResponse.success("AI response generated", response));
    }

    @PostMapping("/admin/query")
    @Operation(
            summary = "Admin AI assistant",
            description = "AI assistant endpoint for administrators. "
                    + "Can access platform-wide analytics, complaint insights, and NGO activity."
    )
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<ApiResponse<AiChatResponse>> adminQuery(
            @RequestHeader("X-Auth-User-Id") Long userId,
            @Valid @RequestBody AiChatRequest request) {
        AiChatResponse response = orchestrationService.processQueryWithRole(request.getMessage(), userId, "ADMIN");
        return ResponseEntity.ok(ApiResponse.success("AI response generated", response));
    }

    // ---- Content generation endpoints ----

    @PostMapping("/articles/generate")
    @Operation(
            summary = "Generate an article draft",
            description = "Generates an AI-assisted article draft based on a topic. "
                    + "The draft is NOT automatically published — it must be reviewed and submitted by the user."
    )
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<ApiResponse<AiChatResponse>> generateArticle(
            @RequestHeader("X-Auth-User-Id") Long userId,
            @RequestBody AiChatRequest request) {
        AiChatResponse response = orchestrationService.generateArticle(request.getMessage(), userId);
        return ResponseEntity.ok(ApiResponse.success("Article draft generated", response));
    }

    @PostMapping("/articles/summarize")
    @Operation(
            summary = "Summarize content",
            description = "Generates a concise summary of provided text content."
    )
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<ApiResponse<AiChatResponse>> summarize(
            @RequestHeader("X-Auth-User-Id") Long userId,
            @RequestBody AiChatRequest request) {
        AiChatResponse response = orchestrationService.summarize(request.getMessage(), userId);
        return ResponseEntity.ok(ApiResponse.success("Summary generated", response));
    }

    // ---- Convenience endpoints (same underlying logic, cleaner API paths) ----

    @PostMapping("/events/discover")
    @Operation(summary = "Discover events and drives", description = "Natural-language event and drive discovery.")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<ApiResponse<AiChatResponse>> discoverEvents(
            @RequestHeader("X-Auth-User-Id") Long userId,
            @Valid @RequestBody AiChatRequest request) {
        AiChatResponse response = orchestrationService.processQuery(request.getMessage(), userId);
        return ResponseEntity.ok(ApiResponse.success("AI response generated", response));
    }

    @PostMapping("/complaints/insights")
    @Operation(summary = "Get complaint insights", description = "AI-powered complaint analytics and insights.")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<ApiResponse<AiChatResponse>> complaintInsights(
            @RequestHeader("X-Auth-User-Id") Long userId,
            @Valid @RequestBody AiChatRequest request) {
        AiChatResponse response = orchestrationService.processQuery(request.getMessage(), userId);
        return ResponseEntity.ok(ApiResponse.success("AI response generated", response));
    }

    @PostMapping("/community/query")
    @Operation(summary = "Community intelligence query", description = "AI-powered community-level queries.")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<ApiResponse<AiChatResponse>> communityQuery(
            @RequestHeader("X-Auth-User-Id") Long userId,
            @Valid @RequestBody AiChatRequest request) {
        AiChatResponse response = orchestrationService.processQuery(request.getMessage(), userId);
        return ResponseEntity.ok(ApiResponse.success("AI response generated", response));
    }
}
