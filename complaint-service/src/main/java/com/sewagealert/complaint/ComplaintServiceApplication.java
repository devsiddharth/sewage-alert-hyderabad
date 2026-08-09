package com.sewagealert.complaint;

import com.sewagealert.complaint.config.RabbitMqProperties;
import com.sewagealert.complaint.config.UploadProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableDiscoveryClient
@EnableFeignClients
@EnableConfigurationProperties({RabbitMqProperties.class, UploadProperties.class})
// Complaint Service: Manages sewage complaint reporting, image storage, GPS coordinates, status tracking, and complaint history.
// Communicates with User Service (via OpenFeign) to validate users and enrich complaint data.
// Publishes domain events to RabbitMQ (notification.*) — consumed by the Notification Service.
public class ComplaintServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(ComplaintServiceApplication.class, args);
    }
}
