package com.example.integrationtestdemoformaya.core.validator;

import com.example.integrationtestdemoformaya.command.CreatePersonCommand;
import com.example.integrationtestdemoformaya.core.exceptions.PersonAlreadyExistsException;
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

class PersonAlreadyExistsValidatorTest {
    private static final String EXISTING_NAME = "Name McName";

    private final PersonRepository mockRepository = mock(PersonRepository.class);

    private final PersonAlreadyExistsValidator validatorToTest = new PersonAlreadyExistsValidator(mockRepository);

    @BeforeEach
    void setUp() {
        doReturn(new Person(UUID.randomUUID(), EXISTING_NAME, "Address PH", 99, UUID.randomUUID()))
                .when(mockRepository).getFirstByName(EXISTING_NAME);
    }

    @Test
    void givenValidCommand_whenValidate_thenDoNotThrow() {
        CreatePersonCommand command = validCommand();

        assertDoesNotThrow(() -> validatorToTest.validate(command));
    }

    @Test
    void givenCommandWithExistingName_whenValidate_thenThrowPersonAlreadyExistsException() {
        CreatePersonCommand command = commandWithExistingName();

        PersonAlreadyExistsException ex = assertThrows(PersonAlreadyExistsException.class, () -> validatorToTest.validate(command));
        assertEquals(String.format("Person '%s' already exists", EXISTING_NAME), ex.getMessage());
    }

    @NotNull
    private static CreatePersonCommand validCommand() {
        return customCommand("Nonexistent Name");
    }

    @NotNull
    private static CreatePersonCommand commandWithExistingName() {
        return customCommand(EXISTING_NAME);
    }

    @NotNull
    private static CreatePersonCommand customCommand(String name) {
        return new CreatePersonCommand(UUID.randomUUID().toString(), "channel", name, "Address PH", 99);
    }
}