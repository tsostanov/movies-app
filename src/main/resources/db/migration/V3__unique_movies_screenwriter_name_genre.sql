-- Enforce business uniqueness: screenwriter + name + genre
ALTER TABLE movies
    ADD CONSTRAINT uk_movies_screenwriter_name_genre
        UNIQUE (screenwriter_id, name, genre);
