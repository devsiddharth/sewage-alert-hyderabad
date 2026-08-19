package com.sewagealert.community.dto;

import com.sewagealert.community.model.NgoProgress;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Schema(description = "NGO measurable progress metrics")
public class NgoProgressResponse {

    private Long id;
    private Long ngoOrganizationId;
    private long complaintsAddressed;
    private long areasCovered;
    private long drivesConducted;
    private long eventsConducted;
    private long volunteersInvolved;
    private long peopleReached;
    private LocalDateTime updatedAt;

    public static NgoProgressResponse fromEntity(NgoProgress p) {
        NgoProgressResponse r = new NgoProgressResponse();
        r.setId(p.getId());
        r.setNgoOrganizationId(p.getNgoOrganizationId());
        r.setComplaintsAddressed(p.getComplaintsAddressed());
        r.setAreasCovered(p.getAreasCovered());
        r.setDrivesConducted(p.getDrivesConducted());
        r.setEventsConducted(p.getEventsConducted());
        r.setVolunteersInvolved(p.getVolunteersInvolved());
        r.setPeopleReached(p.getPeopleReached());
        r.setUpdatedAt(p.getUpdatedAt());
        return r;
    }
}
