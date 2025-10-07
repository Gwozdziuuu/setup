package com.mrngwozdz.setup.messaging.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    // Exchange and Queue names - must match definitions.json
    public static final String EXCHANGE_NAME = "setup.direct.exchange";

    public static final String ORDER_QUEUE = "setup.order.queue";
    public static final String NOTIFICATION_QUEUE = "setup.notification.queue";
    public static final String AUDIT_QUEUE = "setup.audit.queue";

    public static final String ORDER_ROUTING_KEY = "order";
    public static final String NOTIFICATION_ROUTING_KEY = "notification";
    public static final String AUDIT_ROUTING_KEY = "audit";

    // Note: Exchange, Queues and Bindings are configured in services/rabbitmq/definitions.json
    // and loaded automatically by RabbitMQ on startup

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory, MessageConverter messageConverter) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(messageConverter);
        template.setExchange(EXCHANGE_NAME);
        return template;
    }

    @Bean
    public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(
            ConnectionFactory connectionFactory,
            MessageConverter messageConverter) {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setMessageConverter(messageConverter);
        return factory;
    }
}