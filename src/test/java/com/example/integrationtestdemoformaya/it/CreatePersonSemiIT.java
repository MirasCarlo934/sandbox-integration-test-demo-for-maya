package com.example.integrationtestdemoformaya.it;

import com.amazonaws.services.sns.AmazonSNS;
import com.example.integrationtestdemoformaya.TestFixtures;
import com.example.integrationtestdemoformaya.client.RestWalletClient;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.atMostOnce;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * <p>This integration test class leverages the Wiremock, PostgreSQL, and Localstock Docker containers that are running
 * locally.</p>
 *
 * <p>Before this test can be run, the following conditions must be met:</p>
 * <ol>
 *     <li>Run the Wiremock, PostgreSQL, and Localstack Docker containers</li>
 *     <li>Run setup.sh
 *     <ul>
 *         <li>Must have stubbed responses of external service calls prepared in src/test/resources/stubs</li>
 *     </ul>
 *     </li>
 * </ol>
 *
 * <p>Pros:</p>
 * <ul>
 *     <li>Quickest way to do "integration testing"</li>
 *     <li>Requires no additional environment configuration</li>
 * </ul>
 *
 * <p>Cons:</p>
 * <ul>
 *     <li>Not really an "integration test"
 *     <ul>
 *         <li>Only method calls are verified</li>
 *     </ul>
 *     </li>
 *     <li>Since existing instances are used, existing values may affect test results (eg. stored DB entries in manual testing)</li>
 *     <li>Each test case must ensure that the environment stays the same after testing</li>
 * </ul>
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles(value = "local")
class CreatePersonSemiIT {
    private static final String ENDPOINT_URL = "/persons";
    private static final String REQUEST_FILE = "src/test/resources/requests/create-person-request.json";

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @SpyBean
    private RestWalletClient restWalletClient;
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
        String rrn = UUID.randomUUID().toString();
        String channel = "channel";
        JsonNode requestJson = requestJson();

        MockHttpServletResponse response = mockMvc.perform(post(ENDPOINT_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson.toString())
                        .header(TestFixtures.HEADER_REQUEST_REFERENCE_NO, rrn)
                        .header(TestFixtures.HEADER_CHANNEL, channel))
                .andExpect(status().is(200))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andReturn().getResponse();

        JsonNode responseJson = objectMapper.readValue(response.getContentAsByteArray(), JsonNode.class);

        verify(restWalletClient).create(rrn, channel);

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
        String name = requestJson.get("name").textValue();
        String address = "Some Random Address PH";
        int age = 99;

        createExistingPersonInDb(name, address, age);

        mockMvc.perform(post(ENDPOINT_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson.toString())
                        .header(TestFixtures.HEADER_REQUEST_REFERENCE_NO, UUID.randomUUID().toString())
                        .header(TestFixtures.HEADER_CHANNEL, "channel"))
                .andExpect(status().is(400))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message").value(String.format("Person '%s' already exists", name)));

        verify(restWalletClient, never()).create(any(), any());
        verify(personRepository, atMostOnce()).save(any()); // createExistingPersonInDb() calls personRepository.save()
        verify(amazonSNS, never()).publish(any(), any());
    }

    @Test
    @Transactional
    void givenRequestWithPersonAddressAlreadyExists_whenCreatePerson_thenReturn400() throws Exception {
        JsonNode requestJson = requestJson();
        String name = "Some Random Name";
        String address = requestJson.get("address").textValue();
        int age = 99;

        createExistingPersonInDb(name, address, age);

        mockMvc.perform(post(ENDPOINT_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson.toString())
                        .header(TestFixtures.HEADER_REQUEST_REFERENCE_NO, UUID.randomUUID().toString())
                        .header(TestFixtures.HEADER_CHANNEL, "channel"))
                .andExpect(status().is(400))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message").value(String.format("Address '%s' already taken", address)));

        verify(restWalletClient, never()).create(any(), any());
        verify(personRepository, atMostOnce()).save(any()); // createExistingPersonInDb() calls personRepository.save()
        verify(amazonSNS, never()).publish(any(), any());
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
