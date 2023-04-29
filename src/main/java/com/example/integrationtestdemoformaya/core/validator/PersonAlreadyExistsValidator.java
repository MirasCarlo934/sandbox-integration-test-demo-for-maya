package com.example.integrationtestdemoformaya.core.validator;

import com.example.integrationtestdemoformaya.command.CreatePersonCommand;
import com.example.integrationtestdemoformaya.data.PersonRepository;
import com.example.integrationtestdemoformaya.core.exceptions.PersonAlreadyExistsException;
import org.springframework.stereotype.Component;

@Component
public class PersonAlreadyExistsValidator implements Validator<CreatePersonCommand> {
    private final PersonRepository personRepository;

    public PersonAlreadyExistsValidator(PersonRepository personRepository) {
        this.personRepository = personRepository;
    }

    public void validate(CreatePersonCommand command) {
        if (personRepository.getFirstByName(command.name()) != null) {
            throw new PersonAlreadyExistsException(command);
        }
    }
}
