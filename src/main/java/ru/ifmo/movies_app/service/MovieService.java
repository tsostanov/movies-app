package ru.ifmo.movies_app.service;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import ru.ifmo.movies_app.domain.Coordinates;
import ru.ifmo.movies_app.domain.Movie;
import ru.ifmo.movies_app.domain.Person;
import ru.ifmo.movies_app.dto.MovieChangeMessage;
import ru.ifmo.movies_app.dto.MovieChangeMessage.ChangeType;
import ru.ifmo.movies_app.dto.MovieDetailsDto;
import ru.ifmo.movies_app.dto.MovieFormDto;
import ru.ifmo.movies_app.dto.MovieTableFilter;
import ru.ifmo.movies_app.dto.MovieTableRowDto;
import ru.ifmo.movies_app.repository.MovieRepository;
import ru.ifmo.movies_app.repository.PersonRepository;

@Service
public class MovieService {

    private final MovieRepository movieRepository;
    private final PersonRepository personRepository;
    private final SimpMessagingTemplate messagingTemplate;

    public MovieService(MovieRepository movieRepository,
                        PersonRepository personRepository,
                        SimpMessagingTemplate messagingTemplate) {
        this.movieRepository = movieRepository;
        this.personRepository = personRepository;
        this.messagingTemplate = messagingTemplate;
    }

    @Transactional(readOnly = true)
    public Page<MovieTableRowDto> getMovies(MovieTableFilter filter, Pageable pageable) {
        Page<Movie> page = movieRepository.findAll(filter, pageable);
        return page.map(this::toTableRowDto);
    }

    @Transactional(readOnly = true)
    public MovieDetailsDto getDetails(Long id) {
        Movie movie = movieRepository.findByIdWithRelations(id)
                .orElseThrow(() -> new NotFoundException("Movie %d not found".formatted(id)));
        return toDetailsDto(movie);
    }

    @Transactional(readOnly = true)
    public MovieFormDto getForm(Long id) {
        Movie movie = movieRepository.findByIdWithRelations(id)
                .orElseThrow(() -> new NotFoundException("Movie %d not found".formatted(id)));
        return toFormDto(movie);
    }

    @Transactional
    public MovieDetailsDto create(MovieFormDto dto) {
        Movie movie = new Movie();
        applyDto(dto, movie);
        Movie saved = movieRepository.save(movie);
        MovieTableRowDto row = toTableRowDto(saved);
        broadcast(new MovieChangeMessage(ChangeType.CREATED, saved.getId(), row));
        return toDetailsDto(saved);
    }

    @Transactional
    public MovieDetailsDto update(Long id, MovieFormDto dto) {
        Movie movie = movieRepository.findByIdWithRelations(id)
                .orElseThrow(() -> new NotFoundException("Movie %d not found".formatted(id)));
        applyDto(dto, movie);
        Movie saved = movieRepository.save(movie);
        MovieTableRowDto row = toTableRowDto(saved);
        broadcast(new MovieChangeMessage(ChangeType.UPDATED, saved.getId(), row));
        return toDetailsDto(saved);
    }

    @Transactional
    public void delete(Long id) {
        Movie movie = movieRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Movie %d not found".formatted(id)));
        movieRepository.delete(movie);
        broadcast(new MovieChangeMessage(ChangeType.DELETED, id, null));
    }

    public MovieTableRowDto mapToRow(Movie movie) {
        return toTableRowDto(movie);
    }

    private MovieTableRowDto toTableRowDto(Movie movie) {
        Coordinates coordinates = movie.getCoordinates();
        return new MovieTableRowDto(
                movie.getId(),
                movie.getName(),
                coordinates.getX(),
                coordinates.getY(),
                movie.getCreationDate(),
                movie.getOscarsCount(),
                movie.getBudget(),
                movie.getTotalBoxOffice(),
                movie.getMpaaRating(),
                Optional.ofNullable(movie.getDirector()).map(Person::getName).orElse(null),
                Optional.ofNullable(movie.getScreenwriter()).map(Person::getName).orElse(null),
                Optional.ofNullable(movie.getOperator()).map(Person::getName).orElse(null),
                movie.getLength(),
                movie.getGoldenPalmCount(),
                movie.getGenre()
        );
    }

