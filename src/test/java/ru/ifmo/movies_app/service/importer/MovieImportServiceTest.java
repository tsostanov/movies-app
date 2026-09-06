package ru.ifmo.movies_app.service.importer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import jakarta.validation.Validation;
import jakarta.validation.Validator;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.SimpleTransactionStatus;

import ru.ifmo.movies_app.domain.Country;
import ru.ifmo.movies_app.domain.ImportOperation;
import ru.ifmo.movies_app.domain.ImportStatus;
import ru.ifmo.movies_app.domain.Location;
import ru.ifmo.movies_app.domain.MovieGenre;
import ru.ifmo.movies_app.domain.Person;
import ru.ifmo.movies_app.dto.ImportOperationDto;
import ru.ifmo.movies_app.dto.LocationDto;
import ru.ifmo.movies_app.dto.MovieFormDto;
import ru.ifmo.movies_app.dto.PageResponse;
import ru.ifmo.movies_app.dto.importer.PersonPayloadDto.PersonInlineDto;
import ru.ifmo.movies_app.repository.ImportOperationRepository;
import ru.ifmo.movies_app.service.LocationService;
import ru.ifmo.movies_app.service.MovieService;
import ru.ifmo.movies_app.service.PersonService;

class MovieImportServiceTest {

    private MovieService movieService;
    private PersonService personService;
    private LocationService locationService;
    private ImportOperationRepository importOperationRepository;
    private PlatformTransactionManager transactionManager;
    private MovieImportService service;

    @BeforeEach
    void setUp() {
        movieService = mock(MovieService.class);
        personService = mock(PersonService.class);
        locationService = mock(LocationService.class);
        importOperationRepository = mock(ImportOperationRepository.class);
        transactionManager = mock(PlatformTransactionManager.class);
        Validator validator = Validation.buildDefaultValidatorFactory().getValidator();
        service = new MovieImportService(
                new MovieImportParser(),
                movieService,
                personService,
                locationService,
                importOperationRepository,
                validator,
                transactionManager);

        when(importOperationRepository.save(any(ImportOperation.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(transactionManager.getTransaction(any(TransactionDefinition.class))).thenReturn(new SimpleTransactionStatus());
    }

    @Test
    void importYamlCreatesInlineRelationsPersistsMovieAndMarksOperationSuccessful() {
        Person existingScreenwriter = person(7L, "Known Writer");
        Person createdDirector = person(31L, "New Director");
        Location createdLocation = new Location(55.0, 12.5f, "Studio North");
        createdLocation.setId(22L);
        when(personService.getPersonById(7L)).thenReturn(existingScreenwriter);
        when(locationService.create(any(LocationDto.class))).thenReturn(createdLocation);
        when(personService.create(any(PersonInlineDto.class))).thenReturn(createdDirector);

        ImportOperationDto result = service.importYaml(file("""
                movies:
                  - name: "Signal Lake"
                    coordinates:
                      x: 10.5
                      y: 33
                    oscarsCount: 1
                    budget: 120000
                    totalBoxOffice: 250000.5
                    director:
                      data:
                        name: "New Director"
                        weight: 80.0
                        nationality: "SPAIN"
                        location:
                          name: "Studio North"
                          x: 55.0
                          y: 12.5
                    screenwriter:
                      id: 7
                    length: 95
                    goldenPalmCount: 1
                    genre: "FANTASY"
                """), "qa-user");

        assertThat(result.getUsername()).isEqualTo("qa-user");
        assertThat(result.getStatus()).isEqualTo(ImportStatus.SUCCESS);
        assertThat(result.getAddedCount()).isEqualTo(1);
        assertThat(result.getCompletedAt()).isNotNull();
        assertThat(result.getErrorMessage()).isNull();

        ArgumentCaptor<LocationDto> locationCaptor = ArgumentCaptor.forClass(LocationDto.class);
        verify(locationService).create(locationCaptor.capture());
        assertThat(locationCaptor.getValue().getName()).isEqualTo("Studio North");

        ArgumentCaptor<PersonInlineDto> personCaptor = ArgumentCaptor.forClass(PersonInlineDto.class);
        verify(personService).create(personCaptor.capture());
        assertThat(personCaptor.getValue().getName()).isEqualTo("New Director");
        assertThat(personCaptor.getValue().getLocationId()).isEqualTo(22L);

        ArgumentCaptor<MovieFormDto> movieCaptor = ArgumentCaptor.forClass(MovieFormDto.class);
        verify(movieService).create(movieCaptor.capture());
        MovieFormDto movie = movieCaptor.getValue();
        assertThat(movie.getName()).isEqualTo("Signal Lake");
        assertThat(movie.getDirectorId()).isEqualTo(31L);
        assertThat(movie.getScreenwriterId()).isEqualTo(7L);
        assertThat(movie.getGenre()).isEqualTo(MovieGenre.FANTASY);
        verify(transactionManager).commit(any());
    }

    @Test
    void importYamlMarksOperationFailedAndRollsBackWhenMovieCannotBeCreated() {
        when(personService.getPersonById(7L)).thenReturn(person(7L, "Known Writer"));
        when(movieService.create(any(MovieFormDto.class))).thenThrow(new IllegalArgumentException("duplicate movie"));

        assertThatThrownBy(() -> service.importYaml(file("""
                movies:
                  - name: "Signal Lake"
                    coordinates:
                      x: 10.5
                      y: 33
                    budget: 120000
                    totalBoxOffice: 250000.5
                    screenwriter:
                      id: 7
                    goldenPalmCount: 1
                    genre: "DRAMA"
                """), "qa-user"))
                .isInstanceOf(ImportException.class)
                .hasMessage("duplicate movie");

        ArgumentCaptor<ImportOperation> operations = ArgumentCaptor.forClass(ImportOperation.class);
        verify(importOperationRepository, org.mockito.Mockito.times(2)).save(operations.capture());
        ImportOperation failed = operations.getAllValues().get(1);
        assertThat(failed.getStatus()).isEqualTo(ImportStatus.FAILED);
        assertThat(failed.getErrorMessage()).isEqualTo("duplicate movie");
        assertThat(failed.getCompletedAt()).isNotNull();
        verify(transactionManager).rollback(any());
    }

    @Test
    void getHistoryUsesAdminRepositoryOnlyForAdminViewAndBoundsPaging() {
        ImportOperation first = ImportOperation.start("admin");
        first.markSuccess(2);
        when(importOperationRepository.findAll(0, 50)).thenReturn(List.of(first));
        when(importOperationRepository.countAll()).thenReturn(101L);

        PageResponse<ImportOperationDto> history = service.getHistory("ignored", true, -5, 1000);

        assertThat(history.getPage()).isZero();
        assertThat(history.getSize()).isEqualTo(50);
        assertThat(history.getTotalElements()).isEqualTo(101L);
        assertThat(history.getTotalPages()).isEqualTo(3);
        assertThat(history.getContent()).extracting(ImportOperationDto::getStatus)
                .containsExactly(ImportStatus.SUCCESS);
    }

    @Test
    void getHistoryRequiresUsernameForUserScopedView() {
        assertThatThrownBy(() -> service.getHistory(" ", false, 0, 5))
                .isInstanceOf(IllegalArgumentException.class);
    }

    private MockMultipartFile file(String yaml) {
        return new MockMultipartFile("file", "movies.yaml", "application/x-yaml", yaml.getBytes());
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
