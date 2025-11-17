package ru.ifmo.movies_app.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import ru.ifmo.movies_app.domain.ImportOperation;

@Repository
@Transactional(readOnly = true)
public class JpaImportOperationRepository implements ImportOperationRepository {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public Optional<ImportOperation> findById(Long id) {
        return Optional.ofNullable(entityManager.find(ImportOperation.class, id));
    }

    @Override
    public List<ImportOperation> findAll(int offset, int limit) {
        return entityManager.createQuery(
                        "select op from ImportOperation op order by op.createdAt desc",
                        ImportOperation.class)
                .setFirstResult(Math.max(offset, 0))
                .setMaxResults(limit)
                .getResultList();
    }

    @Override
    public long countAll() {
        return entityManager.createQuery("select count(op) from ImportOperation op", Long.class)
                .getSingleResult();
    }

    @Override
    public List<ImportOperation> findByUsername(String username, int offset, int limit) {
        return entityManager.createQuery(
                        "select op from ImportOperation op " +
                                "where op.username = :username order by op.createdAt desc",
                        ImportOperation.class)
                .setParameter("username", username)
                .setFirstResult(Math.max(offset, 0))
                .setMaxResults(limit)
                .getResultList();
    }

    @Override
    public long countByUsername(String username) {
        return entityManager.createQuery(
                        "select count(op) from ImportOperation op where op.username = :username",
                        Long.class)
                .setParameter("username", username)
                .getSingleResult();
    }

    @Override
    @Transactional
    public ImportOperation save(ImportOperation operation) {
        if (operation.getId() == null) {
            entityManager.persist(operation);
            return operation;
        }
        return entityManager.merge(operation);
    }
}
