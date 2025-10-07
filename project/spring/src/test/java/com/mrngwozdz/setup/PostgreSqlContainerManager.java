package com.mrngwozdz.setup;

import org.testcontainers.containers.PostgreSQLContainer;

public class PostgreSqlContainerManager {

    public static final PostgreSQLContainer<?> postgres;

    static {
        postgres = new PostgreSQLContainer<>("postgres")
                .withDatabaseName("setup")
                .withUsername("postgres")
                .withPassword("password");
        postgres.start();
    }

    private PostgreSqlContainerManager() {
        throw new IllegalStateException("Utility class");
    }

}
