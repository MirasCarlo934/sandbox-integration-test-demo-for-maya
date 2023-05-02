package com.example.integrationtestdemoformaya.events.dto;

import com.example.integrationtestdemoformaya.domain.Person;

import java.util.Date;
import java.util.UUID;

public record PersonCreatedEvent(
        String rrn,
        String channel,
        Date createdDate,
        UUID id,
        String name,
        String address,
        UUID walletId,
        int age
) {
    public static PersonCreatedEvent from(Person person, String rrn, String channel, Date createdDate) {
        return new PersonCreatedEvent(
                rrn,
                channel,
                createdDate,
                person.getId(),
                person.getName(),
                person.getAddress(),
                person.getWalletId(),
                person.getAge());
    }
}
