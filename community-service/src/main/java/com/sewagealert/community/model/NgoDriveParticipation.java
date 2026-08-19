package com.sewagealert.community.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * NgoDriveParticipation: Tracks user participation in NGO drives.
 */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "ngo_drive_participations", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"ngo_drive_id", "user_id"})
})
public class NgoDriveParticipation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ngo_drive_id", nullable = false)
    private NgoDrive ngoDrive;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "user_name")
    private String userName;

    @Column(name = "user_email")
    private String userEmail;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private ParticipationStatus status = ParticipationStatus.REGISTERED;

    @Column(name = "hours_contributed")
    private Double hoursContributed;

    @Column(name = "participated_at", updatable = false)
    private LocalDateTime participatedAt;

    @PrePersist
    protected void onCreate() {
        participatedAt = LocalDateTime.now();
    }

    public NgoDriveParticipation(Long userId, String userName, String userEmail) {
        this.userId = userId;
        this.userName = userName;
        this.userEmail = userEmail;
    }

    public enum ParticipationStatus {
        REGISTERED,
        ATTENDED,
        COMPLETED,
        CANCELLED
    }
}
