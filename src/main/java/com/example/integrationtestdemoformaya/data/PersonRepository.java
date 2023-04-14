package com.example.integrationtestdemoformaya.data;

import com.example.integrationtestdemoformaya.domain.Person;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;

@Repository
public class PersonRepository {
    private List<Person> persons = new ArrayList<>(List.of(
            new Person("Carlo Miras", "Laguna", 23)));

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
