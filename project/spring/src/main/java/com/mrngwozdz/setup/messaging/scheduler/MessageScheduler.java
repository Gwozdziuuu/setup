package com.mrngwozdz.setup.messaging.scheduler;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mrngwozdz.setup.messaging.model.OrderRequest;
import com.mrngwozdz.setup.messaging.sender.MessageSender;
import com.mrngwozdz.setup.properties.RabbitMQProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
@Component
@RequiredArgsConstructor
public class MessageScheduler {

    private final MessageSender messageSender;
    private final RabbitMQProperties rabbitMQProperties;
    private final ObjectMapper objectMapper;
    private final AtomicInteger messageCounter = new AtomicInteger(0);
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Scheduled(fixedRate = 5000)
    public void sendScheduledMessages() {
        int count = messageCounter.incrementAndGet();
        String timestamp = LocalDateTime.now().format(FORMATTER);

        log.info("════════════════════════════════════════════════════════════════════════════════");
        log.info("SCHEDULER EXECUTION #{} - Started at {}", count, timestamp);
        log.info("════════════════════════════════════════════════════════════════════════════════");

        // Send Order message as JSON
        sendOrderMessage(count);

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

    private void sendOrderMessage(int count) {
        try {
            // Create a valid OrderRequest
            OrderRequest orderRequest = new OrderRequest(
                    "ORD-" + String.format("%05d", count),
                    "CUST-" + (count % 100), // Cycle through 100 customers
                    new BigDecimal("99.99").add(new BigDecimal(count)), // Varying amounts
                    "PROD-" + (count % 10) // Cycle through 10 products
            );

            String orderMessageJson = objectMapper.writeValueAsString(orderRequest);
            log.info("→ Sending ORDER message (routing key: {}): {}",
                    rabbitMQProperties.getOrder().getRoutingKey(), orderMessageJson);
            messageSender.send(rabbitMQProperties.getOrder().getRoutingKey(), orderMessageJson);
            log.info("✓ ORDER message sent");
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize OrderRequest to JSON", e);
        }
    }
}