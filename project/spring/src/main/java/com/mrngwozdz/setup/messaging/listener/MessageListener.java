package com.mrngwozdz.setup.messaging.listener;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mrngwozdz.setup.database.entity.Order;
import com.mrngwozdz.setup.service.order.data.repository.query.OrderQueryRepository;
import com.mrngwozdz.setup.messaging.model.OrderRequest;
import com.mrngwozdz.setup.platform.result.ErrorCode;
import com.mrngwozdz.setup.platform.result.Failure;
import com.mrngwozdz.setup.platform.result.Success;
import com.mrngwozdz.setup.properties.RabbitMQProperties;
import com.mrngwozdz.setup.service.external.ExternalApiService;
import io.vavr.control.Either;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Slf4j
@Component
@RequiredArgsConstructor
public class MessageListener {

    private final RabbitMQProperties rabbitMQProperties;
    private final OrderQueryRepository orderRepository;
    private final ExternalApiService externalApiService;
    private final ObjectMapper objectMapper;

    @RabbitListener(
            queues = "#{rabbitMQProperties.order.name}",
            containerFactory = "orderListenerContainerFactory"
    )
    @Transactional  // DB rollback on exception
    public void handleOrderMessage(String message) {
        log.info("╔══════════════════════════════════════════════════════════════════════════════");
        log.info("║ ORDER LISTENER - Message received from queue: {}", rabbitMQProperties.getOrder().getName());
        log.info("║ Routing Key: {}", rabbitMQProperties.getOrder().getRoutingKey());
        log.info("║ Message content: {}", message);
        log.info("╠══════════════════════════════════════════════════════════════════════════════");

        processOrderMessage(message)
                .getOrElseThrow(MessageProcessingException::new);

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

        processNotificationMessage(message)
                .getOrElseThrow(MessageProcessingException::new);

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

        processAuditMessage(message)
                .getOrElseThrow(MessageProcessingException::new);

        log.info("║ AUDIT LISTENER - Processing completed");
        log.info("╚══════════════════════════════════════════════════════════════════════════════");
    }

    /**
     * Full example of Either-based message processing with chained operations.
     * Demonstrates:
     * 1. Parse JSON message
     * 2. Validate business rules
     * 3. Save to database (in @Transactional context)
     * 4. Call external API
     * 5. Update order status
     *
     * Each step can fail independently, and errors are handled gracefully.
     * Different error types trigger different behaviors (VALIDATION -> DLQ, TIMEOUT -> retry)
     */
    private Either<Failure, Success<Void>> processOrderMessage(String message) {
        return parseOrder(message)
                .flatMap(this::validateOrder)
                .flatMap(this::checkDuplicateOrder)
                .flatMap(this::saveOrder)
                .flatMap(this::callExternalPaymentApi)
                .flatMap(this::updateOrderStatus)
                .peekLeft(failure -> {
                    // Different reactions based on error type
                    switch (failure.code()) {
                        case VALIDATION:
                            log.error("║ [ORDER] Validation failed - message will go to DLQ: {}", failure.message());
                            break;
                        case CONFLICT:
                            log.warn("║ [ORDER] Duplicate order detected - message will go to DLQ: {}", failure.message());
                            break;
                        case TIMEOUT:
                            log.warn("║ [ORDER] Timeout occurred - will retry with backoff: {}", failure.message());
                            break;
                        case UNAVAILABLE:
                            log.error("║ [ORDER] External service unavailable - will retry: {}", failure.message());
                            break;
                        default:
                            log.error("║ [ORDER] Unexpected error: {}", failure.message());
                    }
                })
                .peek(success -> log.info("║ [ORDER] Order processed successfully"))
                .map(order -> Success.of(null));
    }

    private Either<Failure, OrderRequest> parseOrder(String message) {
        try {
            log.info("║ [ORDER] Step 1/5: Parsing JSON message");
            OrderRequest request = objectMapper.readValue(message, OrderRequest.class);
            log.info("║ [ORDER] Parsed order: {}", request.getOrderId());
            return Either.right(request);
        } catch (Exception e) {
            log.error("║ [ORDER] Failed to parse JSON: {}", e.getMessage());
            return Either.left(Failure.of(ErrorCode.VALIDATION, "Invalid JSON format: " + e.getMessage()));
        }
    }

