package com.sewagealert.user.repository;

import com.sewagealert.user.model.UserProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
// JpaRepository<UserProfile, Long>: Provides built-in CRUD operations (save, findById, findAll, delete) for the UserProfile entity
public interface UserProfileRepository extends JpaRepository<UserProfile, Long> {

    // findByAuthUserId: Custom query to find a profile by the auth service user ID (since the two services use different databases)
    Optional<UserProfile> findByAuthUserId(Long authUserId);

    // existsByAuthUserId: Checks if a profile already exists for a given auth user (used during initial profile creation)
    boolean existsByAuthUserId(Long authUserId);
}
