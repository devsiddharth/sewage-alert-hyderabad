package com.sewagealert.community.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * NgoApplicationRequest: DTO for submitting an NGO application for verification.
 */
@Data
@Schema(description = "NGO application / organization registration request")
public class NgoApplicationRequest {

    @Schema(description = "Organization name", example = "Green Hyderabad Trust")
    @NotBlank(message = "Organization name is required")
    private String organizationName;

    @Schema(description = "Official email", example = "contact@greenhyderabad.org")
    @NotBlank(message = "Official email is required")
    @Email(message = "Official email must be valid")
    private String officialEmail;

    @Schema(description = "Official phone", example = "9876543210")
    private String officialPhone;

    @Schema(description = "Registration number", example = "NGO/2024/12345")
    private String registrationNumber;

    @Schema(description = "Registration details / certificate info")
    private String registrationDetails;

    @Schema(description = "Website URL", example = "https://greenhyderabad.org")
    private String website;

    @Schema(description = "Address", example = "Banjara Hills, Hyderabad")
    private String address;

    @Schema(description = "Operating areas (comma-separated)", example = "Miyapur, Kondapur, Gachibowli")
    private String operatingAreas;

    @Schema(description = "Mission statement")
    private String mission;

    @Schema(description = "Areas of focus (comma-separated)", example = "Water conservation, Sewage awareness")
    private String areasOfFocus;

    @Schema(description = "Communities served")
    private String communitiesServed;

    @Schema(description = "Authorized representative name")
    private String contactPersonName;

    @Schema(description = "Representative email")
    private String contactPersonEmail;

    @Schema(description = "Representative phone")
    private String contactPersonPhone;
}
