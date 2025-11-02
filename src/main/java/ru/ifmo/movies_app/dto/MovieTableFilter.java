package ru.ifmo.movies_app.dto;

import org.springframework.util.StringUtils;

import ru.ifmo.movies_app.domain.MovieGenre;
import ru.ifmo.movies_app.domain.MpaaRating;

public class MovieTableFilter {

    private String name;
    private String directorName;
    private String screenwriterName;
    private String operatorName;
    private MovieGenre genre;
    private MpaaRating mpaaRating;
    private String sortBy;
    private String sortDirection;

    public String getName() {
        return normalize(name);
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDirectorName() {
        return normalize(directorName);
    }

    public void setDirectorName(String directorName) {
        this.directorName = directorName;
    }

    public String getScreenwriterName() {
        return normalize(screenwriterName);
    }

    public void setScreenwriterName(String screenwriterName) {
        this.screenwriterName = screenwriterName;
    }

    public String getOperatorName() {
        return normalize(operatorName);
    }

    public void setOperatorName(String operatorName) {
        this.operatorName = operatorName;
    }

    public MovieGenre getGenre() {
        return genre;
    }

    public void setGenre(MovieGenre genre) {
        this.genre = genre;
    }

    public MpaaRating getMpaaRating() {
        return mpaaRating;
    }

    public void setMpaaRating(MpaaRating mpaaRating) {
        this.mpaaRating = mpaaRating;
    }

    public String getSortBy() {
        return sortBy;
    }

    public void setSortBy(String sortBy) {
        this.sortBy = sortBy;
    }

    public String getSortDirection() {
        return sortDirection;
    }

    public void setSortDirection(String sortDirection) {
        this.sortDirection = sortDirection;
    }

    private String normalize(String value) {
        return StringUtils.hasText(value) ? value.trim() : null;
    }
}
