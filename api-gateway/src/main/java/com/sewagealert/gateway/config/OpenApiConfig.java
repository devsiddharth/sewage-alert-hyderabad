package com.sewagealert.gateway.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * OpenApiConfig: Springdoc/OpenAPI metadata for the API Gateway (WebFlux stack).
 * <p>
 * The gateway is the single entry point for all frontend traffic (port 8080).
 * Each backend microservice exposes its own Swagger UI and OpenAPI document
 * directly on its port; this configuration gives the gateway its own
 * {@code /v3/api-docs} + {@code /swagger-ui.html} describing the routing
 * surface. The bearerAuth scheme reflects the JWT flow the gateway fronts.
 * No secrets are declared here.
 */
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI apiGatewayOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Sewage Alert Hyderabad - API Gateway")
                        .version("1.0.0")
                        .description("Spring Cloud Gateway (port 8080) — single entry point routing to the "
                                + "backend microservices via Eureka service discovery: auth (/api/v1/auth/**), "
                                + "users (/api/v1/users/**), complaints (/api/v1/complaints/**), events, articles, "
                                + "ngos, pipelines, treatment-plants, lakes (/api/v1/.../**) and notifications "
                                + "(/api/v1/notifications/**). Each service's full OpenAPI documentation is "
                                + "available directly on its own port."))
                .components(new Components()
                        .addSecuritySchemes("bearerAuth", new SecurityScheme()
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                                .description("JWT issued by the Auth Service (POST /api/v1/auth/login).")));
    }
}
