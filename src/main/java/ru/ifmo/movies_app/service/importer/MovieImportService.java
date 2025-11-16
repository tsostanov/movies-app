package ru.ifmo.movies_app.service.importer;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;

import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.multipart.MultipartFile;

import ru.ifmo.movies_app.domain.ImportOperation;
import ru.ifmo.movies_app.domain.ImportStatus;
import ru.ifmo.movies_app.dto.ImportOperationDto;
import ru.ifmo.movies_app.dto.LocationDto;
import ru.ifmo.movies_app.dto.MovieFormDto;
import ru.ifmo.movies_app.dto.MovieImportRequest;
import ru.ifmo.movies_app.dto.importer.LocationPayloadDto;
import ru.ifmo.movies_app.dto.importer.MovieImportDto;
import ru.ifmo.movies_app.dto.importer.PersonPayloadDto;
import ru.ifmo.movies_app.dto.importer.PersonPayloadDto.PersonInlineDto;
import ru.ifmo.movies_app.repository.ImportOperationRepository;
import ru.ifmo.movies_app.service.LocationService;
import ru.ifmo.movies_app.service.MovieService;
import ru.ifmo.movies_app.service.PersonService;

@Service
public class MovieImportService {

    private final MovieImportParser parser;
    private final MovieService movieService;
    private final PersonService personService;
    private final LocationService locationService;
    private final ImportOperationRepository importOperationRepository;
    private final Validator validator;
    private final TransactionTemplate transactionTemplate;

    public MovieImportService(MovieImportParser parser,
            MovieService movieService,
            PersonService personService,
            LocationService locationService,
            ImportOperationRepository importOperationRepository,
            Validator validator,
            PlatformTransactionManager transactionManager) {
        this.parser = parser;
        this.movieService = movieService;
        this.personService = personService;
        this.locationService = locationService;
        this.importOperationRepository = importOperationRepository;
        this.validator = validator;
        this.transactionTemplate = new TransactionTemplate(transactionManager);
    }

    public ImportOperationDto importYaml(MultipartFile file, String username) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Файл для импорта не передан.");
        }
        String owner = requireUsername(username);
        ImportOperation operation = importOperationRepository.save(ImportOperation.start(owner));
        try (InputStream stream = file.getInputStream()) {
            MovieImportRequest request = parser.parse(stream);
            validateRequest(request);
            int added = transactionTemplate.execute(status -> {
                int processed = 0;
                for (MovieImportDto movieDto : request.getMovies()) {
                    MovieFormDto formDto = convert(movieDto);
                    validateMovie(formDto);
                    movieService.create(formDto);
                    processed++;
                }
                return processed;
            });
            operation.markSuccess(added);
            return toDto(importOperationRepository.save(operation));
        } catch (IOException e) {
            String message = "Не удалось прочитать YAML-файл.";
            operation.markFailed(message);
            importOperationRepository.save(operation);
            throw new ImportException(message, e);
        } catch (RuntimeException e) {
            String message = shortenMessage(e.getMessage());
            operation.markFailed(message);
            importOperationRepository.save(operation);
            throw new ImportException(message, e);
        }
    }

    public List<ImportOperationDto> getHistory(String username, boolean adminView) {
        List<ImportOperation> operations;
        if (adminView) {
            operations = importOperationRepository.findAllOrderByCreatedAtDesc();
        } else {
            operations = importOperationRepository.findByUsernameOrderByCreatedAtDesc(requireUsername(username));
        }
        return operations.stream()
                .map(this::toDto)
                .toList();
    }

    private void validateRequest(MovieImportRequest request) {
        Set<ConstraintViolation<MovieImportRequest>> violations = validator.validate(request);
        if (!violations.isEmpty()) {
            String message = violations.stream()
                    .map(v -> v.getPropertyPath() + " " + v.getMessage())
                    .collect(Collectors.joining("; "));
            throw new IllegalArgumentException("Некорректная структура файла импорта: " + message);
        }
    }

    private void validateMovie(MovieFormDto dto) {
        Set<ConstraintViolation<MovieFormDto>> violations = validator.validate(dto);
        if (!violations.isEmpty()) {
            String message = violations.stream()
                    .map(v -> v.getPropertyPath() + " " + v.getMessage())
                    .collect(Collectors.joining("; "));
            throw new IllegalArgumentException(
                    "Фильм \"" + (dto.getName() != null ? dto.getName() : "без названия")
                            + "\" содержит ошибки: " + message);
        }
    }

    private MovieFormDto convert(MovieImportDto source) {
        MovieFormDto target = new MovieFormDto();
        target.setName(source.getName());
        target.setCoordinates(source.getCoordinates());
        target.setOscarsCount(source.getOscarsCount());
        target.setBudget(source.getBudget());
        target.setTotalBoxOffice(source.getTotalBoxOffice());
        target.setMpaaRating(source.getMpaaRating());
        target.setDirectorId(resolvePerson(source.getDirector(), "режиссёра"));
        target.setScreenwriterId(requirePerson(source.getScreenwriter(), "сценариста"));
        target.setOperatorId(resolvePerson(source.getOperator(), "оператора"));
        target.setLength(source.getLength());
        target.setGoldenPalmCount(source.getGoldenPalmCount());
        target.setGenre(source.getGenre());
        return target;
    }

    private Long requirePerson(PersonPayloadDto payload, String role) {
        Long personId = resolvePerson(payload, role);
        if (personId == null) {
            throw new IllegalArgumentException("Для роли " + role + " необходимо указать id или заполнить блок data.");
        }
        return personId;
    }

    private Long resolvePerson(PersonPayloadDto payload, String role) {
        if (payload == null) {
            return null;
        }
        if (payload.getId() != null) {
            personService.getPersonById(payload.getId());
            return payload.getId();
        }
        PersonInlineDto data = payload.getData();
        if (data == null) {
            throw new IllegalArgumentException("Для роли " + role + " нужно указать id или описать человека во вложенном объекте data.");
        }
        Long locationId = resolveLocation(data);
        data.setLocationId(locationId);
        return personService.create(data).getId();
    }

    private Long resolveLocation(PersonInlineDto data) {
        if (data.getLocationId() != null) {
            return data.getLocationId();
        }
        LocationPayloadDto locationPayload = data.getLocation();
        if (locationPayload == null) {
            return null;
        }
        LocationDto dto = new LocationDto();
        dto.setName(locationPayload.getName());
        dto.setX(locationPayload.getX());
        dto.setY(locationPayload.getY());
        return locationService.create(dto).getId();
    }

    private ImportOperationDto toDto(ImportOperation importOperation) {
        return new ImportOperationDto(
                importOperation.getId(),
                importOperation.getUsername(),
                importOperation.getStatus(),
                importOperation.getCreatedAt(),
                importOperation.getCompletedAt(),
                importOperation.getAddedCount(),
                importOperation.getErrorMessage());
    }

    private String requireUsername(String username) {
        if (username == null || username.isBlank()) {
            throw new IllegalArgumentException("Не удалось определить пользователя для операции импорта.");
        }
        return username;
    }

    private String shortenMessage(String message) {
        if (message == null) {
            return ImportStatus.FAILED.name();
        }
        return message.length() > 500 ? message.substring(0, 500) + "..." : message;
    }
}
