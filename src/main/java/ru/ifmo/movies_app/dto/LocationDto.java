package ru.ifmo.movies_app.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class LocationDto {

    private Long id;

    @NotNull
    private Double x;

    @NotNull
    private Float y;

    @NotBlank
    @Size(max = 501)
    private String name;

    public LocationDto() {
    }

    public LocationDto(Long id, Double x, Float y, String name) {
        this.id = id;
        this.x = x;
        this.y = y;
        this.name = name;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
