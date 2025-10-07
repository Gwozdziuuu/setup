package com.mrngwozdz.setup.messaging.config;

import com.mrngwozdz.setup.properties.RabbitMQListenerProperties;
import com.mrngwozdz.setup.properties.RabbitMQProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aopalliance.aop.Advice;
import org.springframework.amqp.rabbit.config.RetryInterceptorBuilder;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.retry.MessageRecoverer;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Slf4j
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
            MessageConverter messageConverter,
            Advice retryInterceptor) {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setMessageConverter(messageConverter);
        factory.setMissingQueuesFatal(true);
        factory.setAdviceChain(retryInterceptor);
        factory.setDefaultRequeueRejected(false);
        return factory;
    }

    @Bean
    public SimpleRabbitListenerContainerFactory orderListenerContainerFactory(
            ConnectionFactory connectionFactory,
            MessageConverter messageConverter,
            Advice retryInterceptor) {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setMessageConverter(messageConverter);
        factory.setConcurrentConsumers(parseConcurrency(listenerProperties.getOrderConcurrency())[0]);
        factory.setMaxConcurrentConsumers(parseConcurrency(listenerProperties.getOrderConcurrency())[1]);
        factory.setMissingQueuesFatal(true);
        factory.setAdviceChain(retryInterceptor);
        factory.setDefaultRequeueRejected(false);
        return factory;
    }

    @Bean
    public SimpleRabbitListenerContainerFactory notificationListenerContainerFactory(
            ConnectionFactory connectionFactory,
            MessageConverter messageConverter,
            Advice retryInterceptor) {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setMessageConverter(messageConverter);
        factory.setConcurrentConsumers(parseConcurrency(listenerProperties.getNotificationConcurrency())[0]);
        factory.setMaxConcurrentConsumers(parseConcurrency(listenerProperties.getNotificationConcurrency())[1]);
        factory.setMissingQueuesFatal(true);
        factory.setAdviceChain(retryInterceptor);
        factory.setDefaultRequeueRejected(false);
        return factory;
    }

    @Bean
    public SimpleRabbitListenerContainerFactory auditListenerContainerFactory(
            ConnectionFactory connectionFactory,
            MessageConverter messageConverter,
            Advice retryInterceptor) {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setMessageConverter(messageConverter);
        factory.setConcurrentConsumers(parseConcurrency(listenerProperties.getAuditConcurrency())[0]);
        factory.setMaxConcurrentConsumers(parseConcurrency(listenerProperties.getAuditConcurrency())[1]);
        factory.setMissingQueuesFatal(true);
        factory.setAdviceChain(retryInterceptor);
        factory.setDefaultRequeueRejected(false);
        return factory;
    }

    private int[] parseConcurrency(String concurrency) {
        String[] parts = concurrency.split("-");
        return new int[]{Integer.parseInt(parts[0]), Integer.parseInt(parts[1])};
    }

    @Bean
    public MessageRecoverer messageRecoverer(RabbitTemplate rabbitTemplate) {
        return (message, cause) -> {
            String queueName = (String) message.getMessageProperties().getHeaders().get("x-original-queue");
            String dlqRoutingKey = queueName != null ? queueName + ".dlq" : "unknown.dlq";

            log.error("Message processing failed after {} attempts. Sending to DLQ with routing key: {}. Error: {}",
                    3, dlqRoutingKey, cause.getMessage(), cause);

            rabbitTemplate.send(
                    rabbitMQProperties.getExchangeName() + ".dlx",
                    dlqRoutingKey,
                    message
            );
        };
    }

    @Bean
    public Advice retryInterceptor(MessageRecoverer messageRecoverer) {
        return RetryInterceptorBuilder.stateless()
                .maxAttempts(3)
                .backOffOptions(1000, 2.0, 10000)
                .recoverer(messageRecoverer)
                .build();
    }
}