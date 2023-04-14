package com.example.integrationtestdemoformaya.core;

import com.example.integrationtestdemoformaya.data.PersonRepository;
import com.example.integrationtestdemoformaya.data.exceptions.PersonAlreadyExistsException;
import com.example.integrationtestdemoformaya.domain.Person;
import org.springframework.stereotype.Service;

@Service
public class PersonExistsValidatorService {
    private final PersonRepository personRepository;

    public PersonExistsValidatorService(PersonRepository personRepository) {
        this.personRepository = personRepository;
    }

    public void validate(Person person) {
        if (personRepository.get(person.name()) != null) {
            throw new PersonAlreadyExistsException(person);
        }
    }
}
