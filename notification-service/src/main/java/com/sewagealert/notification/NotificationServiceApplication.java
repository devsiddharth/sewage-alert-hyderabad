package com.sewagealert.notification;

import com.sewagealert.notification.config.AppProperties;
import com.sewagealert.notification.config.EmailJsProperties;
import com.sewagealert.notification.config.RabbitMqProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableDiscoveryClient
@EnableFeignClients
@EnableConfigurationProperties({RabbitMqProperties.class, EmailJsProperties.class, AppProperties.class})
// Notification Service: Consumes domain events from RabbitMQ, stores notifications, and exposes
// REST APIs for retrieving them. Fully event-driven — no other service writes directly to this database.
public class NotificationServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(NotificationServiceApplication.class, args);
    }
}
