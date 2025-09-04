package com.carocart.delivery.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.Contact;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI deliveryServiceOpenAPI() {
        return new OpenAPI()
            .info(new Info()
                .title("CaroCart Delivery Service API")
                .version("1.0.0")
                .description("API documentation for Delivery microservice: assignment, tracking, and operations.")
                .contact(new Contact()
                    .name("CaroCart Team")
                    .email("carocartteam@gmail.com")
                    .url("https://www.carocart.com")
                )
            );
    }
}
