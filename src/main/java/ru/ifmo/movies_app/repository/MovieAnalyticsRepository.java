package ru.ifmo.movies_app.repository;

import java.util.List;

import ru.ifmo.movies_app.domain.Movie;
import ru.ifmo.movies_app.domain.MovieGenre;
import ru.ifmo.movies_app.domain.Person;

public interface MovieAnalyticsRepository {

    long countMoviesWithGenreGreater(MovieGenre genre);

    List<Movie> findMoviesWithNameContaining(String substring);

    List<Movie> findMoviesWithGenreGreater(MovieGenre genre);

    List<Movie> findMoviesWithoutOscars();

    List<Person> findScreenwritersWithoutOscars();
}
