package com.example.integrationtestdemoformaya.core.exceptions;

import lombok.Getter;

public abstract class RequestException extends RuntimeException {
    @Getter
    private final String rrn;
    @Getter
    private final String channel;

    protected RequestException(String message, String rrn, String channel) {
        super(message);
        this.rrn = rrn;
        this.channel = channel;
    }
}
