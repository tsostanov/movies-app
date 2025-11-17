package ru.ifmo.movies_app.repository;

import java.util.List;
import java.util.Optional;

import ru.ifmo.movies_app.domain.ImportOperation;

public interface ImportOperationRepository {

    ImportOperation save(ImportOperation operation);

    Optional<ImportOperation> findById(Long id);

    List<ImportOperation> findAll(int offset, int limit);

    long countAll();

    List<ImportOperation> findByUsername(String username, int offset, int limit);

    long countByUsername(String username);
}
