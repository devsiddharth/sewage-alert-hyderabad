package com.sewagealert.ai.service;

import com.sewagealert.ai.config.AiConfigProperties;
import com.sewagealert.ai.dto.*;
import com.sewagealert.ai.exception.AiProviderException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * AiOrchestrationService: The main AI service layer that orchestrates:
 * 1. Intent detection from the user's query
 * 2. Role-based authorization verification
 * 3. Grounded data retrieval from platform services
 * 4. Prompt construction with context
 * 5. AI provider call for response generation
 * 6. Response packaging with metadata
 *
 * This is the entry point for all AI requests — controllers call this service.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AiOrchestrationService {

    private final AiIntentDetector intentDetector;
    private final AiDataProvider dataProvider;
    private final AiProviderService aiProvider;
    private final AiConfigProperties config;

    /**
     * Process an AI chat request from any authenticated user.
     */
    public AiChatResponse processQuery(String message, Long userId) {
        log.info("AI request received: userId={}, message={}", userId, message);

        // 1. Check if AI is enabled
        if (!aiProvider.isEnabled()) {
            return AiChatResponse.builder()
                    .response("AI assistant is currently disabled. Please contact the administrator to enable it.")
                    .intent(AiIntent.GENERAL_PLATFORM_QUERY)
                    .dataUsed(false)
                    .build();
        }

        // 2. Detect intent
        AiIntent intent = intentDetector.detect(message);
        log.info("Intent detected: {} for userId={}", intent, userId);

        // 3. Get user role for authorization
        String role = dataProvider.getUserRole(userId).orElse("CITIZEN");
        log.debug("User role: {} for userId={}", role, userId);

        // 4. Authorize based on intent and role
        String authError = authorizeIntent(intent, role);
        if (authError != null) {
            return AiChatResponse.builder()
                    .response(authError)
                    .intent(intent)
                    .dataUsed(false)
                    .build();
        }

        // 5. Build grounded context from platform data
        AiDataProvider.ContextResult ctx = dataProvider.buildContext(message, intent, userId, role);

        // 6. Construct the prompt
        String systemPrompt = buildSystemPrompt(ctx.context(), intent, role);
        log.debug("System prompt built: length={}", systemPrompt.length());

        // 7. Call AI provider
        try {
            String aiResponse = aiProvider.generate(systemPrompt, message);
            log.info("AI response generated: length={}", aiResponse.length());

            return AiChatResponse.builder()
                    .response(aiResponse)
                    .intent(intent)
                    .dataUsed(ctx.dataUsed())
                    .suggestion(getSuggestion(intent))
                    .build();

        } catch (AiProviderException e) {
            log.error("AI provider error: {}", e.getMessage());
            // Return a friendly error instead of crashing
            if (e.getMessage() != null && e.getMessage().contains("rate limit")) {
                return AiChatResponse.builder()
                        .response("The AI assistant is experiencing high demand. Please try again in a few moments.")
                        .intent(intent)
                        .dataUsed(ctx.dataUsed())
                        .build();
            }
            if (e.getMessage() != null && e.getMessage().contains("timeout")) {
                return AiChatResponse.builder()
                        .response("The AI assistant took too long to respond. Please try a simpler question.")
                        .intent(intent)
                        .dataUsed(ctx.dataUsed())
                        .build();
            }
            return AiChatResponse.builder()
                    .response("The AI assistant encountered an issue. Please try again later.")
                    .intent(intent)
                    .dataUsed(ctx.dataUsed())
                    .build();
        }
    }

    /**
     * Process an AI chat request from a specific role context (user, ngo, admin).
     * Allows role-specific endpoints to enforce authorization.
     */
    public AiChatResponse processQueryWithRole(String message, Long userId, String requiredRole) {
        // Verify the user has the required role
        String actualRole = dataProvider.getUserRole(userId).orElse("CITIZEN");
        if (!actualRole.equals(requiredRole) && !actualRole.equals("ADMIN")) {
            return AiChatResponse.builder()
                    .response("You don't have permission to access this AI feature.")
                    .intent(AiIntent.GENERAL_PLATFORM_QUERY)
                    .dataUsed(false)
                    .build();
        }
        return processQuery(message, userId);
    }

    /**
     * Generate an article draft using AI.
     */
    public AiChatResponse generateArticle(String topic, Long userId) {
        if (!aiProvider.isEnabled()) {
            return AiChatResponse.builder()
                    .response("AI assistant is currently disabled.")
                    .intent(AiIntent.ARTICLE_GENERATION)
                    .dataUsed(false)
                    .build();
        }

        String role = dataProvider.getUserRole(userId).orElse("CITIZEN");
        AiDataProvider.ContextResult ctx = dataProvider.buildContext(topic, AiIntent.ARTICLE_GENERATION, userId, role);

        String systemPrompt = config.getSystemPrompt() + "\n\n"
                + "You are an article content generator for the SewageAlert Hyderabad platform.\n"
                + "Generate an awareness article draft based on the user's request.\n"
                + "Format the response as:\n"
                + "TITLE: <suggested title>\n"
                + "CATEGORY: <category like WATER_CONSERVATION, SEWAGE_TREATMENT, etc.>\n"
                + "CONTENT:\n<article body in markdown format>\n\n"
                + "Do NOT publish the article — it will be reviewed by the user before publication.\n";

        if (!ctx.context().isBlank()) {
            systemPrompt += "\nReference data:\n" + ctx.context();
        }

        try {
            String aiResponse = aiProvider.generate(systemPrompt,
                    "Create an awareness article about: " + topic);
            return AiChatResponse.builder()
                    .response(aiResponse)
                    .intent(AiIntent.ARTICLE_GENERATION)
                    .dataUsed(ctx.dataUsed())
                    .build();
        } catch (AiProviderException e) {
            return AiChatResponse.builder()
                    .response("Unable to generate article at this time. Please try again later.")
                    .intent(AiIntent.ARTICLE_GENERATION)
                    .dataUsed(false)
                    .build();
        }
    }

    /**
     * Summarize content using AI.
     */
    public AiChatResponse summarize(String content, Long userId) {
        if (!aiProvider.isEnabled()) {
            return AiChatResponse.builder()
                    .response("AI assistant is currently disabled.")
                    .intent(AiIntent.SUMMARY_GENERATION)
                    .dataUsed(false)
                    .build();
        }

        String systemPrompt = config.getSystemPrompt() + "\n\n"
                + "Summarize the following content concisely. "
                + "Focus on key points, actionable items, and important details.";

        try {
            String aiResponse = aiProvider.generate(systemPrompt, "Summarize this:\n" + content);
            return AiChatResponse.builder()
                    .response(aiResponse)
                    .intent(AiIntent.SUMMARY_GENERATION)
                    .dataUsed(false)
                    .build();
        } catch (AiProviderException e) {
            return AiChatResponse.builder()
                    .response("Unable to summarize at this time. Please try again later.")
                    .intent(AiIntent.SUMMARY_GENERATION)
                    .dataUsed(false)
                    .build();
        }
    }

    // ---- Private helpers ----

    private String authorizeIntent(AiIntent intent, String role) {
        return switch (intent) {
            case ADMIN_ANALYTICS -> {
                if (!"ADMIN".equals(role) && !"AUTHORITY".equals(role)) {
                    yield "Admin analytics are only available to administrators.";
                }
                yield null;
            }
            case NGO_ANALYTICS, SUMMARY_GENERATION -> {
                if (!"NGO_REPRESENTATIVE".equals(role) && !"ADMIN".equals(role)) {
                    yield "This feature is only available to verified NGO representatives.";
                }
                yield null;
            }
            case ARTICLE_GENERATION -> {
                // Any authenticated user can generate articles (draft only)
                yield null;
            }
            default -> null;
        };
    }

    private String buildSystemPrompt(String context, AiIntent intent, String role) {
        StringBuilder prompt = new StringBuilder(config.getSystemPrompt());

        prompt.append("\n\nUser role: ").append(role);
        prompt.append("\nDetected intent: ").append(intent);

        if (!context.isBlank()) {
            prompt.append("\n\n--- PLATFORM DATA CONTEXT ---\n");
            prompt.append(context);
            prompt.append("\n--- END PLATFORM DATA CONTEXT ---\n\n");
            prompt.append("Use the above platform data to answer the user's question accurately. ");
            prompt.append("If the data doesn't contain enough information to fully answer, say so clearly. ");
            prompt.append("Do not invent or fabricate any platform-specific information.");
        } else {
            prompt.append("\n\nNo specific platform data was retrieved for this query. ");
            prompt.append("Answer using your general knowledge, but clearly indicate if the answer ");
            prompt.append("is general knowledge rather than platform-specific information.");
        }

        return prompt.toString();
    }

    private String getSuggestion(AiIntent intent) {
        return switch (intent) {
            case NGO_DISCOVERY -> "You can visit the NGOs page for more details and contact information.";
            case DRIVE_DISCOVERY -> "Register for drives from the Events page in your dashboard.";
            case EVENT_DISCOVERY -> "You can register for events from the Events page.";
            case ARTICLE_QUERY -> "Read full articles on the Articles page.";
            case COMPLAINT_QUERY -> "Track your complaint status from the My Complaints page.";
            case COMPLAINT_INSIGHTS, ADMIN_ANALYTICS -> "Visit the Analytics page for detailed charts and reports.";
            case PIPELINE_QUERY, LAKE_QUERY, STP_QUERY, INFRASTRUCTURE_QUERY -> "View infrastructure details on the Infrastructure page.";
            case INFRASTRUCTURE_CORRELATION -> "Check the Hotspot Map and Analytics pages for geographic insights.";
            case USER_HELP -> "Navigate using the sidebar menu or visit the home page for quick links.";
            default -> null;
        };
    }
}
