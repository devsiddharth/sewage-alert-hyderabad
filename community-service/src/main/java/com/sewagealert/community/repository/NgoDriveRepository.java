package com.sewagealert.community.repository;

import com.sewagealert.community.model.NgoDrive;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NgoDriveRepository extends JpaRepository<NgoDrive, Long> {
    List<NgoDrive> findByNgoOrganizationId(Long ngoOrganizationId);
    List<NgoDrive> findByNgoOrganizationIdAndStatus(Long ngoOrganizationId, NgoDrive.DriveStatus status);
}
