package com.mrngwozdz.setup.database.config.datasource;

/**
 * Enum representing the type of DataSource to use in CQRS pattern.
 * Used by RoutingDataSource to determine which physical DataSource to use.
 */
public enum DataSourceType {
    /**
     * Read-only DataSource for query operations.
     * Uses a larger connection pool optimized for read operations.
     */
    READ,

    /**
     * Read-write DataSource for command operations.
     * Uses a smaller connection pool optimized for write operations.
     */
    WRITE
}