package com.sewagealert.complaint.service.impl;

import com.sewagealert.complaint.client.AuthServiceClient;
import com.sewagealert.complaint.client.UserServiceClient;
import com.sewagealert.complaint.config.UploadProperties;
import com.sewagealert.complaint.dto.ComplaintRequest;
import com.sewagealert.complaint.dto.ComplaintResponse;
import com.sewagealert.complaint.dto.ComplaintStatusRequest;
import com.sewagealert.complaint.dto.UserProfileResponse;
import com.sewagealert.complaint.dto.UserRoleResponse;
import com.sewagealert.complaint.exception.ComplaintNotFoundException;
import com.sewagealert.complaint.exception.FieldOfficerNotFoundException;
import com.sewagealert.complaint.exception.ForbiddenException;
import com.sewagealert.complaint.exception.InvalidAssignmentException;
import com.sewagealert.complaint.exception.InvalidImageException;
import com.sewagealert.complaint.exception.UserProfileNotFoundException;
import com.sewagealert.complaint.exception.UserServiceUnavailableException;
import com.sewagealert.complaint.model.*;
import com.sewagealert.complaint.producer.NotificationEventProducer;
import com.sewagealert.complaint.repository.ComplaintRepository;
import com.sewagealert.complaint.service.ComplaintService;
import com.sewagealert.complaint.storage.ImageStorageService;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
// ComplaintServiceImpl: Core business logic for complaint lifecycle — creation, status updates, retrieval, and deletion
public class ComplaintServiceImpl implements ComplaintService {

    /** Content types accepted for complaint images — validated on the bytes, not the file name. */
    private static final Set<String> ALLOWED_IMAGE_TYPES = Set.of("image/jpeg", "image/png", "image/webp");

    private final ComplaintRepository complaintRepository;
    private final UserServiceClient userServiceClient;
    private final AuthServiceClient authServiceClient;
    private final NotificationEventProducer notificationEventProducer;
    private final ImageStorageService imageStorageService;
    private final UploadProperties uploadProperties;

