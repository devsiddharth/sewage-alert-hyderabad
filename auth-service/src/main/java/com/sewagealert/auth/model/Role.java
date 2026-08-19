package com.sewagealert.auth.model;

public enum Role {
    CITIZEN,
    AUTHORITY,
    ADMIN,
    FIELD_OFFICER,  // Field staff who receive assigned complaints and update their status
    NGO_REPRESENTATIVE  // Verified representative of an NGO organization (v2.0.0)
}
