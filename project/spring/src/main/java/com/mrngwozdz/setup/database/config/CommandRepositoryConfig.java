package com.mrngwozdz.setup.database.config;

import com.mrngwozdz.setup.service.order.data.repository.command.OrderCommandRepositoryMarker;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@Configuration
@EnableJpaRepositories(
        basePackageClasses = {
                OrderCommandRepositoryMarker.class
        },
        entityManagerFactoryRef = "commandEntityManagerFactory",
        transactionManagerRef = "commandTransactionManager"
)
public class CommandRepositoryConfig {}