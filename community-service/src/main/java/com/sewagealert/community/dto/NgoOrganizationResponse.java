package com.sewagealert.community.dto;

import com.sewagealert.community.model.NgoApplicationStatus;
import com.sewagealert.community.model.NgoOrganization;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Schema(description = "NGO organization response")
public class NgoOrganizationResponse {

    private Long id;
    private Long representativeUserId;
    private String organizationName;
    private String officialEmail;
    private String officialPhone;
    private String registrationNumber;
    private String registrationDetails;
    private String website;
    private String address;
    private String operatingAreas;
    private String mission;
    private String areasOfFocus;
    private String communitiesServed;
    private String contactPersonName;
    private String contactPersonEmail;
    private String contactPersonPhone;
    private String supportingDocumentUrl;
    private String logoUrl;
    private NgoApplicationStatus status;
    private String rejectionReason;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static NgoOrganizationResponse fromEntity(NgoOrganization org) {
        NgoOrganizationResponse r = new NgoOrganizationResponse();
        r.setId(org.getId());
        r.setRepresentativeUserId(org.getRepresentativeUserId());
        r.setOrganizationName(org.getOrganizationName());
        r.setOfficialEmail(org.getOfficialEmail());
        r.setOfficialPhone(org.getOfficialPhone());
        r.setRegistrationNumber(org.getRegistrationNumber());
        r.setRegistrationDetails(org.getRegistrationDetails());
        r.setWebsite(org.getWebsite());
        r.setAddress(org.getAddress());
        r.setOperatingAreas(org.getOperatingAreas());
        r.setMission(org.getMission());
        r.setAreasOfFocus(org.getAreasOfFocus());
        r.setCommunitiesServed(org.getCommunitiesServed());
        r.setContactPersonName(org.getContactPersonName());
        r.setContactPersonEmail(org.getContactPersonEmail());
        r.setContactPersonPhone(org.getContactPersonPhone());
        r.setSupportingDocumentUrl(org.getSupportingDocumentUrl());
        r.setLogoUrl(org.getLogoUrl());
        r.setStatus(org.getStatus());
        r.setRejectionReason(org.getRejectionReason());
        r.setCreatedAt(org.getCreatedAt());
        r.setUpdatedAt(org.getUpdatedAt());
        return r;
    }
}
