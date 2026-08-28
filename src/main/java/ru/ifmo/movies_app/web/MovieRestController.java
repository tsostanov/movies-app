package ru.ifmo.movies_app.web;

import java.util.List;

import jakarta.validation.Valid;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
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
import ru.ifmo.movies_app.service.MovieService;

@RestController
@RequestMapping("/api/movies")
public class MovieRestController {

    private static final List<String> ALLOWED_SORTS = List.of(
            "name", "directorName", "screenwriterName", "operatorName", "genre", "mpaaRating");
    private static final int DEFAULT_PAGE_SIZE = 10;
    private static final int MAX_PAGE_SIZE = 100;

    private final MovieService movieService;

    public MovieRestController(MovieService movieService) {
        this.movieService = movieService;
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
        int safePage = Math.max(page, 0);
        int safeSize = normalizeSize(size);
        if (sort == null || !ALLOWED_SORTS.contains(sort)) {
            return PageRequest.of(safePage, safeSize);
        }
        Sort.Order order = new Sort.Order(
                "desc".equalsIgnoreCase(direction) ? Sort.Direction.DESC : Sort.Direction.ASC,
                sort);
        return PageRequest.of(safePage, safeSize, Sort.by(order));
    }

    private int normalizeSize(int size) {
        if (size <= 0) {
            return DEFAULT_PAGE_SIZE;
        }
        return Math.min(size, MAX_PAGE_SIZE);
    }
}
