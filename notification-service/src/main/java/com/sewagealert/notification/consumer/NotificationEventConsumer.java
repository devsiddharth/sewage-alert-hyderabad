package com.sewagealert.notification.consumer;

import com.sewagealert.notification.dto.NotificationEvent;
import com.sewagealert.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

// NotificationEventConsumer: Listens on notification.queue for domain events published by
// other microservices and persists them as notifications.
//
// Reliability model:
//   • acknowledge-mode: auto  — the container acks each message only after the listener returns.
//   • On success            → message acked, notification stored.
//   • On transient failure  → container-level retry with exponential backoff (see application.yml),
//                             then rejected with requeue=false.
//   • On permanent failure  → NotificationProcessingException rethrown; after the retry policy is
//                             exhausted the message is rejected and routed to notification.dlq.
//
// This design gives reliable delivery with automatic poison-message quarantine, without manual
// channel bookkeeping.
@Component
@RequiredArgsConstructor
@Slf4j
public class NotificationEventConsumer {

    private final NotificationService notificationService;

    @RabbitListener(queues = "${app.rabbitmq.queue}")
    // onNotificationEvent: Consumes a single JSON event and stores it. Exceptions propagate
    // so the container retry/DLQ machinery handles them.
    public void onNotificationEvent(NotificationEvent event) {
        log.info("RabbitMQ event received — eventId: {}, eventType: {}, userId: {}",
                event != null ? event.getEventId() : null,
                event != null ? event.getEventType() : null,
                event != null ? event.getUserId() : null);

        try {
            notificationService.processEvent(event);
        } catch (Exception ex) {
            log.error("Failed to process notification event — will be retried and, if persistent, " +
                    "routed to the dead letter queue", ex);
            throw ex;
        }
    }
}
