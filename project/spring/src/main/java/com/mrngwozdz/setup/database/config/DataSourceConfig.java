package com.mrngwozdz.setup.database.config;

import com.mrngwozdz.setup.database.config.datasource.DataSourceType;
import com.mrngwozdz.setup.database.config.datasource.RoutingDataSource;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;

/**
 * Configuration for CQRS DataSource routing.
 * Creates a single RoutingDataSource that dynamically routes to either:
 * - READ DataSource (query operations with larger connection pool)
 * - WRITE DataSource (command operations with smaller connection pool)
 *
 * The routing is determined by @ReadOperation or @WriteOperation annotations
 * on business layer methods.
 */
@Configuration
public class DataSourceConfig {

    /**
     * Primary DataSource bean that routes to READ or WRITE DataSource
     * based on the current thread context set by DataSourceAspect.
     */
    @Primary
    @Bean
    public DataSource dataSource(
            @Value("${spring.datasource.command.jdbc-url}") String commandUrl,
            @Value("${spring.datasource.command.username}") String commandUsername,
            @Value("${spring.datasource.command.password}") String commandPassword,
            @Value("${spring.datasource.command.max-connections:5}") int commandMaxConnections,
            @Value("${spring.datasource.command.minimum-idle:3}") int commandMinimumIdle,
            @Value("${spring.datasource.query.jdbc-url}") String queryUrl,
            @Value("${spring.datasource.query.username}") String queryUsername,
            @Value("${spring.datasource.query.password}") String queryPassword,
            @Value("${spring.datasource.query.max-connections:30}") int queryMaxConnections,
            @Value("${spring.datasource.query.minimum-idle:5}") int queryMinimumIdle
    ) {
        // Create READ DataSource
        DataSource readDataSource = createDataSource(
                "QueryDataBaseConnectionPool",
                queryUrl,
                queryUsername,
                queryPassword,
                queryMaxConnections,
                queryMinimumIdle,
                true
        );

        // Create WRITE DataSource
        DataSource writeDataSource = createDataSource(
                "CommandDataBaseConnectionPool",
                commandUrl,
                commandUsername,
                commandPassword,
                commandMaxConnections,
                commandMinimumIdle,
                false
        );

        // Configure routing
        RoutingDataSource routingDataSource = new RoutingDataSource();

        Map<Object, Object> targetDataSources = new HashMap<>();
        targetDataSources.put(DataSourceType.READ, readDataSource);
        targetDataSources.put(DataSourceType.WRITE, writeDataSource);

        routingDataSource.setTargetDataSources(targetDataSources);
        routingDataSource.setDefaultTargetDataSource(writeDataSource);  // Default to WRITE

        return routingDataSource;
    }

    /**
     * Creates a HikariCP DataSource with the specified configuration.
     */
    private DataSource createDataSource(
            String poolName,
            String dbUrl,
            String dbUsername,
            String dbPassword,
            int maxConnections,
            int minimumIdle,
            boolean isReadOnly
    ) {
        int safeMinIdle = Math.min(minimumIdle, maxConnections);

        HikariConfig hikariConfig = new HikariConfig();
        hikariConfig.setPoolName(poolName);
        hikariConfig.setDriverClassName("org.postgresql.Driver");
        hikariConfig.setJdbcUrl(dbUrl);
        hikariConfig.setUsername(dbUsername);
        hikariConfig.setPassword(dbPassword);

        hikariConfig.setMinimumIdle(safeMinIdle);
        hikariConfig.setMaximumPoolSize(maxConnections);
        hikariConfig.setConnectionTimeout(30L * 1000);
        hikariConfig.setIdleTimeout(10L * 60 * 1000);
        hikariConfig.setMaxLifetime(30L * 60 * 1000);
        hikariConfig.setKeepaliveTime(5L * 60 * 1000);
        hikariConfig.setLeakDetectionThreshold(60L * 1000);

        if (isReadOnly) {
            hikariConfig.setReadOnly(true);
            hikariConfig.setAutoCommit(true);
        }

        return new HikariDataSource(hikariConfig);
    }
}