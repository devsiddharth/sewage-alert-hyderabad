import { api } from "@/lib/api";
import type { AiChatRequest, AiChatResponse } from "@/types";

// ---------------------------------------------------------------------------
// AI Assistant API client
//
// Routes through the Spring Cloud Gateway → AI Service (port 8086).
// All AI endpoints require authentication (X-Auth-User-Id header from gateway).
// ---------------------------------------------------------------------------

export const aiService = {
  /**
   * Unified chat endpoint — any authenticated user can ask questions.
   */
  chat: (message: string) =>
    api.post<AiChatResponse>("/api/v1/ai/chat", { message } satisfies AiChatRequest),

  /**
   * User-specific AI query endpoint.
   */
  userQuery: (message: string) =>
    api.post<AiChatResponse>("/api/v1/ai/user/query", { message } satisfies AiChatRequest),

  /**
   * NGO-specific AI query endpoint.
   */
  ngoQuery: (message: string) =>
    api.post<AiChatResponse>("/api/v1/ai/ngo/query", { message } satisfies AiChatRequest),

  /**
   * Admin-specific AI query endpoint.
   */
  adminQuery: (message: string) =>
    api.post<AiChatResponse>("/api/v1/ai/admin/query", { message } satisfies AiChatRequest),

  /**
   * Generate an article draft using AI.
   */
  generateArticle: (topic: string) =>
    api.post<AiChatResponse>("/api/v1/ai/articles/generate", { message: topic } satisfies AiChatRequest),

  /**
   * Summarize content using AI.
   */
  summarize: (content: string) =>
    api.post<AiChatResponse>("/api/v1/ai/articles/summarize", { message: content } satisfies AiChatRequest),

  /**
   * Discover events and drives.
   */
  discoverEvents: (query: string) =>
    api.post<AiChatResponse>("/api/v1/ai/events/discover", { message: query } satisfies AiChatRequest),

  /**
   * Get complaint insights.
   */
  complaintInsights: (query: string) =>
    api.post<AiChatResponse>("/api/v1/ai/complaints/insights", { message: query } satisfies AiChatRequest),

  /**
   * Community intelligence query.
   */
  communityQuery: (query: string) =>
    api.post<AiChatResponse>("/api/v1/ai/community/query", { message: query } satisfies AiChatRequest),
};
