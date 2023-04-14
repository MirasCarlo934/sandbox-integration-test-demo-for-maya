package com.example.integrationtestdemoformaya.data.exceptions;

import com.example.integrationtestdemoformaya.domain.Person;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

public class PersonAlreadyExistsException extends RuntimeException {
    public PersonAlreadyExistsException(Person person) {
        super(String.format("Person '%s' already exists", person.name()));
    }
}
