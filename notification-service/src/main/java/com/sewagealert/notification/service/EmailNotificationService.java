package com.sewagealert.notification.service;

import com.sewagealert.notification.dto.NotificationEvent;

// EmailNotificationService: Owns outgoing transactional email. Consumed from the event
// pipeline — the Notification Service is the single place that talks to EmailJS, keeping
// email concerns out of the Auth/Complaint services and the frontend.
public interface EmailNotificationService {

    // sendVerificationEmail: Sends the "verify your email address" email using the
    // verification token carried in the event metadata to build the verification link.
    void sendVerificationEmail(NotificationEvent event);

    // sendWelcomeEmail: Sends the optional post-verification welcome email (only when a
    // welcome template is configured).
    void sendWelcomeEmail(NotificationEvent event);
}
