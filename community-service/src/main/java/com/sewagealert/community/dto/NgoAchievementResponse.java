package com.sewagealert.community.dto;

import com.sewagealert.community.model.NgoAchievement;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Schema(description = "NGO achievement response")
public class NgoAchievementResponse {

    private Long id;
    private String title;
    private String description;
    private LocalDate date;
    private String evidence;
    private String images;
    private Long ngoOrganizationId;
    private LocalDateTime createdAt;

    public static NgoAchievementResponse fromEntity(NgoAchievement a) {
        NgoAchievementResponse r = new NgoAchievementResponse();
        r.setId(a.getId());
        r.setTitle(a.getTitle());
        r.setDescription(a.getDescription());
        r.setDate(a.getDate());
        r.setEvidence(a.getEvidence());
        r.setImages(a.getImages());
        r.setNgoOrganizationId(a.getNgoOrganizationId());
        r.setCreatedAt(a.getCreatedAt());
        return r;
    }
}
