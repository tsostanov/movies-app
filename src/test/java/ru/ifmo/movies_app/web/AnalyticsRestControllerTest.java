package ru.ifmo.movies_app.web;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import ru.ifmo.movies_app.dto.MovieTableRowDto;
import ru.ifmo.movies_app.service.MovieAnalyticsService;

class AnalyticsRestControllerTest {

    private MovieAnalyticsService movieAnalyticsService;
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        movieAnalyticsService = mock(MovieAnalyticsService.class);
        mockMvc = MockMvcBuilders
                .standaloneSetup(new AnalyticsRestController(movieAnalyticsService))
                .setControllerAdvice(new RestExceptionHandler())
                .build();
    }

    @Test
    void nameSearchReturnsBadRequestWhenServiceRejectsSubstring() throws Exception {
        when(movieAnalyticsService.findMoviesWithNameContaining(" "))
                .thenThrow(new IllegalArgumentException("Search substring must not be blank"));

        mockMvc.perform(get("/api/analytics/name-search").param("substring", " "))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Search substring must not be blank"));
    }

    @Test
    void nameSearchReturnsStructuredBadRequestWhenSubstringIsMissing() throws Exception {
        mockMvc.perform(get("/api/analytics/name-search"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Request parameters are invalid"))
                .andExpect(jsonPath("$.details.substring").value("Required request parameter is missing"));
    }

    @Test
    void genreCountReturnsStructuredBadRequestWhenGenreCannotBeParsed() throws Exception {
        mockMvc.perform(get("/api/analytics/genre-count").param("genre", "NOT_A_GENRE"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Request parameters are invalid"))
                .andExpect(jsonPath("$.details.genre").value("Invalid value: NOT_A_GENRE"));
    }

    @Test
    void nameSearchReturnsRowsFromService() throws Exception {
        when(movieAnalyticsService.findMoviesWithNameContaining("Solar"))
                .thenReturn(List.of(new MovieTableRowDto(
                        7L,
                        "Solar Road",
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
                        null)));

        mockMvc.perform(get("/api/analytics/name-search").param("substring", "Solar"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(7))
                .andExpect(jsonPath("$[0].name").value("Solar Road"));
    }
}
