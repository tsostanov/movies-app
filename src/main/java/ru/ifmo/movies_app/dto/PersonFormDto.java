package ru.ifmo.movies_app.dto;

public class PersonFormDto extends PersonCreateRequest {

    private Long id;

    public PersonFormDto() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }
}
