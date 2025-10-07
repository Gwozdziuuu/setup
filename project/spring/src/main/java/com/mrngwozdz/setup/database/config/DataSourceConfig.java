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
    @Bean(name = "writeDataSource")
    public DataSource writeDataSource(
            @Value("${spring.datasource.write.jdbc-url}") String dbUrl,
            @Value("${spring.datasource.write.username}") String dbUsername,
            @Value("${spring.datasource.write.password}") String dbPassword,
            @Value("${spring.datasource.write.max-connections:5}") int writeMaxConnections,
            @Value("${spring.datasource.write.minimum-idle:3}") int writeMinimumIdle
    ) {
        return getDataSource("WriteDataBaseConnectionPool", dbUrl, dbUsername, dbPassword, writeMaxConnections, writeMinimumIdle);
    }

    @Bean(name = "readDataSource")
    @Qualifier("readDataSource")
    public DataSource readDataSource(
            @Value("${spring.datasource.read.jdbc-url}") String dbUrl,
            @Value("${spring.datasource.read.username}") String dbUsername,
            @Value("${spring.datasource.read.password}") String dbPassword,
            @Value("${spring.datasource.read.max-connections:30}") int readMaxConnections,
            @Value("${spring.datasource.read.minimum-idle:5}") int readMinimumIdle
    ) {
        return getDataSource("ReadDataBaseConnectionPool", dbUrl, dbUsername, dbPassword, readMaxConnections, readMinimumIdle);
    }

    private DataSource getDataSource(String poolName, String dbUrl, String dbUsername, String dbPassword, int maxConnections, int minimumIdle) {
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

        return new HikariDataSource(hikariConfig);
    }
}