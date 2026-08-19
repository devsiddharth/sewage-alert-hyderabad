package com.sewagealert.community.dto;

import com.sewagealert.community.model.NgoFundRecord;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Schema(description = "NGO fund record response")
public class NgoFundResponse {

    private Long id;
    private Long ngoOrganizationId;
    private String source;
    private BigDecimal amount;
    private BigDecimal allocatedAmount;
    private BigDecimal remainingAmount;
    private String projectName;
    private String description;
    private LocalDate receivedDate;
    private String supportingDocumentUrl;
    private LocalDateTime createdAt;

    public static NgoFundResponse fromEntity(NgoFundRecord f) {
        NgoFundResponse r = new NgoFundResponse();
        r.setId(f.getId());
        r.setNgoOrganizationId(f.getNgoOrganizationId());
        r.setSource(f.getSource());
        r.setAmount(f.getAmount());
        r.setAllocatedAmount(f.getAllocatedAmount());
        r.setRemainingAmount(f.getRemainingAmount());
        r.setProjectName(f.getProjectName());
        r.setDescription(f.getDescription());
        r.setReceivedDate(f.getReceivedDate());
        r.setSupportingDocumentUrl(f.getSupportingDocumentUrl());
        r.setCreatedAt(f.getCreatedAt());
        return r;
    }
}
