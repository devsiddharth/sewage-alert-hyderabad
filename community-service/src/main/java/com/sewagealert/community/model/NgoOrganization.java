package com.sewagealert.community.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * NgoOrganization: A first-class NGO organization domain entity.
 * An authenticated NGO representative manages this organization.
 * This is NOT just a USER+ROLE — it has its own data ownership and domain.
 */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "ngo_organizations", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"registration_number"}),
    @UniqueConstraint(columnNames = {"official_email"})
})
public class NgoOrganization {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "representative_user_id", unique = true)
    private Long representativeUserId;  // auth-service user id — null for public applications until approved

    @Column(name = "organization_name", nullable = false)
    private String organizationName;

    @Column(name = "official_email", nullable = false, unique = true)
    private String officialEmail;

    @Column(name = "official_phone")
    private String officialPhone;

    @Column(name = "registration_number", unique = true)
    private String registrationNumber;

    @Column(name = "registration_details", columnDefinition = "TEXT")
    private String registrationDetails;

    @Column(name = "website")
    private String website;

    @Column(name = "address", columnDefinition = "TEXT")
    private String address;

    @Column(name = "operating_areas", columnDefinition = "TEXT")
    private String operatingAreas;  // Comma-separated areas of operation

    @Column(name = "mission", columnDefinition = "TEXT")
    private String mission;

    @Column(name = "areas_of_focus", columnDefinition = "TEXT")
    private String areasOfFocus;  // Comma-separated focus areas

    @Column(name = "communities_served", columnDefinition = "TEXT")
    private String communitiesServed;

    @Column(name = "contact_person_name")
    private String contactPersonName;

    @Column(name = "contact_person_email")
    private String contactPersonEmail;

    @Column(name = "contact_person_phone")
    private String contactPersonPhone;

    @Column(name = "login_password", length = 255)
    private String loginPassword;  // BCrypt-hashed password set by applicant during application

    @Column(name = "supporting_document_url")
    private String supportingDocumentUrl;  // Cloudinary URL

    @Column(name = "logo_url")
    private String logoUrl;  // Cloudinary URL

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private NgoApplicationStatus status = NgoApplicationStatus.PENDING;

    @Column(name = "rejection_reason", columnDefinition = "TEXT")
    private String rejectionReason;

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
}
