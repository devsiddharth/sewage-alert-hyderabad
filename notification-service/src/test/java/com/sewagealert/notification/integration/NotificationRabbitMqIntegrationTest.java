package com.sewagealert.notification.integration;

import com.sewagealert.notification.client.EmailJsClient;
import com.sewagealert.notification.dto.NotificationEvent;
import com.sewagealert.notification.model.Notification;
import com.sewagealert.notification.repository.NotificationRepository;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.amqp.core.AmqpAdmin;
import org.springframework.amqp.core.QueueInformation;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.testcontainers.containers.RabbitMQContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BooleanSupplier;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

/**
 * NotificationRabbitMqIntegrationTest: End-to-end smoke test over a REAL RabbitMQ broker
 * (Testcontainers — the image is the same management image used by docker-compose).
 *
 * Boots the full Notification Service context (real RabbitAdmin topology declaration,
 * real @RabbitListener consumers, real JPA persistence on an in-memory DB) and verifies
 * the whole pipeline for one registration:
 *
 *   publish(notification.user.registered) ──► notification.exchange ──► notification.queue
 *        ──► NotificationEventConsumer ──► processEvent ──► sendVerificationEmail ──► EmailJS
 *
 * The routing key is the EXACT 3-word key the Auth Service publishes. This doubles as a
 * regression guard for the queue binding: with the old single-star binding
 * (notification.*) this key is unroutable, the message is silently dropped, and the
 * assertions below time out.
 *
 * Docker requirement: the test needs a Testcontainers-compatible Docker daemon (Linux
 * daemon or Docker Desktop with working docker-java). When no such daemon is reachable
 * the whole test class is SKIPPED (never fails the build) — see
 * disabledWithoutDocker = true below.
 */
@Testcontainers(disabledWithoutDocker = true)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE, properties = {
        // No Eureka / service discovery in tests
        "eureka.client.enabled=false",
        "spring.cloud.discovery.enabled=false",
        "spring.cloud.service-registry.auto-registration.enabled=false",
        // In-memory database instead of the local MySQL instance
        "spring.datasource.url=jdbc:h2:mem:notificationtest;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.jpa.show-sql=false",
        "spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.H2Dialect",
        // Non-blank EmailJS credentials so EmailNotificationServiceImpl.isConfigured() passes;
        // the EmailJsClient itself is mocked — no real emails are ever sent.
        "emailjs.service-id=test-service",
        "emailjs.template-id=template_w4koj8i",
        "emailjs.public-key=test-public-key",
        "emailjs.private-key=test-private-key"
})
class NotificationRabbitMqIntegrationTest {

    private static final String EXCHANGE = "notification.exchange";
    private static final String QUEUE = "notification.queue";
    private static final String DLQ = "notification.dlq";
    private static final String TEMPLATE_ID = "template_w4koj8i";
    // The exact routing key the Auth Service publishes on registration (3 words — only the
    // notification.# binding routes it).
    private static final String ROUTING_KEY = "notification.user.registered";

    @Container
    @ServiceConnection
    // Pinned to the same image tag as notification-service/docker-compose.yml for reproducibility.
    static final RabbitMQContainer RABBITMQ = new RabbitMQContainer("rabbitmq:3.13-management");

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Autowired
    private AmqpAdmin amqpAdmin;

    @Autowired
    private NotificationRepository notificationRepository;

    @MockitoBean
    private EmailJsClient emailJsClient;

    @Test
    void userRegisteredEventIsConsumedAndTriggersEmailDispatch() throws Exception {
        // 1. Wait until the app's RabbitAdmin has declared the topology (queue + binding).
        //    Publishing before the binding exists would silently drop the message.
        awaitUntil(() -> amqpAdmin.getQueueInfo(QUEUE) != null, Duration.ofSeconds(30),
                "RabbitMQ topology (queue '" + QUEUE + "') was not declared in time");

        // 2. Publish the exact event contract + routing key the Auth Service sends on registration.
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("name", "Integration User");
        metadata.put("email", "it-user@example.com");
        metadata.put("verificationCode", "424242");

        NotificationEvent event = NotificationEvent.builder()
                .eventId("it-" + System.currentTimeMillis())
                .eventType("USER_REGISTERED")
                .userId(99L)
                .title("Verify your email address")
                .message("Welcome to Sewage Alert Hyderabad! Enter the 6-digit code from the email.")
                .createdAt(LocalDateTime.now())
                .metadata(metadata)
                .build();

        rabbitTemplate.convertAndSend(EXCHANGE, ROUTING_KEY, event);

        // 3. The listener must consume it and dispatch the verification email to EmailJS.
        awaitUntil(() -> {
            try {
                verify(emailJsClient).send(eq(TEMPLATE_ID), any());
                return true;
            } catch (AssertionError notCalledYet) {
                return false;
            }
        }, Duration.ofSeconds(30), "Verification email was not dispatched to EmailJS in time");

        // 4. The dispatched template params must carry the OTP — and no verification link.
        ArgumentCaptor<Map<String, Object>> paramsCaptor = ArgumentCaptor.forClass(Map.class);
        verify(emailJsClient).send(eq(TEMPLATE_ID), paramsCaptor.capture());
        Map<String, Object> params = paramsCaptor.getValue();
        assertThat(params.get("name")).isEqualTo("Integration User");
        assertThat(params.get("email")).isEqualTo("it-user@example.com");
        assertThat(params.get("verification_code")).isEqualTo("424242");
        assertThat(params).doesNotContainKey("verification_link");

        // 5. processEvent completed → the notification was persisted (which also means the
        //    listener returned and the message was acknowledged).
        awaitUntil(() -> notificationRepository.count() > 0, Duration.ofSeconds(10),
                "Notification was not persisted in time");

        // 5b. The raw verification code must NEVER be persisted — stripVerificationSecrets
        //     removes it before the event is stored.
        Notification persisted = notificationRepository.findAll().iterator().next();
        assertThat(persisted.getMetadata()).doesNotContainKeys("verificationCode");

        // 6. The message was fully consumed: main queue drained and nothing dead-lettered.
        awaitUntil(() -> {
            QueueInformation queueInfo = amqpAdmin.getQueueInfo(QUEUE);
            QueueInformation dlqInfo = amqpAdmin.getQueueInfo(DLQ);
            return queueInfo != null && queueInfo.getMessageCount() == 0
                    && dlqInfo != null && dlqInfo.getMessageCount() == 0;
        }, Duration.ofSeconds(10), "Message was not fully consumed and acknowledged");
    }

    private static void awaitUntil(BooleanSupplier condition, Duration timeout, String message)
            throws InterruptedException {
        long deadline = System.currentTimeMillis() + timeout.toMillis();
        while (System.currentTimeMillis() < deadline) {
            if (condition.getAsBoolean()) {
                return;
            }
            Thread.sleep(200);
        }
        throw new AssertionError(message);
    }
}
