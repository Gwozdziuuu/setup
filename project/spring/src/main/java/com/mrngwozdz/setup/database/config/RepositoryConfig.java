package com.mrngwozdz.setup.database.config;

import com.mrngwozdz.setup.service.order.data.repository.command.OrderCommandRepositoryMarker;
import com.mrngwozdz.setup.service.order.data.repository.query.OrderQueryRepositoryMarker;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

/**
 * Configuration for JPA repositories.
 * All repositories (both query and command) use the same EntityManagerFactory
 * which delegates to RoutingDataSource for dynamic DataSource selection.
 */
@Configuration
@EnableJpaRepositories(
        basePackageClasses = {
                OrderQueryRepositoryMarker.class,
                OrderCommandRepositoryMarker.class
        },
        entityManagerFactoryRef = "entityManagerFactory",
        transactionManagerRef = "transactionManager"
)
public class RepositoryConfig {
}