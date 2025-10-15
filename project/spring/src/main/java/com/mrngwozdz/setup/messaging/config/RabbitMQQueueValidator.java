package com.mrngwozdz.setup.messaging.config;

import com.mrngwozdz.setup.properties.RabbitMQProperties;
import com.rabbitmq.client.Channel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.Profile;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@Profile("!test")
@RequiredArgsConstructor
public class RabbitMQQueueValidator {

    private final ConnectionFactory connectionFactory;
    private final RabbitMQProperties rabbitMQProperties;

    @EventListener(ApplicationReadyEvent.class)
    public void validateQueues() {
        log.info("Validating RabbitMQ queues...");

        validateMainQueues();
        validateDlqQueues();

        log.info("All RabbitMQ queues validated successfully");
    }

    private void validateMainQueues() {
        List<String> mainQueues = List.of(
                rabbitMQProperties.getOrder().getName(),
                rabbitMQProperties.getNotification().getName(),
                rabbitMQProperties.getAudit().getName()
        );

        for (String queueName : mainQueues) {
            validateQueue(queueName, "Main queue");
        }
    }

    private void validateDlqQueues() {
        List<String> dlqQueues = List.of(
                rabbitMQProperties.getOrder().getName() + ".dlq",
                rabbitMQProperties.getNotification().getName() + ".dlq",
                rabbitMQProperties.getAudit().getName() + ".dlq"
        );

        for (String queueName : dlqQueues) {
            validateQueue(queueName, "DLQ");
        }
    }


    /**
     * Validates that a RabbitMQ queue exists using passive declaration.
     * <p>
     * Passive declaration checks for queue existence without attempting to create it,
     * requiring only read permissions instead of administrative privileges.
     *
     * @param queueName the name of the queue to validate
     * @param queueType the type of queue (e.g., "Main queue", "DLQ") for logging purposes
     * @throws IllegalStateException if the queue does not exist
     */
    private void validateQueue(String queueName, String queueType) {
        try (var connection = connectionFactory.createConnection();
             Channel channel = connection.createChannel(false)) {
            channel.queueDeclarePassive(queueName);
            log.info("{} '{}' validated successfully", queueType, queueName);
        } catch (Exception e) {
            String errorMessage = String.format(
                    "%s '%s' does not exist in RabbitMQ. Please ensure it's defined in services/rabbitmq/definitions.json. Error: %s",
                    queueType, queueName, e.getMessage()
            );
            log.error(errorMessage);
            throw new IllegalStateException(errorMessage, e);
        }
    }
}