package com.sewagealert.community.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * NgoProgress: Measurable progress metrics for an NGO organization.
 * System-derived metrics that cannot be arbitrarily modified by NGOs.
 */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "ngo_progress", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"ngo_organization_id"})
})
public class NgoProgress {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "ngo_organization_id", nullable = false, unique = true)
    private Long ngoOrganizationId;

    @Column(name = "complaints_addressed", nullable = false)
    private Long complaintsAddressed = 0L;

    @Column(name = "areas_covered", nullable = false)
    private Long areasCovered = 0L;

    @Column(name = "drives_conducted", nullable = false)
    private Long drivesConducted = 0L;

    @Column(name = "events_conducted", nullable = false)
    private Long eventsConducted = 0L;

    @Column(name = "volunteers_involved", nullable = false)
    private Long volunteersInvolved = 0L;

    @Column(name = "people_reached", nullable = false)
    private Long peopleReached = 0L;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
