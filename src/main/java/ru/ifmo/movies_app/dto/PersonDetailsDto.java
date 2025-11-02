package ru.ifmo.movies_app.dto;

import ru.ifmo.movies_app.domain.Color;
import ru.ifmo.movies_app.domain.Country;

public class PersonDetailsDto {

    private final Long id;
    private final String name;
    private final Color eyeColor;
    private final Color hairColor;
    private final Float weight;
    private final Country nationality;
    private final Long locationId;
    private final String locationName;
    private final Double locationX;
    private final Float locationY;

    public PersonDetailsDto(Long id,
                            String name,
                            Color eyeColor,
                            Color hairColor,
                            Float weight,
                            Country nationality,
                            Long locationId,
                            String locationName,
                            Double locationX,
                            Float locationY) {
        this.id = id;
        this.name = name;
        this.eyeColor = eyeColor;
        this.hairColor = hairColor;
        this.weight = weight;
        this.nationality = nationality;
        this.locationId = locationId;
        this.locationName = locationName;
        this.locationX = locationX;
        this.locationY = locationY;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public Color getEyeColor() {
        return eyeColor;
    }

    public Color getHairColor() {
        return hairColor;
    }

    public Float getWeight() {
        return weight;
    }

    public Country getNationality() {
        return nationality;
    }

    public Long getLocationId() {
        return locationId;
    }

    public String getLocationName() {
        return locationName;
    }

    public Double getLocationX() {
        return locationX;
    }

    public Float getLocationY() {
        return locationY;
    }
}
