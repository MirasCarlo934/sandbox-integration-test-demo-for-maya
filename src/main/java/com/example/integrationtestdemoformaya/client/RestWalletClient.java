package com.example.integrationtestdemoformaya.client;

import com.example.integrationtestdemoformaya.client.exceptions.ClientResponseException;
import com.example.integrationtestdemoformaya.domain.Wallet;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

@Component
public class RestWalletClient {
    private final WebClient walletWebClient;

    public RestWalletClient(WebClient walletWebClient) {
        this.walletWebClient = walletWebClient;
    }

    public Wallet create() {
        try {
            return walletWebClient.post()
                    .contentType(MediaType.APPLICATION_JSON)
                    .retrieve()
                    .bodyToMono(Wallet.class)
                    .block();
        } catch (WebClientResponseException e) {
            throw new ClientResponseException(String.format("Response %s received from Wallet service", e.getStatusCode()));
        }
    }
}
