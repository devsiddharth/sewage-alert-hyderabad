package com.sewagealert.user;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
// @EnableDiscoveryClient: Registers this service with Eureka so the API Gateway can discover and route to it dynamically
@EnableDiscoveryClient
public class UserServiceApplication {

    public static void main(String[] args) {
        // Entry point: Boots up the User Service on the port defined in application.yml
        SpringApplication.run(UserServiceApplication.class, args);
    }
}
