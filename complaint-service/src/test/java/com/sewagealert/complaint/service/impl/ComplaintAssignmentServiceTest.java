package com.sewagealert.complaint.service.impl;

import com.sewagealert.complaint.client.AuthServiceClient;
import com.sewagealert.complaint.client.UserServiceClient;
import com.sewagealert.complaint.config.UploadProperties;
import com.sewagealert.complaint.dto.ApiResponse;
import com.sewagealert.complaint.dto.ComplaintResponse;
import com.sewagealert.complaint.dto.ComplaintStatusRequest;
import com.sewagealert.complaint.dto.UserRoleResponse;
import com.sewagealert.complaint.exception.ComplaintNotFoundException;
import com.sewagealert.complaint.exception.FieldOfficerNotFoundException;
import com.sewagealert.complaint.exception.ForbiddenException;
import com.sewagealert.complaint.exception.InvalidAssignmentException;
import com.sewagealert.complaint.exception.InvalidImageException;
import com.sewagealert.complaint.exception.ResolutionProofRequiredException;
import com.sewagealert.complaint.model.Complaint;
import com.sewagealert.complaint.model.ComplaintStatus;
import com.sewagealert.complaint.producer.NotificationEventProducer;
import com.sewagealert.complaint.repository.ComplaintRepository;
import com.sewagealert.complaint.storage.ImageStorageService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.mock.web.MockMultipartFile;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * ComplaintAssignmentServiceTest: Covers the admin assignment workflow and the
 * field-officer access rules — role verification, ownership checks, terminal-state
 * immutability, and the assignment event publication.
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class ComplaintAssignmentServiceTest {

    private static final Long ADMIN_ID = 1L;
    private static final Long OFFICER_ID = 10L;
    private static final Long OTHER_OFFICER_ID = 11L;
    private static final Long CITIZEN_ID = 99L;

    @Mock private ComplaintRepository complaintRepository;
    @Mock private UserServiceClient userServiceClient;
    @Mock private AuthServiceClient authServiceClient;
    @Mock private NotificationEventProducer notificationEventProducer;
    @Mock private ImageStorageService imageStorageService;

    private ComplaintServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new ComplaintServiceImpl(complaintRepository, userServiceClient, authServiceClient,
                notificationEventProducer, imageStorageService, new UploadProperties());

        when(authServiceClient.getUserRole(ADMIN_ID))
                .thenReturn(ApiResponse.success("ok", userRole(ADMIN_ID, "ADMIN")));
        when(authServiceClient.getUserRole(OFFICER_ID))
                .thenReturn(ApiResponse.success("ok", userRole(OFFICER_ID, "FIELD_OFFICER")));
        when(authServiceClient.getUserRole(OTHER_OFFICER_ID))
                .thenReturn(ApiResponse.success("ok", userRole(OTHER_OFFICER_ID, "FIELD_OFFICER")));
        when(authServiceClient.getUserRole(CITIZEN_ID))
                .thenReturn(ApiResponse.success("ok", userRole(CITIZEN_ID, "CITIZEN")));
        when(complaintRepository.save(any(Complaint.class))).thenAnswer(inv -> inv.getArgument(0));
    }

    private UserRoleResponse userRole(Long id, String role) {
        UserRoleResponse user = new UserRoleResponse();
        user.setId(id);
        user.setName("Officer " + id);
        user.setEmail("user" + id + "@sewagealert.com");
        user.setRole(role);
        return user;
    }

    private Complaint complaint(Long id, ComplaintStatus status, Long assignedTo) {
        Complaint complaint = new Complaint();
        complaint.setId(id);
        complaint.setTitle("Sewage overflow");
        complaint.setDescription("Leak near the road");
        complaint.setLatitude(17.3850);
        complaint.setLongitude(78.4867);
        complaint.setStatus(status);
        complaint.setAssignedTo(assignedTo);
        return complaint;
    }

    // --- Assignment: happy paths ------------------------------------------------

    @Test
    void adminCanAssignComplaintToFieldOfficer() {
        Complaint complaint = complaint(42L, ComplaintStatus.PENDING, null);
        when(complaintRepository.findById(42L)).thenReturn(Optional.of(complaint));

        ComplaintResponse response = service.assignComplaint(42L, OFFICER_ID, ADMIN_ID);

        assertNotNull(response);
        assertEquals(OFFICER_ID, complaint.getAssignedTo());
        verify(complaintRepository).save(complaint);
        // The assignment event must be published for the notification service
        verify(notificationEventProducer).publishComplaintAssigned(complaint, OFFICER_ID, ADMIN_ID);
    }

    @Test
    void adminCanReassignComplaintToAnotherOfficer() {
        Complaint complaint = complaint(42L, ComplaintStatus.IN_PROGRESS, OTHER_OFFICER_ID);
        when(complaintRepository.findById(42L)).thenReturn(Optional.of(complaint));

        service.assignComplaint(42L, OFFICER_ID, ADMIN_ID);

        assertEquals(OFFICER_ID, complaint.getAssignedTo());
    }

    // --- Assignment: authorization ---------------------------------------------

    @Test
    void nonAdminCannotAssignComplaint() {
        Complaint complaint = complaint(42L, ComplaintStatus.PENDING, null);
        when(complaintRepository.findById(42L)).thenReturn(Optional.of(complaint));

        assertThrows(ForbiddenException.class, () -> service.assignComplaint(42L, OFFICER_ID, CITIZEN_ID));

        verify(complaintRepository, never()).save(any());
        verify(notificationEventProducer, never()).publishComplaintAssigned(any(), any(), any());
    }

    @Test
    void unknownCallerCannotAssignComplaint() {
        when(authServiceClient.getUserRole(999L))
                .thenThrow(new feign.FeignException.NotFound("not found",
                        feign.Request.create(feign.Request.HttpMethod.GET, "http://x",
                                java.util.Map.of(), new byte[0], null), null, null));

        assertThrows(ForbiddenException.class, () -> service.assignComplaint(42L, OFFICER_ID, 999L));
    }

    // --- Assignment: validation -------------------------------------------------

    @Test
    void cannotAssignNonexistentComplaint() {
        when(complaintRepository.findById(404L)).thenReturn(Optional.empty());

        assertThrows(ComplaintNotFoundException.class, () -> service.assignComplaint(404L, OFFICER_ID, ADMIN_ID));
        verify(notificationEventProducer, never()).publishComplaintAssigned(any(), any(), any());
    }

    @Test
    void cannotAssignToNonexistentOfficer() {
        Complaint complaint = complaint(42L, ComplaintStatus.PENDING, null);
        when(complaintRepository.findById(42L)).thenReturn(Optional.of(complaint));
        when(authServiceClient.getUserRole(999L))
                .thenThrow(new feign.FeignException.NotFound("not found",
                        feign.Request.create(feign.Request.HttpMethod.GET, "http://x",
                                java.util.Map.of(), new byte[0], null), null, null));

        assertThrows(FieldOfficerNotFoundException.class, () -> service.assignComplaint(42L, 999L, ADMIN_ID));
    }

    @Test
    void cannotAssignToUserWithoutFieldOfficerRole() {
        Complaint complaint = complaint(42L, ComplaintStatus.PENDING, null);
        when(complaintRepository.findById(42L)).thenReturn(Optional.of(complaint));

        assertThrows(InvalidAssignmentException.class, () -> service.assignComplaint(42L, CITIZEN_ID, ADMIN_ID));
    }

    @Test
    void cannotAssignResolvedComplaint() {
        Complaint complaint = complaint(42L, ComplaintStatus.RESOLVED, null);
        when(complaintRepository.findById(42L)).thenReturn(Optional.of(complaint));

        assertThrows(InvalidAssignmentException.class, () -> service.assignComplaint(42L, OFFICER_ID, ADMIN_ID));
    }

    @Test
    void cannotAssignRejectedComplaint() {
        Complaint complaint = complaint(42L, ComplaintStatus.REJECTED, null);
        when(complaintRepository.findById(42L)).thenReturn(Optional.of(complaint));

        assertThrows(InvalidAssignmentException.class, () -> service.assignComplaint(42L, OFFICER_ID, ADMIN_ID));
    }

    // --- Field officer: retrieval ------------------------------------------------

    @Test
    void fieldOfficerCanRetrieveTheirAssignedComplaints() {
        Complaint c1 = complaint(1L, ComplaintStatus.IN_PROGRESS, OFFICER_ID);
        Complaint c2 = complaint(2L, ComplaintStatus.PENDING, OFFICER_ID);
        when(complaintRepository.findByAssignedTo(OFFICER_ID)).thenReturn(List.of(c1, c2));

        List<ComplaintResponse> responses = service.getAssignedComplaints(OFFICER_ID);

        assertEquals(2, responses.size());
        verify(complaintRepository).findByAssignedTo(OFFICER_ID);
    }

    @Test
    void citizenCannotRetrieveAssignedComplaints() {
        assertThrows(ForbiddenException.class, () -> service.getAssignedComplaints(CITIZEN_ID));
        verify(complaintRepository, never()).findByAssignedTo(any());
    }

    // --- Field officer: status updates --------------------------------------------

    @Test
    void fieldOfficerCanUpdateTheirAssignedComplaint() {
        Complaint complaint = complaint(42L, ComplaintStatus.PENDING, OFFICER_ID);
        when(complaintRepository.findById(42L)).thenReturn(Optional.of(complaint));

        ComplaintStatusRequest request = new ComplaintStatusRequest();
        request.setStatus(ComplaintStatus.IN_PROGRESS);
        request.setRemarks("Starting field work");

        ComplaintResponse response = service.updateAssignedComplaintStatus(42L, OFFICER_ID, request);

        assertEquals(ComplaintStatus.IN_PROGRESS.name(), response.getStatus());
        assertEquals(1, complaint.getHistory().size());
        verify(notificationEventProducer).publishStatusChanged(complaint, ComplaintStatus.PENDING);
    }

    @Test
    void fieldOfficerCannotUpdateUnassignedComplaint() {
        Complaint complaint = complaint(42L, ComplaintStatus.PENDING, null);
        when(complaintRepository.findById(42L)).thenReturn(Optional.of(complaint));

        ComplaintStatusRequest request = new ComplaintStatusRequest();
        request.setStatus(ComplaintStatus.IN_PROGRESS);

        assertThrows(ForbiddenException.class, () -> service.updateAssignedComplaintStatus(42L, OFFICER_ID, request));
        verify(complaintRepository, never()).save(any());
    }

    @Test
    void fieldOfficerCannotUpdateAnotherOfficersComplaint() {
        Complaint complaint = complaint(42L, ComplaintStatus.IN_PROGRESS, OTHER_OFFICER_ID);
        when(complaintRepository.findById(42L)).thenReturn(Optional.of(complaint));

        ComplaintStatusRequest request = new ComplaintStatusRequest();
        request.setStatus(ComplaintStatus.RESOLVED);

        assertThrows(ForbiddenException.class, () -> service.updateAssignedComplaintStatus(42L, OFFICER_ID, request));
        verify(complaintRepository, never()).save(any());
    }

    @Test
    void citizenCannotUpdateAssignedComplaint() {
        Complaint complaint = complaint(42L, ComplaintStatus.PENDING, OFFICER_ID);
        when(complaintRepository.findById(42L)).thenReturn(Optional.of(complaint));

        ComplaintStatusRequest request = new ComplaintStatusRequest();
        request.setStatus(ComplaintStatus.IN_PROGRESS);

        assertThrows(ForbiddenException.class, () -> service.updateAssignedComplaintStatus(42L, CITIZEN_ID, request));
    }

    // --- Field officer: resolution with mandatory proof -----------------------------

    private static final String PROOF_URL = "https://res.cloudinary.com/demo/image/upload/v1/complaints/proof-officer.jpg";

    private ComplaintStatusRequest resolvedRequest() {
        ComplaintStatusRequest request = new ComplaintStatusRequest();
        request.setStatus(ComplaintStatus.RESOLVED);
        request.setRemarks("Issue attended and fixed");
        return request;
    }

    @Test
    void fieldOfficerCanResolveTheirAssignedComplaintWithProof() {
        Complaint complaint = complaint(42L, ComplaintStatus.IN_PROGRESS, OFFICER_ID);
        when(complaintRepository.findById(42L)).thenReturn(Optional.of(complaint));
        MockMultipartFile proof = new MockMultipartFile("proofImage", "proof.jpg", "image/jpeg", new byte[]{1});
        when(imageStorageService.upload(proof)).thenReturn(PROOF_URL);

        ComplaintResponse response = service.resolveAssignedComplaint(42L, OFFICER_ID, resolvedRequest(), proof);

        verify(imageStorageService).upload(proof);
        ArgumentCaptor<Complaint> captor = ArgumentCaptor.forClass(Complaint.class);
        verify(complaintRepository).save(captor.capture());
        assertEquals(ComplaintStatus.RESOLVED, captor.getValue().getStatus());
        assertEquals(PROOF_URL, captor.getValue().getResolutionProofImageUrl());
        assertEquals(ComplaintStatus.RESOLVED.name(), response.getStatus());
        verify(notificationEventProducer).publishStatusChanged(complaint, ComplaintStatus.IN_PROGRESS);
    }

    @Test
    void fieldOfficerCannotResolveWithoutProof() {
        Complaint complaint = complaint(42L, ComplaintStatus.IN_PROGRESS, OFFICER_ID);
        when(complaintRepository.findById(42L)).thenReturn(Optional.of(complaint));

        assertThrows(ResolutionProofRequiredException.class,
                () -> service.resolveAssignedComplaint(42L, OFFICER_ID, resolvedRequest(), null));

        verifyNoInteractions(imageStorageService);
        verify(complaintRepository, never()).save(any());
    }

    @Test
    void fieldOfficerCannotResolveWithInvalidProofType() {
        Complaint complaint = complaint(42L, ComplaintStatus.IN_PROGRESS, OFFICER_ID);
        when(complaintRepository.findById(42L)).thenReturn(Optional.of(complaint));
        MockMultipartFile text = new MockMultipartFile("proofImage", "notes.txt", "text/plain", new byte[]{1});

        assertThrows(InvalidImageException.class,
                () -> service.resolveAssignedComplaint(42L, OFFICER_ID, resolvedRequest(), text));

        verifyNoInteractions(imageStorageService);
        verify(complaintRepository, never()).save(any());
    }

    @Test
    void fieldOfficerCannotResolveAnotherOfficersComplaint() {
        Complaint complaint = complaint(42L, ComplaintStatus.IN_PROGRESS, OTHER_OFFICER_ID);
        when(complaintRepository.findById(42L)).thenReturn(Optional.of(complaint));
        MockMultipartFile proof = new MockMultipartFile("proofImage", "proof.jpg", "image/jpeg", new byte[]{1});

        assertThrows(ForbiddenException.class,
                () -> service.resolveAssignedComplaint(42L, OFFICER_ID, resolvedRequest(), proof));

        verifyNoInteractions(imageStorageService);
        verify(complaintRepository, never()).save(any());
    }

    @Test
    void citizenCannotResolveAssignedComplaint() {
        Complaint complaint = complaint(42L, ComplaintStatus.IN_PROGRESS, OFFICER_ID);
        when(complaintRepository.findById(42L)).thenReturn(Optional.of(complaint));
        MockMultipartFile proof = new MockMultipartFile("proofImage", "proof.jpg", "image/jpeg", new byte[]{1});

        assertThrows(ForbiddenException.class,
                () -> service.resolveAssignedComplaint(42L, CITIZEN_ID, resolvedRequest(), proof));
    }

    @Test
    void officerCannotResolveViaJsonStatusPatch() {
        Complaint complaint = complaint(42L, ComplaintStatus.IN_PROGRESS, OFFICER_ID);
        when(complaintRepository.findById(42L)).thenReturn(Optional.of(complaint));

        assertThrows(ResolutionProofRequiredException.class,
                () -> service.updateAssignedComplaintStatus(42L, OFFICER_ID, resolvedRequest()));

        verify(complaintRepository, never()).save(any());
    }

    // --- Regression: existing admin status update still works ----------------------

    @Test
    void adminStatusUpdateStillWorks() {
        Complaint complaint = complaint(42L, ComplaintStatus.PENDING, null);
        when(complaintRepository.findById(42L)).thenReturn(Optional.of(complaint));

        ComplaintStatusRequest request = new ComplaintStatusRequest();
        request.setStatus(ComplaintStatus.IN_PROGRESS);

        service.updateStatus(42L, ADMIN_ID, request);

        ArgumentCaptor<Complaint> captor = ArgumentCaptor.forClass(Complaint.class);
        verify(complaintRepository).save(captor.capture());
        assertEquals(ComplaintStatus.IN_PROGRESS, captor.getValue().getStatus());
    }
}
