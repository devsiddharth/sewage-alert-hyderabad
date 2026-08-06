package com.sewagealert.complaint.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.core.ExchangeBuilder;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

// RabbitMqConfig: Producer-side RabbitMQ setup for the Complaint Service.
// Only the exchange is declared here (idempotent — the Notification Service declares the same
// exchange plus its queues). Queues are owned by the consumer; producers never bind queues.
//
//   Complaint Service ──publish(notification.*)──► notification.exchange (topic)
//                                                        │ notification.*
//                                                        ▼
//                                              notification.queue (Notification Service)
@Configuration
@RequiredArgsConstructor
public class RabbitMqConfig {

    private final RabbitMqProperties properties;

    @Bean
    public TopicExchange notificationExchange() {
        // Durable: survives broker restarts. Topic: allows per-event-type routing keys.
        return ExchangeBuilder.topicExchange(properties.getExchange()).durable(true).build();
    }

    // Jackson2JsonMessageConverter: JSON payloads both ways. Reuses the Spring-managed
    // ObjectMapper so Java 8 time types (LocalDateTime) serialize/deserialize correctly.
    @Bean
    public MessageConverter messageConverter(ObjectMapper objectMapper) {
        return new Jackson2JsonMessageConverter(objectMapper);
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory, MessageConverter messageConverter) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(messageConverter);
        return template;
    }
}
