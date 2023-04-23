package com.example.integrationtestdemoformaya.core;

import com.example.integrationtestdemoformaya.client.RestWalletClient;
import com.example.integrationtestdemoformaya.data.PersonRepository;
import com.example.integrationtestdemoformaya.domain.Person;
import com.example.integrationtestdemoformaya.domain.Wallet;
import com.example.integrationtestdemoformaya.web.request.PersonRequest;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class PersonCreationService {
    private final PersonExistsValidatorService personExistsValidatorService;
    private final RestWalletClient restWalletClient;
    private final PersonRepository personRepository;

    public PersonCreationService(PersonExistsValidatorService personExistsValidatorService,
                                 RestWalletClient restWalletClient,
                                 PersonRepository personRepository) {
        this.personExistsValidatorService = personExistsValidatorService;
        this.restWalletClient = restWalletClient;
        this.personRepository = personRepository;
    }

    public PersonRequest create(PersonRequest personRequest) {
        personExistsValidatorService.validate(personRequest);
        Wallet wallet = restWalletClient.create();
        Person person = new Person(UUID.randomUUID().toString(), personRequest.name(), personRequest.address(), personRequest.age(), wallet.id());
        personRepository.save(person);
        return personRequest;
    }
}
