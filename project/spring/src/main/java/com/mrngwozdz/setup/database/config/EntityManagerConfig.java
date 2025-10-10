package com.mrngwozdz.setup.database.config;

import com.mrngwozdz.setup.database.entity.DatabaseMarker;
import com.mrngwozdz.setup.database.entity.security.SecurityDatabaseMarker;
import org.springframework.boot.orm.jpa.EntityManagerFactoryBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.sql.DataSource;
import java.util.Objects;

/**
 * Configuration for JPA EntityManager and TransactionManager.
 * Uses a single EntityManagerFactory and TransactionManager that work with
 * the RoutingDataSource to dynamically route to READ or WRITE DataSource.
 */
@Configuration
@EnableTransactionManagement
public class EntityManagerConfig {

    /**
     * Single EntityManagerFactory that uses the RoutingDataSource.
     * The DataSource routing is handled transparently by DataSourceAspect
     * based on @ReadOperation or @WriteOperation annotations.
     */
    @Primary
    @Bean(name = "entityManagerFactory")
    public LocalContainerEntityManagerFactoryBean entityManagerFactory(
            EntityManagerFactoryBuilder builder,
            DataSource dataSource) {
        return builder
                .dataSource(dataSource)  // This is the RoutingDataSource
                .packages(DatabaseMarker.class.getPackageName(), SecurityDatabaseMarker.class.getPackageName())
                .persistenceUnit("default")
                .properties(java.util.Map.of(
                        "hibernate.cache.use_second_level_cache", "false",
                        "hibernate.cache.use_query_cache", "false"
                ))
                .build();
    }

    /**
     * Single TransactionManager that works with the EntityManagerFactory.
     * The DataSource routing happens automatically based on the current thread context.
     */
    @Primary
    @Bean(name = "transactionManager")
    public PlatformTransactionManager transactionManager(
            LocalContainerEntityManagerFactoryBean entityManagerFactory) {
        return new JpaTransactionManager(Objects.requireNonNull(entityManagerFactory.getObject()));
    }
}
