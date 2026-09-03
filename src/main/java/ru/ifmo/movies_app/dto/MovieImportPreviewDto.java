package ru.ifmo.movies_app.dto;

import java.util.List;

public record MovieImportPreviewDto(int totalCount, List<MovieImportPreviewRowDto> movies) {
}
