package ru.ifmo.movies_app.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

// import jakarta.persistence.EntityGraph;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import ru.ifmo.movies_app.domain.Person;

@Repository
@Transactional(readOnly = true)
public class JpaPersonRepository implements PersonRepository {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public List<Person> findAll() {
        return entityManager.createQuery("select p from Person p order by p.name", Person.class)
                .getResultList();
    }

    @Override
    public List<Person> findAllWithLocation() {
        return entityManager.createQuery(
                        "select distinct p from Person p left join fetch p.location order by p.name",
                        Person.class)
                .getResultList();
    }

    @Override
    public Optional<Person> findById(Long id) {
        return Optional.ofNullable(entityManager.find(Person.class, id));
    }

    @Override
    @Transactional
    public Person save(Person person) {
        if (person.getId() == null) {
            entityManager.persist(person);
            // Ensure generated identifier is available immediately (needed for bulk import)
            entityManager.flush();
            return person;
        }
        return entityManager.merge(person);
    }

    @Override
    @Transactional
    public void delete(Person person) {
        Person managed = entityManager.contains(person) ? person : entityManager.merge(person);
        entityManager.remove(managed);
    }
}
