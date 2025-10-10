package com.mrngwozdz.setup.database.config.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.NoRepositoryBean;
import org.springframework.transaction.annotation.Transactional;

/**
 * Base interface for read-only repositories in CQRS pattern.
 * All query repositories should extend this interface to ensure read-only transaction semantics.
 *
 * @param <T> the domain type the repository manages
 * @param <ID> the type of the id of the entity the repository manages
 */
@NoRepositoryBean
@Transactional(readOnly = true, transactionManager = "queryTransactionManager")
public interface ReadOnlyRepository<T, ID> extends JpaRepository<T, ID> {
}