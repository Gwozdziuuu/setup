package com.mrngwozdz.setup.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "spring.rabbitmq.listener")
public class RabbitMQListenerProperties {

    private String orderConcurrency = "1-10";
    private String notificationConcurrency = "1-5";
    private String auditConcurrency = "1-3";
}