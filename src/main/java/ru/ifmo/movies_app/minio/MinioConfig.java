package ru.ifmo.movies_app.minio;

import jakarta.annotation.PostConstruct;

import io.minio.MinioClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(MinioProperties.class)
public class MinioConfig {

    private static final Logger log = LoggerFactory.getLogger(MinioConfig.class);

    private final MinioProperties properties;
    private MinioObjectStorage objectStorage;

    public MinioConfig(MinioProperties properties) {
        this.properties = properties;
    }

    @Bean
    public MinioClient minioClient() {
        return MinioClient.builder()
                .endpoint(properties.getEndpoint())
                .credentials(properties.getAccessKey(), properties.getSecretKey())
                .build();
    }

    @Bean
    public MinioObjectStorage minioObjectStorage(MinioClient client) {
        this.objectStorage = new MinioObjectStorage(client, properties);
        return this.objectStorage;
    }

    @PostConstruct
    public void ensureBucket() {
        if (objectStorage == null) {
            return;
        }
        try {
            objectStorage.ensureBucket();
        } catch (Exception ex) {
            log.error("Failed to ensure MinIO bucket {}", properties.getBucket(), ex);
            throw new IllegalStateException("Cannot initialize MinIO bucket", ex);
        }
    }
}
