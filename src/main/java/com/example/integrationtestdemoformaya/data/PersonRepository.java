package com.example.integrationtestdemoformaya.data;

import com.example.integrationtestdemoformaya.domain.Person;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;

@Repository
public class PersonRepository {
    private final List<Person> persons = new ArrayList<>(List.of(
            new Person(
                    "1050b6b7-eb76-4dff-8d47-23a8120cfe21",
                    "Carlo Miras",
                    "Laguna",
                    23,
                    "29ca7596-8255-477e-b77c-462ad7c3f093")));

    public Person get(String name) {
        for (Person person : persons) {
            if (name.equals(person.name())) return person;
        }
        return null;
    }

    public void save(Person person) {
        persons.add(person);
    }
}
