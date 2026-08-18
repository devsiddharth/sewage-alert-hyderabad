package com.sewagealert.user.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * OpenApiConfig: Springdoc/OpenAPI metadata for the User Service.
 * <p>
 * Exposes Swagger UI at {@code /swagger-ui.html} and the OpenAPI document at
 * {@code /v3/api-docs}. The bearerAuth scheme mirrors the gateway-level JWT
 * authentication used in production. No secrets are declared here.
 */
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI userServiceOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Sewage Alert Hyderabad - User Service API")
                        .version("1.0.0")
                        .description("User profile management APIs for Sewage Alert Hyderabad: "
                                + "create, retrieve, update and delete citizen profiles."))
                .components(new Components()
                        .addSecuritySchemes("bearerAuth", new SecurityScheme()
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                                .description("JWT issued by the Auth Service (POST /api/v1/auth/login).")));
    }
}
