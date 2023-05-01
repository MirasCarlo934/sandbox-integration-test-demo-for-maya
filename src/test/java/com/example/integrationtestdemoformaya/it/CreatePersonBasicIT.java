package com.example.integrationtestdemoformaya.it;

import com.example.integrationtestdemoformaya.data.PersonRepository;
import com.example.integrationtestdemoformaya.domain.Person;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
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

    @Captor
    private ArgumentCaptor<Person> personCaptor;

    @Test
    @Transactional
    void givenValidRequest_whenCreatePerson_thenRespondWithCreatedPerson() throws Exception {
        JsonNode requestJson = requestJson();

        mockMvc.perform(post(ENDPOINT_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson.toString())
                        .header("request-reference-no", UUID.randomUUID().toString())
                        .header("channel", "channel"))
                .andExpect(status().is(200))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(result -> {
                    JsonNode responseJson = objectMapper.readValue(result.getResponse().getContentAsByteArray(), JsonNode.class);

                    verify(personRepository).save(personCaptor.capture());
                    Person savedPerson = personCaptor.getValue();

                    assertDoesNotThrow(() -> savedPerson.getId().equals(UUID.fromString(responseJson.get("id").asText())));
                    assertDoesNotThrow(() -> savedPerson.getWalletId().equals(UUID.fromString(responseJson.get("walletId").asText())));
                    assertEquals(requestJson.get("name").asText(), responseJson.get("name").asText());
                    assertEquals(requestJson.get("address").asText(), responseJson.get("address").asText());
                    assertEquals(requestJson.get("age").asInt(), responseJson.get("age").asInt());
                });
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
