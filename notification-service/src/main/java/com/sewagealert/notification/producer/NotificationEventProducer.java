package com.sewagealert.notification.producer;

import com.sewagealert.notification.config.RabbitMqProperties;
import com.sewagealert.notification.mapper.NotificationMapper;
import com.sewagealert.notification.model.Notification;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

// NotificationEventProducer: Re-publishes successfully stored notifications onto a DEDICATED
// delivery exchange (notification.delivery.exchange) with the delivery routing key.
//
// This is the extension point for future delivery channels: Email, SMS, and Push workers
// (Firebase Cloud Messaging, OneSignal, Azure Notification Hub, WebSockets, ...) each bind
// their own queue to the delivery exchange and fan out from there. Until such a worker
// exists, no queue is bound, so the event is dropped by the broker — in-app storage is
// unaffected. The delivery exchange is separate from notification.exchange so these events
// can never re-enter notification.queue (which would cause an endless store→republish loop).
@Component
@RequiredArgsConstructor
@Slf4j
public class NotificationEventProducer {

    private final RabbitTemplate rabbitTemplate;
    private final RabbitMqProperties properties;

    // publishStoredNotification: Fire-and-forget publish — a broker outage must never
    // break the primary "store + serve" flow, so failures are logged and swallowed.
    public void publishStoredNotification(Notification notification) {
        try {
            rabbitTemplate.convertAndSend(
                    properties.getDeliveryExchange(),
                    properties.getDeliveryRoutingKey(),
                    NotificationMapper.toEvent(notification)
            );
            log.info("Delivery event published for notification id: {} (routing key: {})",
                    notification.getId(), properties.getDeliveryRoutingKey());
        } catch (Exception ex) {
            log.error("Failed to publish delivery event for notification id: {}",
                    notification.getId(), ex);
        }
    }
}
