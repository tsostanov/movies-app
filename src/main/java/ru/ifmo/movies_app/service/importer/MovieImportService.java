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
import ru.ifmo.movies_app.dto.PageResponse;
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
            e.printStackTrace();
            String message = shortenMessage(e.getMessage());
            operation.markFailed(message);
            importOperationRepository.save(operation);
            throw new ImportException(message, e);
        }
    }

    public PageResponse<ImportOperationDto> getHistory(String username, boolean adminView, int page, int size) {
        int boundedSize = normalizeSize(size);
        int safePage = Math.max(page, 0);
        int offset = safePage * boundedSize;
        List<ImportOperation> operations;
        long total;
        if (adminView) {
            operations = importOperationRepository.findAll(offset, boundedSize);
            total = importOperationRepository.countAll();
        } else {
            String owner = requireUsername(username);
            operations = importOperationRepository.findByUsername(owner, offset, boundedSize);
            total = importOperationRepository.countByUsername(owner);
        }
        List<ImportOperationDto> dtos = operations.stream()
                .map(this::toDto)
                .toList();
        return new PageResponse<>(dtos, safePage, boundedSize, total);
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
        PersonPayloadDto screenwriterPayload = source.getScreenwriter();
        target.setScreenwriterId(requirePerson(screenwriterPayload, "сценариста"));
        target.setOperatorId(resolvePerson(source.getOperator(), "оператора"));
        target.setLength(source.getLength());
        target.setGoldenPalmCount(source.getGoldenPalmCount());
        target.setGenre(source.getGenre());
        return target;
    }

    private Long requirePerson(PersonPayloadDto payload, String role) {
        Long personId = resolvePerson(payload, role);
        if (personId == null) {
            throw new IllegalArgumentException("Для роли " + role + " необходимо указать id или описать человека в данных импорта.");
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
        PersonInlineDto data = payload.resolveData();
        if (data == null) {
            throw new IllegalArgumentException("Для роли " + role + " нужно указать id или заполнить поля человека.");
        }
        validatePersonData(data, role);
        Long locationId = resolveLocation(data);
        data.setLocationId(locationId);
        return personService.create(data).getId();
    }

    private void validatePersonData(PersonInlineDto data, String role) {
        Set<ConstraintViolation<PersonInlineDto>> violations = validator.validate(data);
        if (!violations.isEmpty()) {
            String message = violations.stream()
                    .map(v -> v.getPropertyPath() + " " + v.getMessage())
                    .collect(Collectors.joining("; "));
            throw new IllegalArgumentException("Данные для роли " + role + " содержат ошибки: " + message);
        }
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

    private int normalizeSize(int requestedSize) {
        int value = requestedSize <= 0 ? 5 : requestedSize;
        return Math.min(value, 50);
    }

    private String requireUsername(String username) {
        if (username == null || username.isBlank()) {
            return "anonymous";
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
