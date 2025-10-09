package com.mrngwozdz.setup.controller.model.dto;

import com.mrngwozdz.setup.database.entity.Order;
import com.mrngwozdz.setup.database.entity.Order.OrderStatus;
import com.mrngwozdz.setup.platform.result.Failure;
import com.mrngwozdz.setup.platform.validation.Validator;
import io.swagger.v3.oas.annotations.media.Schema;
import io.vavr.control.Either;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static com.mrngwozdz.setup.controller.model.valdation.OrderValidators.*;

@Schema(description = "Order data")
public record OrderData(
        @Schema(description = "Database ID", example = "1")
        Long id,

        @Schema(description = "Unique order identifier", example = "ORD-001")
        String orderId,

        @Schema(description = "Customer identifier", example = "CUST-123")
        String customerId,

        @Schema(description = "Order amount", example = "99.99")
        BigDecimal amount,

        @Schema(description = "Product code", example = "PROD-456")
        String productCode,

        @Schema(description = "Order status", example = "COMPLETED")
        OrderStatus status,

        @Schema(description = "Order creation timestamp")
        LocalDateTime createdAt,

        @Schema(description = "Order processing timestamp")
        LocalDateTime processedAt
) {
    public static OrderData from(Order order) {
        return new OrderData(
                order.getId(),
                order.getOrderId(),
                order.getCustomerId(),
                order.getAmount(),
                order.getProductCode(),
                order.getStatus(),
                order.getCreatedAt(),
                order.getProcessedAt()
        );
    }

    /**
     * Validates the OrderData instance.
     * @return Either containing Failure on validation error or validated OrderData on success
     */
    public Either<Failure, OrderData> validate() {
        return Validator.notNull(this, "OrderData")
                .flatMap(data -> validateOrderId(data.orderId()).map(id -> data))
                .flatMap(data -> validateCustomerId(data.customerId()).map(id -> data))
                .flatMap(data -> validateAmount(data.amount()).map(amount -> data))
                .flatMap(data -> validateProductCode(data.productCode()).map(code -> data))
                .flatMap(data -> validateStatus(data.status()).map(status -> data));
    }
}