package ru.ifmo.movies_app.domain;

import jakarta.persistence.Cacheable;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Enumerated;
import jakarta.persistence.EnumType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.OffsetDateTime;

import org.eclipse.persistence.annotations.Cache;
import org.eclipse.persistence.annotations.CacheType;

@Entity
@Cacheable
@Cache(type = CacheType.SOFT, size = 1000, expiry = 3600000)
@Table(name = "import_operations")
public class ImportOperation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "username", nullable = false)
    private String username;

    @Column(name = "tx_id", nullable = false, unique = true)
    private String txId;

    @Column(name = "original_filename")
    private String originalFilename;

    @Column(name = "file_content_type")
    private String fileContentType;

    @Column(name = "staging_key")
    private String stagingKey;

    @Column(name = "file_key")
    private String fileKey;

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

    private ImportOperation(String username, String txId, String originalFilename, String fileContentType) {
        this.username = username;
        this.txId = txId;
        this.originalFilename = originalFilename;
        this.fileContentType = fileContentType;
        this.status = ImportStatus.IN_PROGRESS;
        this.createdAt = OffsetDateTime.now();
    }

    public static ImportOperation start(String username, String txId, String originalFilename, String fileContentType) {
        String owner = username != null ? username : "anonymous";
        return new ImportOperation(owner, txId, originalFilename, fileContentType);
    }

    public void markFilePrepared(String stagingKey) {
        this.stagingKey = stagingKey;
    }

    public void markPendingFileCommit(int addedCount) {
        this.status = ImportStatus.PENDING_FILE_COMMIT;
        this.completedAt = null;
        this.addedCount = addedCount;
    }

    public void markSuccess(int addedCount, String fileKey) {
        this.status = ImportStatus.SUCCESS;
        this.completedAt = OffsetDateTime.now();
        this.addedCount = addedCount;
        this.errorMessage = null;
        this.fileKey = fileKey;
        this.stagingKey = null;
    }

    public void markFailed(String message) {
        this.status = ImportStatus.FAILED;
        this.completedAt = OffsetDateTime.now();
        this.errorMessage = message;
        this.addedCount = null;
        this.stagingKey = null;
        this.fileKey = null;
    }

    public Long getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public String getTxId() {
        return txId;
    }

    public String getOriginalFilename() {
        return originalFilename;
    }

    public String getFileContentType() {
        return fileContentType;
    }

    public String getStagingKey() {
        return stagingKey;
    }

    public String getFileKey() {
        return fileKey;
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

    public boolean hasFile() {
        return fileKey != null && !fileKey.isBlank();
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }
}
