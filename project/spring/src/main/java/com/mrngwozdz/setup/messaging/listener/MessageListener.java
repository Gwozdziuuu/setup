package com.mrngwozdz.setup.messaging.listener;

import com.mrngwozdz.setup.messaging.config.RabbitMQConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class MessageListener {

    @RabbitListener(queues = RabbitMQConfig.ORDER_QUEUE)
    public void handleOrderMessage(String message) {
        log.info("╔══════════════════════════════════════════════════════════════════════════════");
        log.info("║ ORDER LISTENER - Message received from queue: {}", RabbitMQConfig.ORDER_QUEUE);
        log.info("║ Routing Key: {}", RabbitMQConfig.ORDER_ROUTING_KEY);
        log.info("║ Message content: {}", message);
        log.info("╠══════════════════════════════════════════════════════════════════════════════");
        processOrderMessage(message);
        log.info("║ ORDER LISTENER - Processing completed");
        log.info("╚══════════════════════════════════════════════════════════════════════════════");
    }

    @RabbitListener(queues = RabbitMQConfig.NOTIFICATION_QUEUE)
    public void handleNotificationMessage(String message) {
        log.info("╔══════════════════════════════════════════════════════════════════════════════");
        log.info("║ NOTIFICATION LISTENER - Message received from queue: {}", RabbitMQConfig.NOTIFICATION_QUEUE);
        log.info("║ Routing Key: {}", RabbitMQConfig.NOTIFICATION_ROUTING_KEY);
        log.info("║ Message content: {}", message);
        log.info("╠══════════════════════════════════════════════════════════════════════════════");
        processNotificationMessage(message);
        log.info("║ NOTIFICATION LISTENER - Processing completed");
        log.info("╚══════════════════════════════════════════════════════════════════════════════");
    }

    @RabbitListener(queues = RabbitMQConfig.AUDIT_QUEUE)
    public void handleAuditMessage(String message) {
        log.info("╔══════════════════════════════════════════════════════════════════════════════");
        log.info("║ AUDIT LISTENER - Message received from queue: {}", RabbitMQConfig.AUDIT_QUEUE);
        log.info("║ Routing Key: {}", RabbitMQConfig.AUDIT_ROUTING_KEY);
        log.info("║ Message content: {}", message);
        log.info("╠══════════════════════════════════════════════════════════════════════════════");
        processAuditMessage(message);
        log.info("║ AUDIT LISTENER - Processing completed");
        log.info("╚══════════════════════════════════════════════════════════════════════════════");
    }

    private void processOrderMessage(String message) {
        log.info("║ [ORDER] Processing order: {}", message);
        // Order processing logic here
    }

    private void processNotificationMessage(String message) {
        log.info("║ [NOTIFICATION] Sending notification: {}", message);
        // Notification processing logic here
    }

    private void processAuditMessage(String message) {
        log.info("║ [AUDIT] Logging audit event: {}", message);
        // Audit processing logic here
    }
}