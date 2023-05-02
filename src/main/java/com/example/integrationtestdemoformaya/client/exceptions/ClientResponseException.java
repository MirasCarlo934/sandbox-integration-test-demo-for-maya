package com.example.integrationtestdemoformaya.client.exceptions;

import lombok.EqualsAndHashCode;
import lombok.Value;

@Value
@EqualsAndHashCode(callSuper = true)
public class ClientResponseException extends RuntimeException {
    String rrn;
    String channel;

    public ClientResponseException(String message, String rrn, String channel, Throwable cause) {
        super(message, cause);
        this.rrn = rrn;
        this.channel = channel;
    }
}
