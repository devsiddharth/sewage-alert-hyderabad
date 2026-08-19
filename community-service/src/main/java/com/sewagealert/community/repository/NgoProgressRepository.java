package com.sewagealert.community.repository;

import com.sewagealert.community.model.NgoProgress;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface NgoProgressRepository extends JpaRepository<NgoProgress, Long> {
    Optional<NgoProgress> findByNgoOrganizationId(Long ngoOrganizationId);
}
