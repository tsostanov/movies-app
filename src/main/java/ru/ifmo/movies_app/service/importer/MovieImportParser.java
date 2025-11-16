package ru.ifmo.movies_app.service.importer;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import org.springframework.stereotype.Component;
import ru.ifmo.movies_app.dto.MovieImportRequest;

import java.io.IOException;
import java.io.InputStream;

/**
 * Служебный класс, отвечающий за чтение YAML-файла и преобразование его в DTO.
 */
@Component
public class MovieImportParser {

    private final ObjectMapper objectMapper;

    public MovieImportParser() {
        this.objectMapper = new ObjectMapper(new YAMLFactory());
        this.objectMapper.findAndRegisterModules();
        this.objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    public MovieImportRequest parse(InputStream inputStream) throws IOException {
        MovieImportRequest request = objectMapper.readValue(inputStream, MovieImportRequest.class);
        if (request.getMovies() == null || request.getMovies().isEmpty()) {
            throw new IllegalArgumentException("Файл импорта не содержит фильмов");
        }
        return request;
    }
}
