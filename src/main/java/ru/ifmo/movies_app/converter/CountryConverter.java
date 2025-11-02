package ru.ifmo.movies_app.converter;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import org.postgresql.util.PGobject;
import ru.ifmo.movies_app.domain.Country;

@Converter(autoApply = false)
public class CountryConverter implements AttributeConverter<Country, Object> {

    private static final String PG_TYPE = "country";

    @Override
    public Object convertToDatabaseColumn(Country attribute) {
        if (attribute == null) {
            return null;
        }
        try {
            PGobject pg = new PGobject();
            pg.setType(PG_TYPE);
            pg.setValue(attribute.name());
            return pg;
        } catch (Exception e) {
            throw new IllegalArgumentException("Cannot convert Country to PGobject", e);
        }
    }

    @Override
    public Country convertToEntityAttribute(Object dbData) {
        if (dbData == null) {
            return null;
        }
        if (dbData instanceof PGobject pg) {
            return Country.valueOf(pg.getValue());
        }
        return Country.valueOf(dbData.toString());
    }
}
