package com.email.writer.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.config.CorsRegistry;
import org.springframework.web.reactive.config.WebFluxConfigurer;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebConfig {

    @Bean
    public WebClient.Builder webClientBuilder() {
        return WebClient.builder();
    }

    @Bean
    public WebFluxConfigurer corsConfigurer() {

        return new WebFluxConfigurer() {

            @Override
            public void addCorsMappings(CorsRegistry registry) {

                registry.addMapping("/**")
                        .allowedOriginPatterns("https://mail.google.com", "chrome-extension://*")
                        .allowedMethods("POST", "OPTIONS")
                        .allowedHeaders("Content-Type");
            }
        };
    }
}
