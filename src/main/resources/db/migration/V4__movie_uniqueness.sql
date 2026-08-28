CREATE UNIQUE INDEX uq_movies_screenwriter_name_genre
    ON movies (screenwriter_id, lower(name), genre);
