package ru.ifmo.movies_app.dto;

public class PersonSummaryDto {

    private final Long id;
    private final String name;

    public PersonSummaryDto(Long id, String name) {
        this.id = id;
        this.name = name;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }
}
