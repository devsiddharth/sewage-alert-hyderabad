package com.sewagealert.notification.service.impl;

import com.sewagealert.notification.dto.NotificationEvent;
import com.sewagealert.notification.model.Notification;
import com.sewagealert.notification.producer.NotificationEventProducer;
import com.sewagealert.notification.repository.NotificationRepository;
import com.sewagealert.notification.service.EmailNotificationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.HashMap;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * NotificationServiceImplTest: The shared event pipeline stores every valid event and
 * additionally dispatches auth events to the email channel (EmailJS). Complaint events
 * must NOT trigger email dispatch (they have no email channel yet).
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class NotificationServiceImplTest {

    @Mock private NotificationRepository notificationRepository;
    @Mock private NotificationEventProducer notificationEventProducer;
    @Mock private EmailNotificationService emailNotificationService;

    private NotificationServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new NotificationServiceImpl(notificationRepository, notificationEventProducer,
                emailNotificationService);
        when(notificationRepository.save(any(Notification.class)))
                .thenAnswer(inv -> inv.getArgument(0));
    }

    private NotificationEvent event(String eventType) {
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("name", "Siddhartha");
        metadata.put("email", "customer@example.com");
        metadata.put("verificationCode", "123456");
        return NotificationEvent.builder()
                .eventId("event-" + eventType)
                .eventType(eventType)
                .userId(7L)
                .title("Verify your email address")
                .message("Welcome to Sewage Alert Hyderabad!")
                .metadata(metadata)
                .build();
    }

    @Test
    void userRegisteredEventStoresNotificationAndSendsVerificationEmail() {
        NotificationEvent event = event("USER_REGISTERED");

        service.processEvent(event);

        verify(notificationRepository).save(any(Notification.class));
        verify(emailNotificationService).sendVerificationEmail(event);
        verify(emailNotificationService, never()).sendWelcomeEmail(any());
        verify(notificationEventProducer).publishStoredNotification(any());
    }

    @Test
    void verificationRequestedEventSendsVerificationEmail() {
        NotificationEvent event = event("EMAIL_VERIFICATION_REQUESTED");

        service.processEvent(event);

        verify(emailNotificationService).sendVerificationEmail(event);
        verify(emailNotificationService, never()).sendWelcomeEmail(any());
    }

    @Test
    void emailVerifiedEventSendsWelcomeEmail() {
        NotificationEvent event = event("EMAIL_VERIFIED");

        service.processEvent(event);

        verify(emailNotificationService).sendWelcomeEmail(event);
        verify(emailNotificationService, never()).sendVerificationEmail(any());
    }

    @Test
    void complaintEventDoesNotTriggerEmailDispatch() {
        NotificationEvent event = NotificationEvent.builder()
                .eventId("event-complaint")
                .eventType("COMPLAINT_CREATED")
                .userId(7L)
                .complaintId(42L)
                .title("Complaint Submitted")
                .message("Your complaint #42 has been submitted.")
                .metadata(new HashMap<>())
                .build();

        service.processEvent(event);

        verify(emailNotificationService, never()).sendVerificationEmail(any());
        verify(emailNotificationService, never()).sendWelcomeEmail(any());
        verify(notificationRepository).save(any(Notification.class));
    }
}
