package com.example.integrationtestdemoformaya.config;

import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.sns.AmazonSNS;
import com.amazonaws.services.sns.AmazonSNSClientBuilder;
import com.example.integrationtestdemoformaya.events.dto.PersonCreatedEvent;
import com.example.integrationtestdemoformaya.events.publisher.PersonCreatedEventSnsPublisher;
import com.example.integrationtestdemoformaya.events.publisher.SnsPublisher;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class ApplicationConfig {
    private final String snsEndpoint;
    private final String snsRegion;
    private final String personsSnsTopicArn;
    private final ObjectMapper objectMapper;

    public ApplicationConfig(
            @Value("${aws.sns.endpoint}") String snsEndpoint,
            @Value("${aws.sns.region}") String snsRegion,
            @Value("${aws.sns.topic-arns.persons}") String personsSnsTopicArn,
            ObjectMapper objectMapper) {
        this.snsEndpoint = snsEndpoint;
        this.snsRegion = snsRegion;
        this.personsSnsTopicArn = personsSnsTopicArn;
        this.objectMapper = objectMapper;
    }

    @Bean
    public SnsPublisher<PersonCreatedEvent> personCreatedEventSnsPublisher() {
        return new PersonCreatedEventSnsPublisher(amazonSNS(), personsSnsTopicArn, objectMapper);
    }

    @Bean
    public WebClient walletWebClient() {
        return WebClient.builder()
                .baseUrl(walletServiceEndpointProperties().toUrl())
                .build();
    }

    @Bean
    public AmazonSNS amazonSNS() {
        return AmazonSNSClientBuilder.standard()
                .withEndpointConfiguration(new AwsClientBuilder.EndpointConfiguration(snsEndpoint, snsRegion))
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
