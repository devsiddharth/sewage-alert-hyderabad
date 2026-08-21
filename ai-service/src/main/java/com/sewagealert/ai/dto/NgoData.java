package com.sewagealert.ai.dto;

import lombok.Data;

/**
 * NgoData: Lightweight projection of NGO organization data for AI context building.
 * NOT an API response — internal only.
 */
@Data
public class NgoData {
    private Long id;
    private String organizationName;
    private String operatingAreas;
    private String mission;
    private String areasOfFocus;
    private String status;
    private String address;
}
