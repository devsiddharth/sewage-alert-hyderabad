package com.sewagealert.community.dto;

import com.sewagealert.community.model.Ngo;
import java.time.LocalDateTime;

public class NgoResponse {

    private Long id;
    private String name;
    private String contactPerson;
    private String email;
    private String phone;
    private String website;
    private String description;
    private LocalDateTime createdAt;

    public NgoResponse() {}

    public static NgoResponse fromEntity(Ngo ngo) {
        NgoResponse response = new NgoResponse();
        response.setId(ngo.getId());
        response.setName(ngo.getName());
        response.setContactPerson(ngo.getContactPerson());
        response.setEmail(ngo.getEmail());
        response.setPhone(ngo.getPhone());
        response.setWebsite(ngo.getWebsite());
        response.setDescription(ngo.getDescription());
        response.setCreatedAt(ngo.getCreatedAt());
        return response;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

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

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
