package com.mrngwozdz.setup.database.config.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.NoRepositoryBean;
import org.springframework.transaction.annotation.Transactional;

/**
 * Base interface for write repositories in CQRS pattern.
 * All command repositories should extend this interface to ensure write transaction semantics.
 *
 * @param <T> the domain type the repository manages
 * @param <ID> the type of the id of the entity the repository manages
 */
@NoRepositoryBean
@Transactional(transactionManager = "commandTransactionManager")
public interface WriteRepository<T, ID> extends JpaRepository<T, ID> {
}