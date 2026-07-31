package com.sewagealert.complaint.repository;

import com.sewagealert.complaint.model.ComplaintHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ComplaintHistoryRepository extends JpaRepository<ComplaintHistory, Long> {

    // findByComplaintIdOrderByUpdatedAtAsc: Returns the full audit trail for a complaint in chronological order
    List<ComplaintHistory> findByComplaintIdOrderByUpdatedAtAsc(Long complaintId);
}
