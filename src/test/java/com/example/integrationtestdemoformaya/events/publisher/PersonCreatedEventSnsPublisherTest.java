package com.example.integrationtestdemoformaya.events.publisher;

import com.amazonaws.services.sns.AmazonSNS;
import com.example.integrationtestdemoformaya.TestFixtures;
import com.example.integrationtestdemoformaya.events.dto.PersonCreatedEvent;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.sql.Date;
import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class PersonCreatedEventSnsPublisherTest {
    private static final String TOPIC_ARN = "topic-arn";

    private final AmazonSNS mockAmazonSNS = mock(AmazonSNS.class);
    private final ObjectMapper objectMapper = TestFixtures.objectMapper;

    private final PersonCreatedEventSnsPublisher publisherToTest = new PersonCreatedEventSnsPublisher(
            mockAmazonSNS,
            TOPIC_ARN,
            objectMapper);

    @Test
    void givenValidEvent_whenPublish_thenPublishSuccessfully() throws JsonProcessingException {
        PersonCreatedEvent event = new PersonCreatedEvent(
                UUID.randomUUID().toString(),
                "channel",
                Date.from(Instant.now()),
                UUID.randomUUID(),
                "Name McName",
                "Address PH",
                UUID.randomUUID(),
                99);

        publisherToTest.publish(event);

        ArgumentCaptor<String> topicCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> payloadCaptor = ArgumentCaptor.forClass(String.class);
        verify(mockAmazonSNS).publish(topicCaptor.capture(), payloadCaptor.capture());
        assertEquals(TOPIC_ARN, topicCaptor.getValue());

        JsonNode payloadJson = objectMapper.readValue(payloadCaptor.getValue(), JsonNode.class);
        assertEquals(event.rrn(), payloadJson.get("rrn").asText());
        assertEquals(event.channel(), payloadJson.get("channel").asText());
        assertTrue(payloadJson.get("createdDate").isNumber());
        assertEquals(event.createdDate().toInstant().toEpochMilli(), payloadJson.get("createdDate").asLong());
        assertEquals(event.id().toString(), payloadJson.get("id").asText());
        assertEquals(event.name(), payloadJson.get("name").asText());
        assertEquals(event.address(), payloadJson.get("address").asText());
        assertEquals(event.walletId().toString(), payloadJson.get("walletId").asText());
        assertTrue(payloadJson.get("age").isNumber());
        assertEquals(event.age(), payloadJson.get("age").asInt());
    }
}