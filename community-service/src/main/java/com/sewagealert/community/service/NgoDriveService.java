package com.sewagealert.community.service;

import com.sewagealert.community.dto.*;

import java.util.List;

/**
 * NgoDriveService: NGO drive CRUD, participation management.
 */
public interface NgoDriveService {

    NgoDriveResponse createDrive(Long userId, NgoDriveRequest request);
    List<NgoDriveResponse> getMyDrives(Long userId);
    NgoDriveResponse updateDrive(Long userId, Long driveId, NgoDriveRequest request);
    void deleteDrive(Long userId, Long driveId);
    NgoDriveResponse getDrive(Long driveId);
    void participateInDrive(Long userId, Long driveId);
    void cancelDriveParticipation(Long userId, Long driveId);
    List<NgoParticipantResponse> getDriveParticipants(Long userId, Long driveId, boolean isAdmin);
    NgoDriveResponse updateDriveProgress(Long userId, Long driveId, String progressNotes, String status);
}
