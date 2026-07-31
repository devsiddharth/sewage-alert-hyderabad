package com.sewagealert.user.service;

import com.sewagealert.user.dto.UserProfileRequest;
import com.sewagealert.user.dto.UserProfileResponse;

public interface UserService {

    UserProfileResponse createProfile(Long authUserId, UserProfileRequest request);

    UserProfileResponse getProfile(Long profileId);

    UserProfileResponse getProfileByAuthUserId(Long authUserId);

    UserProfileResponse updateProfile(Long profileId, UserProfileRequest request);

    void deleteProfile(Long profileId);

}