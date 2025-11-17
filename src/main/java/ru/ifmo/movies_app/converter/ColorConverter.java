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
        try {
            PGobject pg = new PGobject();
            pg.setType(PG_TYPE);
            pg.setValue(attribute != null ? attribute.name() : null);
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
            String value = pg.getValue();
            return value != null ? Color.valueOf(value) : null;
        }
        // В теории могут прилетать простые строки, обрабатываем их как enum name
        return Color.valueOf(dbData.toString());
    }
}
