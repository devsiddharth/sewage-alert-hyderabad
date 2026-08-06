package com.sewagealert.complaint.producer.impl;

import com.sewagealert.complaint.config.RabbitMqProperties;
import com.sewagealert.complaint.dto.NotificationEvent;
import com.sewagealert.complaint.model.Complaint;
import com.sewagealert.complaint.model.ComplaintStatus;
import com.sewagealert.complaint.producer.NotificationEventProducer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

// NotificationEventProducerImpl: Builds NotificationEvent payloads and publishes them to the
// notification.exchange topic exchange. Publishing is fire-and-forget: if RabbitMQ is down the
// event is logged and dropped, so complaint creation/status updates never fail because of the
// broker. (Guaranteed delivery via an outbox pattern is a documented future improvement.)
@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationEventProducerImpl implements NotificationEventProducer {

    // Routing keys — the wildcard binding (notification.*) on the consumer queue means new
    // keys require no RabbitMQ changes. Keep in sync with the Notification Service contract.
    private static final String RK_CREATED = "notification.created";
    private static final String RK_ASSIGNED = "notification.assigned";
    private static final String RK_STATUS_UPDATED = "notification.status.updated";
    private static final String RK_RESOLVED = "notification.resolved";
    private static final String RK_REJECTED = "notification.rejected";
    private static final String RK_REOPENED = "notification.reopened";

    private final RabbitTemplate rabbitTemplate;
    private final RabbitMqProperties properties;

    @Override
    public void publishComplaintCreated(Complaint complaint) {
        NotificationEvent event = NotificationEvent.builder()
                .eventId(UUID.randomUUID().toString())
                .eventType("COMPLAINT_CREATED")
                .userId(complaint.getCreatedBy())
                .complaintId(complaint.getId())
                .referenceType("COMPLAINT")
                .referenceId(complaint.getId())
                .title("Complaint Submitted")
                .message("Your complaint #" + complaint.getId() + " — \"" + complaint.getTitle()
                        + "\" has been submitted successfully. It is now pending authority review.")
                .status(complaint.getStatus().name())
                .priority(complaint.getPriority() != null ? complaint.getPriority().name() : null)
                .createdAt(LocalDateTime.now())
                .metadata(metadataOf(complaint, null))
                .build();

        publish(event, RK_CREATED);
    }

    @Override
    public void publishStatusChanged(Complaint complaint, ComplaintStatus previousStatus) {
        ComplaintStatus newStatus = complaint.getStatus();

        String eventType;
        String routingKey;
        String title;
        String message;

        if (newStatus == ComplaintStatus.RESOLVED) {
            eventType = "COMPLAINT_RESOLVED";
            routingKey = RK_RESOLVED;
            title = "Complaint Resolved";
            message = "Great news — your complaint #" + complaint.getId()
                    + " has been resolved." + remarksSuffix(complaint.getResolutionRemarks());
        } else if (newStatus == ComplaintStatus.REJECTED) {
            eventType = "COMPLAINT_REJECTED";
            routingKey = RK_REJECTED;
            title = "Complaint Rejected";
            message = "Your complaint #" + complaint.getId()
                    + " was rejected." + remarksSuffix(complaint.getResolutionRemarks());
        } else if (wasClosed(previousStatus)) {
            // RESOLVED/REJECTED -> PENDING/IN_PROGRESS means the complaint was reopened
            eventType = "COMPLAINT_REOPENED";
            routingKey = RK_REOPENED;
            title = "Complaint Reopened";
            message = "Your complaint #" + complaint.getId()
                    + " has been reopened and is being looked at again.";
        } else if (newStatus == ComplaintStatus.IN_PROGRESS && previousStatus == ComplaintStatus.PENDING) {
            // First IN_PROGRESS transition = an authority has taken ownership of the complaint
            eventType = "COMPLAINT_ASSIGNED";
            routingKey = RK_ASSIGNED;
            title = "Complaint Assigned";
            message = "An authority has picked up your complaint #" + complaint.getId()
                    + " and started working on it.";
        } else {
            eventType = "COMPLAINT_STATUS_UPDATED";
            routingKey = RK_STATUS_UPDATED;
            title = "Complaint Status Updated";
            message = "Your complaint #" + complaint.getId() + " is now " + newStatus + ".";
        }

        NotificationEvent event = NotificationEvent.builder()
                .eventId(UUID.randomUUID().toString())
                .eventType(eventType)
                .userId(complaint.getCreatedBy())
                .complaintId(complaint.getId())
                .referenceType("COMPLAINT")
                .referenceId(complaint.getId())
                .title(title)
                .message(message)
                .status(newStatus.name())
                .priority(complaint.getPriority() != null ? complaint.getPriority().name() : null)
                .createdAt(LocalDateTime.now())
                .metadata(metadataOf(complaint, previousStatus))
                .build();

        publish(event, routingKey);
    }

    // wasClosed: True when the complaint was previously in a terminal state
    private boolean wasClosed(ComplaintStatus status) {
        return status == ComplaintStatus.RESOLVED || status == ComplaintStatus.REJECTED;
    }

    private String remarksSuffix(String remarks) {
        return remarks != null && !remarks.isBlank() ? " Reason: " + remarks : "";
    }

    private Map<String, Object> metadataOf(Complaint complaint, ComplaintStatus previousStatus) {
        Map<String, Object> metadata = new HashMap<>();
        if (previousStatus != null) {
            metadata.put("previousStatus", previousStatus.name());
        }
        if (complaint.getResolutionRemarks() != null) {
            metadata.put("remarks", complaint.getResolutionRemarks());
        }
        if (complaint.getAssignedTo() != null) {
            metadata.put("assignedTo", complaint.getAssignedTo());
        }
        return metadata;
    }

    // publish: Fire-and-forget publish with failure logging — never throws to the caller
    private void publish(NotificationEvent event, String routingKey) {
        try {
            rabbitTemplate.convertAndSend(properties.getExchange(), routingKey, event);
            log.info("Notification event published — eventId: {}, type: {}, routingKey: {}, complaintId: {}",
                    event.getEventId(), event.getEventType(), routingKey, event.getComplaintId());
        } catch (Exception ex) {
            log.error("Failed to publish notification event — type: {}, complaintId: {}, routingKey: {}. " +
                            "Complaint flow continues (event will be lost).",
                    event.getEventType(), event.getComplaintId(), routingKey, ex);
        }
    }
}
