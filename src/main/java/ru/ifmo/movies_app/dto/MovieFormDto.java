package ru.ifmo.movies_app.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import ru.ifmo.movies_app.domain.MovieGenre;
import ru.ifmo.movies_app.domain.MpaaRating;

public class MovieFormDto {

    private Long id;

    @NotBlank
    private String name;

    @Valid
    @NotNull
    private CoordinatesDto coordinates;

    @Positive
    private Long oscarsCount;

    @NotNull
    @Positive
    private Integer budget;

    @NotNull
    @Positive
    private Float totalBoxOffice;

    private MpaaRating mpaaRating;

    private Long directorId;

    @NotNull
    private Long screenwriterId;

    private Long operatorId;

    @Positive
    private Long length;

    @NotNull
    @Positive
    private Integer goldenPalmCount;

    @NotNull
    private MovieGenre genre;

    public MovieFormDto() {
        this.coordinates = new CoordinatesDto();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public CoordinatesDto getCoordinates() {
        return coordinates;
    }

    public void setCoordinates(CoordinatesDto coordinates) {
        this.coordinates = coordinates;
    }

    public Long getOscarsCount() {
        return oscarsCount;
    }

    public void setOscarsCount(Long oscarsCount) {
        this.oscarsCount = oscarsCount;
    }

    public Integer getBudget() {
        return budget;
    }

    public void setBudget(Integer budget) {
        this.budget = budget;
    }

    public Float getTotalBoxOffice() {
        return totalBoxOffice;
    }

    public void setTotalBoxOffice(Float totalBoxOffice) {
        this.totalBoxOffice = totalBoxOffice;
    }

    public MpaaRating getMpaaRating() {
        return mpaaRating;
    }

    public void setMpaaRating(MpaaRating mpaaRating) {
        this.mpaaRating = mpaaRating;
    }

    public Long getDirectorId() {
        return directorId;
    }

    public void setDirectorId(Long directorId) {
        this.directorId = directorId;
    }

    public Long getScreenwriterId() {
        return screenwriterId;
    }

    public void setScreenwriterId(Long screenwriterId) {
        this.screenwriterId = screenwriterId;
    }

    public Long getOperatorId() {
        return operatorId;
    }

    public void setOperatorId(Long operatorId) {
        this.operatorId = operatorId;
    }

    public Long getLength() {
        return length;
    }

    public void setLength(Long length) {
        this.length = length;
    }

    public Integer getGoldenPalmCount() {
        return goldenPalmCount;
    }

    public void setGoldenPalmCount(Integer goldenPalmCount) {
        this.goldenPalmCount = goldenPalmCount;
    }

    public MovieGenre getGenre() {
        return genre;
    }

    public void setGenre(MovieGenre genre) {
        this.genre = genre;
    }
}
