package ru.ifmo.movies_app.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.cache.CacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import ru.ifmo.movies_app.config.CacheConfig;
import ru.ifmo.movies_app.domain.Coordinates;
import ru.ifmo.movies_app.domain.Country;
import ru.ifmo.movies_app.domain.Movie;
import ru.ifmo.movies_app.domain.MovieGenre;
import ru.ifmo.movies_app.domain.Person;
import ru.ifmo.movies_app.dto.MovieFormDto;
import ru.ifmo.movies_app.repository.MovieAnalyticsRepository;
import ru.ifmo.movies_app.repository.MovieRepository;
import ru.ifmo.movies_app.repository.PersonRepository;

@SpringJUnitConfig
@ContextConfiguration(classes = {
        CacheConfig.class,
        AnalyticsCachingIntegrationTest.TestConfig.class
})
class AnalyticsCachingIntegrationTest {

    @jakarta.annotation.Resource
    private MovieAnalyticsService movieAnalyticsService;

    @jakarta.annotation.Resource
    private MovieService movieService;

    @jakarta.annotation.Resource
    private MovieAnalyticsRepository movieAnalyticsRepository;

    @jakarta.annotation.Resource
    private MovieRepository movieRepository;

    @jakarta.annotation.Resource
    private PersonRepository personRepository;

    @jakarta.annotation.Resource
    private MovieUniquenessValidator uniquenessValidator;

    @jakarta.annotation.Resource
    private SimpMessagingTemplate messagingTemplate;

    @jakarta.annotation.Resource
    private CacheManager cacheManager;

    @BeforeEach
    void setUp() {
        reset(movieAnalyticsRepository, movieRepository, personRepository, uniquenessValidator, messagingTemplate);
        clearAnalyticsCaches();
    }

    @Test
    void cachesNameSearchByNormalizedSubstring() {
        when(movieAnalyticsRepository.findMoviesWithNameContaining("Solar")).thenReturn(List.of());

        assertThat(movieAnalyticsService.findMoviesWithNameContaining("  Solar  ")).isEmpty();
        assertThat(movieAnalyticsService.findMoviesWithNameContaining("Solar")).isEmpty();

        verify(movieAnalyticsRepository, times(1)).findMoviesWithNameContaining("Solar");
    }

    @Test
    void evictsAnalyticsCachesAfterMovieCreate() {
        when(movieAnalyticsRepository.countMoviesWithGenreGreater(MovieGenre.DRAMA))
                .thenReturn(3L)
                .thenReturn(4L);
        assertThat(movieAnalyticsService.countMoviesWithGenreGreater(MovieGenre.DRAMA)).isEqualTo(3L);
        assertThat(movieAnalyticsService.countMoviesWithGenreGreater(MovieGenre.DRAMA)).isEqualTo(3L);
        verify(movieAnalyticsRepository, times(1)).countMoviesWithGenreGreater(MovieGenre.DRAMA);

        Person screenwriter = person(11L, "Writer");
        when(personRepository.findById(11L)).thenReturn(Optional.of(screenwriter));
        when(movieRepository.save(any(Movie.class))).thenAnswer(invocation -> {
            Movie movie = invocation.getArgument(0);
            movie.setId(99L);
            movie.setCreationDate(Date.from(Instant.parse("2026-09-06T10:00:00Z")));
            return movie;
        });

        movieService.create(validDto());

        assertThat(movieAnalyticsService.countMoviesWithGenreGreater(MovieGenre.DRAMA)).isEqualTo(4L);
        verify(movieAnalyticsRepository, times(2)).countMoviesWithGenreGreater(MovieGenre.DRAMA);
    }

    private void clearAnalyticsCaches() {
        List.of(
                AnalyticsCacheNames.GENRE_COUNTS,
                AnalyticsCacheNames.NAME_SEARCH,
                AnalyticsCacheNames.GENRE_LISTS,
                AnalyticsCacheNames.NO_OSCARS,
                AnalyticsCacheNames.SCREENWRITERS_NO_OSCARS
        ).forEach(cacheName -> cacheManager.getCache(cacheName).clear());
    }

    private MovieFormDto validDto() {
        MovieFormDto dto = new MovieFormDto();
        dto.setName("Solar Road");
        dto.setCoordinates(new ru.ifmo.movies_app.dto.CoordinatesDto(12.5f, 90L));
        dto.setBudget(100000);
        dto.setTotalBoxOffice(150000.5f);
        dto.setScreenwriterId(11L);
        dto.setLength(118L);
        dto.setGoldenPalmCount(1);
        dto.setGenre(MovieGenre.DRAMA);
        return dto;
    }

    private Person person(Long id, String name) {
        Person person = new Person();
        person.setId(id);
        person.setName(name);
        person.setWeight(70.0f);
        person.setNationality(Country.SPAIN);
        return person;
    }

    @Configuration
    static class TestConfig {

        @Bean
        MovieAnalyticsService movieAnalyticsService(MovieAnalyticsRepository movieAnalyticsRepository,
                                                    MovieService movieService) {
            return new MovieAnalyticsService(movieAnalyticsRepository, movieService);
        }

        @Bean
        MovieService movieService(MovieRepository movieRepository,
                                  PersonRepository personRepository,
                                  SimpMessagingTemplate messagingTemplate,
                                  MovieUniquenessValidator uniquenessValidator) {
            return new MovieService(movieRepository, personRepository, messagingTemplate, uniquenessValidator);
        }

        @Bean
        MovieAnalyticsRepository movieAnalyticsRepository() {
            return mock(MovieAnalyticsRepository.class);
        }

        @Bean
        MovieRepository movieRepository() {
            return mock(MovieRepository.class);
        }

        @Bean
        PersonRepository personRepository() {
            return mock(PersonRepository.class);
        }

        @Bean
        SimpMessagingTemplate messagingTemplate() {
            return mock(SimpMessagingTemplate.class);
        }

        @Bean
        MovieUniquenessValidator uniquenessValidator() {
            return mock(MovieUniquenessValidator.class);
        }
    }
}
