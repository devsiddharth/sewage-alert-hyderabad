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

    // assignComplaint: (Admin-only) Assigns a complaint to a field officer. Verifies the caller
    // is an ADMIN and the target user has the FIELD_OFFICER role — both server-side. Terminal
    // complaints (RESOLVED/REJECTED) cannot be assigned/reassigned.
    ComplaintResponse assignComplaint(Long complaintId, Long fieldOfficerId, Long assignedBy);

    // getAssignedComplaints: (Field-officer-only) Returns complaints assigned to the
    // authenticated field officer — the officer id is derived from the caller, never the client.
    List<ComplaintResponse> getAssignedComplaints(Long officerUserId);

    // updateAssignedComplaintStatus: (Field-officer-only) Updates the status of a complaint
    // assigned to the caller. Officers can never touch unassigned or another officer's complaints.
    ComplaintResponse updateAssignedComplaintStatus(Long complaintId, Long officerUserId, ComplaintStatusRequest request);

    // deleteComplaint: Removes a complaint and all its associated images and history (cascade)
    void deleteComplaint(Long complaintId);
}
