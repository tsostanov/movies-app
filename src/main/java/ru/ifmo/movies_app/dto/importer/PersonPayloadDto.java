package ru.ifmo.movies_app.dto.importer;

import jakarta.validation.Valid;
import jakarta.validation.constraints.AssertTrue;

import ru.ifmo.movies_app.dto.PersonCreateRequest;

public class PersonPayloadDto {

    private Long id;

    @Valid
    private PersonInlineDto data;

    public PersonPayloadDto() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public PersonInlineDto getData() {
        return data;
    }

    public void setData(PersonInlineDto data) {
        this.data = data;
    }

    @AssertTrue(message = "Персона в импорте должна содержать либо id, либо блок data")
    public boolean isValidDefinition() {
        return id != null || data != null;
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
    }
}
