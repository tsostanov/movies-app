package ru.ifmo.movies_app.dto.importer;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import ru.ifmo.movies_app.domain.MovieGenre;
import ru.ifmo.movies_app.domain.MpaaRating;
import ru.ifmo.movies_app.dto.CoordinatesDto;

public class MovieImportDto {

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

    @Valid
    private PersonPayloadDto director;

    @Valid
    @NotNull
    private PersonPayloadDto screenwriter;

    @Valid
    private PersonPayloadDto operator;

    @Positive
    private Long length;

    @NotNull
    @Positive
    private Integer goldenPalmCount;

    @NotNull
    private MovieGenre genre;

    public MovieImportDto() {
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

    public PersonPayloadDto getDirector() {
        return director;
    }

    public void setDirector(PersonPayloadDto director) {
        this.director = director;
    }

    public PersonPayloadDto getScreenwriter() {
        return screenwriter;
    }

    public void setScreenwriter(PersonPayloadDto screenwriter) {
        this.screenwriter = screenwriter;
    }

    public PersonPayloadDto getOperator() {
        return operator;
    }

    public void setOperator(PersonPayloadDto operator) {
        this.operator = operator;
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
