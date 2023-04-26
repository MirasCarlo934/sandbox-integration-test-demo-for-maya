package com.example.integrationtestdemoformaya.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class ApplicationConfig {
    @Bean
    public WebClient walletWebClient() {
        return WebClient.builder()
                .baseUrl(walletServiceEndpointProperties().toUrl())
                .build();
    }

    @Bean
    @ConfigurationProperties(prefix = "uri.wallet")
    public EndpointProperties walletServiceEndpointProperties() {
        return new EndpointProperties();
    }

    @Bean
    @ConfigurationProperties(prefix = "response.error-mappings")
    public Map<String, ErrorResponseMapping> errorMappings() {
        return new HashMap<>();
    }
}
