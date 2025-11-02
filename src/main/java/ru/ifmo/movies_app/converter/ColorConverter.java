package ru.ifmo.movies_app.converter;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import org.postgresql.util.PGobject;
import ru.ifmo.movies_app.domain.Color;

@Converter(autoApply = false)
public class ColorConverter implements AttributeConverter<Color, Object> {

    private static final String PG_TYPE = "color";

    @Override
    public Object convertToDatabaseColumn(Color attribute) {
        if (attribute == null) {
            return null;
        }
        try {
            PGobject pg = new PGobject();
            pg.setType(PG_TYPE);
            pg.setValue(attribute.name());
            return pg;
        } catch (Exception e) {
            throw new IllegalArgumentException("Cannot convert Color to PGobject", e);
        }
    }

    @Override
    public Color convertToEntityAttribute(Object dbData) {
        if (dbData == null) {
            return null;
        }
        if (dbData instanceof PGobject pg) {
            return Color.valueOf(pg.getValue());
        }
        // на всякий случай, если драйвер вернул строку
        return Color.valueOf(dbData.toString());
    }
}
