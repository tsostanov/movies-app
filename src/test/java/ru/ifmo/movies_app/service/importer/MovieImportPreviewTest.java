package ru.ifmo.movies_app.service.importer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;

import jakarta.validation.Validation;
import jakarta.validation.Validator;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.transaction.PlatformTransactionManager;

import ru.ifmo.movies_app.dto.MovieImportPreviewDto;
import ru.ifmo.movies_app.dto.MovieImportPreviewRowDto;
import ru.ifmo.movies_app.repository.ImportOperationRepository;
import ru.ifmo.movies_app.service.LocationService;
import ru.ifmo.movies_app.service.MovieService;
import ru.ifmo.movies_app.service.PersonService;

class MovieImportPreviewTest {

    private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    @Test
    void previewYamlReturnsSummaryWithoutImportingMovies() {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "movies.yaml",
                "application/x-yaml",
                """
                        movies:
                          - name: "Autumn Lights"
                            coordinates:
                              x: 48.7
                              y: 320
                            oscarsCount: 1
                            budget: 4500000
                            totalBoxOffice: 12.4
                            mpaaRating: "PG_13"
                            screenwriter:
                              data:
                                name: "Egor Khazin"
                                weight: 72.3
                                nationality: "SPAIN"
                            goldenPalmCount: 2
                            genre: "DRAMA"
                        """.getBytes());

        MovieImportPreviewDto preview = service().previewYaml(file);

        assertThat(preview.totalCount()).isEqualTo(1);
        assertThat(preview.movies()).hasSize(1);
        assertThat(preview.movies().get(0).name()).isEqualTo("Autumn Lights");
        assertThat(preview.movies().get(0).screenwriter()).contains("Egor Khazin");
    }

    @Test
    void previewYamlLimitsRowsToFirstTwentyButKeepsTotalCount() {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "movies.yaml",
                "application/x-yaml",
                manyMoviesYaml(22).getBytes());

        MovieImportPreviewDto preview = service().previewYaml(file);

        assertThat(preview.totalCount()).isEqualTo(22);
        assertThat(preview.movies()).hasSize(20);
        assertThat(preview.movies()).extracting(MovieImportPreviewRowDto::rowNumber)
                .containsExactlyElementsOf(java.util.stream.IntStream.rangeClosed(1, 20).boxed().toList());
        assertThat(preview.movies().get(19).name()).isEqualTo("Movie 20");
    }

    @Test
    void previewYamlReportsValidationPathForInvalidMovie() {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "broken.yaml",
                "application/x-yaml",
                """
                        movies:
                          - name: ""
                            coordinates:
                              x: 48.7
                              y: 320
                            budget: -1
                            totalBoxOffice: 12.4
                            screenwriter:
                              id: 7
                            goldenPalmCount: 2
                            genre: "DRAMA"
                        """.getBytes());

        assertThatThrownBy(() -> service().previewYaml(file))
                .isInstanceOf(ImportException.class)
                .hasMessageContaining("movies[0].name")
                .hasMessageContaining("movies[0].budget");
    }

    private MovieImportService service() {
        return new MovieImportService(
                new MovieImportParser(),
                mock(MovieService.class),
                mock(PersonService.class),
                mock(LocationService.class),
                mock(ImportOperationRepository.class),
                validator,
                mock(PlatformTransactionManager.class));
    }

    private String manyMoviesYaml(int count) {
        StringBuilder yaml = new StringBuilder("movies:\n");
        for (int index = 1; index <= count; index++) {
            yaml.append("""
                    - name: "Movie %d"
                      coordinates:
                        x: 48.7
                        y: 320
                      oscarsCount: 1
                      budget: 4500000
                      totalBoxOffice: 12.4
                      mpaaRating: "PG_13"
                      screenwriter:
                        id: 7
                      goldenPalmCount: 2
                      genre: "DRAMA"
                    """.formatted(index).indent(2));
        }
        return yaml.toString();
    }
}
