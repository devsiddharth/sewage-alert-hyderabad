package com.sewagealert.community.dto;

import com.sewagealert.community.model.NgoDrive;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Schema(description = "NGO drive response")
public class NgoDriveResponse {

    private Long id;
    private String title;
    private String description;
    private String driveType;
    private String location;
    private LocalDate startDate;
    private LocalDate endDate;
    private Long ngoOrganizationId;
    private String ngoOrganizationName;
    private NgoDrive.DriveStatus status;
    private String images;
    private Integer totalTarget;
    private long currentParticipants;
    private String progressNotes;
    private LocalDateTime createdAt;

    public static NgoDriveResponse fromEntity(NgoDrive drive, String ngoName, long participantCount) {
        NgoDriveResponse r = new NgoDriveResponse();
        r.setId(drive.getId());
        r.setTitle(drive.getTitle());
        r.setDescription(drive.getDescription());
        r.setDriveType(drive.getDriveType());
        r.setLocation(drive.getLocation());
        r.setStartDate(drive.getStartDate());
        r.setEndDate(drive.getEndDate());
        r.setNgoOrganizationId(drive.getNgoOrganizationId());
        r.setNgoOrganizationName(ngoName);
        r.setStatus(drive.getStatus());
        r.setImages(drive.getImages());
        r.setTotalTarget(drive.getTotalTarget());
        r.setCurrentParticipants(participantCount);
        r.setProgressNotes(drive.getProgressNotes());
        r.setCreatedAt(drive.getCreatedAt());
        return r;
    }
}
