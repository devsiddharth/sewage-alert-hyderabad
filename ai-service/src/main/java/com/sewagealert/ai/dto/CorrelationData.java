package com.sewagealert.ai.dto;

import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * CorrelationData: Complaint-to-area correlation data for infrastructure insights.
 * Groups complaints by area and provides breakdowns useful for cross-referencing
 * with pipeline, lake, and treatment plant locations.
 */
@Data
public class CorrelationData {

    private List<AreaSummary> areaSummaries;
    private int totalAreas;
    private long totalComplaints;
    private List<String> highPriorityAreas;

    @Data
    public static class AreaSummary {
        private String area;
        private long totalComplaints;
        private Map<String, Long> statusBreakdown;
        private Map<String, Long> priorityBreakdown;
        private Double avgLatitude;
        private Double avgLongitude;
        private long pendingCount;
        private long overdueCount;
        private String latestComplaint;
    }
}
