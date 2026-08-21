package com.sewagealert.community.repository;

import com.sewagealert.community.model.NgoOrganization;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface NgoOrganizationRepository extends JpaRepository<NgoOrganization, Long> {
    Optional<NgoOrganization> findByRepresentativeUserId(Long userId);
    Optional<NgoOrganization> findByRegistrationNumber(String registrationNumber);
    boolean existsByRegistrationNumber(String registrationNumber);
    boolean existsByRepresentativeUserId(Long userId);
    boolean existsByOfficialEmail(String officialEmail);
    List<NgoOrganization> findByStatus(com.sewagealert.community.model.NgoApplicationStatus status);
}
