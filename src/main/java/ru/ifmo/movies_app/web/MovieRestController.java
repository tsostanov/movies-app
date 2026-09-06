package ru.ifmo.movies_app.web;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import jakarta.validation.Valid;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import ru.ifmo.movies_app.dto.MovieDetailsDto;
import ru.ifmo.movies_app.dto.MovieFormDto;
import ru.ifmo.movies_app.dto.MovieTableFilter;
import ru.ifmo.movies_app.dto.MovieTableRowDto;
import ru.ifmo.movies_app.service.MovieCsvExportService;
import ru.ifmo.movies_app.service.MovieService;
import ru.ifmo.movies_app.service.PaginationSupport;

@RestController
@RequestMapping("/api/movies")
public class MovieRestController {

    private static final List<String> ALLOWED_SORTS = List.of(
            "name", "directorName", "screenwriterName", "operatorName", "genre", "mpaaRating");
    private static final int DEFAULT_PAGE_SIZE = 10;
    private static final int MAX_PAGE_SIZE = 100;
    private static final int EXPORT_PAGE_SIZE = 100;

    private final MovieService movieService;
    private final MovieCsvExportService movieCsvExportService;

    public MovieRestController(MovieService movieService, MovieCsvExportService movieCsvExportService) {
        this.movieService = movieService;
        this.movieCsvExportService = movieCsvExportService;
    }

    @GetMapping
    public Page<MovieTableRowDto> list(@ModelAttribute MovieTableFilter filter,
                                       @RequestParam(defaultValue = "0") int page,
                                       @RequestParam(defaultValue = "10") int size,
                                       @RequestParam(required = false) String sort,
                                       @RequestParam(required = false) String direction) {
        Pageable pageable = buildPageable(page, size, sort, direction);
        return movieService.getMovies(filter, pageable);
    }

    @GetMapping(value = "/export.csv", produces = "text/csv;charset=UTF-8")
    public ResponseEntity<String> exportCsv(@ModelAttribute MovieTableFilter filter,
                                            @RequestParam(required = false) String sort,
                                            @RequestParam(required = false) String direction) {
        applyRestSortParameters(filter, sort, direction);
        List<MovieTableRowDto> rows = loadAllRows(filter);
        return ResponseEntity.ok()
                .contentType(new MediaType("text", "csv", StandardCharsets.UTF_8))
                .header(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.attachment()
                        .filename("movies.csv", StandardCharsets.UTF_8)
                        .build()
                        .toString())
                .body(movieCsvExportService.export(rows));
    }

    @GetMapping("/{id}")
    public MovieDetailsDto get(@PathVariable Long id) {
        return movieService.getDetails(id);
    }

    @GetMapping("/{id}/form")
    public MovieFormDto getForm(@PathVariable Long id) {
        return movieService.getForm(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public MovieDetailsDto create(@Valid @RequestBody MovieFormDto dto) {
        return movieService.create(dto);
    }

    @PutMapping("/{id}")
    public MovieDetailsDto update(@PathVariable Long id, @Valid @RequestBody MovieFormDto dto) {
        return movieService.update(id, dto);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        movieService.delete(id);
    }

    private Pageable buildPageable(int page, int size, String sort, String direction) {
        return PaginationSupport.createPageable(
                page,
                size,
                sort,
                direction,
                ALLOWED_SORTS,
                DEFAULT_PAGE_SIZE,
                MAX_PAGE_SIZE);
    }

    private void applyRestSortParameters(MovieTableFilter filter, String sort, String direction) {
        if (!StringUtils.hasText(sort)) {
            return;
        }
        filter.setSortBy(sort);
        filter.setSortDirection(direction);
    }

    private List<MovieTableRowDto> loadAllRows(MovieTableFilter filter) {
        List<MovieTableRowDto> rows = new ArrayList<>();
        int pageNumber = 0;
        Page<MovieTableRowDto> page;
        do {
            Pageable pageable = buildPageable(pageNumber, EXPORT_PAGE_SIZE, filter.getSortBy(), filter.getSortDirection());
            page = movieService.getMovies(filter, pageable);
            rows.addAll(page.getContent());
            pageNumber++;
        } while (page.hasNext());
        return rows;
    }
}
