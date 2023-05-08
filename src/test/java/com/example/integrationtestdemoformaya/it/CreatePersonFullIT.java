package com.example.integrationtestdemoformaya.it;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.AmazonSQSClientBuilder;
import com.amazonaws.services.sqs.model.Message;
import com.example.integrationtestdemoformaya.TestFixtures;
import com.example.integrationtestdemoformaya.data.PersonRepository;
import com.example.integrationtestdemoformaya.domain.Person;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.junit5.WireMockRuntimeInfo;
import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.localstack.LocalStackContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.shaded.org.bouncycastle.util.Strings;
import org.testcontainers.utility.DockerImageName;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.UUID;

import static com.example.integrationtestdemoformaya.TestFixtures.OBJECT_MAPPER;
import static com.example.integrationtestdemoformaya.it.CreatePersonFullIT.WIREMOCK_PORT;
import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.atMostOnce;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.testcontainers.containers.localstack.LocalStackContainer.Service.SNS;
import static org.testcontainers.containers.localstack.LocalStackContainer.Service.SQS;

@SpringBootTest
@WireMockTest(httpPort = WIREMOCK_PORT)
@Testcontainers
@AutoConfigureMockMvc
@ActiveProfiles(value = "local-it")
class CreatePersonFullIT {
    public static final int WIREMOCK_PORT = 9999;
    private static final String SQS_PERSONS_QUEUE = "persons-queue";
    private static final String ENDPOINT_URL = "/persons";
    private static final String REQUEST_FILE = "src/test/resources/requests/create-person-request.json";
    private static final String CREATE_NEW_WALLET_RESPONSE_FILE = "src/test/resources/responses/walletservice-create-new-wallet-200.json";

    @Container
    private static final LocalStackContainer localStackContainer = new LocalStackContainer(DockerImageName.parse("localstack/localstack:latest"))
            .withServices(SNS, SQS);
    private static AmazonSQS mockAmazonSQS;

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @SpyBean
    private PersonRepository personRepository;

    @Captor
    private ArgumentCaptor<Person> personCaptor;

    @DynamicPropertySource
    static void overrideConfiguration(DynamicPropertyRegistry registry) {
        registry.add("aws.sns.region", localStackContainer::getRegion);
        registry.add("aws.sns.endpoint", () -> localStackContainer.getEndpointOverride(SNS));
    }

    @BeforeAll
    static void beforeAll() throws IOException, InterruptedException {
        String awsRegion = localStackContainer.getRegion();
        String snsPersonsTopicArn = String.format("arn:aws:sns:%s:000000000000:persons-sns-topic", awsRegion);

        localStackContainer.execInContainer(Strings.split(String.format("awslocal sqs create-queue --queue-name=%s", SQS_PERSONS_QUEUE), ' '));

        localStackContainer.execInContainer(Strings.split("awslocal sns create-topic --name persons-sns-topic", ' '));
        localStackContainer.execInContainer(Strings.split("awslocal sns subscribe" +
                String.format(" --topic-arn=%s", snsPersonsTopicArn) +
                " --protocol=sqs" +
                String.format(" --notification-endpoint=arn:aws:sqs:%s:000000000000:%s", awsRegion, SQS_PERSONS_QUEUE), ' '));

        mockAmazonSQS = AmazonSQSClientBuilder
                .standard()
                .withEndpointConfiguration(new AwsClientBuilder.EndpointConfiguration(
                        localStackContainer.getEndpointOverride(SQS).toString(),
                        localStackContainer.getRegion()))
                .withCredentials(new AWSStaticCredentialsProvider(new BasicAWSCredentials(
                        localStackContainer.getAccessKey(),
                        localStackContainer.getSecretKey())))
                .build();
    }

    @BeforeEach
    void setUp(WireMockRuntimeInfo wireMockRuntimeInfo) throws IOException {
        WireMock wireMock = wireMockRuntimeInfo.getWireMock();

        JsonNode createNewWalletResponse = OBJECT_MAPPER.readValue(new File(CREATE_NEW_WALLET_RESPONSE_FILE), JsonNode.class);
        wireMock.register(stubFor(WireMock
                .post("/wallets")
                .willReturn(aResponse()
                        .withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                        .withBody(createNewWalletResponse.toString()))));
    }

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

        WireMock.verify(postRequestedFor(urlEqualTo("/wallets"))
                .withHeader(TestFixtures.HEADER_REQUEST_REFERENCE_NO, equalTo(rrn))
                .withHeader(TestFixtures.HEADER_CHANNEL, equalTo(channel)));

        verify(personRepository).save(personCaptor.capture());
        Person savedPerson = personCaptor.getValue();
        assertDoesNotThrow(() -> savedPerson.getId().equals(UUID.fromString(responseJson.get("id").textValue())));
        assertDoesNotThrow(() -> savedPerson.getWalletId().equals(UUID.fromString(responseJson.get("walletId").textValue())));
        assertEquals(requestJson.get("name").textValue(), responseJson.get("name").textValue());
        assertEquals(requestJson.get("address").textValue(), responseJson.get("address").textValue());
        assertEquals(requestJson.get("age").intValue(), responseJson.get("age").intValue());

        List<Message> sqsReceivedMessages = mockAmazonSQS.receiveMessage(mockAmazonSQS.getQueueUrl(SQS_PERSONS_QUEUE).getQueueUrl()).getMessages();
        assertEquals(1, sqsReceivedMessages.size());
        String messageBody = objectMapper.readValue(sqsReceivedMessages.get(0).getBody(), JsonNode.class).get("Message").textValue();
        JsonNode messagePayloadJson = objectMapper.readValue(messageBody, JsonNode.class);
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

        WireMock.verify(0, postRequestedFor(urlEqualTo("/wallets")));
        verify(personRepository, atMostOnce()).save(any()); // createExistingPersonInDb() calls personRepository.save()
        assertTrue(mockAmazonSQS.receiveMessage(mockAmazonSQS.getQueueUrl(SQS_PERSONS_QUEUE).getQueueUrl()).getMessages().isEmpty());
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

        WireMock.verify(0, postRequestedFor(urlEqualTo("/wallets")));
        verify(personRepository, atMostOnce()).save(any()); // createExistingPersonInDb() calls personRepository.save()
        assertTrue(mockAmazonSQS.receiveMessage(mockAmazonSQS.getQueueUrl(SQS_PERSONS_QUEUE).getQueueUrl()).getMessages().isEmpty());
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
