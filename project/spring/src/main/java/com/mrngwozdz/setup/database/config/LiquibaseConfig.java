package com.mrngwozdz.setup.database.config;

import liquibase.integration.spring.SpringLiquibase;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import javax.sql.DataSource;

@Slf4j
@Configuration
public class LiquibaseConfig {

    @Bean
    public SpringLiquibase liquibase(
            @Value("${spring.datasource.create.username}") String dbUsername,
            @Value("${spring.datasource.create.password}") String dbPassword,
            @Value("${spring.datasource.create.jdbc-url}") String dbUrl) {
        SpringLiquibase liquibase = new SpringLiquibase();
        log.info("Liquibase config: dbUrl = {}, dbUsername = {}, dbPassword = {}***{}",
                dbUrl, dbUsername, dbPassword.isEmpty() ? "" : dbPassword.substring(0, 1),
                dbPassword.isEmpty() ? "" : dbPassword.substring(dbPassword.length() - 1));
        liquibase.setDataSource(dataSource(dbUrl, dbUsername, dbPassword));
        liquibase.setChangeLog("classpath:db/changelog/db.changelog-master.xml");
        return liquibase;
    }

    private DataSource dataSource(String dbUrl, String dbUsername, String dbPassword) {
        DriverManagerDataSource dataSource = new DriverManagerDataSource();
        dataSource.setDriverClassName("org.postgresql.Driver");
        dataSource.setUrl(dbUrl);
        dataSource.setUsername(dbUsername);
        dataSource.setPassword(dbPassword);
        return dataSource;
    }
}