package com.sewagealert.notification.service.impl;

import com.sewagealert.notification.client.EmailJsClient;
import com.sewagealert.notification.config.AppProperties;
import com.sewagealert.notification.config.EmailJsProperties;
import com.sewagealert.notification.dto.NotificationEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * EmailNotificationServiceImplTest: Verifies the correct EmailJS template parameters are
 * passed for the OTP-only verification flow (name / email / verification_code — NO
 * verification link), that a missing code skips the send, and that EmailJS failures
 * never propagate (no real emails are sent — client is mocked).
 */
@ExtendWith(MockitoExtension.class)
class EmailNotificationServiceImplTest {

    @Mock private EmailJsClient emailJsClient;

    private EmailJsProperties emailJsProperties;
    private AppProperties appProperties;
    private EmailNotificationServiceImpl service;

    @BeforeEach
    void setUp() {
        emailJsProperties = new EmailJsProperties();
        emailJsProperties.setServiceId("service_2mr35tu");
        emailJsProperties.setTemplateId("template_w4koj8i");
        emailJsProperties.setPublicKey("public-key");
        emailJsProperties.setPrivateKey("private-key");

        appProperties = new AppProperties();
        appProperties.setFrontendUrl("http://localhost:5173");

        service = new EmailNotificationServiceImpl(emailJsClient, emailJsProperties, appProperties);
    }

    private NotificationEvent registeredEvent() {
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("name", "Siddhartha");
        metadata.put("email", "customer@example.com");
        metadata.put("verificationCode", "123456");
        return NotificationEvent.builder()
                .eventId("event-1")
                .eventType("USER_REGISTERED")
                .userId(7L)
                .metadata(metadata)
                .build();
    }

    @Test
    void verificationEmailPassesNameEmailAndOtpOnly() {
        service.sendVerificationEmail(registeredEvent());

        ArgumentCaptor<Map<String, Object>> paramsCaptor = ArgumentCaptor.forClass(Map.class);
        verify(emailJsClient).send(eq("template_w4koj8i"), paramsCaptor.capture());

        Map<String, Object> params = paramsCaptor.getValue();
        assertThat(params.get("name")).isEqualTo("Siddhartha");
        assertThat(params.get("email")).isEqualTo("customer@example.com");
        // OTP-only verification — the 6-digit code renders with {{verification_code}}
        assertThat(params.get("verification_code")).isEqualTo("123456");
        // No verification link is sent — the token-link mechanism is not part of this flow
        assertThat(params).doesNotContainKey("verification_link");
    }

    @Test
    void emailJsFailureIsSwallowedSoEventPipelineSurvives() {
        doThrow(new RuntimeException("EmailJS provider outage"))
                .when(emailJsClient).send(any(), any());

        assertThatCode(() -> service.sendVerificationEmail(registeredEvent()))
                .doesNotThrowAnyException();
    }

    @Test
    void verificationEmailSkippedWhenEmailJsNotConfigured() {
        emailJsProperties.setPrivateKey(null);

        service.sendVerificationEmail(registeredEvent());

        verify(emailJsClient, never()).send(any(), any());
    }

    @Test
    void verificationEmailSkippedWhenCodeMissingFromMetadata() {
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("name", "Siddhartha");
        metadata.put("email", "customer@example.com");

        service.sendVerificationEmail(NotificationEvent.builder()
                .eventId("event-2").userId(7L).metadata(metadata).build());

        verify(emailJsClient, never()).send(any(), any());
    }

    @Test
    void welcomeEmailSentOnlyWhenWelcomeTemplateConfigured() {
        // No welcome template configured → no send
        service.sendWelcomeEmail(registeredEvent());
        verify(emailJsClient, never()).send(any(), any());

        // Configured → send with name/email/login_url params
        emailJsProperties.setWelcomeTemplateId("template_welcome");

        service.sendWelcomeEmail(registeredEvent());

        ArgumentCaptor<Map<String, Object>> paramsCaptor = ArgumentCaptor.forClass(Map.class);
        verify(emailJsClient).send(eq("template_welcome"), paramsCaptor.capture());
        assertThat(paramsCaptor.getValue().get("name")).isEqualTo("Siddhartha");
        assertThat(paramsCaptor.getValue().get("email")).isEqualTo("customer@example.com");
        // CTA link built from the configured FRONTEND_URL
        assertThat(paramsCaptor.getValue().get("login_url")).isEqualTo("http://localhost:5173/login");
    }
}
