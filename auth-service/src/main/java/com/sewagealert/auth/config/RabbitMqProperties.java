package com.sewagealert.auth.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * RabbitMqProperties: Configuration for the notification event publishing.
 * <p>
 * Bound from the {@code app.rabbitmq} prefix in application.yml. The exchange name must
 * match the one declared by the Notification Service (notification.exchange).
 */
@Getter
@Setter
@ConfigurationProperties(prefix = "app.rabbitmq")
public class RabbitMqProperties {

    /** Topic exchange carrying all notification domain events (declared by Notification Service). */
    private String exchange = "notification.exchange";
}
