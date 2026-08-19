package com.sewagealert.community.dto;

import com.sewagealert.community.model.EventApprovalStatus;
import com.sewagealert.community.model.NgoEvent;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Schema(description = "NGO event response")
public class NgoEventResponse {

    private Long id;
    private String title;
    private String description;
    private String location;
    private LocalDate eventDate;
    private LocalDate endDate;
    private String eventTime;
    private Integer capacity;
    private String category;
    private String images;
    private Long ngoOrganizationId;
    private String ngoOrganizationName;
    private EventApprovalStatus approvalStatus;
    private String rejectionReason;
    private long registeredCount;
    private boolean isRegisteredByCurrentUser;
    private LocalDateTime createdAt;

    public static NgoEventResponse fromEntity(NgoEvent event, String ngoName, long registeredCount) {
        NgoEventResponse r = new NgoEventResponse();
        r.setId(event.getId());
        r.setTitle(event.getTitle());
        r.setDescription(event.getDescription());
        r.setLocation(event.getLocation());
        r.setEventDate(event.getEventDate());
        r.setEndDate(event.getEndDate());
        r.setEventTime(event.getEventTime());
        r.setCapacity(event.getCapacity());
        r.setCategory(event.getCategory());
        r.setImages(event.getImages());
        r.setNgoOrganizationId(event.getNgoOrganizationId());
        r.setNgoOrganizationName(ngoName);
        r.setApprovalStatus(event.getApprovalStatus());
        r.setRejectionReason(event.getRejectionReason());
        r.setRegisteredCount(registeredCount);
        r.setCreatedAt(event.getCreatedAt());
        return r;
    }
}
