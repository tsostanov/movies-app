package ru.ifmo.movies_app.web;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.nio.charset.StandardCharsets;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;

import ru.ifmo.movies_app.domain.MovieGenre;
import ru.ifmo.movies_app.dto.MovieTableFilter;
import ru.ifmo.movies_app.dto.MovieTableRowDto;
import ru.ifmo.movies_app.service.MovieCsvExportService;
import ru.ifmo.movies_app.service.MovieService;

class MovieRestControllerTest {

    private MovieService movieService;
    private MovieCsvExportService csvExportService;
    private MovieRestController controller;

    @BeforeEach
    void setUp() {
        movieService = mock(MovieService.class);
        csvExportService = mock(MovieCsvExportService.class);
        controller = new MovieRestController(movieService, csvExportService);
    }

    @Test
    void listClampsPageAndSizeAndIgnoresUnsupportedSort() {
        when(movieService.getMovies(any(MovieTableFilter.class), any(Pageable.class)))
                .thenAnswer(invocation -> new PageImpl<MovieTableRowDto>(List.of(), invocation.getArgument(1), 0));

        controller.list(new MovieTableFilter(), -3, 1000, "id", "desc");

        ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
        verify(movieService).getMovies(any(MovieTableFilter.class), pageableCaptor.capture());
        Pageable pageable = pageableCaptor.getValue();
        assertThat(pageable.getPageNumber()).isZero();
        assertThat(pageable.getPageSize()).isEqualTo(100);
        assertThat(pageable.getSort().isUnsorted()).isTrue();
    }

    @Test
    void listAppliesAllowedSortWithDescendingDirection() {
        when(movieService.getMovies(any(MovieTableFilter.class), any(Pageable.class)))
                .thenAnswer(invocation -> new PageImpl<MovieTableRowDto>(List.of(), invocation.getArgument(1), 0));

        controller.list(new MovieTableFilter(), 2, 25, "screenwriterName", "DESC");

        ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
        verify(movieService).getMovies(any(MovieTableFilter.class), pageableCaptor.capture());
        Sort.Order order = pageableCaptor.getValue().getSort().getOrderFor("screenwriterName");
        assertThat(order).isNotNull();
        assertThat(order.getDirection()).isEqualTo(Sort.Direction.DESC);
    }

    @Test
    @SuppressWarnings("unchecked")
    void exportCsvLoadsEveryPageAndReturnsDownloadResponse() {
        MovieTableRowDto first = row(1L, "First");
        MovieTableRowDto second = row(2L, "Second");
        when(movieService.getMovies(any(MovieTableFilter.class), eq(PageRequest.of(0, 100))))
                .thenReturn(new PageImpl<>(List.of(first), PageRequest.of(0, 100), 101));
        when(movieService.getMovies(any(MovieTableFilter.class), eq(PageRequest.of(1, 100))))
                .thenReturn(new PageImpl<>(List.of(second), PageRequest.of(1, 100), 101));
        when(csvExportService.export(any())).thenReturn("\uFEFFcsv");

        ResponseEntity<String> response = controller.exportCsv(new MovieTableFilter(), null, null);

        assertThat(response.getHeaders().getContentType().getType()).isEqualTo("text");
        assertThat(response.getHeaders().getContentType().getSubtype()).isEqualTo("csv");
        assertThat(response.getHeaders().getContentType().getCharset()).isEqualTo(StandardCharsets.UTF_8);
        assertThat(response.getHeaders().getFirst(HttpHeaders.CONTENT_DISPOSITION))
                .contains("attachment")
                .contains("movies.csv");
        assertThat(response.getBody()).isEqualTo("\uFEFFcsv");

        ArgumentCaptor<List<MovieTableRowDto>> rowsCaptor = ArgumentCaptor.forClass(List.class);
        verify(csvExportService).export(rowsCaptor.capture());
        assertThat(rowsCaptor.getValue()).extracting(MovieTableRowDto::getName)
                .containsExactly("First", "Second");
    }

    private MovieTableRowDto row(Long id, String name) {
        return new MovieTableRowDto(
                id,
                name,
                1.0f,
                2L,
                null,
                null,
                100,
                200.0f,
                null,
                null,
                "Writer",
                null,
                90L,
                1,
                MovieGenre.DRAMA);
    }
}
