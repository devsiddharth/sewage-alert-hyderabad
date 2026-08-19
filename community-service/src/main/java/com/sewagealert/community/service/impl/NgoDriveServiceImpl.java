package com.sewagealert.community.service.impl;

import com.sewagealert.community.dto.*;
import com.sewagealert.community.exception.ForbiddenException;
import com.sewagealert.community.model.*;
import com.sewagealert.community.repository.*;
import com.sewagealert.community.service.NgoDriveService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class NgoDriveServiceImpl implements NgoDriveService {

    private final NgoOrganizationRepository ngoRepository;
    private final NgoDriveRepository driveRepository;
    private final NgoDriveParticipationRepository participationRepository;
    private final NgoProgressRepository progressRepository;

    @Override
    @Transactional
    public NgoDriveResponse createDrive(Long userId, NgoDriveRequest request) {
        NgoOrganization org = getVerifiedOrg(userId);

        NgoDrive drive = new NgoDrive();
        drive.setTitle(request.getTitle());
        drive.setDescription(request.getDescription());
        drive.setDriveType(request.getDriveType());
        drive.setLocation(request.getLocation());
        drive.setStartDate(request.getStartDate());
        drive.setEndDate(request.getEndDate());
        drive.setTotalTarget(request.getTotalTarget());
        drive.setNgoOrganizationId(org.getId());
        drive.setStatus(NgoDrive.DriveStatus.PLANNED);

        drive = driveRepository.save(drive);
        log.info("NGO drive created — driveId={}, orgId={}", drive.getId(), org.getId());
        return NgoDriveResponse.fromEntity(drive, org.getOrganizationName(), 0);
    }

    @Override
    @Transactional(readOnly = true)
    public List<NgoDriveResponse> getMyDrives(Long userId) {
        NgoOrganization org = getVerifiedOrg(userId);
        return driveRepository.findByNgoOrganizationId(org.getId()).stream()
                .map(d -> NgoDriveResponse.fromEntity(d, org.getOrganizationName(),
                        participationRepository.countByNgoDriveId(d.getId())))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public NgoDriveResponse updateDrive(Long userId, Long driveId, NgoDriveRequest request) {
        NgoOrganization org = getVerifiedOrg(userId);
        NgoDrive drive = getOwnedDrive(driveId, org.getId());

        drive.setTitle(request.getTitle());
        drive.setDescription(request.getDescription());
        drive.setDriveType(request.getDriveType());
        drive.setLocation(request.getLocation());
        drive.setStartDate(request.getStartDate());
        drive.setEndDate(request.getEndDate());
        drive.setTotalTarget(request.getTotalTarget());

        drive = driveRepository.save(drive);
        return NgoDriveResponse.fromEntity(drive, org.getOrganizationName(),
                participationRepository.countByNgoDriveId(driveId));
    }

    @Override
    @Transactional
    public void deleteDrive(Long userId, Long driveId) {
        NgoOrganization org = getVerifiedOrg(userId);
        NgoDrive drive = getOwnedDrive(driveId, org.getId());
        driveRepository.delete(drive);
    }

    @Override
    @Transactional(readOnly = true)
    public NgoDriveResponse getDrive(Long driveId) {
        NgoDrive drive = driveRepository.findById(driveId)
                .orElseThrow(() -> new RuntimeException("Drive not found with id: " + driveId));
        String ngoName = ngoRepository.findById(drive.getNgoOrganizationId())
                .map(NgoOrganization::getOrganizationName).orElse("Unknown NGO");
        return NgoDriveResponse.fromEntity(drive, ngoName,
                participationRepository.countByNgoDriveId(driveId));
    }

    @Override
    @Transactional
    public void participateInDrive(Long userId, Long driveId) {
        NgoDrive drive = driveRepository.findById(driveId)
                .orElseThrow(() -> new RuntimeException("Drive not found with id: " + driveId));

        if (participationRepository.findByNgoDriveIdAndUserId(driveId, userId).isPresent()) {
            throw new RuntimeException("Already participating in this drive.");
        }

        NgoDriveParticipation p = new NgoDriveParticipation(userId, "", "");
        p.setNgoDrive(drive);
        participationRepository.save(p);
        log.info("User {} joined drive {}", userId, driveId);
    }

    @Override
    @Transactional
    public void cancelDriveParticipation(Long userId, Long driveId) {
        NgoDriveParticipation p = participationRepository.findByNgoDriveIdAndUserId(driveId, userId)
                .orElseThrow(() -> new RuntimeException("Participation not found."));
        p.setStatus(NgoDriveParticipation.ParticipationStatus.CANCELLED);
        participationRepository.save(p);
    }

    @Override
    @Transactional(readOnly = true)
    public List<NgoParticipantResponse> getDriveParticipants(Long userId, Long driveId, boolean isAdmin) {
        NgoDrive drive = driveRepository.findById(driveId)
                .orElseThrow(() -> new RuntimeException("Drive not found with id: " + driveId));

        if (!isAdmin) {
            NgoOrganization org = getVerifiedOrg(userId);
            if (!drive.getNgoOrganizationId().equals(org.getId())) {
                throw new ForbiddenException("You can only view participants for your own drives.");
            }
        }

        return participationRepository.findByNgoDriveId(driveId).stream()
                .map(p -> new NgoParticipantResponse(
                        p.getUserId(), p.getUserName(), p.getUserEmail(),
                        p.getStatus().name(), null))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public NgoDriveResponse updateDriveProgress(Long userId, Long driveId, String progressNotes, String status) {
        NgoOrganization org = getVerifiedOrg(userId);
        NgoDrive drive = getOwnedDrive(driveId, org.getId());
        drive.setProgressNotes(progressNotes);
        if (status != null) {
            drive.setStatus(NgoDrive.DriveStatus.valueOf(status));
            if (drive.getStatus() == NgoDrive.DriveStatus.COMPLETED) {
                NgoProgress progress = progressRepository.findByNgoOrganizationId(org.getId())
                        .orElseGet(() -> {
                            NgoProgress p = new NgoProgress();
                            p.setNgoOrganizationId(org.getId());
                            return p;
                        });
                progress.setDrivesConducted(progress.getDrivesConducted() + 1);
                progressRepository.save(progress);
            }
        }
        drive = driveRepository.save(drive);
        return NgoDriveResponse.fromEntity(drive, org.getOrganizationName(),
                participationRepository.countByNgoDriveId(driveId));
    }

    // ---- Helpers ----

    private NgoOrganization getVerifiedOrg(Long userId) {
        NgoOrganization org = ngoRepository.findByRepresentativeUserId(userId)
                .orElseThrow(() -> new RuntimeException("No NGO organization found for this user."));
        if (org.getStatus() != NgoApplicationStatus.APPROVED) {
            throw new ForbiddenException("Only verified NGOs can manage drives.");
        }
        return org;
    }

    private NgoDrive getOwnedDrive(Long driveId, Long ngoId) {
        NgoDrive drive = driveRepository.findById(driveId)
                .orElseThrow(() -> new RuntimeException("Drive not found with id: " + driveId));
        if (!drive.getNgoOrganizationId().equals(ngoId)) {
            throw new ForbiddenException("You can only manage your own drives.");
        }
        return drive;
    }
}
