package ru.ifmo.movies_app.service.importer;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

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
import ru.ifmo.movies_app.domain.Person;
import ru.ifmo.movies_app.dto.ImportOperationDto;
import ru.ifmo.movies_app.dto.LocationDto;
import ru.ifmo.movies_app.dto.MovieFormDto;
import ru.ifmo.movies_app.dto.MovieImportRequest;
import ru.ifmo.movies_app.dto.PageResponse;
import ru.ifmo.movies_app.dto.importer.LocationPayloadDto;
import ru.ifmo.movies_app.dto.importer.MovieImportDto;
import ru.ifmo.movies_app.dto.importer.PersonPayloadDto;
import ru.ifmo.movies_app.dto.importer.PersonPayloadDto.PersonInlineDto;
import ru.ifmo.movies_app.minio.MinioObjectStorage;
import ru.ifmo.movies_app.repository.ImportOperationRepository;
import ru.ifmo.movies_app.service.LocationService;
import ru.ifmo.movies_app.service.MovieService;
import ru.ifmo.movies_app.service.NotFoundException;
import ru.ifmo.movies_app.service.PersonService;
import ru.ifmo.movies_app.support.ImportFailpointToggle;

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
    private final MinioObjectStorage objectStorage;
    private final ImportFailpointToggle failpointToggle;

    public MovieImportService(MovieImportParser parser,
                              MovieService movieService,
                              PersonService personService,
                              LocationService locationService,
                              ImportOperationRepository importOperationRepository,
                              Validator validator,
                              PlatformTransactionManager transactionManager,
                              MinioObjectStorage objectStorage,
                              ImportFailpointToggle failpointToggle) {
        this.parser = parser;
        this.movieService = movieService;
        this.personService = personService;
        this.locationService = locationService;
        this.importOperationRepository = importOperationRepository;
        this.validator = validator;
        this.transactionTemplate = new TransactionTemplate(transactionManager);
        this.objectStorage = objectStorage;
        this.failpointToggle = failpointToggle;
    }

    public ImportOperationDto importYaml(MultipartFile file, String username) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("ýøü> ?>‘? ñ?õ?‘?‘'ø ?ç õç‘?ç?ø?.");
        }
        byte[] content = readBytes(file);
        String owner = requireUsername(username);
        String txId = UUID.randomUUID().toString();
        String filename = normalizeFilename(file.getOriginalFilename(), txId);
        String contentType = file.getContentType();
        String stagingKey = buildStagingKey(txId, filename);

        ImportOperation operation = ImportOperation.start(owner, txId, filename, contentType);
        operation.markFilePrepared(stagingKey);
        importOperationRepository.save(operation);

        try {
            objectStorage.putBytes(stagingKey, content, contentType);
            failpointToggle.afterFileUpload();

            MovieImportRequest request = parser.parse(new ByteArrayInputStream(content));
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

            operation.markPendingFileCommit(added);
            importOperationRepository.save(operation);
            failpointToggle.afterDbCommit();

            finalizeFileCommit(operation, stagingKey, filename);
            importOperationRepository.save(operation);
            return toDto(operation);
        } catch (ImportException ex) {
            throw ex;
        } catch (RuntimeException | IOException ex) {
            handleException(operation, stagingKey, ex);
            throw new ImportException(shortenMessage(ex.getMessage()), ex);
        } catch (Exception ex) {
            handleException(operation, stagingKey, ex);
            throw new ImportException(shortenMessage(ex.getMessage()), ex);
        }
    }

    public int recoverPendingFileCommits(int limit) {
        List<ImportOperation> pending = importOperationRepository.findPendingFileCommits(limit);
        int recovered = 0;
        for (ImportOperation op : pending) {
            String stagingKey = op.getStagingKey();
            if (stagingKey == null || stagingKey.isBlank()) {
                continue;
            }
            String filename = normalizeFilename(op.getOriginalFilename(), op.getTxId());
            try {
                finalizeFileCommit(op, stagingKey, filename);
                importOperationRepository.save(op);
                recovered++;
            } catch (Exception ex) {
                log.warn("Failed to recover import operation {}: {}", op.getId(), ex.getMessage());
            }
        }
        return recovered;
    }

    public ImportFileResource loadImportFile(Long id) {
        ImportOperation operation = importOperationRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Import operation %d not found".formatted(id)));
        if (!operation.hasFile()) {
            throw new IllegalStateException("?‘?ó ??ç??ø ?>‘? +>?óøú ñ?õ?‘?‘'");
        }
        String filename = normalizeFilename(operation.getOriginalFilename(), operation.getTxId());
        String contentType = operation.getFileContentType() != null
                ? operation.getFileContentType()
                : "application/octet-stream";
        try {
            InputStream stream = objectStorage.getObject(operation.getFileKey());
            return new ImportFileResource(stream, filename, contentType);
        } catch (Exception ex) {
            throw new ImportException("?ç ‘??ø>?‘?‘? >øç?øø ñ?õ?‘?‘'", ex);
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
            throw new IllegalArgumentException("?çó?‘?‘?çó‘'?ø‘? ‘?‘'‘?‘?ó‘'‘?‘?ø ‘\"øü>ø ñ?õ?‘?‘'ø: " + message);
        }
    }

    private void validateMovie(MovieFormDto dto) {
        Set<ConstraintViolation<MovieFormDto>> violations = validator.validate(dto);
        if (!violations.isEmpty()) {
            String message = violations.stream()
                    .map(v -> v.getPropertyPath() + " " + v.getMessage())
                    .collect(Collectors.joining("; "));
            throw new IllegalArgumentException(
                    "ýñ>‘?? \"" + (dto.getName() != null ? dto.getName() : "+çú ?øú?ø?ñ‘?")
                            + "\" ‘???ç‘?ñ‘' ?‘?ñ+óñ: " + message);
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
        target.setDirectorId(resolvePerson(source.getDirector(), "‘?çñ‘?‘?‘'‘?ø"));
        PersonPayloadDto screenwriterPayload = source.getScreenwriter();
        target.setScreenwriterId(requirePerson(screenwriterPayload, "‘?‘Åç?ø‘?ñ‘?‘'ø"));
        target.setOperatorId(resolvePerson(source.getOperator(), "?õç‘?ø‘'?‘?ø"));
        target.setLength(source.getLength());
        target.setGoldenPalmCount(source.getGoldenPalmCount());
        target.setGenre(source.getGenre());
        return target;
    }

    private Long requirePerson(PersonPayloadDto payload, String role) {
        Long personId = resolvePerson(payload, role);
        if (personId == null) {
            throw new IllegalArgumentException("\">‘? ‘??>ñ " + role + " ?ç?+‘:??ñ?? ‘?óøúø‘'‘? id ñ>ñ ?õñ‘?ø‘'‘? ‘Øç>??çóø ? ?ø??‘<‘: ñ?õ?‘?‘'ø.");
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
            throw new IllegalArgumentException("\">‘? ‘??>ñ " + role + " ?‘??? ‘?óøúø‘'‘? id ñ>ñ úøõ?>?ñ‘'‘? õ?>‘? ‘Øç>??çóø.");
        }
        validatePersonData(data, role);
        Optional<Person> existing = personService.findByName(data.getName());
        if (existing.isPresent()) {
            return existing.get().getId();
        }
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
            throw new IllegalArgumentException("\"ø??‘<ç ?>‘? ‘??>ñ " + role + " ‘???ç‘?ø‘' ?‘?ñ+óñ: " + message);
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
                importOperation.getErrorMessage(),
                importOperation.getOriginalFilename(),
                importOperation.hasFile());
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

    private byte[] readBytes(MultipartFile file) {
        try {
            return file.getBytes();
        } catch (IOException ex) {
            throw new ImportException("?ç ‘??ø>?‘?‘? ?çó??ñ?? ñ?õ?‘?‘'ø", ex);
        }
    }

    private void finalizeFileCommit(ImportOperation operation, String stagingKey, String filename) throws Exception {
        String finalKey = buildFinalKey(operation.getId(), filename);
        objectStorage.copyObject(stagingKey, finalKey);
        objectStorage.removeQuietly(stagingKey);
        int added = operation.getAddedCount() != null ? operation.getAddedCount() : 0;
        operation.markSuccess(added, finalKey);
    }

    private void handleException(ImportOperation operation, String stagingKey, Exception ex) {
        String message = shortenMessage(ex.getMessage());
        if (operation.getStatus() == ImportStatus.PENDING_FILE_COMMIT) {
            operation.setErrorMessage(message);
            importOperationRepository.save(operation);
            return;
        }
        operation.markFailed(message);
        importOperationRepository.save(operation);
        objectStorage.removeQuietly(stagingKey);
    }

    private String buildStagingKey(String txId, String filename) {
        return "staging/" + txId + "/" + filename;
    }

    private String buildFinalKey(Long id, String filename) {
        return "imports/" + id + "/" + filename;
    }

    private String normalizeFilename(String originalFilename, String txId) {
        String fallback = "import-" + txId + ".yaml";
        if (originalFilename == null || originalFilename.isBlank()) {
            return fallback;
        }
        String trimmed = originalFilename.replace("\\", "/");
        String nameOnly = trimmed.substring(trimmed.lastIndexOf('/') + 1);
        String sanitized = nameOnly.replaceAll("[^A-Za-z0-9._-]", "_");
        return sanitized.isBlank() ? fallback : sanitized;
    }

    public record ImportFileResource(InputStream stream, String filename, String contentType) {
    }
}
