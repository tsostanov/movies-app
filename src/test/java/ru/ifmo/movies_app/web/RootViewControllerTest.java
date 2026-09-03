package ru.ifmo.movies_app.web;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.security.Principal;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.ui.ConcurrentModel;
import org.springframework.ui.Model;

import ru.ifmo.movies_app.dto.MovieTableFilter;
import ru.ifmo.movies_app.dto.MovieTableRowDto;
import ru.ifmo.movies_app.repository.ImportOperationRepository;
import ru.ifmo.movies_app.service.LocationService;
import ru.ifmo.movies_app.service.MovieService;
import ru.ifmo.movies_app.service.PersonService;

class RootViewControllerTest {

    private MovieService movieService;
    private PersonService personService;
    private LocationService locationService;
    private ImportOperationRepository importOperationRepository;
    private RootViewController controller;

    @BeforeEach
    void setUp() {
        movieService = mock(MovieService.class);
        personService = mock(PersonService.class);
        locationService = mock(LocationService.class);
        importOperationRepository = mock(ImportOperationRepository.class);
        controller = new RootViewController(movieService, personService, locationService, importOperationRepository);

        Page<MovieTableRowDto> emptyMoviePage = new PageImpl<>(List.of(), Pageable.ofSize(1), 7);
        when(movieService.getMovies(any(MovieTableFilter.class), any(Pageable.class))).thenReturn(emptyMoviePage);
        when(personService.getAllSummaries()).thenReturn(List.of());
        when(locationService.getAll()).thenReturn(List.of());
        when(importOperationRepository.countByUsername("user")).thenReturn(3L);
        when(importOperationRepository.findByUsername(eq("user"), eq(0), eq(5))).thenReturn(List.of());
    }

    @Test
    void indexBuildsHomeDashboardForCurrentUser() {
        Principal principal = () -> "user";
        Model model = new ConcurrentModel();

        String viewName = controller.index(principal, null, model);

        assertThat(viewName).isEqualTo("home/index");
        assertThat(model.getAttribute("currentUsername")).isEqualTo("user");
        assertThat(model.getAttribute("movieCount")).isEqualTo(7L);
        assertThat(model.getAttribute("importCount")).isEqualTo(3L);
        assertThat(model.getAttribute("recentImports")).isEqualTo(List.of());
    }

    @Test
    void indexUsesAllImportsForAdmin() {
        TestingAuthenticationToken authentication = new TestingAuthenticationToken("admin", null, "ROLE_ADMIN");
        when(importOperationRepository.countAll()).thenReturn(11L);
        when(importOperationRepository.findAll(0, 5)).thenReturn(List.of());
        Model model = new ConcurrentModel();

        String viewName = controller.index(() -> "admin", authentication, model);

        assertThat(viewName).isEqualTo("home/index");
        assertThat(model.getAttribute("isAdmin")).isEqualTo(true);
        assertThat(model.getAttribute("importCount")).isEqualTo(11L);
    }
}
