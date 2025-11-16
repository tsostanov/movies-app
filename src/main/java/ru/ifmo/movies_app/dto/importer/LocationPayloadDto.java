package ru.ifmo.movies_app.dto.importer;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class LocationPayloadDto {

    @NotBlank
    private String name;

    @NotNull
    private Double x;

    @NotNull
    private Float y;

    public LocationPayloadDto() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Double getX() {
        return x;
    }

    public void setX(Double x) {
        this.x = x;
    }

    public Float getY() {
        return y;
    }

    public void setY(Float y) {
        this.y = y;
    }
}
