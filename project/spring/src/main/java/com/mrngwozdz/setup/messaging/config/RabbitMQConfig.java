package com.mrngwozdz.setup.messaging.config;

import com.mrngwozdz.setup.properties.RabbitMQListenerProperties;
import com.mrngwozdz.setup.properties.RabbitMQProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class RabbitMQConfig {

    private final RabbitMQProperties rabbitMQProperties;
    private final RabbitMQListenerProperties listenerProperties;

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
        template.setExchange(rabbitMQProperties.getExchangeName());
        return template;
    }

    @Bean
    public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(
            ConnectionFactory connectionFactory,
            MessageConverter messageConverter) {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setMessageConverter(messageConverter);
        factory.setMissingQueuesFatal(true);
        return factory;
    }

    @Bean
    public SimpleRabbitListenerContainerFactory orderListenerContainerFactory(
            ConnectionFactory connectionFactory,
            MessageConverter messageConverter) {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setMessageConverter(messageConverter);
        factory.setConcurrentConsumers(parseConcurrency(listenerProperties.getOrderConcurrency())[0]);
        factory.setMaxConcurrentConsumers(parseConcurrency(listenerProperties.getOrderConcurrency())[1]);
        factory.setMissingQueuesFatal(true);
        return factory;
    }

    @Bean
    public SimpleRabbitListenerContainerFactory notificationListenerContainerFactory(
            ConnectionFactory connectionFactory,
            MessageConverter messageConverter) {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setMessageConverter(messageConverter);
        factory.setConcurrentConsumers(parseConcurrency(listenerProperties.getNotificationConcurrency())[0]);
        factory.setMaxConcurrentConsumers(parseConcurrency(listenerProperties.getNotificationConcurrency())[1]);
        factory.setMissingQueuesFatal(true);
        return factory;
    }

    @Bean
    public SimpleRabbitListenerContainerFactory auditListenerContainerFactory(
            ConnectionFactory connectionFactory,
            MessageConverter messageConverter) {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setMessageConverter(messageConverter);
        factory.setConcurrentConsumers(parseConcurrency(listenerProperties.getAuditConcurrency())[0]);
        factory.setMaxConcurrentConsumers(parseConcurrency(listenerProperties.getAuditConcurrency())[1]);
        factory.setMissingQueuesFatal(true);
        return factory;
    }

    private int[] parseConcurrency(String concurrency) {
        String[] parts = concurrency.split("-");
        return new int[]{Integer.parseInt(parts[0]), Integer.parseInt(parts[1])};
    }
}