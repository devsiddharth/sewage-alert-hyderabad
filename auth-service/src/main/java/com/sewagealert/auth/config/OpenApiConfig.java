package com.sewagealert.auth.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * OpenApiConfig: Springdoc/OpenAPI metadata for the Auth Service.
 * <p>
 * Exposes Swagger UI at {@code /swagger-ui.html} and the OpenAPI document at
 * {@code /v3/api-docs}. Registers the JWT bearer security scheme so protected
 * endpoints can be tested from Swagger UI via the Authorize button. No secrets
 * are declared here — the JWT is pasted by the developer, never configured.
 */
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI authServiceOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Sewage Alert Hyderabad - Auth Service API")
                        .version("1.0.0")
                        .description("Authentication and authorization APIs for Sewage Alert Hyderabad: "
                                + "registration with OTP email verification, login (JWT issuance), profile "
                                + "retrieval and field-officer management."))
                .components(new Components()
                        .addSecuritySchemes("bearerAuth", new SecurityScheme()
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                                .description("JWT returned by POST /api/v1/auth/login — authorize with: Bearer <token>")));
    }
}
