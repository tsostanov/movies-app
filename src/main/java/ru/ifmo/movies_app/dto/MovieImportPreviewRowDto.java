package ru.ifmo.movies_app.dto;

import ru.ifmo.movies_app.domain.MovieGenre;
import ru.ifmo.movies_app.domain.MpaaRating;

public record MovieImportPreviewRowDto(
        int rowNumber,
        String name,
        MovieGenre genre,
        MpaaRating mpaaRating,
        String director,
        String screenwriter,
        String operator) {
}
