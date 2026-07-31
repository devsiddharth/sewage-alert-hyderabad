package com.sewagealert.user.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Entity
// @Table: Maps this entity to the "user_profiles" table — separate from auth-service "users" table
@Table(name = "user_profiles")
public class UserProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // authUserId: Links this profile to the corresponding user in auth-service (not a JPA FK, just a reference ID for loose coupling between microservices)
    @Column(name = "auth_user_id", nullable = false, unique = true)
    private Long authUserId;

    @Column(nullable = false)
    private String name;

    private Long phone;

    // profilePictureUrl: Stores the URL/path to the user's uploaded avatar image
    @Column(name = "profile_picture_url")
    private String profilePictureUrl;

    // address: User's residential address for location-based services
    private String address;

    // preferences: JSON string storing user preferences (notification settings, language, etc.)
    @Column(columnDefinition = "TEXT")
    private String preferences;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public UserProfile(Long authUserId, String name, Long phone) {
        this.authUserId = authUserId;
        this.name = name;
        this.phone = phone;
    }

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
