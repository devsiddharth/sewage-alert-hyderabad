package com.sewagealert.community.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * NgoAchievement: Records an NGO's achievements with evidence and supporting images.
 */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "ngo_achievements")
public class NgoAchievement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "title", nullable = false)
    private String title;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "date")
    private LocalDate date;

    @Column(name = "evidence", columnDefinition = "TEXT")
    private String evidence;  // Description of evidence/proof

    @Column(name = "images", columnDefinition = "TEXT")
    private String images;  // Comma-separated Cloudinary URLs

    @Column(name = "ngo_organization_id", nullable = false)
    private Long ngoOrganizationId;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
