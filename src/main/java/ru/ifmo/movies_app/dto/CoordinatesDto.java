package ru.ifmo.movies_app.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

public class CoordinatesDto {

    @DecimalMin(value = "-924", inclusive = false)
    private float x;

    @NotNull
    private Long y;

    public CoordinatesDto() {
    }

    public CoordinatesDto(float x, Long y) {
        this.x = x;
        this.y = y;
    }

    public float getX() {
        return x;
    }

    public void setX(float x) {
        this.x = x;
    }

    public Long getY() {
        return y;
    }

    public void setY(Long y) {
        this.y = y;
    }
}
