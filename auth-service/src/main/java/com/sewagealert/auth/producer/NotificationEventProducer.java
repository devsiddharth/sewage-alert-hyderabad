package com.sewagealert.auth.producer;

// NotificationEventProducer: Publishes user-auth domain events to RabbitMQ so the
// Notification Service can send transactional emails (verification / welcome) — the
// Auth Service never talks to EmailJS or any email provider directly.
public interface NotificationEventProducer {

    // publishUserRegistered: Called after a customer registers. The 6-digit verification code
    // travels in the event metadata so the Notification Service can include it in the email
    // (OTP-only verification — no verification link is used). The raw code is never logged.
    // Routing key: notification.user.registered — event type USER_REGISTERED
    void publishUserRegistered(Long userId, String name, String email, String verificationCode);

    // publishVerificationRequested: Called by the resend-verification endpoint — a distinct
    // event type (EMAIL_VERIFICATION_REQUESTED) so the domain model stays meaningful.
    // Routing key: notification.user.registered
    void publishVerificationRequested(Long userId, String name, String email, String verificationCode);

    // publishEmailVerified: Called after the customer successfully verifies their email.
    // Enables the optional welcome email. Routing key: notification.user.verified
    void publishEmailVerified(Long userId, String name, String email);
}
