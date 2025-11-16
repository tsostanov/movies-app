package ru.ifmo.movies_app.repository;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import ru.ifmo.movies_app.domain.Movie;
import ru.ifmo.movies_app.domain.MovieGenre;
import ru.ifmo.movies_app.domain.Person;
import ru.ifmo.movies_app.dto.MovieTableFilter;

public interface MovieRepository {

    Optional<Movie> findById(Long id);

    Optional<Movie> findByIdWithRelations(Long id);

    Page<Movie> findAll(MovieTableFilter filter, Pageable pageable);

    Movie save(Movie movie);

    void delete(Movie movie);

    java.util.List<Movie> findByDirector(Person director);

    java.util.List<Movie> findByScreenwriter(Person screenwriter);

    java.util.List<Movie> findByOperator(Person operator);

    boolean existsByNameIgnoreCase(String name, Long excludeId);

    boolean existsByScreenwriterAndGenre(Long screenwriterId, MovieGenre genre, Long excludeId);
}
