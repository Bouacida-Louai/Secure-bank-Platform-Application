package com.securebank.config;


import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI secureBankOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("SecureBank API")
                        .description("""
                    A production-grade Banking & Loan Management API
                    featuring:
                    - Multi-role RBAC with Keycloak + OAuth2
                    - Amount-aware permission engine
                    - ACID-compliant transaction processing
                    - Immutable audit trail
                    - Fraud detection engine
                    """)
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("Louai")
                                .email("louai@securebank.com"))
                )
                .addSecurityItem(new SecurityRequirement()
                        .addList("bearerAuth"))
                .components(new Components()
                        .addSecuritySchemes("bearerAuth",
                                new SecurityScheme()
                                        .name("bearerAuth")
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")
                                        .description("Paste your Keycloak JWT token here")
                        )
                );
    }
}