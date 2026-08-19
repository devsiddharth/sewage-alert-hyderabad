package com.sewagealert.community.dto;

import com.sewagealert.community.model.NgoExpenseRecord;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Schema(description = "NGO expense record response")
public class NgoExpenseResponse {

    private Long id;
    private Long fundRecordId;
    private Long ngoOrganizationId;
    private String category;
    private BigDecimal amount;
    private String description;
    private LocalDate expenseDate;
    private String supportingDocumentUrl;
    private LocalDateTime createdAt;

    public static NgoExpenseResponse fromEntity(NgoExpenseRecord e) {
        NgoExpenseResponse r = new NgoExpenseResponse();
        r.setId(e.getId());
        r.setFundRecordId(e.getFundRecordId());
        r.setNgoOrganizationId(e.getNgoOrganizationId());
        r.setCategory(e.getCategory());
        r.setAmount(e.getAmount());
        r.setDescription(e.getDescription());
        r.setExpenseDate(e.getExpenseDate());
        r.setSupportingDocumentUrl(e.getSupportingDocumentUrl());
        r.setCreatedAt(e.getCreatedAt());
        return r;
    }
}
