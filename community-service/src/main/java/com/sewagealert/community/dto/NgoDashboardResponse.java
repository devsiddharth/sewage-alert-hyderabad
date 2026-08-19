package com.sewagealert.community.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Schema(description = "NGO dashboard overview")
public class NgoDashboardResponse {

    private NgoOrganizationResponse organization;
    private NgoProgressResponse progress;
    private long totalEvents;
    private long pendingEvents;
    private long publishedEvents;
    private long totalDrives;
    private long totalAchievements;
    private long totalParticipants;
    private BigDecimal totalFundsReceived;
    private BigDecimal totalExpenses;
    private BigDecimal remainingBalance;
}
