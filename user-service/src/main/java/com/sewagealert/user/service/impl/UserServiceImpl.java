package com.sewagealert.user.service.impl;

import com.sewagealert.user.dto.UserProfileRequest;
import com.sewagealert.user.dto.UserProfileResponse;
import com.sewagealert.user.exception.UserProfileNotFoundException;
import com.sewagealert.user.model.UserProfile;
import com.sewagealert.user.repository.UserProfileRepository;
import com.sewagealert.user.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService {

    private final UserProfileRepository userProfileRepository;

    @Override
    public UserProfileResponse createProfile(Long authUserId, UserProfileRequest request) {

        UserProfile profile = new UserProfile(
                authUserId,
                request.getName(),
                request.getPhone()
        );

        profile.setProfilePictureUrl(request.getProfilePictureUrl());
        profile.setAddress(request.getAddress());
        profile.setPreferences(request.getPreferences());

        profile = userProfileRepository.save(profile);

        log.info("User profile created for authUserId: {}", authUserId);

        return UserProfileResponse.fromEntity(profile);
    }

    @Override
    public UserProfileResponse getProfile(Long profileId) {

        UserProfile profile = userProfileRepository.findById(profileId)
                .orElseThrow(() ->
                        new UserProfileNotFoundException(
                                "User profile not found with id: " + profileId));

        return UserProfileResponse.fromEntity(profile);
    }

    @Override
    public UserProfileResponse getProfileByAuthUserId(Long authUserId) {

        UserProfile profile = userProfileRepository.findByAuthUserId(authUserId)
                .orElseThrow(() ->
                        new UserProfileNotFoundException(
                                "User profile not found for auth user id: " + authUserId));

        return UserProfileResponse.fromEntity(profile);
    }

    @Override
    public UserProfileResponse updateProfile(Long profileId,
                                             UserProfileRequest request) {

        UserProfile profile = userProfileRepository.findById(profileId)
                .orElseThrow(() ->
                        new UserProfileNotFoundException(
                                "User profile not found with id: " + profileId));

        profile.setName(request.getName());

        if (request.getPhone() != null) {
            profile.setPhone(request.getPhone());
        }

        if (request.getProfilePictureUrl() != null) {
            profile.setProfilePictureUrl(request.getProfilePictureUrl());
        }

        if (request.getAddress() != null) {
            profile.setAddress(request.getAddress());
        }

        if (request.getPreferences() != null) {
            profile.setPreferences(request.getPreferences());
        }

        profile = userProfileRepository.save(profile);

        log.info("User profile updated for id: {}", profileId);

        return UserProfileResponse.fromEntity(profile);
    }

    @Override
    public void deleteProfile(Long profileId) {

        if (!userProfileRepository.existsById(profileId)) {
            throw new UserProfileNotFoundException(
                    "User profile not found with id: " + profileId);
        }

        userProfileRepository.deleteById(profileId);

        log.info("User profile deleted for id: {}", profileId);
    }
}