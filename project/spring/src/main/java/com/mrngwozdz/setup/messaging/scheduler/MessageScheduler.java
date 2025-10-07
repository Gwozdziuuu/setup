package com.mrngwozdz.setup.messaging.scheduler;

import com.mrngwozdz.setup.messaging.sender.MessageSender;
import com.mrngwozdz.setup.properties.RabbitMQProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
@Component
@RequiredArgsConstructor
public class MessageScheduler {

    private final MessageSender messageSender;
    private final RabbitMQProperties rabbitMQProperties;
    private final AtomicInteger messageCounter = new AtomicInteger(0);
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Scheduled(fixedRate = 5000)
    public void sendScheduledMessages() {
        int count = messageCounter.incrementAndGet();
        String timestamp = LocalDateTime.now().format(FORMATTER);

        log.info("════════════════════════════════════════════════════════════════════════════════");
        log.info("SCHEDULER EXECUTION #{} - Started at {}", count, timestamp);
        log.info("════════════════════════════════════════════════════════════════════════════════");

        // Send Order message
        String orderMessage = String.format("Order #%d created at %s", count, timestamp);
        log.info("→ Sending ORDER message (routing key: {})", rabbitMQProperties.getOrder().getRoutingKey());
        messageSender.send(rabbitMQProperties.getOrder().getRoutingKey(), orderMessage);
        log.info("✓ ORDER message sent");

        // Send Notification message
        String notificationMessage = String.format("Notification #%d sent at %s", count, timestamp);
        log.info("→ Sending NOTIFICATION message (routing key: {})", rabbitMQProperties.getNotification().getRoutingKey());
        messageSender.send(rabbitMQProperties.getNotification().getRoutingKey(), notificationMessage);
        log.info("✓ NOTIFICATION message sent");

        // Send Audit message
        String auditMessage = String.format("Audit event #%d logged at %s", count, timestamp);
        log.info("→ Sending AUDIT message (routing key: {})", rabbitMQProperties.getAudit().getRoutingKey());
        messageSender.send(rabbitMQProperties.getAudit().getRoutingKey(), auditMessage);
        log.info("✓ AUDIT message sent");

        log.info("════════════════════════════════════════════════════════════════════════════════");
        log.info("SCHEDULER EXECUTION #{} - Completed. All 3 messages sent successfully", count);
        log.info("════════════════════════════════════════════════════════════════════════════════");
        log.info("");
    }
}