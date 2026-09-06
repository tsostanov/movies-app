package ru.ifmo.movies_app.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import ru.ifmo.movies_app.domain.Coordinates;
import ru.ifmo.movies_app.domain.Movie;
import ru.ifmo.movies_app.domain.MovieGenre;
import ru.ifmo.movies_app.dto.MovieTableRowDto;
import ru.ifmo.movies_app.repository.MovieAnalyticsRepository;

class MovieAnalyticsServiceTest {

    private MovieAnalyticsRepository analyticsRepository;
    private MovieService movieService;
    private MovieAnalyticsService service;

    @BeforeEach
    void setUp() {
        analyticsRepository = mock(MovieAnalyticsRepository.class);
        movieService = mock(MovieService.class);
        service = new MovieAnalyticsService(analyticsRepository, movieService);
    }

    @ParameterizedTest
    @ValueSource(strings = {"", " ", "\t\n"})
    void findMoviesWithNameContainingRejectsBlankSubstring(String substring) {
        assertThatThrownBy(() -> service.findMoviesWithNameContaining(substring))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Search substring must not be blank");

        verify(analyticsRepository, never()).findMoviesWithNameContaining(org.mockito.ArgumentMatchers.anyString());
    }

    @Test
    void findMoviesWithNameContainingRejectsTooLongSubstring() {
        String tooLong = "a".repeat(101);

        assertThatThrownBy(() -> service.findMoviesWithNameContaining(tooLong))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Search substring must be at most 100 characters");

        verify(analyticsRepository, never()).findMoviesWithNameContaining(org.mockito.ArgumentMatchers.anyString());
    }

    @Test
    void findMoviesWithNameContainingTrimsSubstringBeforeQueryingRepository() {
        Movie movie = movie(7L, "Solar Road");
        MovieTableRowDto row = new MovieTableRowDto(
                7L,
                "Solar Road",
                1.0f,
                2L,
                null,
                null,
                100,
                200.0f,
                null,
                null,
                "Writer",
                null,
                90L,
                1,
                MovieGenre.DRAMA);
        when(analyticsRepository.findMoviesWithNameContaining("Solar")).thenReturn(List.of(movie));
        when(movieService.mapToRow(movie)).thenReturn(row);

        List<MovieTableRowDto> result = service.findMoviesWithNameContaining("  Solar  ");

        assertThat(result).containsExactly(row);
        verify(analyticsRepository).findMoviesWithNameContaining("Solar");
    }

    private Movie movie(Long id, String name) {
        Movie movie = new Movie();
        movie.setId(id);
        movie.setName(name);
        movie.setCoordinates(new Coordinates(1.0f, 2L));
        movie.setBudget(100);
        movie.setTotalBoxOffice(200.0f);
        movie.setGoldenPalmCount(1);
        movie.setGenre(MovieGenre.DRAMA);
        return movie;
    }
}
