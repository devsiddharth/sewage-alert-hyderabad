package com.sewagealert.community.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * NgoEventRegistration: Tracks citizen registrations for NGO events.
 * Prevents duplicate registrations and enforces capacity.
 */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "ngo_event_registrations", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"ngo_event_id", "user_id"})
})
public class NgoEventRegistration {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ngo_event_id", nullable = false)
    private NgoEvent ngoEvent;

    @Column(name = "user_id", nullable = false)
    private Long userId;  // auth-service citizen user id

    @Column(name = "user_name")
    private String userName;

    @Column(name = "user_email")
    private String userEmail;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private RegistrationStatus status = RegistrationStatus.REGISTERED;

    @Enumerated(EnumType.STRING)
    @Column(name = "attendance")
    private AttendanceStatus attendance;

    @Column(name = "registered_at", updatable = false)
    private LocalDateTime registeredAt;

    @PrePersist
    protected void onCreate() {
        registeredAt = LocalDateTime.now();
    }

    public NgoEventRegistration(Long userId, String userName, String userEmail) {
        this.userId = userId;
        this.userName = userName;
        this.userEmail = userEmail;
    }

    public enum RegistrationStatus {
        REGISTERED,
        CANCELLED,
        WAITLISTED
    }

    public enum AttendanceStatus {
        PENDING,
        ATTENDED,
        COMPLETED,
        NO_SHOW
    }
}
