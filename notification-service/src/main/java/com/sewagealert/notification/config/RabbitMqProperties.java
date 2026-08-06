package com.sewagealert.notification.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * RabbitMqProperties: Configuration for the notification messaging topology.
 * <p>
 * Bound from the {@code app.rabbitmq} prefix in application.yml
 * (replaces scattered {@code @Value} annotations — same convention as the
 * community-service external API properties).
 */
@Getter
@Setter
@ConfigurationProperties(prefix = "app.rabbitmq")
public class RabbitMqProperties {

    /** Topic exchange carrying all notification domain events. */
    private String exchange = "notification.exchange";

    /** Main consumer queue — bound to the exchange with the routing key pattern. */
    private String queue = "notification.queue";

    /** Dead letter exchange — receives rejected messages from the main queue. */
    private String dlqExchange = "notification.dlx";

    /** Dead letter queue — holds permanently failed messages for inspection/replay. */
    private String dlqQueue = "notification.dlq";

    /** Routing key used to bind the DLQ to the DLX. */
    private String dlqRoutingKey = "notification.dlq";

    /** Topic wildcard binding: every {@code notification.*} event routes to the main queue. */
    private String routingKeyPattern = "notification.*";

    /** Dedicated exchange for re-published "delivery" events. Kept separate from the main
     *  exchange so delivery events can NEVER re-enter notification.queue (no wildcard binding). */
    private String deliveryExchange = "notification.delivery.exchange";

    /** Routing key used by this service when re-publishing stored notifications for
     *  future delivery channels (email/SMS/push workers bind a queue with this key). */
    private String deliveryRoutingKey = "notification.delivered";
}
