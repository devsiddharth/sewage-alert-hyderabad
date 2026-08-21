package com.sewagealert.ai.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 * ComplaintInsights: Aggregated complaint analytics computed by the AI data provider.
 * Used for admin and NGO analytics queries — not exposed as an API response.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ComplaintInsights {

    private long totalComplaints;
    private long pendingCount;
    private long inProgressCount;
    private long resolvedCount;
    private long rejectedCount;
    private Map<String, Long> statusDistribution;
    private Map<String, Long> priorityDistribution;
    private List<LocationCount> topLocations;
    private List<LocationCount> recurringLocations;
    private long overdueCount;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LocationCount {
        private String title;
        private long count;
        private Double latitude;
        private Double longitude;
    }
}
