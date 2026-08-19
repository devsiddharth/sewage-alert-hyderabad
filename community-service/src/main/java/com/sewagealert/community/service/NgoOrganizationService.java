package com.sewagealert.community.service;

import com.sewagealert.community.dto.*;
import com.sewagealert.community.model.NgoApplicationStatus;

import java.util.List;

/**
 * NgoOrganizationService: Business logic for NGO applications, verification, and profile management.
 */
public interface NgoOrganizationService {

    // Submit a new NGO application (any authenticated user can apply)
    NgoOrganizationResponse submitApplication(Long userId, NgoApplicationRequest request);

    // Get the NGO organization for a representative user
    NgoOrganizationResponse getMyOrganization(Long userId);

    // Update NGO profile (only approved NGOs)
    NgoOrganizationResponse updateProfile(Long userId, NgoApplicationRequest request);

    // Get the NGO dashboard overview
    NgoDashboardResponse getDashboard(Long userId);

    // ---- Admin endpoints ----

    // List all NGO applications with optional status filter
    List<NgoOrganizationResponse> getAllApplications(NgoApplicationStatus status);

    // Get a specific NGO application (admin)
    NgoOrganizationResponse getApplicationById(Long ngoId);

    // Approve an NGO application
    NgoOrganizationResponse approveNgo(Long ngoId, Long adminUserId);

    // Reject an NGO application
    NgoOrganizationResponse rejectNgo(Long ngoId, Long adminUserId, String reason);

    // Suspend/deactivate an NGO
    NgoOrganizationResponse suspendNgo(Long ngoId, Long adminUserId, String reason);

    // Reactivate a suspended NGO
    NgoOrganizationResponse reactivateNgo(Long ngoId, Long adminUserId);
}
