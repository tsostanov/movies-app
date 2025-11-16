package ru.ifmo.movies_app.domain;

import jakarta.persistence.*;

import java.time.OffsetDateTime;

@Entity
@Table(name = "import_operations")
public class ImportOperation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "username", nullable = false)
    private String username;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private ImportStatus status;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    @Column(name = "completed_at")
    private OffsetDateTime completedAt;

    @Column(name = "added_count")
    private Integer addedCount;

    @Column(name = "error_message", length = 2000)
    private String errorMessage;

    protected ImportOperation() {
    }

    private ImportOperation(String username) {
        this.username = username;
        this.status = ImportStatus.IN_PROGRESS;
        this.createdAt = OffsetDateTime.now();
    }

    public static ImportOperation start(String username) {
        return new ImportOperation(username != null ? username : "anonymous");
    }

    public void markSuccess(int addedCount) {
        this.status = ImportStatus.SUCCESS;
        this.completedAt = OffsetDateTime.now();
        this.addedCount = addedCount;
        this.errorMessage = null;
    }

    public void markFailed(String message) {
        this.status = ImportStatus.FAILED;
        this.completedAt = OffsetDateTime.now();
        this.errorMessage = message;
        this.addedCount = null;
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
