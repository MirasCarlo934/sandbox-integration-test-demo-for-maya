package com.example.integrationtestdemoformaya.core;

import com.example.integrationtestdemoformaya.data.PersonRepository;
import com.example.integrationtestdemoformaya.domain.Person;
import org.springframework.stereotype.Service;

@Service
public class PersonCreationService {
    private final PersonExistsValidatorService personExistsValidatorService;
    private final PersonRepository personRepository;

    public PersonCreationService(PersonExistsValidatorService personExistsValidatorService,
                                 PersonRepository personRepository) {
        this.personExistsValidatorService = personExistsValidatorService;
        this.personRepository = personRepository;
    }

    public Person create(Person person) {
        personExistsValidatorService.validate(person);
        personRepository.save(person);
        return person;
    }
}
