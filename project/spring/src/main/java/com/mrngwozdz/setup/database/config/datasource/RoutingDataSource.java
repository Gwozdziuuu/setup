package com.mrngwozdz.setup.database.config.datasource;

import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;

/**
 * Dynamic DataSource router for CQRS pattern.
 * Routes database operations to either READ (query) or WRITE (command) DataSource
 * based on the current thread context set by @ReadOperation or @WriteOperation annotations.
 *
 * This allows having a single EntityManagerFactory and TransactionManager while
 * still maintaining separate connection pools for read and write operations.
 */
@Slf4j
public class RoutingDataSource extends AbstractRoutingDataSource {

    /**
     * Determines which DataSource to use based on the current thread context.
     * This method is called by Spring before each database operation.
     *
     * @return the DataSourceType key (READ or WRITE) to look up the target DataSource
     */
    @Override
    protected Object determineCurrentLookupKey() {
        DataSourceType type = DataSourceContextHolder.getDataSourceType();
        log.trace("Routing to DataSource: {}", type);
        return type;
    }
}