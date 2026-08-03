package com.trackflow.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Swagger/OpenAPI Configuration for TrackFlow.
 *
 * <p>This class customizes the auto-generated API documentation.
 * Once configured, visit {@code /api/swagger-ui.html} to see all
 * endpoints with their request/response schemas.</p>
 *
 * <h3>Why document APIs?</h3>
 * <ul>
 *   <li>Frontend developers can integrate without reading backend code</li>
 *   <li>QA teams can test endpoints directly from the Swagger UI</li>
 *   <li>New team members can onboard faster</li>
 *   <li>It serves as a living contract between frontend and backend</li>
 * </ul>
 *
 * <h3>Interview Question:</h3>
 * <p>"What is the difference between Swagger and OpenAPI?"</p>
 * <p>Answer: OpenAPI is the specification (the standard).
 * Swagger is the toolset (UI, Codegen, Editor) that implements the spec.
 * Think of it like: OpenAPI = SQL, Swagger = MySQL Workbench.</p>
 */
@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI trackFlowOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("TrackFlow API")
                        .description("Intelligent Issue & Sprint Management Platform — REST API Documentation")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("TrackFlow Team")
                                .email("support@trackflow.io"))
                        .license(new License()
                                .name("MIT License")
                                .url("https://opensource.org/licenses/MIT")))
                // Configure JWT Bearer token in Swagger UI
                .addSecurityItem(new SecurityRequirement().addList("Bearer Authentication"))
                .components(new Components()
                        .addSecuritySchemes("Bearer Authentication",
                                new SecurityScheme()
                                        .type(SecurityScheme.Type.HTTP)
                                        .bearerFormat("JWT")
                                        .scheme("bearer")
                                        .description("Enter your JWT token")));
    }
}
