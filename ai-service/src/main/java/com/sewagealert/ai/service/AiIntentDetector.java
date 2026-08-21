package com.sewagealert.ai.service;

import com.sewagealert.ai.dto.AiIntent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * AiIntentDetector: Classifies the user's query intent using keyword-based pattern matching.
 * Fast, deterministic, and maintainable — no AI calls needed for classification.
 * Falls back to GENERAL_PLATFORM_QUERY when no specific intent is detected.
 */
@Service
@Slf4j
public class AiIntentDetector {

    private final Map<AiIntent, Pattern> intentPatterns = new LinkedHashMap<>();

    public AiIntentDetector() {
        // Order matters — more specific patterns first
        intentPatterns.put(AiIntent.ARTICLE_GENERATION,
                Pattern.compile("(?i)(create|generate|write|draft|compose).*(article|post|blog|awareness|content)"));
        intentPatterns.put(AiIntent.EVENT_DESCRIPTION_GENERATION,
                Pattern.compile("(?i)(generate|create|write|draft).*(event description|event summary|event details)"));
        intentPatterns.put(AiIntent.SUMMARY_GENERATION,
                Pattern.compile("(?i)(summarize|summary|summarise|overview of).*(drive|activity|event|complaint|month|week|recent|progress)"));

        intentPatterns.put(AiIntent.ADMIN_ANALYTICS,
                Pattern.compile("(?i)(complaint hotspots|recurring (complaints|issues|problems)|overdue|trending|monthly summary|areas need attention|worsening|admin)"));
        intentPatterns.put(AiIntent.COMPLAINT_INSIGHTS,
                Pattern.compile("(?i)(complaint (insight|analytics|hotspot|trend|pattern|volume|distribution)|which areas|recurring|overdue complaint|increasing)"));
        intentPatterns.put(AiIntent.NGO_ANALYTICS,
                Pattern.compile("(?i)(our (drives|events|activity|progress|ngo)|which drive|best drive|focus area|volunteer|participat|summarize our|our ngo)"));

        intentPatterns.put(AiIntent.DRIVE_DISCOVERY,
                Pattern.compile("(?i)(drive|cleanup|cleanliness|plantation|awareness drive|community inspection|cleanup drive|upcoming drive|near.*drive|drive.*near)"));
        intentPatterns.put(AiIntent.EVENT_DISCOVERY,
                Pattern.compile("(?i)(event|happening|weekend|upcoming|this (week|month)|conference|workshop|seminar)"));
        intentPatterns.put(AiIntent.NGO_DISCOVERY,
                Pattern.compile("(?i)(ngo|organization|non-government|charity|foundation|which ngo|active ngo|sanitation ngo)"));

        intentPatterns.put(AiIntent.ARTICLE_QUERY,
                Pattern.compile("(?i)(article|read|published|awareness post|sanitation article|sewage article|water conservation|lake restoration)"));
        intentPatterns.put(AiIntent.COMPLAINT_QUERY,
                Pattern.compile("(?i)(complaint|report|sewage overflow|blocked drain|open manhole|sewage leak|bad odour|complaint status|track complaint)"));
        intentPatterns.put(AiIntent.COMMUNITY_QUERY,
                Pattern.compile("(?i)(community|local area|neighborhood|near me|active|sanitation activity|community drive|most common)"));

        intentPatterns.put(AiIntent.INFRASTRUCTURE_CORRELATION,
                Pattern.compile("(?i)(complaint.*pipeline|pipeline.*complaint|complaint.*lake|lake.*complaint|complaint.*stp|stp.*complaint|complaint.*infrastructure|infrastructure.*complaint|which (area|location|pipeline|lake|stp).*complaint|complaint.*correlat|correlat.*complaint|complaint.*near|near.*complaint|sewage.*infrastructure|infrastructure.*sewage|which infrastructure|infrastructure issue|infrastructure problem|pipeline area|stp area|lake area)"));
        intentPatterns.put(AiIntent.PIPELINE_QUERY,
                Pattern.compile("(?i)(pipeline|sewage pipe|drainage pipe|sewer line|pipe infrastructure|pipe network)"));
        intentPatterns.put(AiIntent.LAKE_QUERY,
                Pattern.compile("(?i)(lake|pond|water body|restoration|water source|environmental update)"));
        intentPatterns.put(AiIntent.STP_QUERY,
                Pattern.compile("(?i)(treatment plant|stp|sewage treatment|water treatment|capacity mld|treatment method|water reuse)"));
        intentPatterns.put(AiIntent.INFRASTRUCTURE_QUERY,
                Pattern.compile("(?i)(infrastructure|pipeline|treatment plant|lake|stp|sewage system|drainage system|maintenance|operational status|decommissioned)"));

        intentPatterns.put(AiIntent.USER_HELP,
                Pattern.compile("(?i)(how (do i|can i|to)|help|guide|tutorial|what is|explain|feature|navigate|use|register|sign up|login|account|password)"));
    }

    /**
     * Detect the intent of a user query.
     * Returns the most specific matching intent, or GENERAL_PLATFORM_QUERY if none match.
     */
    public AiIntent detect(String query) {
        if (query == null || query.isBlank()) {
            return AiIntent.GENERAL_PLATFORM_QUERY;
        }

        for (Map.Entry<AiIntent, Pattern> entry : intentPatterns.entrySet()) {
            if (entry.getValue().matcher(query).find()) {
                log.debug("Intent detected: {} for query: {}", entry.getKey(), query);
                return entry.getKey();
            }
        }

        log.debug("No specific intent detected, using GENERAL_PLATFORM_QUERY for: {}", query);
        return AiIntent.GENERAL_PLATFORM_QUERY;
    }
}
