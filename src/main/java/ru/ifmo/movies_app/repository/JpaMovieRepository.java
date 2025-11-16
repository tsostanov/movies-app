package ru.ifmo.movies_app.repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import ru.ifmo.movies_app.domain.Movie;
import ru.ifmo.movies_app.domain.MovieGenre;
import ru.ifmo.movies_app.domain.Person;
import ru.ifmo.movies_app.dto.MovieTableFilter;

@Repository
@Transactional(readOnly = true)
public class JpaMovieRepository implements MovieRepository {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public Optional<Movie> findById(Long id) {
        return Optional.ofNullable(entityManager.find(Movie.class, id));
    }

    @Override
    public Optional<Movie> findByIdWithRelations(Long id) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Movie> query = cb.createQuery(Movie.class);
        Root<Movie> root = query.from(Movie.class);
        root.fetch("director", JoinType.LEFT);
        root.fetch("screenwriter", JoinType.LEFT);
        root.fetch("operator", JoinType.LEFT);
        query.select(root).where(cb.equal(root.get("id"), id));
        return entityManager.createQuery(query).getResultStream().findFirst();
    }

    @Override
    public Page<Movie> findAll(MovieTableFilter filter, Pageable pageable) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();

        CriteriaQuery<Movie> dataQuery = cb.createQuery(Movie.class);
        Root<Movie> root = dataQuery.from(Movie.class);
        Join<Movie, Person> director = root.join("director", JoinType.LEFT);
        Join<Movie, Person> screenwriter = root.join("screenwriter", JoinType.LEFT);
        Join<Movie, Person> operator = root.join("operator", JoinType.LEFT);
        dataQuery.select(root).distinct(true);

        List<Predicate> predicates = composePredicates(filter, cb, root, director, screenwriter, operator);
        if (!predicates.isEmpty()) {
            dataQuery.where(predicates.toArray(Predicate[]::new));
        }

        applySorting(pageable, cb, dataQuery, root, director, screenwriter, operator);

        TypedQuery<Movie> typedQuery = entityManager.createQuery(dataQuery);
        typedQuery.setFirstResult((int) pageable.getOffset());
        typedQuery.setMaxResults(pageable.getPageSize());
        List<Movie> content = typedQuery.getResultList();

        long total = countByFilter(filter);
        return new PageImpl<>(content, pageable, total);
    }

    private long countByFilter(MovieTableFilter filter) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Long> countQuery = cb.createQuery(Long.class);
        Root<Movie> root = countQuery.from(Movie.class);
        Join<Movie, Person> director = root.join("director", JoinType.LEFT);
        Join<Movie, Person> screenwriter = root.join("screenwriter", JoinType.LEFT);
        Join<Movie, Person> operator = root.join("operator", JoinType.LEFT);
        countQuery.select(cb.countDistinct(root));

        List<Predicate> predicates = composePredicates(filter, cb, root, director, screenwriter, operator);
        if (!predicates.isEmpty()) {
            countQuery.where(predicates.toArray(Predicate[]::new));
        }

        return entityManager.createQuery(countQuery).getSingleResult();
    }

    private List<Predicate> composePredicates(
            MovieTableFilter filter,
            CriteriaBuilder cb,
            Root<Movie> root,
            Join<Movie, Person> director,
            Join<Movie, Person> screenwriter,
            Join<Movie, Person> operator) {
        List<Predicate> predicates = new ArrayList<>();

        if (filter.getName() != null) {
            predicates.add(cb.equal(
                    cb.lower(root.get("name")),
                    filter.getName().toLowerCase(Locale.ROOT)));
        }
        if (filter.getDirectorName() != null) {
            predicates.add(cb.equal(
                    cb.lower(director.get("name")),
                    filter.getDirectorName().toLowerCase(Locale.ROOT)));
        }
        if (filter.getScreenwriterName() != null) {
            predicates.add(cb.equal(
                    cb.lower(screenwriter.get("name")),
                    filter.getScreenwriterName().toLowerCase(Locale.ROOT)));
        }
        if (filter.getOperatorName() != null) {
            predicates.add(cb.equal(
                    cb.lower(operator.get("name")),
                    filter.getOperatorName().toLowerCase(Locale.ROOT)));
        }
        if (filter.getGenre() != null) {
            predicates.add(cb.equal(root.get("genre"), filter.getGenre()));
        }
        if (filter.getMpaaRating() != null) {
            predicates.add(cb.equal(root.get("mpaaRating"), filter.getMpaaRating()));
        }
        return predicates;
    }

    private void applySorting(
            Pageable pageable,
            CriteriaBuilder cb,
            CriteriaQuery<Movie> query,
            Root<Movie> root,
            Join<Movie, Person> director,
            Join<Movie, Person> screenwriter,
            Join<Movie, Person> operator) {
        if (pageable.getSort().isUnsorted()) {
            return;
        }

        List<jakarta.persistence.criteria.Order> orders = new ArrayList<>();
        for (Sort.Order order : pageable.getSort()) {
            Expression<?> expression = switch (order.getProperty()) {
                case "name" -> root.get("name");
                case "directorName" -> director.get("name");
                case "screenwriterName" -> screenwriter.get("name");
                case "operatorName" -> operator.get("name");
                case "genre" -> root.get("genre");
                case "mpaaRating" -> root.get("mpaaRating");
                default -> null;
            };
            if (expression == null) {
                continue;
            }
            orders.add(order.isAscending() ? cb.asc(expression) : cb.desc(expression));
        }
        query.orderBy(orders);
    }

    @Override
    @Transactional
    public Movie save(Movie movie) {
        if (movie.getId() == null) {
            entityManager.persist(movie);
            return movie;
        }
        return entityManager.merge(movie);
    }

    @Override
    @Transactional
    public void delete(Movie movie) {
        Movie managed = entityManager.contains(movie) ? movie : entityManager.merge(movie);
        entityManager.remove(managed);
    }

    @Override
    public List<Movie> findByDirector(Person director) {
        return entityManager.createQuery(
                        "select m from Movie m where m.director = :person", Movie.class)
                .setParameter("person", director)
                .getResultList();
    }

    @Override
    public List<Movie> findByScreenwriter(Person screenwriter) {
        return entityManager.createQuery(
                        "select m from Movie m where m.screenwriter = :person", Movie.class)
                .setParameter("person", screenwriter)
                .getResultList();
    }

    @Override
    public List<Movie> findByOperator(Person operator) {
        return entityManager.createQuery(
                        "select m from Movie m where m.operator = :person", Movie.class)
                .setParameter("person", operator)
                .getResultList();
    }

    @Override
    public boolean existsByNameIgnoreCase(String name, Long excludeId) {
        if (name == null) {
            return false;
        }
        String normalized = name.trim().toLowerCase(Locale.ROOT);
        StringBuilder jpql = new StringBuilder("select count(m) from Movie m where lower(m.name) = :name");
        if (excludeId != null) {
            jpql.append(" and m.id <> :excludeId");
        }
        var query = entityManager.createQuery(jpql.toString(), Long.class)
                .setParameter("name", normalized);
        if (excludeId != null) {
            query.setParameter("excludeId", excludeId);
        }
        Long count = query.getSingleResult();
        return count != null && count > 0;
    }

    @Override
    public boolean existsByScreenwriterAndGenre(Long screenwriterId, MovieGenre genre, Long excludeId) {
        if (screenwriterId == null || genre == null) {
            return false;
        }
        StringBuilder jpql = new StringBuilder(
                "select count(m) from Movie m where m.screenwriter.id = :screenwriterId and m.genre = :genre");
        if (excludeId != null) {
            jpql.append(" and m.id <> :excludeId");
        }
        var query = entityManager.createQuery(jpql.toString(), Long.class)
                .setParameter("screenwriterId", screenwriterId)
                .setParameter("genre", genre);
        if (excludeId != null) {
            query.setParameter("excludeId", excludeId);
        }
        Long count = query.getSingleResult();
        return count != null && count > 0;
    }
}
