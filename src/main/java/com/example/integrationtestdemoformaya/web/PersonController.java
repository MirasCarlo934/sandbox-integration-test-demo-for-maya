package com.example.integrationtestdemoformaya.web;

import com.example.integrationtestdemoformaya.command.CreatePersonCommand;
import com.example.integrationtestdemoformaya.core.PersonCreationService;
import com.example.integrationtestdemoformaya.domain.Person;
import com.example.integrationtestdemoformaya.web.request.CreatePersonRequest;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
public class PersonController {
    private final PersonCreationService personCreationService;

    public PersonController(PersonCreationService personCreationService) {
        this.personCreationService = personCreationService;
    }

    @PostMapping("/persons")
    @ResponseStatus(HttpStatus.OK)
    public Person createPerson(
            @RequestHeader("request-reference-no") String rrn,
            @RequestHeader("channel") String channel,
            @RequestBody CreatePersonRequest createPersonRequest) {
        CreatePersonCommand command = CreatePersonCommand.fromRequest(createPersonRequest, rrn, channel);
        return personCreationService.create(command);
    }
}
