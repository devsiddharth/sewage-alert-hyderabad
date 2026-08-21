package com.sewagealert.community.service.impl;

import com.sewagealert.community.client.AuthServiceClient;
import com.sewagealert.community.dto.*;
import com.sewagealert.community.exception.ForbiddenException;
import com.sewagealert.community.model.NgoApplicationStatus;
import com.sewagealert.community.model.NgoOrganization;
import com.sewagealert.community.model.NgoProgress;
import com.sewagealert.community.repository.*;
import com.sewagealert.community.service.NgoOrganizationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class NgoOrganizationServiceImpl implements NgoOrganizationService {

    private final NgoOrganizationRepository ngoRepository;
    private final NgoProgressRepository progressRepository;
    private final NgoEventRepository eventRepository;
    private final NgoDriveRepository driveRepository;
    private final NgoAchievementRepository achievementRepository;
    private final NgoFundRecordRepository fundRepository;
    private final NgoExpenseRecordRepository expenseRepository;
    private final NgoEventRegistrationRepository eventRegistrationRepository;
    private final NgoDriveParticipationRepository driveParticipationRepository;
    private final AuthServiceClient authServiceClient;

    @Override
    @Transactional
    public NgoOrganizationResponse submitApplication(Long userId, NgoApplicationRequest request) {
        // Prevent duplicate active applications
        if (ngoRepository.existsByRepresentativeUserId(userId)) {
            throw new RuntimeException("You already have an NGO application or organization on file.");
        }
        if (request.getRegistrationNumber() != null && ngoRepository.existsByRegistrationNumber(request.getRegistrationNumber())) {
            throw new RuntimeException("An NGO with this registration number already exists.");
        }

        NgoOrganization org = new NgoOrganization();
        org.setRepresentativeUserId(userId);
        org.setOrganizationName(request.getOrganizationName());
        org.setOfficialEmail(request.getOfficialEmail());
        org.setOfficialPhone(request.getOfficialPhone());
        org.setRegistrationNumber(request.getRegistrationNumber());
        org.setRegistrationDetails(request.getRegistrationDetails());
        org.setWebsite(request.getWebsite());
        org.setAddress(request.getAddress());
        org.setOperatingAreas(request.getOperatingAreas());
        org.setMission(request.getMission());
        org.setAreasOfFocus(request.getAreasOfFocus());
        org.setCommunitiesServed(request.getCommunitiesServed());
        org.setContactPersonName(request.getContactPersonName());
        org.setContactPersonEmail(request.getContactPersonEmail());
        org.setContactPersonPhone(request.getContactPersonPhone());
        org.setStatus(NgoApplicationStatus.PENDING);

        org = ngoRepository.save(org);
        log.info("NGO application submitted — orgId={}, userId={}", org.getId(), userId);

        return NgoOrganizationResponse.fromEntity(org);
    }

    @Override
    @Transactional
    public NgoOrganizationResponse submitPublicApplication(NgoApplicationRequest request) {
        // Prevent duplicate applications by official email
        if (ngoRepository.existsByOfficialEmail(request.getOfficialEmail())) {
            throw new RuntimeException("An NGO application with this email already exists.");
        }
        String regNum = (request.getRegistrationNumber() != null && !request.getRegistrationNumber().isBlank()) ? request.getRegistrationNumber() : null;
        if (regNum != null && ngoRepository.existsByRegistrationNumber(regNum)) {
            throw new RuntimeException("An NGO with this registration number already exists.");
        }

        NgoOrganization org = new NgoOrganization();
        // representativeUserId is null — will be set when admin approves and creates the account
        org.setOrganizationName(request.getOrganizationName());
        org.setOfficialEmail(request.getOfficialEmail());
        org.setOfficialPhone(request.getOfficialPhone());
        org.setRegistrationNumber(regNum);
        org.setRegistrationDetails(request.getRegistrationDetails());
        org.setWebsite(request.getWebsite());
        org.setAddress(request.getAddress());
        org.setOperatingAreas(request.getOperatingAreas());
        org.setMission(request.getMission());
        org.setAreasOfFocus(request.getAreasOfFocus());
        org.setCommunitiesServed(request.getCommunitiesServed());
        org.setContactPersonName(request.getContactPersonName());
        org.setContactPersonEmail(request.getContactPersonEmail());
        org.setContactPersonPhone(request.getContactPersonPhone());
        org.setStatus(NgoApplicationStatus.PENDING);

        // Hash and store the password the applicant set
        if (request.getPassword() != null && !request.getPassword().isBlank()) {
            org.setLoginPassword(new BCryptPasswordEncoder().encode(request.getPassword()));
        }

        org = ngoRepository.save(org);
        log.info("Public NGO application submitted — orgId={}, email={}", org.getId(), org.getOfficialEmail());

        return NgoOrganizationResponse.fromEntity(org);
    }

    @Override
    @Transactional(readOnly = true)
    public NgoOrganizationResponse getMyOrganization(Long userId) {
        NgoOrganization org = getOrgByUser(userId);
        return NgoOrganizationResponse.fromEntity(org);
    }

    @Override
    @Transactional
    public NgoOrganizationResponse updateProfile(Long userId, NgoApplicationRequest request) {
        NgoOrganization org = getOrgByUser(userId);
        if (org.getStatus() != NgoApplicationStatus.APPROVED) {
            throw new ForbiddenException("Only verified NGOs can update their profile.");
        }

        org.setOrganizationName(request.getOrganizationName());
        org.setOfficialEmail(request.getOfficialEmail());
        org.setOfficialPhone(request.getOfficialPhone());
        org.setWebsite(request.getWebsite());
        org.setAddress(request.getAddress());
        org.setOperatingAreas(request.getOperatingAreas());
        org.setMission(request.getMission());
        org.setAreasOfFocus(request.getAreasOfFocus());
        org.setCommunitiesServed(request.getCommunitiesServed());
        org.setContactPersonName(request.getContactPersonName());
        org.setContactPersonEmail(request.getContactPersonEmail());
        org.setContactPersonPhone(request.getContactPersonPhone());

        org = ngoRepository.save(org);
        log.info("NGO profile updated — orgId={}", org.getId());
        return NgoOrganizationResponse.fromEntity(org);
    }

    @Override
    @Transactional(readOnly = true)
    public NgoDashboardResponse getDashboard(Long userId) {
        NgoOrganization org = getOrgByUser(userId);
        if (org.getStatus() != NgoApplicationStatus.APPROVED) {
            throw new ForbiddenException("NGO dashboard is only available to verified organizations.");
        }

        NgoDashboardResponse dashboard = new NgoDashboardResponse();
        dashboard.setOrganization(NgoOrganizationResponse.fromEntity(org));

        // Progress
        NgoProgress progress = progressRepository.findByNgoOrganizationId(org.getId()).orElse(null);
        dashboard.setProgress(progress != null ? NgoProgressResponse.fromEntity(progress) : null);

        // Events
        long totalEvents = eventRepository.findByNgoOrganizationId(org.getId()).size();
        long pendingEvents = eventRepository.findByNgoOrganizationIdAndApprovalStatus(
                org.getId(), com.sewagealert.community.model.EventApprovalStatus.PENDING_APPROVAL).size();
        long publishedEvents = eventRepository.findByNgoOrganizationIdAndApprovalStatus(
                org.getId(), com.sewagealert.community.model.EventApprovalStatus.PUBLISHED).size();
        dashboard.setTotalEvents(totalEvents);
        dashboard.setPendingEvents(pendingEvents);
        dashboard.setPublishedEvents(publishedEvents);

        // Drives
        dashboard.setTotalDrives(driveRepository.findByNgoOrganizationId(org.getId()).size());

        // Achievements
        dashboard.setTotalAchievements(achievementRepository.findByNgoOrganizationIdOrderByDateDesc(org.getId()).size());

        // Participants (sum of all event registrations + drive participations)
        long totalParticipants = 0;
        for (var event : eventRepository.findByNgoOrganizationId(org.getId())) {
            totalParticipants += eventRegistrationRepository.countByNgoEventId(event.getId());
        }
        for (var drive : driveRepository.findByNgoOrganizationId(org.getId())) {
            totalParticipants += driveParticipationRepository.countByNgoDriveId(drive.getId());
        }
        dashboard.setTotalParticipants(totalParticipants);

        // Funds & Expenses
        BigDecimal totalFunds = fundRepository.sumTotalFundsByNgo(org.getId());
        BigDecimal totalExpenses = expenseRepository.sumTotalExpensesByNgo(org.getId());
        dashboard.setTotalFundsReceived(totalFunds);
        dashboard.setTotalExpenses(totalExpenses);
        dashboard.setRemainingBalance(totalFunds.subtract(totalExpenses));

        return dashboard;
    }

    // ---- Admin endpoints ----

    @Override
    @Transactional(readOnly = true)
    public List<NgoOrganizationResponse> getAllApplications(NgoApplicationStatus status) {
        List<NgoOrganization> orgs;
        if (status != null) {
            orgs = ngoRepository.findByStatus(status);
        } else {
            orgs = ngoRepository.findAll();
        }
        return orgs.stream()
                .map(NgoOrganizationResponse::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public NgoOrganizationResponse getApplicationById(Long ngoId) {
        NgoOrganization org = ngoRepository.findById(ngoId)
                .orElseThrow(() -> new RuntimeException("NGO not found with id: " + ngoId));
        return NgoOrganizationResponse.fromEntity(org);
    }

    @Override
    @Transactional
    public NgoOrganizationResponse approveNgo(Long ngoId, Long adminUserId) {
        NgoOrganization org = ngoRepository.findById(ngoId)
                .orElseThrow(() -> new RuntimeException("NGO not found with id: " + ngoId));
        if (org.getStatus() == NgoApplicationStatus.APPROVED) {
            throw new RuntimeException("NGO is already approved.");
        }
        org.setStatus(NgoApplicationStatus.APPROVED);
        org.setRejectionReason(null);

        // If this was a public application (no user account yet), create one now
        if (org.getRepresentativeUserId() == null) {
            try {
                String phone = org.getContactPersonPhone() != null ? org.getContactPersonPhone() : org.getOfficialPhone();
                CreateNgoUserRequest userReq = new CreateNgoUserRequest(
                        org.getContactPersonName() != null ? org.getContactPersonName() : org.getOrganizationName(),
                        org.getContactPersonEmail() != null ? org.getContactPersonEmail() : org.getOfficialEmail(),
                        phone,
                        org.getLoginPassword()  // pre-set password from application
                );
                ApiResponse<CreateNgoUserResponse> userRes = authServiceClient.createNgoUser(userReq);
                if (userRes.isSuccess() && userRes.getData() != null) {
                    org.setRepresentativeUserId(userRes.getData().getUserId());
                    log.info("NGO user account created — orgId={}, userId={}, tempPassword={}",
                            org.getId(), userRes.getData().getUserId(), userRes.getData().getTemporaryPassword());
                }
            } catch (Exception ex) {
                log.error("Failed to create NGO user account for orgId={}", org.getId(), ex);
                // Approval still succeeds — admin can create the account manually later
                // Approval still succeeds — admin can create the account manually later
            }
        }

        org = ngoRepository.save(org);

        // Initialize progress record
        if (progressRepository.findByNgoOrganizationId(ngoId).isEmpty()) {
            NgoProgress progress = new NgoProgress();
            progress.setNgoOrganizationId(ngoId);
            progressRepository.save(progress);
        }

        log.info("NGO approved — orgId={}, by admin={}", ngoId, adminUserId);
        return NgoOrganizationResponse.fromEntity(org);
    }

    @Override
    @Transactional
    public NgoOrganizationResponse rejectNgo(Long ngoId, Long adminUserId, String reason) {
        NgoOrganization org = ngoRepository.findById(ngoId)
                .orElseThrow(() -> new RuntimeException("NGO not found with id: " + ngoId));
        org.setStatus(NgoApplicationStatus.REJECTED);
        org.setRejectionReason(reason);
        org = ngoRepository.save(org);
        log.info("NGO rejected — orgId={}, by admin={}, reason={}", ngoId, adminUserId, reason);
        return NgoOrganizationResponse.fromEntity(org);
    }

    @Override
    @Transactional
    public NgoOrganizationResponse suspendNgo(Long ngoId, Long adminUserId, String reason) {
        NgoOrganization org = ngoRepository.findById(ngoId)
                .orElseThrow(() -> new RuntimeException("NGO not found with id: " + ngoId));
        org.setStatus(NgoApplicationStatus.SUSPENDED);
        org.setRejectionReason(reason);
        org = ngoRepository.save(org);
        log.info("NGO suspended — orgId={}, by admin={}", ngoId, adminUserId);
        return NgoOrganizationResponse.fromEntity(org);
    }

    @Override
    @Transactional
    public NgoOrganizationResponse reactivateNgo(Long ngoId, Long adminUserId) {
        NgoOrganization org = ngoRepository.findById(ngoId)
                .orElseThrow(() -> new RuntimeException("NGO not found with id: " + ngoId));
        org.setStatus(NgoApplicationStatus.APPROVED);
        org.setRejectionReason(null);
        org = ngoRepository.save(org);
        log.info("NGO reactivated — orgId={}, by admin={}", ngoId, adminUserId);
        return NgoOrganizationResponse.fromEntity(org);
    }

    // ---- Helpers ----

    private NgoOrganization getOrgByUser(Long userId) {
        return ngoRepository.findByRepresentativeUserId(userId)
                .orElseThrow(() -> new RuntimeException("No NGO organization found for this user."));
    }
}