    private Either<Failure, OrderRequest> validateOrder(OrderRequest request) {
        log.info("║ [ORDER] Step 2/5: Validating business rules");

        if (request.getOrderId() == null || request.getOrderId().isBlank()) {
            return Either.left(Failure.of(ErrorCode.VALIDATION, "Order ID is required"));
        }

        if (request.getCustomerId() == null || request.getCustomerId().isBlank()) {
            return Either.left(Failure.of(ErrorCode.VALIDATION, "Customer ID is required"));
        }

        if (request.getAmount() == null || request.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            return Either.left(Failure.of(ErrorCode.VALIDATION, "Amount must be greater than zero"));
        }

        if (request.getAmount().compareTo(new BigDecimal("10000")) > 0) {
            return Either.left(Failure.of(ErrorCode.VALIDATION, "Amount exceeds maximum allowed (10000)"));
        }

        if (request.getProductCode() == null || request.getProductCode().isBlank()) {
            return Either.left(Failure.of(ErrorCode.VALIDATION, "Product code is required"));
        }

        log.info("║ [ORDER] Validation passed");
        return Either.right(request);
    }

    private Either<Failure, OrderRequest> checkDuplicateOrder(OrderRequest request) {
        log.info("║ [ORDER] Step 3/5: Checking for duplicates");

        if (orderRepository.existsByOrderId(request.getOrderId())) {
            log.warn("║ [ORDER] Duplicate order detected: {}", request.getOrderId());
            return Either.left(Failure.of(ErrorCode.CONFLICT,
                "Order already exists: " + request.getOrderId()));
        }

        log.info("║ [ORDER] No duplicates found");
        return Either.right(request);
    }

    private Either<Failure, Order> saveOrder(OrderRequest request) {
        log.info("║ [ORDER] Step 4/5: Saving to database");

        try {
            Order order = new Order();
            order.setOrderId(request.getOrderId());
            order.setCustomerId(request.getCustomerId());
            order.setAmount(request.getAmount());
            order.setProductCode(request.getProductCode());
            order.setStatus(Order.OrderStatus.PENDING);
            order.setCreatedAt(LocalDateTime.now());

            Order saved = orderRepository.save(order);
            log.info("║ [ORDER] Order saved with ID: {}", saved.getId());
            return Either.right(saved);
        } catch (Exception e) {
            log.error("║ [ORDER] Database error: {}", e.getMessage(), e);
            return Either.left(Failure.of(ErrorCode.UNKNOWN, "Database error: " + e.getMessage()));
        }
    }

    private Either<Failure, Order> callExternalPaymentApi(Order order) {
        log.info("║ [ORDER] Step 5/5: Calling external payment API");

        return externalApiService.processPayment(order)
                .map(transactionId -> {
                    log.info("║ [ORDER] Payment processed: {}", transactionId.value());
                    return order;
                })
                .mapLeft(failure -> {
                    log.error("║ [ORDER] Payment API failed: {}", failure.message());
                    // Update order status to FAILED before returning error
                    order.setStatus(Order.OrderStatus.FAILED);
                    orderRepository.save(order);
                    return failure;
                });
    }

    private Either<Failure, Order> updateOrderStatus(Order order) {
        log.info("║ [ORDER] Updating order status to COMPLETED");

        try {
            order.setStatus(Order.OrderStatus.COMPLETED);
            order.setProcessedAt(LocalDateTime.now());
            Order updated = orderRepository.save(order);
            log.info("║ [ORDER] Order status updated successfully");
            return Either.right(updated);
        } catch (Exception e) {
            log.error("║ [ORDER] Failed to update status: {}", e.getMessage());
            return Either.left(Failure.of(ErrorCode.UNKNOWN, "Failed to update order status"));
        }
    }

    private Either<Failure, Success<Void>> processNotificationMessage(String message) {
        log.info("║ [NOTIFICATION] Sending notification: {}", message);
        // Notification processing logic here
        return Either.right(Success.of(null));
    }

    private Either<Failure, Success<Void>> processAuditMessage(String message) {
        log.info("║ [AUDIT] Logging audit event: {}", message);
        // Audit processing logic here
        return Either.right(Success.of(null));
    }

    private static class MessageProcessingException extends RuntimeException {
        public MessageProcessingException(Failure failure) {
            super(failure.message());
        }
    }
}