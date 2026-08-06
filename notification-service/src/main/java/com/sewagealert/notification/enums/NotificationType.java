package com.sewagealert.notification.enums;

// NotificationType: Categorizes every notification stored by this service.
// The string name of each constant is the event contract — producers must publish
// eventType values that exactly match these names (see NotificationEvent.eventType).
// Keep this enum extensible: adding a new type is all that is required to support a
// new event source, as long as the producer's eventType matches.
public enum NotificationType {

    COMPLAINT_CREATED,       // Citizen submitted a new complaint
    COMPLAINT_ASSIGNED,      // Authority picked up / was assigned the complaint
    COMPLAINT_STATUS_UPDATED,// Generic status change (e.g., IN_PROGRESS)
    COMPLAINT_RESOLVED,      // Complaint has been resolved
    COMPLAINT_REJECTED,      // Complaint was rejected
    COMPLAINT_REOPENED,      // Closed complaint was reopened

    COMMUNITY_EVENT,         // Event published / citizen registered for an event
    ARTICLE,                 // New educational article published (future-ready)

    SYSTEM,                  // System-level alerts
    ADMIN                    // Admin announcements (future-ready)
}
