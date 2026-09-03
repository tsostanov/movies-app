package ru.ifmo.movies_app.service.importer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import jakarta.validation.Validation;
import jakarta.validation.Validator;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.transaction.PlatformTransactionManager;

import ru.ifmo.movies_app.dto.MovieImportPreviewDto;
import ru.ifmo.movies_app.repository.ImportOperationRepository;
import ru.ifmo.movies_app.service.LocationService;
import ru.ifmo.movies_app.service.MovieService;
import ru.ifmo.movies_app.service.PersonService;

class MovieImportPreviewTest {

    private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    @Test
    void previewYamlReturnsSummaryWithoutImportingMovies() {
        MovieImportService service = new MovieImportService(
                new MovieImportParser(),
                mock(MovieService.class),
                mock(PersonService.class),
                mock(LocationService.class),
                mock(ImportOperationRepository.class),
                validator,
                mock(PlatformTransactionManager.class));
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

        MovieImportPreviewDto preview = service.previewYaml(file);

        assertThat(preview.totalCount()).isEqualTo(1);
        assertThat(preview.movies()).hasSize(1);
        assertThat(preview.movies().get(0).name()).isEqualTo("Autumn Lights");
        assertThat(preview.movies().get(0).screenwriter()).isEqualTo("новая персона: Egor Khazin");
    }
}
