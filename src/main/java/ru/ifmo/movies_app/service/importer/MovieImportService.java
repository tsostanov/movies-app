package ru.ifmo.movies_app.service.importer;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.multipart.MultipartFile;

import ru.ifmo.movies_app.domain.ImportOperation;
import ru.ifmo.movies_app.domain.ImportStatus;
import ru.ifmo.movies_app.dto.ImportOperationDto;
import ru.ifmo.movies_app.dto.LocationDto;
import ru.ifmo.movies_app.dto.MovieFormDto;
import ru.ifmo.movies_app.dto.MovieImportPreviewDto;
import ru.ifmo.movies_app.dto.MovieImportPreviewRowDto;
import ru.ifmo.movies_app.dto.PageResponse;
import ru.ifmo.movies_app.dto.MovieImportRequest;
import ru.ifmo.movies_app.dto.importer.LocationPayloadDto;
import ru.ifmo.movies_app.dto.importer.MovieImportDto;
import ru.ifmo.movies_app.dto.importer.PersonPayloadDto;
import ru.ifmo.movies_app.dto.importer.PersonPayloadDto.PersonInlineDto;
import ru.ifmo.movies_app.repository.ImportOperationRepository;
import ru.ifmo.movies_app.service.LocationService;
import ru.ifmo.movies_app.service.MovieService;
import ru.ifmo.movies_app.service.PaginationSupport;
import ru.ifmo.movies_app.service.PersonService;

@Service
public class MovieImportService {

    private static final Logger log = LoggerFactory.getLogger(MovieImportService.class);

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
            log.warn("Movie import failed for user {}: {}", owner, message);
            log.debug("Movie import failure details for user {}", owner, e);
            operation.markFailed(message);
            importOperationRepository.save(operation);
            throw new ImportException(message, e);
        }
    }

    public MovieImportPreviewDto previewYaml(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Файл для предпросмотра не передан.");
        }
        try (InputStream stream = file.getInputStream()) {
            MovieImportRequest request = parser.parse(stream);
            validateRequest(request);
            List<MovieImportDto> movies = request.getMovies();
            List<MovieImportPreviewRowDto> rows = IntStream.range(0, Math.min(movies.size(), 20))
                    .mapToObj(index -> toPreviewRow(index + 1, movies.get(index)))
                    .toList();
            return new MovieImportPreviewDto(movies.size(), rows);
        } catch (IOException e) {
            throw new ImportException("Не удалось прочитать YAML-файл.", e);
        } catch (RuntimeException e) {
            throw new ImportException(shortenMessage(e.getMessage()), e);
        }
    }

    public PageResponse<ImportOperationDto> getHistory(String username, boolean adminView, int page, int size) {
        int boundedSize = PaginationSupport.normalizeSize(size, 5, 50);
        int safePage = PaginationSupport.normalizePage(page);
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

    private MovieImportPreviewRowDto toPreviewRow(int rowNumber, MovieImportDto movie) {
        return new MovieImportPreviewRowDto(
                rowNumber,
                movie.getName(),
                movie.getGenre(),
                movie.getMpaaRating(),
                describePerson(movie.getDirector()),
                describePerson(movie.getScreenwriter()),
                describePerson(movie.getOperator()));
    }

    private String describePerson(PersonPayloadDto payload) {
        if (payload == null) {
            return "—";
        }
        if (payload.getId() != null) {
            return "ID " + payload.getId();
        }
        PersonInlineDto data = payload.resolveData();
        if (data == null || data.getName() == null || data.getName().isBlank()) {
            return "новая персона";
        }
        return "новая персона: " + data.getName();
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
