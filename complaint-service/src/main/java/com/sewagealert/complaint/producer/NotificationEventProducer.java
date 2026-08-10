package com.sewagealert.complaint.producer;

import com.sewagealert.complaint.model.Complaint;
import com.sewagealert.complaint.model.ComplaintStatus;

// NotificationEventProducer: Publishes complaint domain events to RabbitMQ so the
// Notification Service can store notifications — the Complaint Service never writes
// directly to the notification database.
public interface NotificationEventProducer {

    // publishComplaintCreated: Called after a complaint is saved — routing key notification.created
    void publishComplaintCreated(Complaint complaint);

    // publishStatusChanged: Called after a status update. Derives the correct event type
    // (ASSIGNED / STATUS_UPDATED / RESOLVED / REJECTED / REOPENED) from the previous vs new status.
    void publishStatusChanged(Complaint complaint, ComplaintStatus previousStatus);

    // publishComplaintAssigned: Called after an admin assigns a complaint to a field officer.
    // The notification is addressed to the officer (userId = fieldOfficerId) — routing key
    // notification.assigned, event type COMPLAINT_ASSIGNED.
    void publishComplaintAssigned(Complaint complaint, Long fieldOfficerId, Long assignedBy);
}
