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
    public List<ImportOperation> findAllOrderByCreatedAtDesc() {
        return entityManager.createQuery(
                        "select op from ImportOperation op order by op.createdAt desc",
                        ImportOperation.class)
                .getResultList();
    }

    @Override
    public List<ImportOperation> findByUsernameOrderByCreatedAtDesc(String username) {
        return entityManager.createQuery(
                        "select op from ImportOperation op " +
                                "where op.username = :username order by op.createdAt desc",
                        ImportOperation.class)
                .setParameter("username", username)
                .getResultList();
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
