package com.sewagealert.complaint.repository;

import com.sewagealert.complaint.model.Complaint;
import com.sewagealert.complaint.model.ComplaintStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ComplaintRepository extends JpaRepository<Complaint, Long> {

    // findByCreatedBy: Retrieves all complaints submitted by a specific citizen
    List<Complaint> findByCreatedBy(Long createdBy);

    // findByAssignedTo: Retrieves all complaints assigned to a specific authority
    List<Complaint> findByAssignedTo(Long assignedTo);

    // findByStatus: Filters complaints by their current status (e.g., all PENDING complaints)
    List<Complaint> findByStatus(ComplaintStatus status);

    // findByStatusNot: Retrieves all complaints except those with a specific status (useful for excluding PENDING)
    List<Complaint> findByStatusNot(ComplaintStatus status);
}
