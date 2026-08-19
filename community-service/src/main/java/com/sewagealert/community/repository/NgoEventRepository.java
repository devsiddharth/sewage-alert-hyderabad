package com.sewagealert.community.repository;

import com.sewagealert.community.model.EventApprovalStatus;
import com.sewagealert.community.model.NgoEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface NgoEventRepository extends JpaRepository<NgoEvent, Long> {
    List<NgoEvent> findByNgoOrganizationId(Long ngoOrganizationId);
    List<NgoEvent> findByApprovalStatus(EventApprovalStatus status);
    List<NgoEvent> findByApprovalStatusAndEventDateAfter(EventApprovalStatus status, LocalDate date);
    List<NgoEvent> findByNgoOrganizationIdAndApprovalStatus(Long ngoOrganizationId, EventApprovalStatus status);
    List<NgoEvent> findByEventDateAfter(LocalDate date);
    List<NgoEvent> findByNgoOrganizationIdAndEventDateAfter(Long ngoOrganizationId, LocalDate date);
}
