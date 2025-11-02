package ru.ifmo.movies_app.repository;

import java.util.List;
import java.util.Optional;

import ru.ifmo.movies_app.domain.Location;

public interface LocationRepository {

    List<Location> findAll();

    Optional<Location> findById(Long id);

    Location save(Location location);

    void delete(Location location);
}
