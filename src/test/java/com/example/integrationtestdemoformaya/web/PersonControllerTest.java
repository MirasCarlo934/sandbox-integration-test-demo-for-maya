package com.example.integrationtestdemoformaya.web;

import com.example.integrationtestdemoformaya.core.PersonCreationService;
import com.example.integrationtestdemoformaya.domain.Person;
import com.example.integrationtestdemoformaya.web.request.CreatePersonRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

class PersonControllerTest {
    private static final UUID PERSON_ID = UUID.randomUUID();
    private static final UUID PERSON_WALLET_ID = UUID.randomUUID();
    private static final String PERSON_NAME = "Name McNamely";
    private static final String PERSON_ADDRESS = "Address, Address City, PH";
    private static final int PERSON_AGE = 99;

    private final PersonCreationService mockPersonCreationService = mock(PersonCreationService.class);

    private final PersonController controllerToTest = new PersonController(mockPersonCreationService);

    @BeforeEach
    void setUp() {
        doReturn(new Person(PERSON_ID, PERSON_NAME, PERSON_ADDRESS, PERSON_AGE, PERSON_WALLET_ID))
                .when(mockPersonCreationService).create(any());
    }

    @Test
    void givenValidCreatePersonRequest_whenCreatePerson_thenReturn200ResponseWithPerson() {
        CreatePersonRequest request = new CreatePersonRequest(PERSON_NAME, PERSON_ADDRESS, PERSON_AGE);

        Person createdPerson = controllerToTest.createPerson(UUID.randomUUID().toString(), "channel", request);

        assertEquals(PERSON_ID, createdPerson.getId());
        assertEquals(PERSON_WALLET_ID, createdPerson.getWalletId());
        assertEquals(PERSON_NAME, createdPerson.getName());
        assertEquals(PERSON_ADDRESS, createdPerson.getAddress());
        assertEquals(PERSON_AGE, createdPerson.getAge());
    }
}