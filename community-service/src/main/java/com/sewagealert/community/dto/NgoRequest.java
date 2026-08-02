package com.sewagealert.community.dto;

import jakarta.validation.constraints.NotBlank;

public class NgoRequest {

    @NotBlank(message = "NGO name is required")
    private String name;

    private String contactPerson;
    private String email;
    private String phone;
    private String website;
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
