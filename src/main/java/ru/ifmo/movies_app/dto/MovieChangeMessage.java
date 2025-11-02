package ru.ifmo.movies_app.dto;

public class MovieChangeMessage {

    public enum ChangeType {
        CREATED, UPDATED, DELETED
    }

    private final ChangeType type;
    private final Long id;
    private final MovieTableRowDto payload;

    public MovieChangeMessage(ChangeType type, Long id, MovieTableRowDto payload) {
        this.type = type;
        this.id = id;
        this.payload = payload;
    }

    public ChangeType getType() {
        return type;
    }

    public Long getId() {
        return id;
    }

    public MovieTableRowDto getPayload() {
        return payload;
    }
}
