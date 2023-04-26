package com.example.integrationtestdemoformaya.core.exceptions;

import com.example.integrationtestdemoformaya.command.CreatePersonCommand;

public class PersonAddressAlreadyTakenException extends RequestException {
    public PersonAddressAlreadyTakenException(CreatePersonCommand command) {
        super(String.format("Address '%s' already taken", command.address()), command.rrn(), command.channel());
    }
}