    @Transactional
    @Override
    // createComplaint: Validates the user, uploads images to object storage, then persists the
    // complaint with only the returned image URLs. No Base64 payloads ever reach the database.
    public ComplaintResponse createComplaint(Long authUserId, ComplaintRequest request, MultipartFile[] images) {

        // Confirm the user profile exists in USER-SERVICE before creating the complaint
        UserProfileResponse profile = validateUserExists(authUserId);
        log.info("User lookup successful — authUserId: {} (name: {})", authUserId, profile.getName());

        // 1. Validate every file up-front (content type, non-empty, size) — no uploads happen
        //    if any single file is rejected.
        List<MultipartFile> files = images != null ? List.of(images) : List.of();
        for (int i = 0; i < files.size(); i++) {
            validateImage(files.get(i), i);
        }

        // 2. Upload all files to object storage. If a later upload fails, already-uploaded
        //    objects are deleted (best effort) so nothing is orphaned in Cloudinary.
        List<String> uploadedUrls = new ArrayList<>();
        try {
            for (MultipartFile file : files) {
                uploadedUrls.add(imageStorageService.upload(file));
            }
        } catch (RuntimeException ex) {
            log.error("Image upload failed — rolling back {} already-uploaded image(s)", uploadedUrls.size(), ex);
            deleteUploaded(uploadedUrls);
            throw ex;
        }

        // 3. If this transaction later rolls back (DB save/commit failure), remove the
        //    already-uploaded objects so nothing is orphaned in Cloudinary.
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCompletion(int status) {
                    if (status == STATUS_ROLLED_BACK) {
                        deleteUploaded(uploadedUrls);
                    }
                }
            });
        }

        // 4. Persist the complaint with only the object-storage URLs.
        Complaint complaint = new Complaint();
        complaint.setTitle(request.getTitle());
        complaint.setDescription(request.getDescription());
        complaint.setLatitude(request.getLatitude());
        complaint.setLongitude(request.getLongitude());
        complaint.setStatus(ComplaintStatus.PENDING);
        complaint.setCreatedBy(authUserId);

        // Attach initial history entry marking the creation
        complaint.addHistory(new ComplaintHistory(ComplaintStatus.PENDING, "Complaint submitted", authUserId));

        // Attach images if provided — one ComplaintImage per object-storage URL
        uploadedUrls.forEach(url -> complaint.addImage(new ComplaintImage(url)));

        Complaint savedComplaint = complaintRepository.save(complaint);
        log.info("Complaint created with id: {} by authUserId: {} ({} image(s))", savedComplaint.getId(), authUserId, uploadedUrls.size());

        // Event-driven notification: publish to RabbitMQ so the Notification Service can store
        // a confirmation notification. Fire-and-forget — never blocks or fails the request.
        notificationEventProducer.publishComplaintCreated(savedComplaint);

        return ComplaintResponse.fromEntity(savedComplaint);
    }

    // validateImage: Rejects empty files, unsupported content types, and files over the
    // configured maximum size. Content type is checked (not just the file extension).
    private void validateImage(MultipartFile file, int index) {
        String label = "Image " + (index + 1);
        if (file == null || file.isEmpty()) {
            throw new InvalidImageException(label + " is empty. Please choose a valid image file.");
        }
        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_IMAGE_TYPES.contains(contentType.toLowerCase(Locale.ROOT))) {
            throw new InvalidImageException(label + " has an unsupported type. Only JPG, PNG and WEBP images are allowed.");
        }
        long maxBytes = uploadProperties.getMaxFileSize().toBytes();
        if (file.getSize() > maxBytes) {
            throw new InvalidImageException(
                    label + " exceeds the maximum size of " + uploadProperties.getMaxFileSize().toMegabytes() + " MB.");
        }
    }

    // deleteUploaded: Best-effort removal of images already pushed to object storage when a
    // multi-image upload fails part-way — prevents orphaned objects.
    private void deleteUploaded(List<String> uploadedUrls) {
        for (String url : uploadedUrls) {
            try {
                imageStorageService.delete(url);
            } catch (RuntimeException cleanupEx) {
                log.warn("Failed to clean up uploaded image after error: {}", url, cleanupEx);
            }
        }
    }

    // validateUserExists: Confirms the auth user has a profile in USER-SERVICE via OpenFeign.
    // Translates raw Feign failures into meaningful business exceptions.
    private UserProfileResponse validateUserExists(Long authUserId) {
        try {
            UserProfileResponse profile = userServiceClient.getUserProfile(authUserId).getData();
            if (profile == null) {
                throw new UserProfileNotFoundException("User profile not found for auth user id: " + authUserId);
            }
            return profile;
        } catch (FeignException.NotFound ex) {
            log.error("User lookup failed — USER-SERVICE returned 404 for authUserId: {}", authUserId);
            throw new UserProfileNotFoundException("User profile not found for auth user id: " + authUserId);
        } catch (FeignException ex) {
            log.error("User lookup failed — OpenFeign call to USER-SERVICE errored for authUserId: {}", authUserId, ex);
            throw new UserServiceUnavailableException("User Service is currently unavailable. Please try again later.");
        }
    }

    @Override
    // getComplaint: Retrieves a single complaint by its ID
    public ComplaintResponse getComplaint(Long complaintId) {
        Complaint complaint = complaintRepository.findById(complaintId)
                .orElseThrow(() -> new ComplaintNotFoundException("Complaint not found with id: " + complaintId));
        return ComplaintResponse.fromEntity(complaint);
    }

    @Override
    // getAllComplaints: Returns all complaints in the system (for admin/authority dashboards)
    public List<ComplaintResponse> getAllComplaints() {
        return complaintRepository.findAll().stream()
                .map(ComplaintResponse::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    // getComplaintsByUser: Returns all complaints submitted by a specific user
    public List<ComplaintResponse> getComplaintsByUser(Long userId) {
        return complaintRepository.findByCreatedBy(userId).stream()
                .map(ComplaintResponse::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional
    @Override
    // updateStatus: Updates the complaint status, priority, and adds a history entry — only callable by authority/admin
    public ComplaintResponse updateStatus(Long complaintId, Long updatedBy, ComplaintStatusRequest request) {
        Complaint complaint = complaintRepository.findById(complaintId)
                .orElseThrow(() -> new ComplaintNotFoundException("Complaint not found with id: " + complaintId));
        return applyStatusChange(complaint, updatedBy, request);
    }

    @Transactional
    @Override
    // assignComplaint: Admin-only assignment workflow. Authorization is enforced server-side:
    // the caller must be ADMIN and the target user must exist with the FIELD_OFFICER role.
    // Terminal complaints (RESOLVED/REJECTED) are immutable and cannot be (re)assigned.
    public ComplaintResponse assignComplaint(Long complaintId, Long fieldOfficerId, Long assignedBy) {
        // 1. The caller must be an administrator
        verifyRole(assignedBy, "ADMIN", "Only administrators can assign complaints");

        // 2. The complaint must exist
        Complaint complaint = complaintRepository.findById(complaintId)
                .orElseThrow(() -> new ComplaintNotFoundException("Complaint not found with id: " + complaintId));

        // 3. Terminal states are immutable
        if (complaint.getStatus() == ComplaintStatus.RESOLVED || complaint.getStatus() == ComplaintStatus.REJECTED) {
            throw new InvalidAssignmentException(
                    "Complaint is already " + complaint.getStatus() + " and can no longer be assigned");
        }

        // 4. The officer must exist and actually hold the FIELD_OFFICER role
        UserRoleResponse officer = fetchUserRole(fieldOfficerId);
        if (officer == null) {
            throw new FieldOfficerNotFoundException("Field officer not found");
        }
        if (!"FIELD_OFFICER".equals(officer.getRole())) {
            throw new InvalidAssignmentException("Selected user is not a field officer");
        }

        // 5. Persist the assignment (reassignment simply overwrites assignedTo atomically)
        complaint.setAssignedTo(fieldOfficerId);
        complaint.addHistory(new ComplaintHistory(complaint.getStatus(),
                "Complaint assigned to " + officer.getName(), assignedBy));
        complaint = complaintRepository.save(complaint);
        log.info("Complaint {} assigned to field officer {} by admin: {}", complaintId, fieldOfficerId, assignedBy);

        // 6. Notify the field officer via the existing RabbitMQ event flow
        notificationEventProducer.publishComplaintAssigned(complaint, fieldOfficerId, assignedBy);

        return ComplaintResponse.fromEntity(complaint);
    }

    @Override
    // getAssignedComplaints: Field officers see ONLY complaints assigned to them. The officer
    // id comes from the authenticated caller (gateway header), never from the client.
    public List<ComplaintResponse> getAssignedComplaints(Long officerUserId) {
        verifyRole(officerUserId, "FIELD_OFFICER", "Only field officers can access their assigned complaints");
        return complaintRepository.findByAssignedTo(officerUserId).stream()
                .map(ComplaintResponse::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional
    @Override
    // updateAssignedComplaintStatus: Field officers update the status only of complaints
    // assigned to them — ownership is enforced here, never trusted from the frontend.
    public ComplaintResponse updateAssignedComplaintStatus(Long complaintId, Long officerUserId,
                                                           ComplaintStatusRequest request) {
        verifyRole(officerUserId, "FIELD_OFFICER", "Only field officers can update their assigned complaints");

        Complaint complaint = complaintRepository.findById(complaintId)
                .orElseThrow(() -> new ComplaintNotFoundException("Complaint not found with id: " + complaintId));

        // Unassigned complaints and complaints assigned to another officer are off-limits
        if (!officerUserId.equals(complaint.getAssignedTo())) {
            throw new ForbiddenException("You can only update complaints assigned to you");
        }

        return applyStatusChange(complaint, officerUserId, request);
    }

    // applyStatusChange: Shared status mutation used by both the admin and field-officer paths.
    private ComplaintResponse applyStatusChange(Complaint complaint, Long updatedBy, ComplaintStatusRequest request) {
        ComplaintStatus previousStatus = complaint.getStatus();
        complaint.setStatus(request.getStatus());
        if (request.getPriority() != null) {
            complaint.setPriority(request.getPriority());
        }

        // If resolving, store resolution remarks
        if (request.getStatus() == ComplaintStatus.RESOLVED || request.getStatus() == ComplaintStatus.REJECTED) {
            complaint.setResolutionRemarks(request.getRemarks());
        }

        // Add history entry for audit trail
        complaint.addHistory(new ComplaintHistory(request.getStatus(), request.getRemarks(), updatedBy));

        complaint = complaintRepository.save(complaint);
        log.info("Complaint {} status updated to {} by user: {}", complaint.getId(), request.getStatus(), updatedBy);

        // Event-driven notification: publish the status-change event to RabbitMQ (fire-and-forget)
        notificationEventProducer.publishStatusChanged(complaint, previousStatus);

        return ComplaintResponse.fromEntity(complaint);
    }

    // fetchUserRole: Calls AUTH-SERVICE for identity + role. Returns null when the user does
    // not exist; surfaces broker/network failures as a 503 so callers can decide the semantics.
    private UserRoleResponse fetchUserRole(Long userId) {
        try {
            UserRoleResponse user = authServiceClient.getUserRole(userId).getData();
            return user != null ? user : null;
        } catch (FeignException.NotFound ex) {
            return null;
        } catch (FeignException ex) {
            log.error("Role lookup failed — AUTH-SERVICE call errored for userId: {}", userId, ex);
            throw new UserServiceUnavailableException("Auth Service is currently unavailable. Please try again later.");
        }
    }

    // verifyRole: Server-side role enforcement — throws 403 unless the caller exists with the
    // expected role. This is the security boundary for admin/officer endpoints.
    private UserRoleResponse verifyRole(Long userId, String expectedRole, String forbiddenMessage) {
        UserRoleResponse user = fetchUserRole(userId);
        if (user == null || !expectedRole.equals(user.getRole())) {
            throw new ForbiddenException(forbiddenMessage);
        }
        return user;
    }

    @Transactional
    @Override
    // deleteComplaint: Removes a complaint and all its associated images and history (cascade).
    // Also removes the underlying objects from Cloudinary on a best-effort basis.
    public void deleteComplaint(Long complaintId) {
        Complaint complaint = complaintRepository.findById(complaintId)
                .orElseThrow(() -> new ComplaintNotFoundException("Complaint not found with id: " + complaintId));

        // Best-effort object removal — a Cloudinary failure must never block the DB delete.
        for (ComplaintImage image : complaint.getImages()) {
            try {
                imageStorageService.delete(image.getImageUrl());
            } catch (RuntimeException ex) {
                log.warn("Failed to delete complaint image from storage: {}", image.getImageUrl(), ex);
            }
        }

        complaintRepository.delete(complaint);
        log.info("Complaint deleted with id: {}", complaintId);
    }
}
