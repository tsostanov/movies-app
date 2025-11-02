package ru.ifmo.movies_app.web;

import java.util.List;
import java.util.stream.Collectors;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import ru.ifmo.movies_app.domain.Location;
import ru.ifmo.movies_app.domain.Person;
import ru.ifmo.movies_app.dto.PersonCreateRequest;
import ru.ifmo.movies_app.dto.PersonDetailsDto;
import ru.ifmo.movies_app.dto.PersonFormDto;
import ru.ifmo.movies_app.dto.PersonReassignmentDto;
import ru.ifmo.movies_app.service.PersonService;

@RestController
@RequestMapping("/api/persons")
public class PersonRestController {

    private final PersonService personService;

    public PersonRestController(PersonService personService) {
        this.personService = personService;
    }

    @GetMapping
    public List<PersonDetailsDto> list() {
        return personService.getAllPersons().stream()
                .map(this::toDetailsDto)
                .collect(Collectors.toList());
    }

    @GetMapping("/{id}")
    public PersonFormDto get(@PathVariable Long id) {
        return personService.getForm(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public PersonDetailsDto create(@Valid @RequestBody PersonCreateRequest dto) {
        Person created = personService.create(dto);
        return toDetailsDto(created);
    }

    @PutMapping("/{id}")
    public PersonDetailsDto update(@PathVariable Long id, @Valid @RequestBody PersonFormDto dto) {
        Person updated = personService.update(id, dto);
        return toDetailsDto(updated);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id,
                       @RequestBody(required = false) PersonReassignmentDto reassignment) {
        PersonReassignmentDto payload = reassignment != null ? reassignment : new PersonReassignmentDto();
        personService.delete(id, payload);
    }

    private PersonDetailsDto toDetailsDto(Person person) {
        Location location = person.getLocation();
        return new PersonDetailsDto(
                person.getId(),
                person.getName(),
                person.getEyeColor(),
                person.getHairColor(),
                person.getWeight(),
                person.getNationality(),
                location != null ? location.getId() : null,
                location != null ? location.getName() : null
        );
    }
}
