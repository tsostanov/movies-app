package ru.ifmo.movies_app.converter;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import org.postgresql.util.PGobject;
import ru.ifmo.movies_app.domain.MovieGenre;

@Converter(autoApply = false)
public class MovieGenreConverter implements AttributeConverter<MovieGenre, Object> {

    private static final String PG_TYPE = "movie_genre";

    @Override
    public Object convertToDatabaseColumn(MovieGenre attribute) {
        if (attribute == null) {
            return null;
        }
        try {
            PGobject pg = new PGobject();
            pg.setType(PG_TYPE);
            pg.setValue(attribute.name());
            return pg;
        } catch (Exception e) {
            throw new IllegalArgumentException("Cannot convert MovieGenre to PGobject", e);
        }
    }

    @Override
    public MovieGenre convertToEntityAttribute(Object dbData) {
        if (dbData == null) {
            return null;
        }
        if (dbData instanceof PGobject pg) {
            return MovieGenre.valueOf(pg.getValue());
        }
        return MovieGenre.valueOf(dbData.toString());
    }
}
