package com.mrngwozdz.setup.controller.model.request;

import com.mrngwozdz.setup.database.entity.Order.OrderStatus;
import com.mrngwozdz.setup.platform.result.Failure;
import com.mrngwozdz.setup.controller.model.valdation.OrderValidators;
import com.mrngwozdz.setup.platform.validation.Validator;
import io.swagger.v3.oas.annotations.media.Schema;
import io.vavr.control.Either;

import java.math.BigDecimal;

@Schema(description = "Request to update an existing order")
public record UpdateOrderRequest(
        @Schema(description = "Customer identifier", example = "CUST-123", nullable = true)
        String customerId,

        @Schema(description = "Order amount", example = "99.99", nullable = true)
        BigDecimal amount,

        @Schema(description = "Product code", example = "PROD-456", nullable = true)
        String productCode,

        @Schema(description = "Order status", example = "COMPLETED", nullable = true)
        OrderStatus status
) {
    /**
     * Validates the UpdateOrderRequest instance.
     * All fields are optional, but if present they must be valid.
     * @return Either containing Failure on validation error or validated request on success
     */
    public Either<Failure, UpdateOrderRequest> validate() {
        return Validator.notNull(this, "UpdateOrderRequest")
                .flatMap(req -> Validator.validateIfPresent(req.customerId(), OrderValidators::validateCustomerId, req))
                .flatMap(req -> Validator.validateIfPresent(req.amount(), OrderValidators::validateAmount, req))
                .flatMap(req -> Validator.validateIfPresent(req.productCode(), OrderValidators::validateProductCode, req))
                .flatMap(req -> Validator.validateIfPresent(req.status(), OrderValidators::validateStatus, req));
    }
}