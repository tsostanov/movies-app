package ru.ifmo.movies_app.web;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import ru.ifmo.movies_app.domain.MovieGenre;
import ru.ifmo.movies_app.dto.MovieTableRowDto;
import ru.ifmo.movies_app.dto.PersonSummaryDto;
import ru.ifmo.movies_app.service.MovieAnalyticsService;

@RestController
@RequestMapping("/api/analytics")
public class AnalyticsRestController {

    private final MovieAnalyticsService movieAnalyticsService;

    public AnalyticsRestController(MovieAnalyticsService movieAnalyticsService) {
        this.movieAnalyticsService = movieAnalyticsService;
    }

    @GetMapping("/genre-count")
    public long countByGenre(@RequestParam MovieGenre genre) {
        return movieAnalyticsService.countMoviesWithGenreGreater(genre);
    }

    @GetMapping("/name-search")
    public List<MovieTableRowDto> searchByName(@RequestParam String substring) {
        return movieAnalyticsService.findMoviesWithNameContaining(substring);
    }

    @GetMapping("/genre-list")
    public List<MovieTableRowDto> findByGenre(@RequestParam MovieGenre genre) {
        return movieAnalyticsService.findMoviesWithGenreGreater(genre);
    }

    @GetMapping("/no-oscars")
    public List<MovieTableRowDto> moviesWithoutOscars() {
        return movieAnalyticsService.findMoviesWithoutOscars();
    }

    @GetMapping("/screenwriters-no-oscars")
    public List<PersonSummaryDto> screenwritersWithoutOscars() {
        return movieAnalyticsService.findScreenwritersWithoutOscars();
    }
}
