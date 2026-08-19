package com.sewagealert.community.repository;

import com.sewagealert.community.model.NgoEventRegistration;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface NgoEventRegistrationRepository extends JpaRepository<NgoEventRegistration, Long> {
    List<NgoEventRegistration> findByNgoEventId(Long eventId);
    List<NgoEventRegistration> findByUserId(Long userId);
    Optional<NgoEventRegistration> findByNgoEventIdAndUserId(Long eventId, Long userId);
    long countByNgoEventId(Long eventId);
    List<NgoEventRegistration> findByUserIdAndStatus(Long userId, NgoEventRegistration.RegistrationStatus status);
}
