package ru.ifmo.movies_app.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import java.util.Date;
import java.util.List;

import org.junit.jupiter.api.Test;

import ru.ifmo.movies_app.domain.MovieGenre;
import ru.ifmo.movies_app.domain.MpaaRating;
import ru.ifmo.movies_app.dto.MovieTableRowDto;

class MovieCsvExportServiceTest {

    private final MovieCsvExportService service = new MovieCsvExportService();

    @Test
    void exportAddsHeaderAndMovieRows() {
        MovieTableRowDto row = new MovieTableRowDto(
                42L,
                "Solar Road",
                12.5f,
                90L,
                Date.from(Instant.parse("2026-09-03T10:00:00Z")),
                2L,
                100000,
                150000.5f,
                MpaaRating.PG_13,
                "Mila North",
                "Egor Khazin",
                null,
                118L,
                1,
                MovieGenre.DRAMA);

        String csv = service.export(List.of(row));

        assertThat(csv).startsWith("\uFEFFID,Название,X,Y,Создан");
        assertThat(csv).contains("42,Solar Road,12.5,90,2026-09-03T10:00:00Z,2,100000,150000.5,PG_13");
        assertThat(csv).contains("Mila North,Egor Khazin,,118,1,DRAMA");
    }

    @Test
    void exportEscapesCommasQuotesAndLineBreaks() {
        MovieTableRowDto row = new MovieTableRowDto(
                1L,
                "Film, \"A\"\nSecond line",
                1.0f,
                2L,
                null,
                null,
                null,
                3.0f,
                null,
                null,
                "Writer, One",
                null,
                null,
                1,
                MovieGenre.COMEDY);

        String csv = service.export(List.of(row));

        assertThat(csv).contains("\"Film, \"\"A\"\"\nSecond line\"");
        assertThat(csv).contains("\"Writer, One\"");
    }
}
