package com.mrngwozdz.setup.database.config.datasource;

import lombok.extern.slf4j.Slf4j;

/**
 * Thread-local context holder for DataSource routing.
 * Stores the current DataSourceType for the executing thread.
 * Used by RoutingDataSource to determine which DataSource to use.
 */
@Slf4j
public class DataSourceContextHolder {

    private static final ThreadLocal<DataSourceType> CONTEXT = new ThreadLocal<>();

    /**
     * Sets the DataSource type for the current thread.
     * This will be used by RoutingDataSource to select the appropriate DataSource.
     *
     * @param dataSourceType the type of DataSource to use (READ or WRITE)
     */
    public static void setDataSourceType(DataSourceType dataSourceType) {
        if (dataSourceType == null) {
            log.warn("Attempted to set null DataSourceType, using WRITE as default");
            CONTEXT.set(DataSourceType.WRITE);
        } else {
            log.trace("Setting DataSource type to: {}", dataSourceType);
            CONTEXT.set(dataSourceType);
        }
    }

    /**
     * Gets the current DataSource type for this thread.
     * Returns WRITE as default if no type has been set.
     *
     * @return the current DataSourceType (defaults to WRITE if not set)
     */
    public static DataSourceType getDataSourceType() {
        DataSourceType type = CONTEXT.get();
        if (type == null) {
            log.trace("No DataSource type set, defaulting to WRITE");
            return DataSourceType.WRITE;
        }
        return type;
    }

    /**
     * Clears the DataSource type from the current thread.
     * Should be called after the operation completes to prevent memory leaks.
     */
    public static void clear() {
        log.trace("Clearing DataSource type from context");
        CONTEXT.remove();
    }
}