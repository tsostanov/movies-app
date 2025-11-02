package ru.ifmo.movies_app.dto;

import java.util.Date;

import ru.ifmo.movies_app.domain.MovieGenre;
import ru.ifmo.movies_app.domain.MpaaRating;
import ru.ifmo.movies_app.domain.Person;

public class MovieDetailsDto {

    private final Long id;
    private final String name;
    private final float coordX;
    private final Long coordY;
    private final Date creationDate;
    private final Long oscarsCount;
    private final Integer budget;
    private final float totalBoxOffice;
    private final MpaaRating mpaaRating;
    private final Person director;
    private final Person screenwriter;
    private final Person operator;
    private final Long length;
    private final int goldenPalmCount;
    private final MovieGenre genre;

    public MovieDetailsDto(
            Long id,
            String name,
            float coordX,
            Long coordY,
            Date creationDate,
            Long oscarsCount,
            Integer budget,
            float totalBoxOffice,
            MpaaRating mpaaRating,
            Person director,
            Person screenwriter,
            Person operator,
            Long length,
            int goldenPalmCount,
            MovieGenre genre) {
        this.id = id;
        this.name = name;
        this.coordX = coordX;
        this.coordY = coordY;
        this.creationDate = creationDate;
        this.oscarsCount = oscarsCount;
        this.budget = budget;
        this.totalBoxOffice = totalBoxOffice;
        this.mpaaRating = mpaaRating;
        this.director = director;
        this.screenwriter = screenwriter;
        this.operator = operator;
        this.length = length;
        this.goldenPalmCount = goldenPalmCount;
        this.genre = genre;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public float getCoordX() {
        return coordX;
    }

    public Long getCoordY() {
        return coordY;
    }

    public Date getCreationDate() {
        return creationDate;
    }

    public Long getOscarsCount() {
        return oscarsCount;
    }

    public Integer getBudget() {
        return budget;
    }

    public float getTotalBoxOffice() {
        return totalBoxOffice;
    }

    public MpaaRating getMpaaRating() {
        return mpaaRating;
    }

    public Person getDirector() {
        return director;
    }

    public Person getScreenwriter() {
        return screenwriter;
    }

    public Person getOperator() {
        return operator;
    }

    public Long getLength() {
        return length;
    }

    public int getGoldenPalmCount() {
        return goldenPalmCount;
    }

    public MovieGenre getGenre() {
        return genre;
    }
}
