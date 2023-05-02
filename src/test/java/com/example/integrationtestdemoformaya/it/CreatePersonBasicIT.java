package com.example.integrationtestdemoformaya.it;

import com.amazonaws.services.sns.AmazonSNS;
import com.example.integrationtestdemoformaya.data.PersonRepository;
import com.example.integrationtestdemoformaya.domain.Person;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles(value = "local")
class CreatePersonBasicIT {
    private static final String ENDPOINT_URL = "/persons";
    private static final String REQUEST_FILE = "src/test/resources/requests/create-person-request.json";

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @SpyBean
    private PersonRepository personRepository;
    @SpyBean
    private AmazonSNS amazonSNS;
    @Value("${aws.sns.topic-arns.persons}")
    private String personsTopic;

    @Captor
    private ArgumentCaptor<String> snsMessagePayloadCaptor;
    @Captor
    private ArgumentCaptor<Person> personCaptor;

    @Test
    @Transactional
    void givenValidRequest_whenCreatePerson_thenRespondWithCreatedPerson() throws Exception {
        JsonNode requestJson = requestJson();

        MockHttpServletResponse response = mockMvc.perform(post(ENDPOINT_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson.toString())
                        .header("request-reference-no", UUID.randomUUID().toString())
                        .header("channel", "channel"))
                .andExpect(status().is(200))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andReturn().getResponse();

        JsonNode responseJson = objectMapper.readValue(response.getContentAsByteArray(), JsonNode.class);

        verify(personRepository).save(personCaptor.capture());
        Person savedPerson = personCaptor.getValue();
        assertDoesNotThrow(() -> savedPerson.getId().equals(UUID.fromString(responseJson.get("id").textValue())));
        assertDoesNotThrow(() -> savedPerson.getWalletId().equals(UUID.fromString(responseJson.get("walletId").textValue())));
        assertEquals(requestJson.get("name").textValue(), responseJson.get("name").textValue());
        assertEquals(requestJson.get("address").textValue(), responseJson.get("address").textValue());
        assertEquals(requestJson.get("age").intValue(), responseJson.get("age").intValue());

        verify(amazonSNS).publish(eq(personsTopic), snsMessagePayloadCaptor.capture());
        JsonNode messagePayloadJson = objectMapper.readValue(snsMessagePayloadCaptor.getValue(), JsonNode.class);
        assertEquals(savedPerson.getId().toString(), messagePayloadJson.get("id").textValue());
        assertEquals(savedPerson.getWalletId().toString(), messagePayloadJson.get("walletId").textValue());
        assertEquals(savedPerson.getName(), messagePayloadJson.get("name").textValue());
        assertEquals(savedPerson.getAddress(), messagePayloadJson.get("address").textValue());
        assertEquals(savedPerson.getAge(), messagePayloadJson.get("age").intValue());
    }

    @Test
    @Transactional
    void givenRequestWithPersonNameAlreadyExists_whenCreatePerson_thenReturn400() throws Exception {
        JsonNode requestJson = requestJson();
        String name = requestJson.get("name").asText();
        String address = "Some Random Address PH";
        int age = 99;

        createExistingPersonInDb(name, address, age);

        mockMvc.perform(post(ENDPOINT_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson.toString())
                        .header("request-reference-no", UUID.randomUUID().toString())
                        .header("channel", "channel"))
                .andExpect(status().is(400))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message").value(String.format("Person '%s' already exists", name)));
    }

    @Test
    @Transactional
    void givenRequestWithPersonAddressAlreadyExists_whenCreatePerson_thenReturn400() throws Exception {
        JsonNode requestJson = requestJson();
        String name = "Some Random Name";
        String address = requestJson.get("address").asText();
        int age = 99;

        createExistingPersonInDb(name, address, age);

        mockMvc.perform(post(ENDPOINT_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson.toString())
                        .header("request-reference-no", UUID.randomUUID().toString())
                        .header("channel", "channel"))
                .andExpect(status().is(400))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message").value(String.format("Address '%s' already taken", address)));
    }

    @Transactional
    protected void createExistingPersonInDb(String name, String address, int age) {
        personRepository.save(new Person(
                UUID.randomUUID(),
                name,
                address,
                age,
                UUID.randomUUID()));
    }

    private JsonNode requestJson() throws IOException {
        return objectMapper.readValue(new File(REQUEST_FILE), JsonNode.class);
    }
}
