package com.sewagealert.community.service.impl;

import com.sewagealert.community.dto.*;
import com.sewagealert.community.exception.ForbiddenException;
import com.sewagealert.community.model.*;
import com.sewagealert.community.repository.*;
import com.sewagealert.community.service.NgoTransparencyService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class NgoTransparencyServiceImpl implements NgoTransparencyService {

    private final NgoOrganizationRepository ngoRepository;
    private final NgoAchievementRepository achievementRepository;
    private final NgoProgressRepository progressRepository;
    private final NgoFundRecordRepository fundRepository;
    private final NgoExpenseRecordRepository expenseRepository;

    // ---- Achievements ----

    @Override
    @Transactional
    public NgoAchievementResponse createAchievement(Long userId, NgoAchievementRequest request) {
        NgoOrganization org = getVerifiedOrg(userId);

        NgoAchievement a = new NgoAchievement();
        a.setTitle(request.getTitle());
        a.setDescription(request.getDescription());
        a.setDate(request.getDate());
        a.setEvidence(request.getEvidence());
        a.setNgoOrganizationId(org.getId());

        a = achievementRepository.save(a);
        log.info("NGO achievement created — achievementId={}, orgId={}", a.getId(), org.getId());
        return NgoAchievementResponse.fromEntity(a);
    }

    @Override
    @Transactional(readOnly = true)
    public List<NgoAchievementResponse> getMyAchievements(Long userId) {
        NgoOrganization org = getVerifiedOrg(userId);
        return achievementRepository.findByNgoOrganizationIdOrderByDateDesc(org.getId()).stream()
                .map(NgoAchievementResponse::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public NgoAchievementResponse updateAchievement(Long userId, Long achievementId, NgoAchievementRequest request) {
        NgoOrganization org = getVerifiedOrg(userId);
        NgoAchievement a = achievementRepository.findById(achievementId)
                .orElseThrow(() -> new RuntimeException("Achievement not found with id: " + achievementId));
        if (!a.getNgoOrganizationId().equals(org.getId())) {
            throw new ForbiddenException("You can only update your own achievements.");
        }
        a.setTitle(request.getTitle());
        a.setDescription(request.getDescription());
        a.setDate(request.getDate());
        a.setEvidence(request.getEvidence());
        a = achievementRepository.save(a);
        return NgoAchievementResponse.fromEntity(a);
    }

    @Override
    @Transactional
    public void deleteAchievement(Long userId, Long achievementId) {
        NgoOrganization org = getVerifiedOrg(userId);
        NgoAchievement a = achievementRepository.findById(achievementId)
                .orElseThrow(() -> new RuntimeException("Achievement not found with id: " + achievementId));
        if (!a.getNgoOrganizationId().equals(org.getId())) {
            throw new ForbiddenException("You can only delete your own achievements.");
        }
        achievementRepository.delete(a);
    }

    // ---- Progress ----

    @Override
    @Transactional(readOnly = true)
    public NgoProgressResponse getMyProgress(Long userId) {
        NgoOrganization org = getVerifiedOrg(userId);
        NgoProgress progress = progressRepository.findByNgoOrganizationId(org.getId())
                .orElseThrow(() -> new RuntimeException("No progress data found for this NGO."));
        return NgoProgressResponse.fromEntity(progress);
    }

    // ---- Funds ----

    @Override
    @Transactional
    public NgoFundResponse createFundRecord(Long userId, NgoFundRequest request) {
        NgoOrganization org = getVerifiedOrg(userId);

        NgoFundRecord f = new NgoFundRecord();
        f.setNgoOrganizationId(org.getId());
        f.setSource(request.getSource());
        f.setAmount(request.getAmount());
        f.setAllocatedAmount(request.getAllocatedAmount() != null ? request.getAllocatedAmount() : request.getAmount());
        f.setRemainingAmount(f.getAllocatedAmount());
        f.setProjectName(request.getProjectName());
        f.setDescription(request.getDescription());
        f.setReceivedDate(request.getReceivedDate());

        f = fundRepository.save(f);
        log.info("NGO fund record created — fundId={}, orgId={}", f.getId(), org.getId());
        return NgoFundResponse.fromEntity(f);
    }

    @Override
    @Transactional(readOnly = true)
    public List<NgoFundResponse> getMyFunds(Long userId) {
        NgoOrganization org = getVerifiedOrg(userId);
        return fundRepository.findByNgoOrganizationIdOrderByReceivedDateDesc(org.getId()).stream()
                .map(NgoFundResponse::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public NgoFundResponse updateFundRecord(Long userId, Long fundId, NgoFundRequest request) {
        NgoOrganization org = getVerifiedOrg(userId);
        NgoFundRecord f = fundRepository.findById(fundId)
                .orElseThrow(() -> new RuntimeException("Fund record not found with id: " + fundId));
        if (!f.getNgoOrganizationId().equals(org.getId())) {
            throw new ForbiddenException("You can only update your own fund records.");
        }
        f.setSource(request.getSource());
        f.setAmount(request.getAmount());
        f.setAllocatedAmount(request.getAllocatedAmount());
        f.setProjectName(request.getProjectName());
        f.setDescription(request.getDescription());
        f.setReceivedDate(request.getReceivedDate());
        f = fundRepository.save(f);
        return NgoFundResponse.fromEntity(f);
    }

    // ---- Expenses ----

    @Override
    @Transactional
    public NgoExpenseResponse createExpenseRecord(Long userId, NgoExpenseRequest request) {
        NgoOrganization org = getVerifiedOrg(userId);

        // Validate against fund allocation
        NgoFundRecord fund = fundRepository.findById(request.getFundRecordId())
                .orElseThrow(() -> new RuntimeException("Fund record not found with id: " + request.getFundRecordId()));
        if (!fund.getNgoOrganizationId().equals(org.getId())) {
            throw new ForbiddenException("Fund record does not belong to your organization.");
        }

        BigDecimal totalExpensesForFund = expenseRepository.sumExpensesByFundRecord(fund.getId());
        BigDecimal newRemaining = fund.getAllocatedAmount().subtract(totalExpensesForFund).subtract(request.getAmount());
        if (newRemaining.compareTo(BigDecimal.ZERO) < 0) {
            throw new RuntimeException("Expense exceeds available allocated funds. Remaining: " + fund.getRemainingAmount());
        }

        NgoExpenseRecord e = new NgoExpenseRecord();
        e.setFundRecordId(fund.getId());
        e.setNgoOrganizationId(org.getId());
        e.setCategory(request.getCategory());
        e.setAmount(request.getAmount());
        e.setDescription(request.getDescription());
        e.setExpenseDate(request.getExpenseDate());

        e = expenseRepository.save(e);

        // Update fund remaining
        fund.setRemainingAmount(newRemaining);
        fundRepository.save(fund);

        log.info("NGO expense recorded — expenseId={}, fundId={}, orgId={}", e.getId(), fund.getId(), org.getId());
        return NgoExpenseResponse.fromEntity(e);
    }

    @Override
    @Transactional(readOnly = true)
    public List<NgoExpenseResponse> getMyExpenses(Long userId) {
        NgoOrganization org = getVerifiedOrg(userId);
        return expenseRepository.findByNgoOrganizationIdOrderByExpenseDateDesc(org.getId()).stream()
                .map(NgoExpenseResponse::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public NgoExpenseResponse getExpense(Long userId, Long expenseId) {
        NgoOrganization org = getVerifiedOrg(userId);
        NgoExpenseRecord e = expenseRepository.findById(expenseId)
                .orElseThrow(() -> new RuntimeException("Expense record not found with id: " + expenseId));
        if (!e.getNgoOrganizationId().equals(org.getId())) {
            throw new ForbiddenException("You can only view your own expense records.");
        }
        return NgoExpenseResponse.fromEntity(e);
    }

    // ---- Helpers ----

    private NgoOrganization getVerifiedOrg(Long userId) {
        NgoOrganization org = ngoRepository.findByRepresentativeUserId(userId)
                .orElseThrow(() -> new RuntimeException("No NGO organization found for this user."));
        if (org.getStatus() != NgoApplicationStatus.APPROVED) {
            throw new ForbiddenException("Only verified NGOs can access this feature.");
        }
        return org;
    }
}
