package com.example.integrationtestdemoformaya.core.exceptions;

import com.example.integrationtestdemoformaya.command.CreatePersonCommand;

public class PersonAlreadyExistsException extends RuntimeException {
    public PersonAlreadyExistsException(CreatePersonCommand command) {
        super(String.format("Person '%s' already exists", command.name()));
    }
}
