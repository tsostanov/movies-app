package ru.ifmo.movies_app.repository;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import ru.ifmo.movies_app.domain.Movie;
import ru.ifmo.movies_app.domain.MovieGenre;
import ru.ifmo.movies_app.domain.Person;

@Repository
@Transactional(readOnly = true)
public class JpaMovieAnalyticsRepository implements MovieAnalyticsRepository {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public long countMoviesWithGenreGreater(MovieGenre genre) {
        Number result = (Number) entityManager
                .createNativeQuery("SELECT fn_count_movies_with_genre_greater(CAST(?1 AS movie_genre))")
                .setParameter(1, genre.name())
                .getSingleResult();
        return result.longValue();
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<Movie> findMoviesWithNameContaining(String substring) {
        return entityManager
                .createNativeQuery("SELECT * FROM fn_movies_with_name_containing(?1)", Movie.class)
                .setParameter(1, substring)
                .getResultList();
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<Movie> findMoviesWithGenreGreater(MovieGenre genre) {
        return entityManager
                .createNativeQuery("SELECT * FROM fn_movies_with_genre_greater(CAST(?1 AS movie_genre))", Movie.class)
                .setParameter(1, genre.name())
                .getResultList();
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<Movie> findMoviesWithoutOscars() {
        return entityManager
                .createNativeQuery("SELECT * FROM fn_movies_without_oscars()", Movie.class)
                .getResultList();
    }

    @Override
    public List<Person> findScreenwritersWithoutOscars() {
        @SuppressWarnings("unchecked")
        List<Number> ids = entityManager
                .createNativeQuery("SELECT screenwriter_id FROM fn_screenwriters_without_oscars()")
                .getResultList();
        if (ids.isEmpty()) {
            return Collections.emptyList();
        }
        List<Long> personIds = ids.stream()
                .map(Number::longValue)
                .collect(Collectors.toList());
        return entityManager
                .createQuery("select p from Person p where p.id in :ids order by p.name", Person.class)
                .setParameter("ids", personIds)
                .getResultList();
    }
}
