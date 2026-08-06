package com.sewagealert.complaint.service.impl;

import com.sewagealert.complaint.client.UserServiceClient;
import com.sewagealert.complaint.dto.ComplaintRequest;
import com.sewagealert.complaint.dto.ComplaintResponse;
import com.sewagealert.complaint.dto.ComplaintStatusRequest;
import com.sewagealert.complaint.dto.UserProfileResponse;
import com.sewagealert.complaint.exception.ComplaintNotFoundException;
import com.sewagealert.complaint.exception.UserProfileNotFoundException;
import com.sewagealert.complaint.exception.UserServiceUnavailableException;
import com.sewagealert.complaint.model.*;
import com.sewagealert.complaint.producer.NotificationEventProducer;
import com.sewagealert.complaint.repository.ComplaintRepository;
import com.sewagealert.complaint.service.ComplaintService;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
// ComplaintServiceImpl: Core business logic for complaint lifecycle — creation, status updates, retrieval, and deletion
public class ComplaintServiceImpl implements ComplaintService {

    private final ComplaintRepository complaintRepository;
    private final UserServiceClient userServiceClient;
    private final NotificationEventProducer notificationEventProducer;

    @Transactional
    @Override
    // createComplaint: Validates the user exists in User Service, then creates a new complaint with PENDING status
    public ComplaintResponse createComplaint(Long authUserId, ComplaintRequest request) {

        // Confirm the user profile exists in USER-SERVICE before creating the complaint
        UserProfileResponse profile = validateUserExists(authUserId);
        log.info("User lookup successful — authUserId: {} (name: {})", authUserId, profile.getName());

        Complaint complaint = new Complaint();
        complaint.setTitle(request.getTitle());
        complaint.setDescription(request.getDescription());
        complaint.setLatitude(request.getLatitude());
        complaint.setLongitude(request.getLongitude());
        complaint.setStatus(ComplaintStatus.PENDING);
        complaint.setCreatedBy(authUserId);

        // Attach initial history entry marking the creation
        complaint.addHistory(new ComplaintHistory(ComplaintStatus.PENDING, "Complaint submitted", authUserId));

        // Attach images if provided
        if (request.getImageUrls() != null) {
            request.getImageUrls().forEach(url -> complaint.addImage(new ComplaintImage(url)));
        }

        Complaint savedComplaint = complaintRepository.save(complaint);
        log.info("Complaint created with id: {} by authUserId: {}", savedComplaint.getId(), authUserId);

        // Event-driven notification: publish to RabbitMQ so the Notification Service can store
        // a confirmation notification. Fire-and-forget — never blocks or fails the request.
        notificationEventProducer.publishComplaintCreated(savedComplaint);

        return ComplaintResponse.fromEntity(savedComplaint);
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
        log.info("Complaint {} status updated to {} by user: {}", complaintId, request.getStatus(), updatedBy);

        // Event-driven notification: publish the status-change event to RabbitMQ (fire-and-forget)
        notificationEventProducer.publishStatusChanged(complaint, previousStatus);

        return ComplaintResponse.fromEntity(complaint);
    }

    @Transactional
    @Override
    // deleteComplaint: Removes a complaint and all its associated images and history (cascade)
    public void deleteComplaint(Long complaintId) {
        if (!complaintRepository.existsById(complaintId)) {
            throw new ComplaintNotFoundException("Complaint not found with id: " + complaintId);
        }
        complaintRepository.deleteById(complaintId);
        log.info("Complaint deleted with id: {}", complaintId);
    }
}
