package ru.ifmo.movies_app.converter;

import org.junit.jupiter.api.Test;
import org.postgresql.util.PGobject;
import ru.ifmo.movies_app.domain.Color;
import ru.ifmo.movies_app.domain.Country;

import static org.assertj.core.api.Assertions.assertThat;

class EnumConvertersTest {

    private final ColorConverter colorConverter = new ColorConverter();
    private final CountryConverter countryConverter = new CountryConverter();

    @Test
    void colorConverterKeepsPgTypeForNull() throws Exception {
        Object dbValue = colorConverter.convertToDatabaseColumn(null);
        assertThat(dbValue).isInstanceOf(PGobject.class);
        PGobject pg = (PGobject) dbValue;
        assertThat(pg.getType()).isEqualTo("color");
        assertThat(pg.getValue()).isNull();
        assertThat(colorConverter.convertToEntityAttribute(pg)).isNull();
    }

    @Test
    void countryConverterKeepsPgTypeForNull() throws Exception {
        Object dbValue = countryConverter.convertToDatabaseColumn(null);
        assertThat(dbValue).isInstanceOf(PGobject.class);
        PGobject pg = (PGobject) dbValue;
        assertThat(pg.getType()).isEqualTo("country");
        assertThat(pg.getValue()).isNull();
        assertThat(countryConverter.convertToEntityAttribute(pg)).isNull();
    }

    @Test
    void convertsValuesRoundTrip() {
        PGobject colorObject = (PGobject) colorConverter.convertToDatabaseColumn(Color.GREEN);
        assertThat(colorConverter.convertToEntityAttribute(colorObject)).isEqualTo(Color.GREEN);

        PGobject countryObject = (PGobject) countryConverter.convertToDatabaseColumn(Country.SPAIN);
        assertThat(countryConverter.convertToEntityAttribute(countryObject)).isEqualTo(Country.SPAIN);
    }
}
