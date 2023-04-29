package com.example.integrationtestdemoformaya.web;

import com.example.integrationtestdemoformaya.client.exceptions.ClientResponseException;
import com.example.integrationtestdemoformaya.config.ErrorResponseMapping;
import com.example.integrationtestdemoformaya.core.exceptions.RequestException;
import com.example.integrationtestdemoformaya.web.response.ErrorResponseBody;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.util.Map;

@ControllerAdvice
public class ControllerExceptionHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger(ControllerExceptionHandler.class);
    private static final String DEFAULT_MAPPING = "DEFAULT";

    private final Map<String, ErrorResponseMapping> errorMappings;

    public ControllerExceptionHandler(Map<String, ErrorResponseMapping> errorMappings) {
        this.errorMappings = errorMappings;
    }

    @ExceptionHandler(ClientResponseException.class)
    @ResponseStatus(HttpStatus.BAD_GATEWAY)
    protected ErrorResponseBody handleClientResponseException(ClientResponseException ex) {
        LOGGER.error("rrn: {}, channel: {}, exception: {}", ex.getRrn(), ex.getChannel(), ex.getClass().getSimpleName(), ex);
        return new ErrorResponseBody(ex.getMessage());
    }

    @ExceptionHandler(RequestException.class)
    protected ResponseEntity<ErrorResponseBody> handleRequestException(RequestException ex) {
        LOGGER.error("rrn: {}, channel: {}, exception: {}", ex.getRrn(), ex.getChannel(), ex.getClass().getSimpleName(), ex);
        ErrorResponseMapping mapping = errorMappings.get(ex.getClass().getSimpleName());
        String message = ex.getMessage();
        if (mapping == null) {
            mapping = errorMappings.get(DEFAULT_MAPPING);
            message = mapping.message();
        }
        return new ResponseEntity<>(new ErrorResponseBody(message), HttpStatus.valueOf(mapping.status()));
    }

    @ExceptionHandler(Exception.class)
    protected ResponseEntity<ErrorResponseBody> handleUncaughtException(Exception ex) {
        LOGGER.error("exception: {}", ex.getClass().getSimpleName(), ex);
        ErrorResponseMapping mapping = errorMappings.get(DEFAULT_MAPPING);
        return new ResponseEntity<>(new ErrorResponseBody(mapping.message()), HttpStatus.valueOf(mapping.status()));
    }
}
