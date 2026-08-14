package com.sewagealert.community.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "NGO information create/update request")
public class NgoRequest {

    @Schema(description = "NGO name", example = "Green Hyderabad Trust")
    @NotBlank(message = "NGO name is required")
    private String name;

    @Schema(description = "Contact person name", example = "Anand Rao")
    private String contactPerson;

    @Schema(description = "Contact email", example = "contact@greenhyderabad.org")
    private String email;

    @Schema(description = "Contact phone", example = "9876543210")
    private String phone;

    @Schema(description = "Website URL", example = "https://greenhyderabad.org")
    private String website;

    @Schema(description = "Short description of the NGO's work")
    private String description;

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getContactPerson() { return contactPerson; }
    public void setContactPerson(String contactPerson) { this.contactPerson = contactPerson; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getWebsite() { return website; }
    public void setWebsite(String website) { this.website = website; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
}
