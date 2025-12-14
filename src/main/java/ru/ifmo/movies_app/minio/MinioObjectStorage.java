package ru.ifmo.movies_app.minio;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import io.minio.BucketExistsArgs;
import io.minio.CopyObjectArgs;
import io.minio.CopySource;
import io.minio.GetObjectArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.RemoveObjectArgs;
import io.minio.StatObjectArgs;
import io.minio.errors.ErrorResponseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MinioObjectStorage {

    private static final Logger log = LoggerFactory.getLogger(MinioObjectStorage.class);

    private final MinioClient client;
    private final MinioProperties properties;

    public MinioObjectStorage(MinioClient client, MinioProperties properties) {
        this.client = client;
        this.properties = properties;
    }

    public void ensureBucket() throws Exception {
        boolean exists = client.bucketExists(BucketExistsArgs.builder()
                .bucket(properties.getBucket())
                .build());
        if (!exists) {
            client.makeBucket(
                    MakeBucketArgs.builder()
                            .bucket(properties.getBucket())
                            .region(properties.getRegion())
                            .build()
            );
        }
    }

    public void putBytes(String key, byte[] content, String contentType) throws Exception {
        try (ByteArrayInputStream stream = new ByteArrayInputStream(content)) {
            client.putObject(
                    PutObjectArgs.builder()
                            .bucket(properties.getBucket())
                            .object(key)
                            .stream(stream, content.length, -1)
                            .contentType(contentType != null ? contentType : "application/octet-stream")
                            .build()
            );
        }
    }

    public void copyObject(String sourceKey, String targetKey) throws Exception {
        client.copyObject(
                CopyObjectArgs.builder()
                        .bucket(properties.getBucket())
                        .object(targetKey)
                        .source(CopySource.builder()
                                .bucket(properties.getBucket())
                                .object(sourceKey)
                                .build())
                        .build()
        );
    }

    public InputStream getObject(String key) throws Exception {
        return client.getObject(GetObjectArgs.builder()
                .bucket(properties.getBucket())
                .object(key)
                .build());
    }

    public boolean exists(String key) {
        try {
            client.statObject(StatObjectArgs.builder()
                    .bucket(properties.getBucket())
                    .object(key)
                    .build());
            return true;
        } catch (ErrorResponseException ex) {
            if ("NoSuchKey".equalsIgnoreCase(ex.errorResponse().code())) {
                return false;
            }
            throw new RuntimeException(ex);
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    public void removeQuietly(String key) {
        try {
            client.removeObject(RemoveObjectArgs.builder()
                    .bucket(properties.getBucket())
                    .object(key)
                    .build());
        } catch (Exception ex) {
            log.warn("Failed to delete object {}", key, ex);
        }
    }
}
