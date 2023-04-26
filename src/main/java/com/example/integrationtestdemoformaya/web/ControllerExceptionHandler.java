package com.example.integrationtestdemoformaya.web;

import com.example.integrationtestdemoformaya.client.exceptions.ClientResponseException;
import com.example.integrationtestdemoformaya.config.ErrorResponseMapping;
import com.example.integrationtestdemoformaya.web.response.ErrorResponseBody;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.util.Map;

@ControllerAdvice
public class ControllerExceptionHandler {
    private static final String DEFAULT_MAPPING = "DEFAULT";

    private final Map<String, ErrorResponseMapping> errorMappings;

    public ControllerExceptionHandler(Map<String, ErrorResponseMapping> errorMappings) {
        this.errorMappings = errorMappings;
    }

    @ExceptionHandler(ClientResponseException.class)
    @ResponseStatus(HttpStatus.BAD_GATEWAY)
    protected ErrorResponseBody handleClientResponseException(ClientResponseException ex) {
        return new ErrorResponseBody(ex.getMessage());
    }

    @ExceptionHandler(RuntimeException.class)
    protected ResponseEntity<ErrorResponseBody> handleRuntimeExceptions(RuntimeException ex) {
        ErrorResponseMapping mapping = errorMappings.get(ex.getClass().getSimpleName());
        System.out.println(errorMappings.keySet());
        System.out.println(ex.getClass().getSimpleName());
        if (mapping == null) {
            mapping = errorMappings.get(DEFAULT_MAPPING);
        }
        return new ResponseEntity<>(new ErrorResponseBody(mapping.message()), HttpStatus.valueOf(mapping.status()));
    }
}
