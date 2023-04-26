package com.example.integrationtestdemoformaya.core.exceptions;

import com.example.integrationtestdemoformaya.command.CreatePersonCommand;

public class PersonAddressAlreadyTakenException extends RuntimeException {
    public PersonAddressAlreadyTakenException(CreatePersonCommand command) {
        super(String.format("Address '%s' already taken", command.address()));
    }
}
