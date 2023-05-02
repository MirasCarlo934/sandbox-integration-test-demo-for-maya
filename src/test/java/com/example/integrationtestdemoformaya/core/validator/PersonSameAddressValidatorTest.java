package com.example.integrationtestdemoformaya.core.validator;

import com.example.integrationtestdemoformaya.command.CreatePersonCommand;
import com.example.integrationtestdemoformaya.core.exceptions.PersonAddressAlreadyTakenException;
import com.example.integrationtestdemoformaya.data.PersonRepository;
import com.example.integrationtestdemoformaya.domain.Person;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

class PersonSameAddressValidatorTest {
    private static final String EXISTING_ADDRESS = "Address, Address City, PH";

    private final PersonRepository mockRepository = mock(PersonRepository.class);

    private final PersonSameAddressValidator validatorToTest = new PersonSameAddressValidator(mockRepository);

    @BeforeEach
    void setUp() {
        doReturn(new Person(UUID.randomUUID(), "Name McName", EXISTING_ADDRESS, 99, UUID.randomUUID()))
                .when(mockRepository).getFirstByAddress(EXISTING_ADDRESS);
    }

    @Test
    void givenValidCommand_whenValidate_thenDoNotThrow() {
        CreatePersonCommand command = validCommand();

        assertDoesNotThrow(() -> validatorToTest.validate(command));
    }

    @Test
    void givenCommandWithExistingAddress_whenValidate_thenThrowPersonAlreadyExistsException() {
        CreatePersonCommand command = commandWithExistingAddress();

        PersonAddressAlreadyTakenException ex = assertThrows(PersonAddressAlreadyTakenException.class, () -> validatorToTest.validate(command));
        assertEquals(String.format("Address '%s' already taken", EXISTING_ADDRESS), ex.getMessage());
    }

    @NotNull
    private static CreatePersonCommand validCommand() {
        return customCommand("Nonexistent Address, PH");
    }

    @NotNull
    private static CreatePersonCommand commandWithExistingAddress() {
        return customCommand(EXISTING_ADDRESS);
    }

    @NotNull
    private static CreatePersonCommand customCommand(String address) {
        return new CreatePersonCommand(UUID.randomUUID().toString(), "channel", "Name McName", address, 99);
    }
}