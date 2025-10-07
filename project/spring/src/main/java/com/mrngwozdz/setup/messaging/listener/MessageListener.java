package com.mrngwozdz.setup.messaging.listener;

import com.mrngwozdz.setup.properties.RabbitMQProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class MessageListener {

    private final RabbitMQProperties rabbitMQProperties;

    @RabbitListener(
            queues = "#{rabbitMQProperties.order.name}",
            containerFactory = "orderListenerContainerFactory"
    )
    public void handleOrderMessage(String message) {
        log.info("╔══════════════════════════════════════════════════════════════════════════════");
        log.info("║ ORDER LISTENER - Message received from queue: {}", rabbitMQProperties.getOrder().getName());
        log.info("║ Routing Key: {}", rabbitMQProperties.getOrder().getRoutingKey());
        log.info("║ Message content: {}", message);
        log.info("╠══════════════════════════════════════════════════════════════════════════════");
        processOrderMessage(message);
        log.info("║ ORDER LISTENER - Processing completed");
        log.info("╚══════════════════════════════════════════════════════════════════════════════");
    }

    @RabbitListener(
            queues = "#{rabbitMQProperties.notification.name}",
            containerFactory = "notificationListenerContainerFactory"
    )
    public void handleNotificationMessage(String message) {
        log.info("╔══════════════════════════════════════════════════════════════════════════════");
        log.info("║ NOTIFICATION LISTENER - Message received from queue: {}", rabbitMQProperties.getNotification().getName());
        log.info("║ Routing Key: {}", rabbitMQProperties.getNotification().getRoutingKey());
        log.info("║ Message content: {}", message);
        log.info("╠══════════════════════════════════════════════════════════════════════════════");
        processNotificationMessage(message);
        log.info("║ NOTIFICATION LISTENER - Processing completed");
        log.info("╚══════════════════════════════════════════════════════════════════════════════");
    }

    @RabbitListener(
            queues = "#{rabbitMQProperties.audit.name}",
            containerFactory = "auditListenerContainerFactory"
    )
    public void handleAuditMessage(String message) {
        log.info("╔══════════════════════════════════════════════════════════════════════════════");
        log.info("║ AUDIT LISTENER - Message received from queue: {}", rabbitMQProperties.getAudit().getName());
        log.info("║ Routing Key: {}", rabbitMQProperties.getAudit().getRoutingKey());
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