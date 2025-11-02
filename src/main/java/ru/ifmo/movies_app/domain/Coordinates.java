package ru.ifmo.movies_app.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

@Embeddable
public class Coordinates {

    @DecimalMin(value = "-924", inclusive = false)
    @Column(name = "coord_x", nullable = false)
    private float x;

    @NotNull
    @Column(name = "coord_y", nullable = false)
    private Long y;

    public Coordinates() {
    }

    public Coordinates(float x, Long y) {
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
