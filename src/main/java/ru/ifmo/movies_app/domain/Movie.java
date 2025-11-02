package ru.ifmo.movies_app.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import jakarta.persistence.Version;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;


import java.util.Date;

import ru.ifmo.movies_app.converter.MovieGenreConverter;
import ru.ifmo.movies_app.converter.MpaaRatingConverter;

@Entity
@Table(name = "movies")
public class Movie {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "movie_seq")
    @SequenceGenerator(name = "movie_seq", sequenceName = "movie_seq", allocationSize = 1)
    private Long id;

    @NotBlank
    @Column(name = "name", nullable = false)
    private String name;

    @Valid
    @NotNull
    @Embedded
    private Coordinates coordinates;

    @NotNull
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "creation_date", nullable = false, updatable = false)
    private Date creationDate;

    @Positive
    @Column(name = "oscars_count")
    private Long oscarsCount;

    @NotNull
    @Positive
    @Column(name = "budget", nullable = false)
    private Integer budget;

    @Positive
    @Column(name = "total_box_office", nullable = false)
    private float totalBoxOffice;

    @Convert(converter = MpaaRatingConverter.class)
    @Column(name = "mpaa_rating")
    private MpaaRating mpaaRating;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "director_id")
    private Person director;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "screenwriter_id", nullable = false)
    private Person screenwriter;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "operator_id")
    private Person operator;

    @Positive
    @Column(name = "length")
    private Long length;

    @Positive
    @Column(name = "golden_palm_count", nullable = false)
    private int goldenPalmCount;

    @NotNull
    @Convert(converter = MovieGenreConverter.class)
    @Column(name = "genre", nullable = false)
    private MovieGenre genre;

    @Version
    @Column(name = "version", nullable = false)
    private Long version;

    public Movie() {
    }

    @PrePersist
    void assignCreationDate() {
        if (creationDate == null) {
            creationDate = new Date();
        }
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

    public Coordinates getCoordinates() {
        return coordinates;
    }

    public void setCoordinates(Coordinates coordinates) {
        this.coordinates = coordinates;
    }

    public Date getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(Date creationDate) {
        this.creationDate = creationDate;
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

    public float getTotalBoxOffice() {
        return totalBoxOffice;
    }

    public void setTotalBoxOffice(float totalBoxOffice) {
        this.totalBoxOffice = totalBoxOffice;
    }

    public MpaaRating getMpaaRating() {
        return mpaaRating;
    }

    public void setMpaaRating(MpaaRating mpaaRating) {
        this.mpaaRating = mpaaRating;
    }

    public Person getDirector() {
        return director;
    }

    public void setDirector(Person director) {
        this.director = director;
    }

    public Person getScreenwriter() {
        return screenwriter;
    }

    public void setScreenwriter(Person screenwriter) {
        this.screenwriter = screenwriter;
    }

    public Person getOperator() {
        return operator;
    }

    public void setOperator(Person operator) {
        this.operator = operator;
    }

    public Long getLength() {
        return length;
    }

    public void setLength(Long length) {
        this.length = length;
    }

    public int getGoldenPalmCount() {
        return goldenPalmCount;
    }

    public void setGoldenPalmCount(int goldenPalmCount) {
        this.goldenPalmCount = goldenPalmCount;
    }

    public MovieGenre getGenre() {
        return genre;
    }

    public void setGenre(MovieGenre genre) {
        this.genre = genre;
    }

    public Long getVersion() {
        return version;
    }

    public void setVersion(Long version) {
        this.version = version;
    }
}
