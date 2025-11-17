package ru.ifmo.movies_app.dto.importer;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonUnwrapped;
import jakarta.validation.Valid;
import jakarta.validation.constraints.AssertTrue;

import ru.ifmo.movies_app.dto.PersonCreateRequest;

public class PersonPayloadDto {

    private Long id;

    @JsonUnwrapped
    private PersonInlineDto inline = new PersonInlineDto();

    @Valid
    private PersonInlineDto explicitData;

    public PersonPayloadDto() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public PersonInlineDto getInline() {
        return inline;
    }

    public void setInline(PersonInlineDto inline) {
        this.inline = inline;
    }

    @JsonProperty("data")
    public PersonInlineDto getExplicitData() {
        return explicitData;
    }

    @JsonProperty("data")
    public void setExplicitData(PersonInlineDto explicitData) {
        this.explicitData = explicitData;
    }

    public PersonInlineDto resolveData() {
        if (explicitData != null && explicitData.hasAnyField()) {
            return explicitData;
        }
        if (inline != null && inline.hasAnyField()) {
            return inline;
        }
        return null;
    }

    @AssertTrue(message = "Для персоны в импорте нужно указать id либо заполнить поля человека")
    public boolean isValidDefinition() {
        return id != null || resolveData() != null;
    }

    public static class PersonInlineDto extends PersonCreateRequest {

        @Valid
        private LocationPayloadDto location;

        public PersonInlineDto() {
        }

        public LocationPayloadDto getLocation() {
            return location;
        }

        public void setLocation(LocationPayloadDto location) {
            this.location = location;
        }

        @AssertTrue(message = "Нельзя одновременно задавать locationId и вложенный объект location")
        public boolean isLocationReferenceValid() {
            return getLocationId() == null || location == null;
        }

        public boolean hasAnyField() {
            return getName() != null
                    || getEyeColor() != null
                    || getHairColor() != null
                    || getWeight() != null
                    || getNationality() != null
                    || getLocationId() != null
                    || location != null;
        }
    }
}
