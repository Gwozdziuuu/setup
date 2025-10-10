package com.mrngwozdz.setup.database.config;

import com.mrngwozdz.setup.database.entity.DatabaseMarker;
import com.mrngwozdz.setup.database.entity.security.SecurityDatabaseMarker;
import org.springframework.beans.factory.annotation.Qualifier;
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

@Configuration
@EnableTransactionManagement
public class EntityManagerConfig {

    @Primary
    @Bean(name = "commandEntityManagerFactory")
    public LocalContainerEntityManagerFactoryBean commandEntityManagerFactory(
            EntityManagerFactoryBuilder builder,
            @Qualifier("commandDataSource") DataSource dataSource) {
        return builder
                .dataSource(dataSource)
                .packages(DatabaseMarker.class.getPackageName(), SecurityDatabaseMarker.class.getPackageName())
                .persistenceUnit("command")
                .properties(java.util.Map.of(
                        "hibernate.cache.use_second_level_cache", "false",
                        "hibernate.cache.use_query_cache", "false"
                ))
                .build();
    }

    @Bean(name = "queryEntityManagerFactory")
    public LocalContainerEntityManagerFactoryBean queryEntityManagerFactory(
            EntityManagerFactoryBuilder builder,
            @Qualifier("queryDataSource") DataSource dataSource) {
        return builder
                .dataSource(dataSource)
                .packages(DatabaseMarker.class.getPackageName(), SecurityDatabaseMarker.class.getPackageName())
                .persistenceUnit("query")
                .build();
    }

    @Primary
    @Bean(name = "commandTransactionManager")
    public PlatformTransactionManager commandTransactionManager(
            @Qualifier("commandEntityManagerFactory") LocalContainerEntityManagerFactoryBean commandEntityManagerFactoryBean) {
        return new JpaTransactionManager(Objects.requireNonNull(commandEntityManagerFactoryBean.getObject()));
    }

    @Bean(name = "queryTransactionManager")
    public PlatformTransactionManager queryTransactionManager(
            @Qualifier("queryEntityManagerFactory") LocalContainerEntityManagerFactoryBean queryEntityManagerFactoryBean) {
        return new JpaTransactionManager(Objects.requireNonNull(queryEntityManagerFactoryBean.getObject()));
    }
}
