package ru.ifmo.movies_app.service;

import org.junit.jupiter.api.Test;

import ru.ifmo.movies_app.domain.Movie;
import ru.ifmo.movies_app.domain.MovieGenre;
import ru.ifmo.movies_app.domain.Person;
import ru.ifmo.movies_app.dto.MovieFormDto;
import ru.ifmo.movies_app.repository.MovieRepository;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class MovieUniquenessValidatorTest {

    private final MovieRepository movieRepository = mock(MovieRepository.class);
    private final MovieUniquenessValidator validator = new MovieUniquenessValidator(movieRepository);

    @Test
    void validateDtoThrowsWhenSameScreenwriterReusesNameAndGenre() {
        MovieFormDto dto = new MovieFormDto();
        dto.setName("Mirage Lines");
        dto.setScreenwriterId(42L);
        dto.setGenre(MovieGenre.DRAMA);

        when(movieRepository.existsByScreenwriterAndNameAndGenre(42L, "Mirage Lines", MovieGenre.DRAMA, null))
                .thenReturn(true);

        assertThatThrownBy(() -> validator.validate(dto, null))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void validateDtoAllowsSameNameIfGenreDiffers() {
        MovieFormDto dto = new MovieFormDto();
        dto.setName("Mirage Lines");
        dto.setScreenwriterId(42L);
        dto.setGenre(MovieGenre.COMEDY);

        when(movieRepository.existsByScreenwriterAndNameAndGenre(42L, "Mirage Lines", MovieGenre.COMEDY, null))
                .thenReturn(false);

        validator.validate(dto, null);

        verify(movieRepository).existsByScreenwriterAndNameAndGenre(42L, "Mirage Lines", MovieGenre.COMEDY, null);
    }

    @Test
    void validateEntityThrowsWhenDuplicateExists() {
        Movie movie = new Movie();
        movie.setId(7L);
        movie.setName("Silent Reef");
        movie.setGenre(MovieGenre.DRAMA);

        Person screenwriter = new Person();
        screenwriter.setId(52L);
        movie.setScreenwriter(screenwriter);

        when(movieRepository.existsByScreenwriterAndNameAndGenre(52L, "Silent Reef", MovieGenre.DRAMA, 7L))
                .thenReturn(true);

        assertThatThrownBy(() -> validator.validate(movie))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
