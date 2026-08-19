package com.sewagealert.community.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * NgoExpenseRecord: Tracks expenses against allocated funds.
 * Validates that expenses don't exceed allocated amounts.
 */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "ngo_expense_records")
public class NgoExpenseRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "fund_record_id", nullable = false)
    private Long fundRecordId;  // FK to NgoFundRecord

    @Column(name = "ngo_organization_id", nullable = false)
    private Long ngoOrganizationId;

    @Column(name = "category", nullable = false)
    private String category;  // e.g. Equipment, Transportation, Materials, Awareness

    @Column(name = "amount", nullable = false)
    private BigDecimal amount;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "expense_date", nullable = false)
    private LocalDate expenseDate;

    @Column(name = "supporting_document_url")
    private String supportingDocumentUrl;  // Cloudinary URL

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
