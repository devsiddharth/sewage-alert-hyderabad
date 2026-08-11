package com.sewagealert.notification.service.impl;

import com.sewagealert.notification.client.EmailJsClient;
import com.sewagealert.notification.config.AppProperties;
import com.sewagealert.notification.config.EmailJsProperties;
import com.sewagealert.notification.dto.NotificationEvent;
import com.sewagealert.notification.service.EmailNotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

// EmailNotificationServiceImpl: Builds EmailJS template parameters from domain events and
// delegates the HTTP call to EmailJsClient.
//
// Failure model: every send is wrapped — a provider outage must never break the event
// pipeline or the customer's account. Failures are logged (with the eventId for tracing)
// and the customer can always use the resend-verification endpoint later. RabbitMQ retry /
// dead-lettering remains reserved for structural payload failures.
@Service
@RequiredArgsConstructor
@Slf4j
public class EmailNotificationServiceImpl implements EmailNotificationService {

    private final EmailJsClient emailJsClient;
    private final EmailJsProperties emailJsProperties;
    private final AppProperties appProperties;

    @Override
    public void sendVerificationEmail(NotificationEvent event) {
        Map<String, Object> metadata = event.getMetadata();

        String name = stringValue(metadata, "name");
        String email = stringValue(metadata, "email");
        String token = stringValue(metadata, "verificationToken");

        if (name == null || email == null || token == null) {
            log.warn("Verification email skipped — event {} is missing name/email/verificationToken metadata",
                    event.getEventId());
            return;
        }
        if (!isConfigured()) {
            log.warn("EmailJS not configured — skipping verification email for eventId {} "
                            + "(set EMAILJS_SERVICE_ID / EMAILJS_TEMPLATE_ID / EMAILJS_PUBLIC_KEY / EMAILJS_PRIVATE_KEY)",
                    event.getEventId());
            return;
        }

        String verificationLink = appProperties.getFrontendUrl() + "/verify-email?token=" + token;

        Map<String, Object> params = new HashMap<>();
        params.put("name", name);
        params.put("email", email);
        params.put("verification_link", verificationLink);

        try {
            emailJsClient.send(emailJsProperties.getTemplateId(), params);
            log.info("Verification email sent successfully — eventId: {}, userId: {}",
                    event.getEventId(), event.getUserId());
        } catch (Exception ex) {
            // Provider failure — do not rethrow: the notification is already stored and the
            // customer can resend. Logged with the eventId for correlation, never with the token.
            log.error("EmailJS verification email failed for eventId: {} — customer can use "
                    + "resend-verification", event.getEventId(), ex);
        }
    }

    @Override
    public void sendWelcomeEmail(NotificationEvent event) {
        String welcomeTemplateId = emailJsProperties.getWelcomeTemplateId();
        if (welcomeTemplateId == null || welcomeTemplateId.isBlank()) {
            log.debug("No welcome email template configured — skipping welcome email for eventId {}",
                    event.getEventId());
            return;
        }
        if (!isConfigured()) {
            log.warn("EmailJS not configured — skipping welcome email for eventId {}", event.getEventId());
            return;
        }

        Map<String, Object> metadata = event.getMetadata();
        String name = stringValue(metadata, "name");
        String email = stringValue(metadata, "email");

        Map<String, Object> params = new HashMap<>();
        params.put("name", name != null ? name : "there");
        params.put("email", email != null ? email : "");
        // CTA link for the welcome email's "Sign in" button — built from FRONTEND_URL
        params.put("login_url", appProperties.getFrontendUrl() + "/login");

        try {
            emailJsClient.send(welcomeTemplateId, params);
            log.info("Welcome email sent successfully — eventId: {}, userId: {}",
                    event.getEventId(), event.getUserId());
        } catch (Exception ex) {
            log.error("EmailJS welcome email failed for eventId: {}", event.getEventId(), ex);
        }
    }

    private boolean isConfigured() {
        return notBlank(emailJsProperties.getServiceId())
                && notBlank(emailJsProperties.getTemplateId())
                && notBlank(emailJsProperties.getPublicKey())
                && notBlank(emailJsProperties.getPrivateKey());
    }

    private boolean notBlank(String value) {
        return value != null && !value.isBlank();
    }

    private String stringValue(Map<String, Object> metadata, String key) {
        Object value = metadata != null ? metadata.get(key) : null;
        return value != null ? String.valueOf(value) : null;
    }
}
