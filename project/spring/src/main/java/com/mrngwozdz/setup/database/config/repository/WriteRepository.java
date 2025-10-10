package com.mrngwozdz.setup.database.config.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.NoRepositoryBean;

/**
 * Base interface for write repositories in CQRS pattern.
 * All command repositories should extend this interface.
 *
 * Transaction management is handled at the Business Layer using @Transactional
 * combined with @WriteOperation annotation to route to the WRITE DataSource.
 *
 * @param <T> the domain type the repository manages
 * @param <ID> the type of the id of the entity the repository manages
 */
@NoRepositoryBean
public interface WriteRepository<T, ID> extends JpaRepository<T, ID> {
}