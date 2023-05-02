package com.example.integrationtestdemoformaya.core;

import com.example.integrationtestdemoformaya.client.RestWalletClient;
import com.example.integrationtestdemoformaya.command.CreatePersonCommand;
import com.example.integrationtestdemoformaya.data.PersonRepository;
import com.example.integrationtestdemoformaya.domain.Person;
import com.example.integrationtestdemoformaya.domain.Wallet;
import com.example.integrationtestdemoformaya.events.dto.PersonCreatedEvent;
import com.example.integrationtestdemoformaya.events.publisher.PersonCreatedEventSnsPublisher;
import com.example.integrationtestdemoformaya.events.publisher.SnsPublisher;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class PersonCreationServiceTest {
    private final PersonCreationValidatorService mockValidatorService = mock(PersonCreationValidatorService.class);
    private final RestWalletClient mockWalletClient = mock(RestWalletClient.class);
    private final PersonRepository mockPersonRepository = mock(PersonRepository.class);
    private final SnsPublisher<PersonCreatedEvent> mockPublisher = mock(PersonCreatedEventSnsPublisher.class);

    private final PersonCreationService serviceToTest = new PersonCreationService(
            mockValidatorService,
            mockWalletClient,
            mockPersonRepository,
            mockPublisher);

    private final Wallet mockWallet = new Wallet(UUID.randomUUID());

    @BeforeEach
    void setUp() {
        doNothing().when(mockValidatorService).validate(any());
        doReturn(mockWallet).when(mockWalletClient).create(any(), any());
        doNothing().when(mockPublisher).publish(any());
    }

    @Test
    void givenValidCommand_whenCreate_thenReturnCreatedPerson() {
        CreatePersonCommand command = new CreatePersonCommand(
                UUID.randomUUID().toString(),
                "channel",
                "Name McName",
                "Address PH",
                99);

        Person createdPerson = serviceToTest.create(command);

        ArgumentCaptor<Person> personCaptor = ArgumentCaptor.forClass(Person.class);
        ArgumentCaptor<PersonCreatedEvent> eventCaptor = ArgumentCaptor.forClass(PersonCreatedEvent.class);
        verify(mockValidatorService).validate(command);
        verify(mockPersonRepository).save(personCaptor.capture());
        verify(mockPublisher).publish(eventCaptor.capture());

        Person savedPerson = personCaptor.getValue();
        PersonCreatedEvent publishedEvent = eventCaptor.getValue();
        assertEquals(createdPerson, savedPerson);
        assertEquals(command.name(), createdPerson.getName());
        assertEquals(command.name(), publishedEvent.name());
        assertEquals(command.address(), createdPerson.getAddress());
        assertEquals(command.address(), publishedEvent.address());
        assertEquals(command.age(), createdPerson.getAge());
        assertEquals(command.age(), publishedEvent.age());
        assertEquals(createdPerson.getId(), publishedEvent.id());
        assertEquals(createdPerson.getWalletId(), publishedEvent.walletId());
    }
}