package com.sewagealert.ai.dto;

import lombok.Data;

/**
 * ComplaintData: Lightweight projection of complaint data for AI context building.
 */
@Data
public class ComplaintData {
    private Long id;
    private String title;
    private String description;
    private Double latitude;
    private Double longitude;
    private String status;
    private String priority;
    private String createdAt;
    private String updatedAt;
}
