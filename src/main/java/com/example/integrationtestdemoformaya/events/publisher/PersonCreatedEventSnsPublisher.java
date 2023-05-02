package com.example.integrationtestdemoformaya.events.publisher;

import com.amazonaws.services.sns.AmazonSNS;
import com.example.integrationtestdemoformaya.events.dto.PersonCreatedEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;

public class PersonCreatedEventSnsPublisher extends SnsPublisher<PersonCreatedEvent> {
    private final String personsSnsTopicArn;
    private final ObjectMapper objectMapper;

    public PersonCreatedEventSnsPublisher(AmazonSNS amazonSNS, String personsSnsTopicArn, ObjectMapper objectMapper) {
        super(amazonSNS);
        this.personsSnsTopicArn = personsSnsTopicArn;
        this.objectMapper = objectMapper;
    }

    @Override
    @SneakyThrows
    public void publish(PersonCreatedEvent event) {
        publish(personsSnsTopicArn, objectMapper.writeValueAsString(event));
    }
}
