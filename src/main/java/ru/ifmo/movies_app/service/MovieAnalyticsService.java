package ru.ifmo.movies_app.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import ru.ifmo.movies_app.domain.Movie;
import ru.ifmo.movies_app.domain.MovieGenre;
import ru.ifmo.movies_app.dto.MovieTableRowDto;
import ru.ifmo.movies_app.dto.PersonSummaryDto;
import ru.ifmo.movies_app.repository.MovieAnalyticsRepository;

@Service
@Transactional(readOnly = true)
public class MovieAnalyticsService {

    private final MovieAnalyticsRepository movieAnalyticsRepository;
    private final MovieService movieService;

    public MovieAnalyticsService(MovieAnalyticsRepository movieAnalyticsRepository,
                                 MovieService movieService) {
        this.movieAnalyticsRepository = movieAnalyticsRepository;
        this.movieService = movieService;
    }

    public long countMoviesWithGenreGreater(MovieGenre genre) {
        return movieAnalyticsRepository.countMoviesWithGenreGreater(genre);
    }

    public List<MovieTableRowDto> findMoviesWithNameContaining(String substring) {
        return mapToRows(movieAnalyticsRepository.findMoviesWithNameContaining(substring));
    }

    public List<MovieTableRowDto> findMoviesWithGenreGreater(MovieGenre genre) {
        return mapToRows(movieAnalyticsRepository.findMoviesWithGenreGreater(genre));
    }

    public List<MovieTableRowDto> findMoviesWithoutOscars() {
        return mapToRows(movieAnalyticsRepository.findMoviesWithoutOscars());
    }

    public List<PersonSummaryDto> findScreenwritersWithoutOscars() {
        return movieAnalyticsRepository.findScreenwritersWithoutOscars().stream()
                .map(person -> new PersonSummaryDto(person.getId(), person.getName()))
                .collect(Collectors.toList());
    }

    private List<MovieTableRowDto> mapToRows(List<Movie> movies) {
        return movies.stream()
                .map(movieService::mapToRow)
                .collect(Collectors.toList());
    }
}   
