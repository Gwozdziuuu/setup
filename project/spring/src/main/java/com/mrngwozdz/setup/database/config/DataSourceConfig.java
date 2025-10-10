package com.mrngwozdz.setup.database.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import javax.sql.DataSource;

@Configuration
public class DataSourceConfig {

    @Primary
    @Bean(name = "commandDataSource")
    public DataSource commandDataSource(
            @Value("${spring.datasource.command.jdbc-url}") String dbUrl,
            @Value("${spring.datasource.command.username}") String dbUsername,
            @Value("${spring.datasource.command.password}") String dbPassword,
            @Value("${spring.datasource.command.max-connections:5}") int commandMaxConnections,
            @Value("${spring.datasource.command.minimum-idle:3}") int commandMinimumIdle
    ) {
        return getDataSource("CommandDataBaseConnectionPool", dbUrl, dbUsername, dbPassword, commandMaxConnections, commandMinimumIdle, false);
    }

    @Bean(name = "queryDataSource")
    public DataSource queryDataSource(
            @Value("${spring.datasource.query.jdbc-url}") String dbUrl,
            @Value("${spring.datasource.query.username}") String dbUsername,
            @Value("${spring.datasource.query.password}") String dbPassword,
            @Value("${spring.datasource.query.max-connections:30}") int queryMaxConnections,
            @Value("${spring.datasource.query.minimum-idle:5}") int queryMinimumIdle
    ) {
        return getDataSource("QueryDataBaseConnectionPool", dbUrl, dbUsername, dbPassword, queryMaxConnections, queryMinimumIdle, true);
    }

    private DataSource getDataSource(String poolName, String dbUrl, String dbUsername, String dbPassword, int maxConnections, int minimumIdle, boolean isReadOnly) {
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