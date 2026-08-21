package com.sewagealert.ai.dto;

/**
 * AiIntent: Classification of the user's query intent.
 * Used to route queries to the appropriate data retrieval logic
 * and to label responses for analytics and debugging.
 */
public enum AiIntent {
    NGO_DISCOVERY,
    EVENT_DISCOVERY,
    DRIVE_DISCOVERY,
    ARTICLE_QUERY,
    ARTICLE_GENERATION,
    COMPLAINT_QUERY,
    COMPLAINT_INSIGHTS,
    COMMUNITY_QUERY,
    USER_HELP,
    NGO_ANALYTICS,
    ADMIN_ANALYTICS,
    EVENT_DESCRIPTION_GENERATION,
    SUMMARY_GENERATION,
    GENERAL_PLATFORM_QUERY,
    INFRASTRUCTURE_QUERY,
    PIPELINE_QUERY,
    LAKE_QUERY,
    STP_QUERY,
    INFRASTRUCTURE_CORRELATION
}
