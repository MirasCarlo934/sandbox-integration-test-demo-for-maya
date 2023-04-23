package com.example.integrationtestdemoformaya.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class ApplicationConfig {
    @Bean
    public WebClient walletWebClient() {
        System.out.println(walletServiceEndpointProperties().toUrl());
        return WebClient.builder()
                .baseUrl(walletServiceEndpointProperties().toUrl())
                .build();
    }

    @Bean
    @ConfigurationProperties(prefix = "uri.wallet")
    public EndpointProperties walletServiceEndpointProperties() {
        return new EndpointProperties();
    }
}
