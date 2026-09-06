package ru.ifmo.movies_app.web;

import static org.hamcrest.Matchers.hasKey;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import ru.ifmo.movies_app.service.LocationService;
import ru.ifmo.movies_app.service.NotFoundException;

class RestExceptionHandlerMvcTest {

    private LocationService locationService;
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        locationService = mock(LocationService.class);
        LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();
        validator.afterPropertiesSet();
        mockMvc = MockMvcBuilders
                .standaloneSetup(new LocationRestController(locationService))
                .setControllerAdvice(new RestExceptionHandler())
                .setValidator(validator)
                .build();
    }

    @Test
    void validationErrorReturnsBadRequestWithFieldDetails() throws Exception {
        mockMvc.perform(post("/api/locations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "x": null,
                                  "y": null,
                                  "name": ""
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").exists())
                .andExpect(jsonPath("$.details", hasKey("x")))
                .andExpect(jsonPath("$.details", hasKey("y")))
                .andExpect(jsonPath("$.details", hasKey("name")));
    }

    @Test
    void notFoundExceptionReturns404WithServiceMessage() throws Exception {
        when(locationService.getById(404L)).thenThrow(new NotFoundException("location missing"));

        mockMvc.perform(get("/api/locations/404"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("location missing"));
    }
}
