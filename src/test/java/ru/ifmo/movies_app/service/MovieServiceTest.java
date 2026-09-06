package ru.ifmo.movies_app.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.Date;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import ru.ifmo.movies_app.domain.Coordinates;
import ru.ifmo.movies_app.domain.Country;
import ru.ifmo.movies_app.domain.Movie;
import ru.ifmo.movies_app.domain.MovieGenre;
import ru.ifmo.movies_app.domain.MpaaRating;
import ru.ifmo.movies_app.domain.Person;
import ru.ifmo.movies_app.dto.MovieChangeMessage;
import ru.ifmo.movies_app.dto.MovieChangeMessage.ChangeType;
import ru.ifmo.movies_app.dto.MovieDetailsDto;
import ru.ifmo.movies_app.dto.MovieFormDto;
import ru.ifmo.movies_app.repository.MovieRepository;
import ru.ifmo.movies_app.repository.PersonRepository;

class MovieServiceTest {

    private MovieRepository movieRepository;
    private PersonRepository personRepository;
    private SimpMessagingTemplate messagingTemplate;
    private MovieUniquenessValidator uniquenessValidator;
    private MovieService service;

    @BeforeEach
    void setUp() {
        movieRepository = mock(MovieRepository.class);
        personRepository = mock(PersonRepository.class);
        messagingTemplate = mock(SimpMessagingTemplate.class);
        uniquenessValidator = mock(MovieUniquenessValidator.class);
        service = new MovieService(movieRepository, personRepository, messagingTemplate, uniquenessValidator);
    }

    @Test
    void createMapsDtoPersistsMovieAndBroadcastsCreatedRow() {
        Person director = person(11L, "Mira Stone");
        Person screenwriter = person(12L, "Egor Khazin");
        Person operator = person(13L, "Lina Park");
        when(personRepository.findById(11L)).thenReturn(Optional.of(director));
        when(personRepository.findById(12L)).thenReturn(Optional.of(screenwriter));
        when(personRepository.findById(13L)).thenReturn(Optional.of(operator));
        when(movieRepository.save(org.mockito.ArgumentMatchers.any(Movie.class))).thenAnswer(invocation -> {
            Movie movie = invocation.getArgument(0);
            movie.setId(77L);
            movie.setCreationDate(Date.from(Instant.parse("2026-09-03T12:30:00Z")));
            return movie;
        });

        MovieDetailsDto details = service.create(validDto());

        ArgumentCaptor<Movie> movieCaptor = ArgumentCaptor.forClass(Movie.class);
        verify(movieRepository).save(movieCaptor.capture());
        Movie saved = movieCaptor.getValue();
        assertThat(saved.getName()).isEqualTo("Solar Road");
        assertThat(saved.getCoordinates().getX()).isEqualTo(12.5f);
        assertThat(saved.getCoordinates().getY()).isEqualTo(90L);
        assertThat(saved.getDirector()).isSameAs(director);
        assertThat(saved.getScreenwriter()).isSameAs(screenwriter);
        assertThat(saved.getOperator()).isSameAs(operator);

        assertThat(details.getId()).isEqualTo(77L);
        assertThat(details.getScreenwriter()).isSameAs(screenwriter);
        verify(uniquenessValidator).validate(org.mockito.ArgumentMatchers.any(MovieFormDto.class), eq(null));

        ArgumentCaptor<MovieChangeMessage> messageCaptor = ArgumentCaptor.forClass(MovieChangeMessage.class);
        verify(messagingTemplate).convertAndSend(eq("/topic/movies"), messageCaptor.capture());
        MovieChangeMessage message = messageCaptor.getValue();
        assertThat(message.getType()).isEqualTo(ChangeType.CREATED);
        assertThat(message.getId()).isEqualTo(77L);
        assertThat(message.getPayload().getName()).isEqualTo("Solar Road");
        assertThat(message.getPayload().getScreenwriterName()).isEqualTo("Egor Khazin");
    }

    @Test
    void updateDoesNotPersistWhenMovieIsMissing() {
        when(movieRepository.findByIdWithRelations(404L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.update(404L, validDto()))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("Movie 404 not found");

        verify(movieRepository, never()).save(org.mockito.ArgumentMatchers.any(Movie.class));
        verify(messagingTemplate, never()).convertAndSend(eq("/topic/movies"), org.mockito.ArgumentMatchers.any(Object.class));
    }

    @Test
    void deleteRemovesMovieAndBroadcastsDeletedMessage() {
        Movie movie = new Movie();
        movie.setId(9L);
        when(movieRepository.findById(9L)).thenReturn(Optional.of(movie));

        service.delete(9L);

        verify(movieRepository).delete(movie);
        ArgumentCaptor<MovieChangeMessage> messageCaptor = ArgumentCaptor.forClass(MovieChangeMessage.class);
        verify(messagingTemplate).convertAndSend(eq("/topic/movies"), messageCaptor.capture());
        assertThat(messageCaptor.getValue().getType()).isEqualTo(ChangeType.DELETED);
        assertThat(messageCaptor.getValue().getId()).isEqualTo(9L);
        assertThat(messageCaptor.getValue().getPayload()).isNull();
    }

    private MovieFormDto validDto() {
        MovieFormDto dto = new MovieFormDto();
        dto.setName("Solar Road");
        dto.getCoordinates().setX(12.5f);
        dto.getCoordinates().setY(90L);
        dto.setOscarsCount(2L);
        dto.setBudget(100000);
        dto.setTotalBoxOffice(150000.5f);
        dto.setMpaaRating(MpaaRating.PG_13);
        dto.setDirectorId(11L);
        dto.setScreenwriterId(12L);
        dto.setOperatorId(13L);
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
}
