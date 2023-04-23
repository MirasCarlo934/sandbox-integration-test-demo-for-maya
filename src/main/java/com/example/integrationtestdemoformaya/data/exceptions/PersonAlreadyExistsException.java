package com.example.integrationtestdemoformaya.data.exceptions;

import com.example.integrationtestdemoformaya.web.request.PersonRequest;

public class PersonAlreadyExistsException extends RuntimeException {
    public PersonAlreadyExistsException(PersonRequest personRequest) {
        super(String.format("Person '%s' already exists", personRequest.name()));
    }
}
