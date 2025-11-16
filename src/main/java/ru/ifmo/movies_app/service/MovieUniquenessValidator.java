package ru.ifmo.movies_app.service;

import org.springframework.stereotype.Component;

import ru.ifmo.movies_app.domain.Movie;
import ru.ifmo.movies_app.domain.MovieGenre;
import ru.ifmo.movies_app.dto.MovieFormDto;
import ru.ifmo.movies_app.repository.MovieRepository;

@Component
public class MovieUniquenessValidator {

    private final MovieRepository movieRepository;

    public MovieUniquenessValidator(MovieRepository movieRepository) {
        this.movieRepository = movieRepository;
    }

    public void validate(MovieFormDto dto, Long currentId) {
        String name = dto.getName();
        Long screenwriterId = dto.getScreenwriterId();
        var genre = dto.getGenre();
        checkName(name, currentId);
        checkScreenwriterGenre(screenwriterId, genre, currentId);
    }

    public void validate(Movie movie) {
        Long currentId = movie.getId();
        checkName(movie.getName(), currentId);
        var screenwriter = movie.getScreenwriter();
        checkScreenwriterGenre(
                screenwriter != null ? screenwriter.getId() : null,
                movie.getGenre(),
                currentId);
    }

    private void checkName(String name, Long currentId) {
        if (name != null && movieRepository.existsByNameIgnoreCase(name, currentId)) {
            throw new IllegalArgumentException("Фильм с таким названием уже существует");
        }
    }

    private void checkScreenwriterGenre(Long screenwriterId,
                                        MovieGenre genre,
                                        Long currentId) {
        if (screenwriterId != null
                && genre != null
                && movieRepository.existsByScreenwriterAndGenre(screenwriterId, genre, currentId)) {
            throw new IllegalArgumentException("Этот сценарист уже создавал фильм в данном жанре");
        }
    }
}
