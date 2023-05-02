package com.example.integrationtestdemoformaya.events.publisher;

import com.amazonaws.services.sns.AmazonSNS;
import com.example.integrationtestdemoformaya.events.dto.PersonCreatedEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import org.springframework.stereotype.Component;

@Component
public class PersonCreatedEventSnsPublisher extends SnsPublisher<PersonCreatedEvent> {
    private final String personSnsTopicArn;
    private final ObjectMapper objectMapper;

    public PersonCreatedEventSnsPublisher(AmazonSNS amazonSNS, String personSnsTopicArn, ObjectMapper objectMapper) {
        super(amazonSNS);
        this.personSnsTopicArn = personSnsTopicArn;
        this.objectMapper = objectMapper;
    }

    @Override
    @SneakyThrows
    void publish(PersonCreatedEvent event) {
        publish(personSnsTopicArn, objectMapper.writeValueAsString(event));
    }
}
