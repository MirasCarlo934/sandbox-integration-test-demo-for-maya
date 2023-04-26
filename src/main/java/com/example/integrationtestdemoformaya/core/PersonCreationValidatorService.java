package com.example.integrationtestdemoformaya.core;

import com.example.integrationtestdemoformaya.command.CreatePersonCommand;
import com.example.integrationtestdemoformaya.core.validator.Validator;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PersonCreationValidatorService {
    private final List<Validator<CreatePersonCommand>> validators;

    public PersonCreationValidatorService(List<Validator<CreatePersonCommand>> validators) {
        this.validators = validators;
    }

    public void validate(CreatePersonCommand command) {
        for (Validator<CreatePersonCommand> validator : validators) {
            validator.validate(command);
        }
    }
}
