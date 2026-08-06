package com.sewagealert.notification.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.DefaultJackson2JavaTypeMapper;
import org.springframework.amqp.support.converter.Jackson2JavaTypeMapper;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

// RabbitMqConfig: Declares the complete notification messaging topology.
//
//   Producer (Complaint Service etc.)            RabbitMQ                             Notification Service
//   ──────────────────────────────────           ────────                             ─────────────────────
//   publish(notification.created) ──────────►    notification.exchange (topic)
//                                                    │ notification.*
//                                                    ▼
//                                                notification.queue ──(reject after retries)──► notification.dlx ──► notification.dlq
//
//   Notification Service (republish)          ──► notification.delivery.exchange (topic)
//                                                    (no queues bound yet — future email/SMS/push workers)
//
// The main queue is durable, and dead-lettering is configured via queue arguments so that
// messages rejected after exhausting the retry policy are automatically parked in the DLQ.
@Configuration
@RequiredArgsConstructor
public class RabbitMqConfig {

    private final RabbitMqProperties properties;

    // ── Topology ────────────────────────────────────────────────────────────────

    @Bean
    public TopicExchange notificationExchange() {
        return ExchangeBuilder.topicExchange(properties.getExchange()).durable(true).build();
    }

    @Bean
    public Queue notificationQueue() {
        Map<String, Object> args = new HashMap<>();
        args.put("x-dead-letter-exchange", properties.getDlqExchange());
        args.put("x-dead-letter-routing-key", properties.getDlqRoutingKey());
        return QueueBuilder.durable(properties.getQueue()).withArguments(args).build();
    }

    @Bean
    public DirectExchange deadLetterExchange() {
        return ExchangeBuilder.directExchange(properties.getDlqExchange()).durable(true).build();
    }

    @Bean
    public Queue deadLetterQueue() {
        return QueueBuilder.durable(properties.getDlqQueue()).build();
    }

    @Bean
    public Binding notificationBinding(TopicExchange notificationExchange, Queue notificationQueue) {
        // Wildcard binding: catches every notification.* routing key, so new event types
        // are consumed without touching RabbitMQ configuration.
        return BindingBuilder.bind(notificationQueue).to(notificationExchange).with(properties.getRoutingKeyPattern());
    }

    @Bean
    public Binding deadLetterBinding(DirectExchange deadLetterExchange, Queue deadLetterQueue) {
        return BindingBuilder.bind(deadLetterQueue).to(deadLetterExchange).with(properties.getDlqRoutingKey());
    }

    // Delivery exchange: deliberately separate from notification.exchange so re-published
    // delivery events can never re-enter notification.queue (which would create an endless
    // consume→store→republish loop). Future channel workers bind their own queues here.
    @Bean
    public TopicExchange deliveryExchange() {
        return ExchangeBuilder.topicExchange(properties.getDeliveryExchange()).durable(true).build();
    }

    // ── Serialization & template ────────────────────────────────────────────────

    // Jackson2JsonMessageConverter: JSON payloads both ways. Reuses the Spring-managed
    // ObjectMapper so Java 8 time types (LocalDateTime) serialize/deserialize correctly.
    //
    // The type mapper is pinned to TypePrecedence.INFERRED: producers send a __TypeId__ header
    // naming THEIR event class (e.g. com.sewagealert.complaint.dto.NotificationEvent), which does
    // not exist on this classpath. INFERRED makes the converter use the @RabbitListener method
    // parameter type (this service's NotificationEvent) instead of the header.
    @Bean
    public MessageConverter messageConverter(ObjectMapper objectMapper) {
        Jackson2JsonMessageConverter converter = new Jackson2JsonMessageConverter(objectMapper);
        DefaultJackson2JavaTypeMapper typeMapper = new DefaultJackson2JavaTypeMapper();
        typeMapper.setTypePrecedence(Jackson2JavaTypeMapper.TypePrecedence.INFERRED);
        converter.setJavaTypeMapper(typeMapper);
        return converter;
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory, MessageConverter messageConverter) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(messageConverter);
        // Connection/Channel reuse is handled by the connection factory — a single
        // RabbitTemplate instance is shared by all producers in this service.
        return template;
    }
}
