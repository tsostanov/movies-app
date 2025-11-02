package ru.ifmo.movies_app.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import ru.ifmo.movies_app.domain.Location;

@Repository
@Transactional(readOnly = true)
public class JpaLocationRepository implements LocationRepository {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public List<Location> findAll() {
        return entityManager.createQuery("select l from Location l order by l.name", Location.class)
                .getResultList();
    }

    @Override
    public Optional<Location> findById(Long id) {
        return Optional.ofNullable(entityManager.find(Location.class, id));
    }

    @Override
    @Transactional
    public Location save(Location location) {
        if (location.getId() == null) {
            entityManager.persist(location);
            return location;
        }
        return entityManager.merge(location);
    }

    @Override
    @Transactional
    public void delete(Location location) {
        Location managed = entityManager.contains(location) ? location : entityManager.merge(location);
        entityManager.remove(managed);
    }
}
