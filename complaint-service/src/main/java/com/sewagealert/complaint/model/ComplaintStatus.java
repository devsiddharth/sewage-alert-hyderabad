package com.sewagealert.complaint.model;

// ComplaintStatus: Tracks the lifecycle of a complaint through the resolution process
public enum ComplaintStatus {
    PENDING,      // Complaint submitted — awaiting authority review
    IN_PROGRESS,  // Authority has acknowledged and is working on it
    RESOLVED,     // Issue has been fixed
    REJECTED      // Complaint was deemed invalid or duplicate
}
