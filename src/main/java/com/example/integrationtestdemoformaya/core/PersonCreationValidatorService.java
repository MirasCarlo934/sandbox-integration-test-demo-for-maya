package com.example.integrationtestdemoformaya.core;

import com.example.integrationtestdemoformaya.command.CreatePersonCommand;
import com.example.integrationtestdemoformaya.core.validator.PersonAlreadyExistsValidator;
import org.springframework.stereotype.Service;

@Service
public class PersonCreationValidatorService {
    private final PersonAlreadyExistsValidator personAlreadyExistsValidator;

    public PersonCreationValidatorService(PersonAlreadyExistsValidator personAlreadyExistsValidator) {
        this.personAlreadyExistsValidator = personAlreadyExistsValidator;
    }

    public void validate(CreatePersonCommand command) {
        personAlreadyExistsValidator.validate(command);
    }
}
