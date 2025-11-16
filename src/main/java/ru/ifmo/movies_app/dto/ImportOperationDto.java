package ru.ifmo.movies_app.dto;

import java.time.OffsetDateTime;

import ru.ifmo.movies_app.domain.ImportStatus;

public class ImportOperationDto {

    private final Long id;
    private final String username;
    private final ImportStatus status;
    private final OffsetDateTime createdAt;
    private final OffsetDateTime completedAt;
    private final Integer addedCount;
    private final String errorMessage;

    public ImportOperationDto(Long id,
                              String username,
                              ImportStatus status,
                              OffsetDateTime createdAt,
                              OffsetDateTime completedAt,
                              Integer addedCount,
                              String errorMessage) {
        this.id = id;
        this.username = username;
        this.status = status;
        this.createdAt = createdAt;
        this.completedAt = completedAt;
        this.addedCount = addedCount;
        this.errorMessage = errorMessage;
    }

    public Long getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public ImportStatus getStatus() {
        return status;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public OffsetDateTime getCompletedAt() {
        return completedAt;
    }

    public Integer getAddedCount() {
        return addedCount;
    }

    public String getErrorMessage() {
        return errorMessage;
    }
}
