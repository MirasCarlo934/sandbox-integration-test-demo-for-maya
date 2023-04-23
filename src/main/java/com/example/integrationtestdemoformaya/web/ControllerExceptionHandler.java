package com.example.integrationtestdemoformaya.web;

import com.example.integrationtestdemoformaya.client.exceptions.ClientResponseException;
import com.example.integrationtestdemoformaya.web.response.ErrorResponseBody;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;

@ControllerAdvice
public class ControllerExceptionHandler {
    @ExceptionHandler(ClientResponseException.class)
    @ResponseStatus(HttpStatus.BAD_GATEWAY)
    protected ErrorResponseBody handleClientResponseException(ClientResponseException ex) {
        return new ErrorResponseBody(ex.getMessage());
    }
}
