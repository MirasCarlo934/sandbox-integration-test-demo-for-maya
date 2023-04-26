package com.example.integrationtestdemoformaya.client;

import com.example.integrationtestdemoformaya.client.exceptions.ClientResponseException;
import com.example.integrationtestdemoformaya.domain.Wallet;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import static com.example.integrationtestdemoformaya.config.Constants.CHANNEL;
import static com.example.integrationtestdemoformaya.config.Constants.REQUEST_REFERENCE_NO;

@Component
public class RestWalletClient {
    private final WebClient walletWebClient;

    public RestWalletClient(WebClient walletWebClient) {
        this.walletWebClient = walletWebClient;
    }

    public Wallet create(String rrn, String channel) {
        try {
            return walletWebClient.post()
                    .contentType(MediaType.APPLICATION_JSON)
                    .header(REQUEST_REFERENCE_NO, rrn)
                    .header(CHANNEL, channel)
                    .retrieve()
                    .bodyToMono(Wallet.class)
                    .block();
        } catch (WebClientResponseException e) {
            throw new ClientResponseException(String.format("Response %s received from Wallet service", e.getStatusCode()), rrn, channel);
        }
    }
}
