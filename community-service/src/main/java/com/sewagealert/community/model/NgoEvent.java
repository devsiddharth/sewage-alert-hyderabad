package com.sewagealert.community.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * NgoEvent: A first-class event entity created by NGOs, subject to admin approval.
 * Separate from the existing Event entity (which is authority-created).
 * Lifecycle: NGO creates → PENDING_APPROVAL → ADMIN reviews → PUBLISHED or REJECTED
 */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "ngo_events")
public class NgoEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "title", nullable = false)
    private String title;

    @Column(name = "description", nullable = false, columnDefinition = "TEXT")
    private String description;

    @Column(name = "location", nullable = false)
    private String location;

    @Column(name = "event_date", nullable = false)
    private LocalDate eventDate;

    @Column(name = "end_date")
    private LocalDate endDate;

    @Column(name = "event_time")
    private String eventTime;

    @Column(name = "capacity")
    private Integer capacity;

    @Column(name = "category")
    private String category;

    @Column(name = "images", columnDefinition = "TEXT")
    private String images;  // Comma-separated Cloudinary URLs

    @Column(name = "ngo_organization_id", nullable = false)
    private Long ngoOrganizationId;  // FK to NgoOrganization

    @Enumerated(EnumType.STRING)
    @Column(name = "approval_status", nullable = false)
    private EventApprovalStatus approvalStatus = EventApprovalStatus.PENDING_APPROVAL;

    @Column(name = "rejection_reason", columnDefinition = "TEXT")
    private String rejectionReason;

    @Column(name = "approved_by")
    private Long approvedBy;  // admin user id

    @Column(name = "approved_at")
    private LocalDateTime approvedAt;

    @OneToMany(mappedBy = "ngoEvent", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<NgoEventRegistration> registrations = new ArrayList<>();

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public void addRegistration(NgoEventRegistration registration) {
        registrations.add(registration);
        registration.setNgoEvent(this);
    }
}
