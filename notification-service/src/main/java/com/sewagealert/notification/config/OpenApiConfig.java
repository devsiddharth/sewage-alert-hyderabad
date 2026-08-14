package com.sewagealert.notification.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * OpenApiConfig: Springdoc/OpenAPI metadata for the Notification Service.
 * <p>
 * Exposes Swagger UI at {@code /swagger-ui.html} and the OpenAPI document at
 * {@code /v3/api-docs}. The bearerAuth scheme mirrors the gateway-level JWT
 * authentication used in production. No secrets are declared here.
 */
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI notificationServiceOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Sewage Alert Hyderabad - Notification Service API")
                        .version("1.0.0")
                        .description("Notification retrieval APIs for Sewage Alert Hyderabad: paginated "
                                + "in-app notifications, unread counts and read-state management. Notifications "
                                + "are created by the RabbitMQ event consumer, not by callers."))
                .components(new Components()
                        .addSecuritySchemes("bearerAuth", new SecurityScheme()
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                                .description("JWT issued by the Auth Service (POST /api/v1/auth/login).")));
    }
}
