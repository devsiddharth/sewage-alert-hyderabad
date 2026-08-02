package com.sewagealert.community.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "pipelines")
// Pipeline: Infrastructure information about sewage pipelines in different localities of Hyderabad
public class Pipeline {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String locality;  // Area or neighborhood served by this pipeline

    @Column(name = "installation_year")
    private Integer installationYear;  // Year the pipeline was laid

    @Column(name = "designed_capacity")
    private Integer designedCapacity;  // Designed population capacity in thousands

    @Column(name = "maintenance_date")
    private LocalDate maintenanceDate;  // Last maintenance/servicing date

    @Enumerated(EnumType.STRING)
    @Column(name = "operational_status", nullable = false)
    private OperationalStatus operationalStatus;  // ACTIVE, UNDER_MAINTENANCE, DECOMMISSIONED

    @Column(columnDefinition = "TEXT")
    private String notes;  // Additional information or remarks

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }


    // OperationalStatus enum
    public enum OperationalStatus {
        ACTIVE,              // Pipeline is functioning normally
        UNDER_MAINTENANCE,   // Pipeline is being repaired or serviced
        DECOMMISSIONED       // Pipeline is no longer in use
    }

}
