package com.sewagealert.complaint;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableDiscoveryClient
@EnableFeignClients
// Complaint Service: Manages sewage complaint reporting, image storage, GPS coordinates, status tracking, and complaint history.
// Communicates with User Service (via OpenFeign) to validate users and enrich complaint data.
public class ComplaintServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(ComplaintServiceApplication.class, args);
    }
}
