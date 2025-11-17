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
        Long screenwriterId = dto.getScreenwriterId();
        checkScreenwriterNameAndGenre(screenwriterId, dto.getName(), dto.getGenre(), currentId);
    }

    public void validate(Movie movie) {
        Long currentId = movie.getId();
        var screenwriter = movie.getScreenwriter();
        Long screenwriterId = screenwriter != null ? screenwriter.getId() : null;
        checkScreenwriterNameAndGenre(screenwriterId, movie.getName(), movie.getGenre(), currentId);
    }

    private void checkScreenwriterNameAndGenre(Long screenwriterId,
                                               String name,
                                               MovieGenre genre,
                                               Long currentId) {
        if (screenwriterId != null
                && name != null
                && genre != null
                && movieRepository.existsByScreenwriterAndNameAndGenre(screenwriterId, name, genre, currentId)) {
            throw new IllegalArgumentException("Этот сценарист уже создал фильм с таким названием в этом жанре");
        }
    }
}
