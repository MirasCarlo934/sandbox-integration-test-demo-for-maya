package com.example.integrationtestdemoformaya.events.dto;

import java.util.Date;
import java.util.UUID;

public record PersonCreatedEvent(
        String rrn,
        String channel,
        Date createdDate,
        UUID id,
        String name,
        String address,
        int age
) { }
