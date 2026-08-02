package com.sewagealert.community.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "lakes")
// Lake: Information about lakes in Hyderabad — their restoration status and connection to treatment plants
public class Lake {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    private String location;

    // restorationStatus: Current state of lake restoration efforts (e.g., COMPLETED, IN_PROGRESS, PLANNED, NOT_STARTED)
    @Column(name = "restoration_status")
    private String restorationStatus;

    // waterSource: Source of water for the lake (e.g., Rain-fed, Treated Sewage, Natural Spring)
    @Column(name = "water_source")
    private String waterSource;

    // connectedStpId: FK reference to the TreatmentPlant that supplies treated water to this lake
    @Column(name = "connected_stp_id")
    private Long connectedStpId;

    // environmentalUpdates: Latest environmental information about the lake
    @Column(name = "environmental_updates", columnDefinition = "TEXT")
    private String environmentalUpdates;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

}
