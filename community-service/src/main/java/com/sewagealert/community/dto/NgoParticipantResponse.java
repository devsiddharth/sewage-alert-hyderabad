package com.sewagealert.community.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * NgoParticipantResponse: Minimal participant info exposed to NGOs.
 * Never exposes passwords, auth info, or unnecessary personal data.
 */
@Data
@Schema(description = "Participant info (minimal, privacy-safe)")
public class NgoParticipantResponse {

    private Long userId;
    private String name;
    private String email;
    private String registrationStatus;
    private String attendanceStatus;

    public NgoParticipantResponse() {}

    public NgoParticipantResponse(Long userId, String name, String email,
                                   String registrationStatus, String attendanceStatus) {
        this.userId = userId;
        this.name = name;
        this.email = email;
        this.registrationStatus = registrationStatus;
        this.attendanceStatus = attendanceStatus;
    }
}
