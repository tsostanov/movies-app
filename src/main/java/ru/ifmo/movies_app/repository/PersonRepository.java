package ru.ifmo.movies_app.repository;

import java.util.List;
import java.util.Optional;

import ru.ifmo.movies_app.domain.Person;

public interface PersonRepository {

    List<Person> findAll();

    List<Person> findAllWithLocation();

    Optional<Person> findById(Long id);

    Optional<Person> findByNameIgnoreCase(String name);

    Person save(Person person);

    void delete(Person person);
}
