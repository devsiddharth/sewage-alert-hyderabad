package com.sewagealert.community.service;

import com.sewagealert.community.dto.*;

import java.util.List;

/**
 * NgoTransparencyService: Achievements, progress, funds, expenses.
 */
public interface NgoTransparencyService {

    // Achievements
    NgoAchievementResponse createAchievement(Long userId, NgoAchievementRequest request);
    List<NgoAchievementResponse> getMyAchievements(Long userId);
    NgoAchievementResponse updateAchievement(Long userId, Long achievementId, NgoAchievementRequest request);
    void deleteAchievement(Long userId, Long achievementId);

    // Progress
    NgoProgressResponse getMyProgress(Long userId);

    // Funds
    NgoFundResponse createFundRecord(Long userId, NgoFundRequest request);
    List<NgoFundResponse> getMyFunds(Long userId);
    NgoFundResponse updateFundRecord(Long userId, Long fundId, NgoFundRequest request);

    // Expenses
    NgoExpenseResponse createExpenseRecord(Long userId, NgoExpenseRequest request);
    List<NgoExpenseResponse> getMyExpenses(Long userId);
    NgoExpenseResponse getExpense(Long userId, Long expenseId);
}
