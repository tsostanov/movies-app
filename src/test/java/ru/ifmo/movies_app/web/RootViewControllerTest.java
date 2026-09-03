package ru.ifmo.movies_app.web;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class RootViewControllerTest {

    @Test
    void redirectToMoviesReturnsMoviesRedirect() {
        RootViewController controller = new RootViewController();

        String viewName = controller.redirectToMovies();

        assertThat(viewName).isEqualTo("redirect:/movies");
    }
}
