package com.sewagealert.complaint.service;

import com.sewagealert.complaint.dto.ComplaintRequest;
import com.sewagealert.complaint.dto.ComplaintResponse;
import com.sewagealert.complaint.dto.ComplaintStatusRequest;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

// ComplaintService: Core business logic for complaint lifecycle — creation, status updates, retrieval, and deletion
public interface ComplaintService {

    // createComplaint: Creates a new complaint with PENDING status. Images are uploaded to
    // object storage first and only the returned URLs are attached to the complaint.
    ComplaintResponse createComplaint(Long authUserId, ComplaintRequest request, MultipartFile[] images);

    // getComplaint: Retrieves a single complaint by its ID
    ComplaintResponse getComplaint(Long complaintId);

    // getAllComplaints: Returns all complaints in the system (for admin/authority dashboards)
    List<ComplaintResponse> getAllComplaints();

    // getComplaintsByUser: Returns all complaints submitted by a specific user
    List<ComplaintResponse> getComplaintsByUser(Long userId);

    // updateStatus: Updates the complaint status, priority, and adds a history entry — only callable by authority/admin
    ComplaintResponse updateStatus(Long complaintId, Long updatedBy, ComplaintStatusRequest request);

    // deleteComplaint: Removes a complaint and all its associated images and history (cascade)
    void deleteComplaint(Long complaintId);
}
