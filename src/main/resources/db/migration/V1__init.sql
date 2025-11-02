CREATE TYPE mpaa_rating AS ENUM ('G', 'PG', 'PG_13', 'R', 'NC_17');
CREATE TYPE movie_genre AS ENUM ('WESTERN', 'DRAMA', 'COMEDY', 'MUSICAL', 'FANTASY');
CREATE TYPE color AS ENUM ('GREEN', 'BLUE', 'YELLOW', 'ORANGE', 'WHITE');
CREATE TYPE country AS ENUM ('GERMANY', 'SPAIN', 'THAILAND');

CREATE SEQUENCE movie_seq START WITH 1 INCREMENT BY 1;

CREATE TABLE locations (
    id      BIGSERIAL PRIMARY KEY,
    x       DOUBLE PRECISION NOT NULL,
    y       REAL NOT NULL,
    name    VARCHAR(501) NOT NULL CHECK (char_length(name) <= 501)
);

CREATE TABLE persons (
    id           BIGSERIAL PRIMARY KEY,
    name         VARCHAR(255) NOT NULL CHECK (char_length(trim(name)) > 0),
    eye_color    color,
    hair_color   color,
    location_id  BIGINT REFERENCES locations(id) ON DELETE SET NULL,
    weight       REAL NOT NULL CHECK (weight > 0),
    nationality  country NOT NULL
);

CREATE TABLE movies (
    id                 BIGINT PRIMARY KEY DEFAULT nextval('movie_seq'),
    name               VARCHAR(255) NOT NULL CHECK (char_length(trim(name)) > 0),
    coord_x            REAL NOT NULL CHECK (coord_x > -924),
    coord_y            BIGINT NOT NULL,
    creation_date      TIMESTAMPTZ NOT NULL DEFAULT now(),
    version            BIGINT NOT NULL DEFAULT 0,
    oscars_count       BIGINT CHECK (oscars_count IS NULL OR oscars_count > 0),
    budget             INTEGER NOT NULL CHECK (budget > 0),
    total_box_office   REAL NOT NULL CHECK (total_box_office > 0),
    mpaa_rating        mpaa_rating,
    director_id        BIGINT REFERENCES persons(id) ON DELETE SET NULL,
    screenwriter_id    BIGINT NOT NULL REFERENCES persons(id),
    operator_id        BIGINT REFERENCES persons(id) ON DELETE SET NULL,
    length             BIGINT CHECK (length IS NULL OR length > 0),
    golden_palm_count  INTEGER NOT NULL CHECK (golden_palm_count > 0),
    genre              movie_genre NOT NULL
);

CREATE INDEX idx_movies_name ON movies (name);
CREATE INDEX idx_movies_genre ON movies (genre);
CREATE INDEX idx_movies_mpaa ON movies (mpaa_rating);
