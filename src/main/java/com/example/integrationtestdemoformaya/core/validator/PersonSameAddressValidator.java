package com.example.integrationtestdemoformaya.core.validator;

import com.example.integrationtestdemoformaya.command.CreatePersonCommand;
import com.example.integrationtestdemoformaya.core.exceptions.PersonAddressAlreadyTakenException;
import com.example.integrationtestdemoformaya.data.PersonRepository;
import org.springframework.stereotype.Component;

@Component
public class PersonSameAddressValidator implements Validator<CreatePersonCommand> {
    private final PersonRepository personRepository;

    public PersonSameAddressValidator(PersonRepository personRepository) {
        this.personRepository = personRepository;
    }

    public void validate(CreatePersonCommand command) {
        if (personRepository.getByAddress(command.address()) != null) {
            throw new PersonAddressAlreadyTakenException(command);
        }
    }
}
