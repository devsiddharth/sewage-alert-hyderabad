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

    // updateStatus: Updates the complaint status, priority, and adds a history entry — only callable by authority/admin.
    // Resolving (status = RESOLVED) through this JSON endpoint is NOT allowed — a mandatory proof image must be
    // uploaded via {@link #resolveComplaint}. The service rejects RESOLVED here so no client can bypass the proof.
    ComplaintResponse updateStatus(Long complaintId, Long updatedBy, ComplaintStatusRequest request);

    // resolveComplaint: (Admin-only) Marks a complaint RESOLVED. A valid proof image is mandatory:
    // it is uploaded to object storage FIRST and only after a successful upload is the complaint
    // resolved, with the returned proof URL persisted on the complaint. Upload failures leave the
    // complaint untouched.
    ComplaintResponse resolveComplaint(Long complaintId, Long resolvedBy, ComplaintStatusRequest request,
                                       MultipartFile proofImage);

    // assignComplaint: (Admin-only) Assigns a complaint to a field officer. Verifies the caller
    // is an ADMIN and the target user has the FIELD_OFFICER role — both server-side. Terminal
    // complaints (RESOLVED/REJECTED) cannot be assigned/reassigned.
    ComplaintResponse assignComplaint(Long complaintId, Long fieldOfficerId, Long assignedBy);

    // getAssignedComplaints: (Field-officer-only) Returns complaints assigned to the
    // authenticated field officer — the officer id is derived from the caller, never the client.
    List<ComplaintResponse> getAssignedComplaints(Long officerUserId);

    // updateAssignedComplaintStatus: (Field-officer-only) Updates the status of a complaint
    // assigned to the caller. Officers can never touch unassigned or another officer's complaints.
    // Resolving (status = RESOLVED) is rejected here — use {@link #resolveAssignedComplaint} with a proof image.
    ComplaintResponse updateAssignedComplaintStatus(Long complaintId, Long officerUserId, ComplaintStatusRequest request);

    // resolveAssignedComplaint: (Field-officer-only) Marks an ASSIGNED complaint RESOLVED with a
    // mandatory proof image (uploaded before the status change, same rules as resolveComplaint).
    ComplaintResponse resolveAssignedComplaint(Long complaintId, Long officerUserId, ComplaintStatusRequest request,
                                               MultipartFile proofImage);

    // deleteComplaint: Removes a complaint and all its associated images and history (cascade)
    void deleteComplaint(Long complaintId);
}