    private MovieDetailsDto toDetailsDto(Movie movie) {
        Coordinates coordinates = movie.getCoordinates();
        return new MovieDetailsDto(
                movie.getId(),
                movie.getName(),
                coordinates.getX(),
                coordinates.getY(),
                movie.getCreationDate(),
                movie.getOscarsCount(),
                movie.getBudget(),
                movie.getTotalBoxOffice(),
                movie.getMpaaRating(),
                movie.getDirector(),
                movie.getScreenwriter(),
                movie.getOperator(),
                movie.getLength(),
                movie.getGoldenPalmCount(),
                movie.getGenre()
        );
    }

    private MovieFormDto toFormDto(Movie movie) {
        MovieFormDto dto = new MovieFormDto();
        dto.setId(movie.getId());
        dto.setName(movie.getName());
        Coordinates coordinates = movie.getCoordinates();
        dto.getCoordinates().setX(coordinates.getX());
        dto.getCoordinates().setY(coordinates.getY());
        dto.setOscarsCount(movie.getOscarsCount());
        dto.setBudget(movie.getBudget());
        dto.setTotalBoxOffice(movie.getTotalBoxOffice());
        dto.setMpaaRating(movie.getMpaaRating());
        dto.setDirectorId(Optional.ofNullable(movie.getDirector()).map(Person::getId).orElse(null));
        dto.setScreenwriterId(Optional.ofNullable(movie.getScreenwriter()).map(Person::getId).orElse(null));
        dto.setOperatorId(Optional.ofNullable(movie.getOperator()).map(Person::getId).orElse(null));
        dto.setLength(movie.getLength());
        dto.setGoldenPalmCount(movie.getGoldenPalmCount());
        dto.setGenre(movie.getGenre());
        return dto;
    }

    private void applyDto(MovieFormDto dto, Movie movie) {
        movie.setName(dto.getName());
        if (movie.getCoordinates() == null) {
            movie.setCoordinates(new Coordinates());
        }
        movie.getCoordinates().setX(dto.getCoordinates().getX());
        movie.getCoordinates().setY(dto.getCoordinates().getY());
        movie.setOscarsCount(dto.getOscarsCount());
        movie.setBudget(dto.getBudget());
        movie.setTotalBoxOffice(dto.getTotalBoxOffice());
        movie.setMpaaRating(dto.getMpaaRating());
        movie.setDirector(resolvePerson(dto.getDirectorId()).orElse(null));
        if (dto.getScreenwriterId() == null) {
            throw new IllegalArgumentException("Screenwriter must be provided");
        }
        movie.setScreenwriter(resolvePerson(dto.getScreenwriterId())
                .orElseThrow(() -> new NotFoundException("Screenwriter not found")));
        movie.setOperator(resolvePerson(dto.getOperatorId()).orElse(null));
        movie.setLength(dto.getLength());
        movie.setGoldenPalmCount(dto.getGoldenPalmCount());
        movie.setGenre(dto.getGenre());
    }

    private Optional<Person> resolvePerson(Long id) {
        if (id == null) {
            return Optional.empty();
        }
        return personRepository.findById(id);
    }

    private void broadcast(MovieChangeMessage message) {
        messagingTemplate.convertAndSend("/topic/movies", message);
    }

    @Transactional
    public void persistAndBroadcast(Movie movie) {
        Movie saved = movieRepository.save(movie);
        MovieTableRowDto row = toTableRowDto(saved);
        broadcast(new MovieChangeMessage(ChangeType.UPDATED, saved.getId(), row));
    }
}
