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
@Table(name = "treatment_plants")
// TreatmentPlant: Information about Sewage Treatment Plants (STPs) that process waste water in Hyderabad
public class TreatmentPlant {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;  // Name of the treatment plant

    // capacityMld: Treatment capacity in Million Liters per Day (MLD)
    @Column(name = "capacity_mld", nullable = false)
    private Double capacityMld;

    @Column(nullable = false)
    private String location;  // Physical address or area of the plant

    // treatmentMethod: Type of treatment technology used (e.g., Activated Sludge, SBR, MBBR)
    @Column(name = "treatment_method")
    private String treatmentMethod;

    // waterReuseInfo: Description of how treated water is reused (e.g., gardening, industrial, groundwater recharge)
    @Column(name = "water_reuse_info", columnDefinition = "TEXT")
    private String waterReuseInfo;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
