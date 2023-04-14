package com.example.integrationtestdemoformaya.domain;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record Person(
        @NotNull(message = "name is required")
        String name,

        @NotNull(message = "address is required")
        String address,

        @NotNull(message = "age is required")
        @Min(value = 1, message = "age must be greater than 0")
        int age
) { }
