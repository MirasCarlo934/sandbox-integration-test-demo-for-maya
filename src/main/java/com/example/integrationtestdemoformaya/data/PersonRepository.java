package com.example.integrationtestdemoformaya.data;

import com.example.integrationtestdemoformaya.domain.Person;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface PersonRepository extends JpaRepository<Person, UUID> {
    Person getFirstByName(String name);
    Person getFirstByAddress(String address);
}
