package com.sewagealert.complaint.model;

// ComplaintPriority: Determines the urgency level — set by authorities to prioritize resource allocation
public enum ComplaintPriority {
    LOW,      // Minor issue — no immediate health or safety risk
    MEDIUM,   // Moderate issue — needs attention
    HIGH,     // Significant issue — affects public health or safety
    CRITICAL  // Emergency — immediate action required (e.g., sewage overflow on main road)
}
