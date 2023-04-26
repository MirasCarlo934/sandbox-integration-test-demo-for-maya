package com.example.integrationtestdemoformaya.data.exceptions;

import com.example.integrationtestdemoformaya.command.CreatePersonCommand;

public class PersonAlreadyExistsException extends RuntimeException {
    public PersonAlreadyExistsException(CreatePersonCommand createPersonCommand) {
        super(String.format("Person '%s' already exists", createPersonCommand.name()));
    }
}
