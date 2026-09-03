package ru.ifmo.movies_app.service;

import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.List;

import org.springframework.stereotype.Service;

import ru.ifmo.movies_app.dto.MovieTableRowDto;

@Service
public class MovieCsvExportService {

    private static final String[] HEADERS = {
            "ID",
            "Название",
            "X",
            "Y",
            "Создан",
            "Оскары",
            "Бюджет",
            "Сборы",
            "MPAA",
            "Режиссёр",
            "Сценарист",
            "Оператор",
            "Длина",
            "Золотые пальмы",
            "Жанр"
    };

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ISO_OFFSET_DATE_TIME
            .withZone(ZoneOffset.UTC);

    public String export(List<MovieTableRowDto> rows) {
        StringBuilder csv = new StringBuilder("\uFEFF");
        appendRow(csv, (Object[]) HEADERS);
        rows.forEach(row -> appendRow(csv,
                row.getId(),
                row.getName(),
                row.getCoordX(),
                row.getCoordY(),
                row.getCreationDate(),
                row.getOscarsCount(),
                row.getBudget(),
                row.getTotalBoxOffice(),
                row.getMpaaRating(),
                row.getDirectorName(),
                row.getScreenwriterName(),
                row.getOperatorName(),
                row.getLength(),
                row.getGoldenPalmCount(),
                row.getGenre()));
        return csv.toString();
    }

    private void appendRow(StringBuilder csv, Object... values) {
        for (int i = 0; i < values.length; i++) {
            if (i > 0) {
                csv.append(',');
            }
            csv.append(escape(format(values[i])));
        }
        csv.append("\r\n");
    }

    private String format(Object value) {
        if (value == null) {
            return "";
        }
        if (value instanceof Date date) {
            return DATE_FORMATTER.format(date.toInstant());
        }
        return String.valueOf(value);
    }

    private String escape(String value) {
        if (value.contains(",") || value.contains("\"") || value.contains("\r") || value.contains("\n")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }
}
