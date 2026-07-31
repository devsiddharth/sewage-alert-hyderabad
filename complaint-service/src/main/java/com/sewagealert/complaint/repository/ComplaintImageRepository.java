package com.sewagealert.complaint.repository;

import com.sewagealert.complaint.model.ComplaintImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ComplaintImageRepository extends JpaRepository<ComplaintImage, Long> {

    // findByComplaintId: Retrieves all images associated with a specific complaint
    List<ComplaintImage> findByComplaintId(Long complaintId);
}
