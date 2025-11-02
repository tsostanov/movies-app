package ru.ifmo.movies_app.converter;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import org.postgresql.util.PGobject;
import ru.ifmo.movies_app.domain.MpaaRating;

@Converter(autoApply = false)
public class MpaaRatingConverter implements AttributeConverter<MpaaRating, Object> {

    private static final String PG_TYPE = "mpaa_rating";

    @Override
    public Object convertToDatabaseColumn(MpaaRating attribute) {
        if (attribute == null) {
            return null;
        }
        try {
            PGobject pg = new PGobject();
            pg.setType(PG_TYPE);
            pg.setValue(attribute.name());
            return pg;
        } catch (Exception e) {
            throw new IllegalArgumentException("Cannot convert MpaaRating to PGobject", e);
        }
    }

    @Override
    public MpaaRating convertToEntityAttribute(Object dbData) {
        if (dbData == null) {
            return null;
        }
        if (dbData instanceof PGobject pg) {
            return MpaaRating.valueOf(pg.getValue());
        }
        return MpaaRating.valueOf(dbData.toString());
    }
}
