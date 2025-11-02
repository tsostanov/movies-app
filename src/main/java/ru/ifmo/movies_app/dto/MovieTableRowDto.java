package ru.ifmo.movies_app.dto;

import java.util.Date;

import ru.ifmo.movies_app.domain.MovieGenre;
import ru.ifmo.movies_app.domain.MpaaRating;

public class MovieTableRowDto {

    private final Long id;
    private final String name;
    private final float coordX;
    private final Long coordY;
    private final Date creationDate;
    private final Long oscarsCount;
    private final Integer budget;
    private final float totalBoxOffice;
    private final MpaaRating mpaaRating;
    private final String directorName;
    private final String screenwriterName;
    private final String operatorName;
    private final Long length;
    private final int goldenPalmCount;
    private final MovieGenre genre;

    public MovieTableRowDto(
            Long id,
            String name,
            float coordX,
            Long coordY,
            Date creationDate,
            Long oscarsCount,
            Integer budget,
            float totalBoxOffice,
            MpaaRating mpaaRating,
            String directorName,
            String screenwriterName,
            String operatorName,
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
        this.directorName = directorName;
        this.screenwriterName = screenwriterName;
        this.operatorName = operatorName;
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

    public String getDirectorName() {
        return directorName;
    }

    public String getScreenwriterName() {
        return screenwriterName;
    }

    public String getOperatorName() {
        return operatorName;
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
