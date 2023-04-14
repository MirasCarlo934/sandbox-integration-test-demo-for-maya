package com.example.integrationtestdemoformaya.web;

import com.example.integrationtestdemoformaya.core.PersonCreationService;
import com.example.integrationtestdemoformaya.domain.Person;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
public class RestExampleController {
    private final PersonCreationService personCreationService;

    public RestExampleController(PersonCreationService personCreationService) {
        this.personCreationService = personCreationService;
    }

    @PostMapping("/persons")
    @ResponseStatus(HttpStatus.OK)
    public Person createPerson(@RequestBody Person person) {
        return personCreationService.create(person);
    }
}
