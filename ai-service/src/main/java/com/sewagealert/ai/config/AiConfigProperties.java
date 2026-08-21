package com.sewagealert.ai.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * AiConfigProperties: Typed configuration for the AI provider.
 * Bound to the {@code ai.*} namespace in application.yml.
 * All secrets are read from environment variables — never hard-coded.
 */
@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "ai")
public class AiConfigProperties {

    /** Master toggle — when false, all AI endpoints return a friendly "AI is disabled" response. */
    private boolean enabled = false;

    /** API key for the OpenAI-compatible provider. Read from AI_API_KEY env var. */
    private String apiKey = "";

    /** Model identifier (e.g. gpt-4o-mini, llama-3.3-70b-versatile, etc.). */
    private String model = "gpt-4o-mini";

    /** Base URL of the OpenAI-compatible API (without trailing slash). */
    private String baseUrl = "https://api.openai.com/v1";

    /** Maximum tokens for the AI response. */
    private int maxTokens = 1024;

    /** Temperature for response generation (0.0 = deterministic, 1.0 = creative). */
    private double temperature = 0.7;

    /** HTTP timeout for AI provider calls, in seconds. */
    private int timeoutSeconds = 30;

    /** System prompt injected at the start of every AI conversation. */
    private String systemPrompt = "You are a helpful AI assistant for the SewageAlert Hyderabad platform.";
}
