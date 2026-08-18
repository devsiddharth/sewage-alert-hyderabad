package com.sewagealert.complaint.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * OpenApiConfig: Springdoc/OpenAPI metadata for the Complaint Service.
 * <p>
 * Exposes Swagger UI at {@code /swagger-ui.html} and the OpenAPI document at
 * {@code /v3/api-docs}. The bearerAuth scheme mirrors the gateway-level JWT
 * authentication used in production. No secrets are declared here.
 */
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI complaintServiceOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Sewage Alert Hyderabad - Complaint Service API")
                        .version("1.0.0")
                        .description("Complaint management APIs for Sewage Alert Hyderabad: "
                                + "citizen complaint creation with image uploads, status tracking, "
                                + "admin assignment and field-officer workflows."))
                .components(new Components()
                        .addSecuritySchemes("bearerAuth", new SecurityScheme()
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                                .description("JWT issued by the Auth Service (POST /api/v1/auth/login).")));
    }
}
