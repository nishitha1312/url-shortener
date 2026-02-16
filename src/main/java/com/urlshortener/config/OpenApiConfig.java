package com.urlshortener.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI urlShortenerOpenAPI() {
        return new OpenAPI()
            .info(new Info()
                .title("URL Shortener API")
                .version("1.0.0")
                .description("Scalable URL shortening service with analytics, rate limiting, and expiration support.")
                .contact(new Contact()
                    .name("URL Shortener Team")
                    .url("https://github.com/your-username/url-shortener"))
                .license(new License()
                    .name("MIT")
                    .url("https://opensource.org/licenses/MIT")));
    }
}
