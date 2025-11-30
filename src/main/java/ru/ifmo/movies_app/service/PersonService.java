package ru.ifmo.movies_app.service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import ru.ifmo.movies_app.domain.Location;
import ru.ifmo.movies_app.domain.Movie;
import ru.ifmo.movies_app.domain.Person;
import ru.ifmo.movies_app.dto.PersonCreateRequest;
import ru.ifmo.movies_app.dto.PersonFormDto;
import ru.ifmo.movies_app.dto.PersonReassignmentDto;
import ru.ifmo.movies_app.dto.PersonSummaryDto;
import ru.ifmo.movies_app.repository.LocationRepository;
import ru.ifmo.movies_app.repository.MovieRepository;
import ru.ifmo.movies_app.repository.PersonRepository;

@Service
public class PersonService {

    private final PersonRepository personRepository;
    private final LocationRepository locationRepository;
    private final MovieRepository movieRepository;
    private final MovieService movieService;

    public PersonService(PersonRepository personRepository,
                         LocationRepository locationRepository,
                         MovieRepository movieRepository,
                         MovieService movieService) {
        this.personRepository = personRepository;
        this.locationRepository = locationRepository;
        this.movieRepository = movieRepository;
        this.movieService = movieService;
    }

    @Transactional(readOnly = true)
    public List<PersonSummaryDto> getAllSummaries() {
        return personRepository.findAll().stream()
                .map(person -> new PersonSummaryDto(person.getId(), person.getName()))
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<Person> getAllPersons() {
        return personRepository.findAllWithLocation();
    }

    @Transactional(readOnly = true)
    public Person getPersonById(Long id) {
        return personRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Персона %d не найдена".formatted(id)));
    }

    @Transactional(readOnly = true)
    public Optional<Person> findByName(String name) {
        return personRepository.findByNameIgnoreCase(name);
    }

    @Transactional(readOnly = true)
    public PersonFormDto getForm(Long id) {
        Person person = getPersonById(id);
        PersonFormDto dto = new PersonFormDto();
        dto.setId(person.getId());
        dto.setName(person.getName());
        dto.setEyeColor(person.getEyeColor());
        dto.setHairColor(person.getHairColor());
        dto.setWeight(person.getWeight());
        dto.setNationality(person.getNationality());
        dto.setLocationId(Optional.ofNullable(person.getLocation()).map(Location::getId).orElse(null));
        return dto;
    }

    @Transactional
    public Person create(PersonCreateRequest dto) {
        Person person = new Person();
        applyDto(dto, person);
        return personRepository.save(person);
    }

    @Transactional
    public Person update(Long id, PersonFormDto dto) {
        Person person = getPersonById(id);
        applyDto(dto, person);
        return personRepository.save(person);
    }

    @Transactional
    public void delete(Long id, PersonReassignmentDto reassignment) {
        Person person = getPersonById(id);
        reassignMovies(person, reassignment);
        personRepository.delete(person);
    }

    private void applyDto(PersonCreateRequest dto, Person person) {
        person.setName(dto.getName());
        person.setEyeColor(dto.getEyeColor());
        person.setHairColor(dto.getHairColor());
        person.setWeight(dto.getWeight());
        person.setNationality(dto.getNationality());
        if (dto.getLocationId() != null) {
            Location location = locationRepository.findById(dto.getLocationId())
                    .orElseThrow(() -> new NotFoundException("Локация не найдена"));
            person.setLocation(location);
        } else {
            person.setLocation(null);
        }
    }

    private void applyDto(PersonFormDto dto, Person person) {
        applyDto((PersonCreateRequest) dto, person);
    }

    private void reassignMovies(Person person, PersonReassignmentDto reassignment) {
        List<Movie> directed = movieRepository.findByDirector(person);
        if (!directed.isEmpty()) {
            Person replacement = requireReplacement(reassignment.getDirectorReplacementId(), person, "director");
            directed.forEach(movie -> movie.setDirector(replacement));
            directed.forEach(movieService::persistAndBroadcast);
        }

        List<Movie> written = movieRepository.findByScreenwriter(person);
        if (!written.isEmpty()) {
            Person replacement = requireReplacement(reassignment.getScreenwriterReplacementId(), person, "screenwriter");
            written.forEach(movie -> movie.setScreenwriter(replacement));
            written.forEach(movieService::persistAndBroadcast);
        }

        List<Movie> operated = movieRepository.findByOperator(person);
        if (!operated.isEmpty()) {
            Person replacement = requireReplacement(reassignment.getOperatorReplacementId(), person, "operator");
            operated.forEach(movie -> movie.setOperator(replacement));
            operated.forEach(movieService::persistAndBroadcast);
        }
    }

    private Person requireReplacement(Long replacementId, Person current, String role) {
        if (replacementId == null) {
            throw new IllegalArgumentException("Нужно выбрать замену для %s".formatted(roleInGenitive(role)));
        }
        if (replacementId.equals(current.getId())) {
            throw new IllegalArgumentException("Заменой не может быть удаляемый человек");
        }
        return personRepository.findById(replacementId)
                .orElseThrow(() -> new NotFoundException("Выбранная замена не найдена"));
    }

    private String roleInGenitive(String role) {
        return switch (role) {
            case "director" -> "режиссёра";
            case "screenwriter" -> "сценариста";
            case "operator" -> "оператора";
            default -> "сотрудника";
        };
    }
}
