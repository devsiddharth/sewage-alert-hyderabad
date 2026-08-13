package com.sewagealert.auth.producer.impl;

import com.sewagealert.auth.config.RabbitMqProperties;
import com.sewagealert.auth.dto.NotificationEvent;
import com.sewagealert.auth.producer.NotificationEventProducer;
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
// event is logged and dropped, so registration/verification never fail because of the broker.
// (Guaranteed delivery via an outbox pattern is a documented future improvement.)
//
// The raw 6-digit verification code is intentionally included here — the Notification Service
// needs it to render the OTP inside the verification email. It is NOT logged anywhere.
@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationEventProducerImpl implements NotificationEventProducer {

    // Routing keys — the consumer queue binding (notification.#) on the Notification Service
    // matches every key under notification.*, so new keys require no RabbitMQ changes.
    // Keep in sync with the Notification Service contract.
    private static final String RK_USER_REGISTERED = "notification.user.registered";
    private static final String RK_USER_VERIFIED = "notification.user.verified";

    private final RabbitTemplate rabbitTemplate;
    private final RabbitMqProperties properties;

    @Override
    public void publishUserRegistered(Long userId, String name, String email, String verificationCode) {
        publishAuthEvent("USER_REGISTERED", userId, name, email, verificationCode, RK_USER_REGISTERED);
    }

    @Override
    public void publishVerificationRequested(Long userId, String name, String email, String verificationCode) {
        publishAuthEvent("EMAIL_VERIFICATION_REQUESTED", userId, name, email, verificationCode, RK_USER_REGISTERED);
    }

    private void publishAuthEvent(String eventType, Long userId, String name, String email,
                                  String verificationCode, String routingKey) {
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("name", name);
        metadata.put("email", email);
        metadata.put("verificationCode", verificationCode);

        NotificationEvent event = NotificationEvent.builder()
                .eventId(UUID.randomUUID().toString())
                .eventType(eventType)
                .userId(userId)
                .title("Verify your email address")
                .message("Welcome to Sewage Alert Hyderabad! Enter the 6-digit code from the "
                        + "email to activate your account — it expires in 30 minutes.")
                .createdAt(LocalDateTime.now())
                .metadata(metadata)
                .build();

        publish(event, routingKey);
    }

    @Override
    public void publishEmailVerified(Long userId, String name, String email) {
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("name", name);
        metadata.put("email", email);

        NotificationEvent event = NotificationEvent.builder()
                .eventId(UUID.randomUUID().toString())
                .eventType("EMAIL_VERIFIED")
                .userId(userId)
                .title("Email verified")
                .message("Your email address has been verified. Welcome to Sewage Alert Hyderabad!")
                .createdAt(LocalDateTime.now())
                .metadata(metadata)
                .build();

        publish(event, RK_USER_VERIFIED);
    }

    // publish: Fire-and-forget publish with failure logging — never throws to the caller
    private void publish(NotificationEvent event, String routingKey) {
        try {
            rabbitTemplate.convertAndSend(properties.getExchange(), routingKey, event);
            log.info("Notification event published — eventId: {}, type: {}, routingKey: {}, userId: {}",
                    event.getEventId(), event.getEventType(), routingKey, event.getUserId());
        } catch (Exception ex) {
            log.error("Failed to publish notification event — type: {}, userId: {}, routingKey: {}. "
                            + "Auth flow continues (event will be lost).",
                    event.getEventType(), event.getUserId(), routingKey, ex);
        }
    }
}
