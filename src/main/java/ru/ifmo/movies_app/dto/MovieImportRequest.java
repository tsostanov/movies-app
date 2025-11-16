package ru.ifmo.movies_app.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;

import java.util.ArrayList;
import java.util.List;

import ru.ifmo.movies_app.dto.importer.MovieImportDto;

/**
 * DTO, описывающее структуру YAML-файла для импорта фильмов.
 * Формат:
 * <pre>
 * movies:
 *   - name: ...
 *     coordinates:
 *       x: ...
 *       y: ...
 *     ...
 * </pre>
 */
public class MovieImportRequest {

    @Valid
    @NotEmpty
    private List<MovieImportDto> movies = new ArrayList<>();

    public List<MovieImportDto> getMovies() {
        return movies;
    }

    public void setMovies(List<MovieImportDto> movies) {
        this.movies = movies != null ? movies : new ArrayList<>();
    }
}
