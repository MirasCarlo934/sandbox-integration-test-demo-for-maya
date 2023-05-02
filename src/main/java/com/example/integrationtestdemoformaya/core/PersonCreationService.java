package com.example.integrationtestdemoformaya.core;

import com.example.integrationtestdemoformaya.client.RestWalletClient;
import com.example.integrationtestdemoformaya.command.CreatePersonCommand;
import com.example.integrationtestdemoformaya.data.PersonRepository;
import com.example.integrationtestdemoformaya.domain.Person;
import com.example.integrationtestdemoformaya.domain.Wallet;
import com.example.integrationtestdemoformaya.events.dto.PersonCreatedEvent;
import com.example.integrationtestdemoformaya.events.publisher.SnsPublisher;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class PersonCreationService {
    private final PersonCreationValidatorService personCreationValidatorService;
    private final RestWalletClient restWalletClient;
    private final PersonRepository personRepository;
    private final SnsPublisher<PersonCreatedEvent> personCreatedEventSnsPublisher;

    public PersonCreationService(PersonCreationValidatorService personCreationValidatorService,
                                 RestWalletClient restWalletClient,
                                 PersonRepository personRepository,
                                 SnsPublisher<PersonCreatedEvent> personCreatedEventSnsPublisher) {
        this.personCreationValidatorService = personCreationValidatorService;
        this.restWalletClient = restWalletClient;
        this.personRepository = personRepository;
        this.personCreatedEventSnsPublisher = personCreatedEventSnsPublisher;
    }

    public Person create(CreatePersonCommand command) {
        personCreationValidatorService.validate(command);
        Wallet wallet = restWalletClient.create(command.rrn(), command.channel());
        Person person = new Person(UUID.randomUUID(), command.name(), command.address(), command.age(), wallet.id());
        personRepository.save(person);
        return person;
    }
}
