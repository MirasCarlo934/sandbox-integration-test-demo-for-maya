package com.example.integrationtestdemoformaya.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Entity
@Table(name = "persons")
@NoArgsConstructor
@AllArgsConstructor
public class Person {
    @Id
    @Getter
    private UUID id;
    @Getter
    private String name;
    @Getter
    private String address;
    @Getter
    private int age;
    @Getter
    private UUID walletId;
}
