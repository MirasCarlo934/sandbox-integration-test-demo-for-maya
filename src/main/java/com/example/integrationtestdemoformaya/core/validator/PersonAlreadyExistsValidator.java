package com.example.integrationtestdemoformaya.core.validator;

import com.example.integrationtestdemoformaya.command.CreatePersonCommand;
import com.example.integrationtestdemoformaya.data.PersonRepository;
import com.example.integrationtestdemoformaya.data.exceptions.PersonAlreadyExistsException;
import org.springframework.stereotype.Service;

@Service
public class PersonAlreadyExistsValidator {
    private final PersonRepository personRepository;

    public PersonAlreadyExistsValidator(PersonRepository personRepository) {
        this.personRepository = personRepository;
    }

    public void validate(CreatePersonCommand command) {
        if (personRepository.get(command.name()) != null) {
            throw new PersonAlreadyExistsException(command);
        }
    }
}
