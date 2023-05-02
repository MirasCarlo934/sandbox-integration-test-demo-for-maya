package com.example.integrationtestdemoformaya.core;


import com.example.integrationtestdemoformaya.command.CreatePersonCommand;
import com.example.integrationtestdemoformaya.core.validator.Validator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;

class PersonCreationValidatorServiceTest {
    private final Validator<CreatePersonCommand> mockValidator = mock(MockValidator.class);

    private final PersonCreationValidatorService serviceToTest = new PersonCreationValidatorService(List.of(mockValidator));

    @BeforeEach
    void setUp() {
        doNothing().when(mockValidator).validate(any());
    }

    @Test
    void givenValidCommand_whenValidate_thenDoNotThrow() {
        CreatePersonCommand command = new CreatePersonCommand(
                UUID.randomUUID().toString(),
                "channel",
                "Name McName",
                "Address PH",
                99);

        assertDoesNotThrow(() -> serviceToTest.validate(command));
    }

    private interface MockValidator extends Validator<CreatePersonCommand> {}
}