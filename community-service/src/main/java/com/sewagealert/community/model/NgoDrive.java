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
 * NgoDrive: A first-class drive entity (Cleanliness Drive, Plantation Drive, etc.)
 * Separate from NgoEvent. Drives have their own participation and progress tracking.
 */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "ngo_drives")
public class NgoDrive {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "title", nullable = false)
    private String title;

    @Column(name = "description", nullable = false, columnDefinition = "TEXT")
    private String description;

    @Column(name = "drive_type")
    private String driveType;  // Cleanliness, Plantation, Awareness, Community Inspection

    @Column(name = "location", nullable = false)
    private String location;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date")
    private LocalDate endDate;

    @Column(name = "ngo_organization_id", nullable = false)
    private Long ngoOrganizationId;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private DriveStatus status = DriveStatus.PLANNED;

    @Column(name = "images", columnDefinition = "TEXT")
    private String images;  // Comma-separated Cloudinary URLs

    @Column(name = "total_target")
    private Integer totalTarget;  // e.g. target number of volunteers

    @Column(name = "progress_notes", columnDefinition = "TEXT")
    private String progressNotes;

    @OneToMany(mappedBy = "ngoDrive", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<NgoDriveParticipation> participations = new ArrayList<>();

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

    public enum DriveStatus {
        PLANNED,
        IN_PROGRESS,
        COMPLETED,
        CANCELLED
    }
}
