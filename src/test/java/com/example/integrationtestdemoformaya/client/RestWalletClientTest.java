package com.example.integrationtestdemoformaya.client;

import com.example.integrationtestdemoformaya.TestFixtures;
import com.example.integrationtestdemoformaya.client.exceptions.ClientResponseException;
import com.example.integrationtestdemoformaya.domain.Wallet;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;

import java.io.IOException;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class RestWalletClientTest {
    private final MockWebServer mockWalletServiceServer = new MockWebServer();
    private final ObjectMapper objectMapper = TestFixtures.OBJECT_MAPPER;

    private RestWalletClient clientToTest;

    @BeforeEach
    void setUp() throws IOException {
        mockWalletServiceServer.start();
        WebClient mockWebClient = WebClient.builder()
                .baseUrl(String.format("http://%s:%s", mockWalletServiceServer.getHostName(), mockWalletServiceServer.getPort()))
                .build();
        clientToTest = new RestWalletClient(mockWebClient);
    }

    @Test
    void givenValidRrnAndChannel_whenCreate_thenReturnCreatedWallet() throws JsonProcessingException {
        Wallet expectedWallet = new Wallet(UUID.randomUUID());

        mockWalletServiceServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                .setBody(objectMapper.writeValueAsString(expectedWallet)));

        Wallet createdWallet = assertDoesNotThrow(() -> clientToTest.create("rrn", "channel"));
        assertEquals(expectedWallet, createdWallet);
    }

    @ParameterizedTest
    @EnumSource(value = HttpStatus.class, names = {"BAD_REQUEST", "INTERNAL_SERVER_ERROR"})
    void givenValidRrnAndChannel_whenCreateAndEncounteredErrorResponse_thenThrowClientResponseException(HttpStatusCode errorCode) {
        mockWalletServiceServer.enqueue(new MockResponse()
                .setResponseCode(errorCode.value()));

        ClientResponseException ex = assertThrows(ClientResponseException.class, () -> clientToTest.create("rrn", "channel"));
        assertEquals(String.format("Response %s received from Wallet service", errorCode), ex.getMessage());
    }
}