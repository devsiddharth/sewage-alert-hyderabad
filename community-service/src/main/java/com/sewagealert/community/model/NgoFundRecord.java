package com.sewagealert.community.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * NgoFundRecord: Tracks funds received by an NGO for transparency.
 * No payment gateway — just a transparency ledger/reporting system.
 */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "ngo_fund_records")
public class NgoFundRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "ngo_organization_id", nullable = false)
    private Long ngoOrganizationId;

    @Column(name = "source", nullable = false)
    private String source;  // Fund source name

    @Column(name = "amount", nullable = false)
    private BigDecimal amount;

    @Column(name = "allocated_amount")
    private BigDecimal allocatedAmount;  // Amount allocated for spending

    @Column(name = "remaining_amount")
    private BigDecimal remainingAmount;  // Calculated: allocatedAmount - total expenses

    @Column(name = "project_name")
    private String projectName;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "received_date", nullable = false)
    private LocalDate receivedDate;

    @Column(name = "supporting_document_url")
    private String supportingDocumentUrl;  // Cloudinary URL

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (allocatedAmount == null) {
            allocatedAmount = amount;
        }
        if (remainingAmount == null) {
            remainingAmount = allocatedAmount;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
