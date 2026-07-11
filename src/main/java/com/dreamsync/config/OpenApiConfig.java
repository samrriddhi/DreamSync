package com.dreamsync.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI dreamSyncOpenAPI() {

        return new OpenAPI()
                .info(new Info()
                        .title("DreamSync API")
                        .version("v1.0.0")
                        .description("A Spring Boot REST API for project management, dependency analysis, impact analysis, and dashboard analytics.")
                        .contact(new Contact()
                                .name("Samriddhi Chauhan")
                                .email("samriddhichauhan27@gamil.com"))
                        .license(new License()
                                .name("MIT License")));
    }
}