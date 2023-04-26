package com.example.integrationtestdemoformaya.command;

import com.example.integrationtestdemoformaya.web.request.CreatePersonRequest;

public record CreatePersonCommand(
        String rrn,
        String channel,
        String name,
        String address,
        int age) {

    public static CreatePersonCommand fromRequest(CreatePersonRequest request, String rrn, String channel) {
        return new CreatePersonCommand(rrn, channel, request.name(), request.address(), request.age());
    }

}
