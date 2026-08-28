CREATE OR REPLACE FUNCTION fn_count_movies_with_genre_greater(p_genre movie_genre)
RETURNS BIGINT
LANGUAGE SQL
STABLE
AS $$
    SELECT COUNT(*) FROM movies WHERE genre > p_genre;
$$;

CREATE OR REPLACE FUNCTION fn_movies_with_name_containing(p_substring VARCHAR)
RETURNS SETOF movies
LANGUAGE SQL
STABLE
AS $$
    SELECT * FROM movies
    WHERE POSITION(upper(p_substring) IN upper(name)) > 0;
$$;

CREATE OR REPLACE FUNCTION fn_movies_with_genre_greater(p_genre movie_genre)
RETURNS SETOF movies
LANGUAGE SQL
STABLE
AS $$
    SELECT * FROM movies WHERE genre > p_genre;
$$;

CREATE OR REPLACE FUNCTION fn_movies_without_oscars()
RETURNS SETOF movies
LANGUAGE SQL
STABLE
AS $$
    SELECT * FROM movies WHERE oscars_count IS NULL OR oscars_count = 0;
$$;

CREATE OR REPLACE FUNCTION fn_screenwriters_without_oscars()
RETURNS TABLE(screenwriter_id BIGINT)
LANGUAGE SQL
STABLE
AS $$
    SELECT DISTINCT m.screenwriter_id
    FROM movies m
    LEFT JOIN movies awarded
        ON awarded.screenwriter_id = m.screenwriter_id
       AND awarded.oscars_count IS NOT NULL
       AND awarded.oscars_count > 0
    WHERE m.screenwriter_id IS NOT NULL
    GROUP BY m.screenwriter_id
    HAVING bool_and(awarded.id IS NULL);
$$;
