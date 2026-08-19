package com.sewagealert.community.repository;

import com.sewagealert.community.model.NgoDriveParticipation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface NgoDriveParticipationRepository extends JpaRepository<NgoDriveParticipation, Long> {
    List<NgoDriveParticipation> findByNgoDriveId(Long driveId);
    List<NgoDriveParticipation> findByUserId(Long userId);
    Optional<NgoDriveParticipation> findByNgoDriveIdAndUserId(Long driveId, Long userId);
    long countByNgoDriveId(Long driveId);
}
