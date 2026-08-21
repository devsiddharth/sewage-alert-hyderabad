package com.sewagealert.ai.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sewagealert.ai.config.AiConfigProperties;
import com.sewagealert.ai.dto.OpenAiRequest;
import com.sewagealert.ai.dto.OpenAiResponse;
import com.sewagealert.ai.exception.AiProviderException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientRequestException;
import reactor.netty.http.client.HttpClient;

import java.time.Duration;
import java.util.List;

/**
 * AiProviderService: Handles communication with the OpenAI-compatible AI provider.
 * Supports any API that follows the OpenAI chat completions format:
 * OpenAI, Groq, Together AI, Azure OpenAI, local Ollama, etc.
 *
 * All secrets are read from environment variables through AiConfigProperties.
 * Never logs API keys or raw prompts/responses in production.
 */
@Service
@Slf4j
public class AiProviderService {

    private final AiConfigProperties config;
    private final WebClient webClient;
    private final ObjectMapper objectMapper;

    public AiProviderService(AiConfigProperties config) {
        this.config = config;
        this.objectMapper = new ObjectMapper();

        HttpClient httpClient = HttpClient.create()
                .responseTimeout(Duration.ofSeconds(config.getTimeoutSeconds()));

        this.webClient = WebClient.builder()
                .baseUrl(config.getBaseUrl())
                .defaultHeader("Authorization", "Bearer " + config.getApiKey())
                .defaultHeader("Content-Type", "application/json")
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .codecs(configurer -> configurer.defaultCodecs().maxInMemorySize(16 * 1024 * 1024))
                .build();
    }

    /**
     * Check if the AI provider is enabled and configured.
     */
    public boolean isEnabled() {
        return config.isEnabled() && config.getApiKey() != null && !config.getApiKey().isBlank();
    }

    /**
     * Generate an AI response from a system prompt and user message.
     *
     * @param systemPrompt The system-level instructions (includes platform context)
     * @param userMessage  The user's query
     * @return The AI-generated response text
     * @throws AiProviderException if the provider is unavailable or returns an error
     */
    public String generate(String systemPrompt, String userMessage) {
        if (!isEnabled()) {
            throw new AiProviderException("AI is not enabled. Set AI_ENABLED=true and provide AI_API_KEY.");
        }

        OpenAiRequest request = OpenAiRequest.builder()
                .model(config.getModel())
                .maxTokens(config.getMaxTokens())
                .temperature(config.getTemperature())
                .messages(List.of(
                        OpenAiRequest.Message.builder().role("system").content(systemPrompt).build(),
                        OpenAiRequest.Message.builder().role("user").content(userMessage).build()
                ))
                .build();

        try {
            String responseJson = webClient.post()
                    .uri("/chat/completions")
                    .bodyValue(request)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block(Duration.ofSeconds(config.getTimeoutSeconds() + 5));

            if (responseJson == null || responseJson.isBlank()) {
                throw new AiProviderException("Empty response from AI provider");
            }

            OpenAiResponse response = objectMapper.readValue(responseJson, OpenAiResponse.class);

            // Check for provider-level errors
            if (response.getError() != null) {
                String errorMsg = response.getError().getMessage();
                if (errorMsg != null && errorMsg.contains("rate")) {
                    throw new AiProviderException("AI provider rate limit exceeded. Please try again later.");
                }
                throw new AiProviderException("AI provider error: " + errorMsg);
            }

            if (response.getChoices() == null || response.getChoices().isEmpty()) {
                throw new AiProviderException("AI provider returned no choices");
            }

            String content = response.getChoices().get(0).getMessage().getContent();
            if (content == null || content.isBlank()) {
                throw new AiProviderException("AI provider returned empty content");
            }

            // Strip <think>...</think> tags from models like Qwen3 that include reasoning
            content = stripThinkingTags(content);

            log.debug("AI response generated: tokens={}", response.getUsage() != null ? response.getUsage().getTotalTokens() : "unknown");
            return content.trim();

        } catch (AiProviderException e) {
            throw e;
        } catch (WebClientRequestException e) {
            String msg = e.getMessage() != null ? e.getMessage() : "";
            if (msg.contains("429") || msg.contains("Too Many Requests")) {
                throw new AiProviderException(
                        "AI provider rate limit exceeded. Please wait a moment and try again, "
                        + "or switch to a free provider like Groq (https://console.groq.com) "
                        + "by setting AI_BASE_URL=https://api.groq.com/openai/v1 and AI_MODEL=llama-3.3-70b-versatile",
                        e);
            }
            if (msg.contains("timeout")) {
                throw new AiProviderException("AI provider request timed out", e);
            }
            throw new AiProviderException("Unable to reach AI provider: " + msg, e);
        } catch (Exception e) {
            throw new AiProviderException("AI provider communication failed: " + e.getMessage(), e);
        }
    }

    /**
     * Strip <think>...</think> tags from AI responses.
     * Some models (Qwen3, DeepSeek, etc.) include internal reasoning in these tags.
     * Users should see only the final answer.
     */
    private String stripThinkingTags(String content) {
        if (content == null) return null;
        // Remove <think>...</think> blocks (may span multiple lines)
        String cleaned = content.replaceAll("(?s)<think>.*?</think>", "");
        return cleaned.stripLeading().stripTrailing();
    }
}
