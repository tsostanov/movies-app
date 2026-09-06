package ru.ifmo.movies_app.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.cache.annotation.Cacheable;
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

    @Cacheable(cacheNames = AnalyticsCacheNames.GENRE_COUNTS, key = "#genre")
    public long countMoviesWithGenreGreater(MovieGenre genre) {
        return movieAnalyticsRepository.countMoviesWithGenreGreater(genre);
    }

    @Cacheable(cacheNames = AnalyticsCacheNames.NAME_SEARCH, key = "T(ru.ifmo.movies_app.service.AnalyticsCacheKeys).nameSearch(#substring)")
    public List<MovieTableRowDto> findMoviesWithNameContaining(String substring) {
        return mapToRows(movieAnalyticsRepository.findMoviesWithNameContaining(AnalyticsCacheKeys.nameSearch(substring)));
    }

    @Cacheable(cacheNames = AnalyticsCacheNames.GENRE_LISTS, key = "#genre")
    public List<MovieTableRowDto> findMoviesWithGenreGreater(MovieGenre genre) {
        return mapToRows(movieAnalyticsRepository.findMoviesWithGenreGreater(genre));
    }

    @Cacheable(cacheNames = AnalyticsCacheNames.NO_OSCARS, key = "'all'")
    public List<MovieTableRowDto> findMoviesWithoutOscars() {
        return mapToRows(movieAnalyticsRepository.findMoviesWithoutOscars());
    }

    @Cacheable(cacheNames = AnalyticsCacheNames.SCREENWRITERS_NO_OSCARS, key = "'all'")
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
