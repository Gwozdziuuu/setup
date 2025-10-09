package com.mrngwozdz.setup.controller.model.valdation;

import com.mrngwozdz.setup.database.entity.Order.OrderStatus;
import com.mrngwozdz.setup.platform.result.ErrorCode;
import com.mrngwozdz.setup.platform.result.Failure;
import com.mrngwozdz.setup.platform.validation.Validator;
import io.vavr.control.Either;

import java.math.BigDecimal;

/**
 * Reusable validators for order-related fields.
 * These validators can be used across DTOs, requests, and responses.
 */
public class OrderValidators {

    private OrderValidators() {}

    public static Either<Failure, String> validateOrderId(String orderId) {
        return Validator.notBlank(orderId, "orderId")
                .flatMap(id -> {
                    if (!id.matches("^ORD-\\d+$")) {
                        return Either.left(Failure.of(
                                ErrorCode.VALIDATION,
                                "Order ID must match pattern ORD-XXX where XXX is a number"
                        ));
                    }
                    return Either.right(id);
                });
    }

    public static Either<Failure, String> validateCustomerId(String customerId) {
        return Validator.notBlank(customerId, "customerId")
                .flatMap(id -> {
                    if (!id.matches("^CUST-\\d+$")) {
                        return Either.left(Failure.of(
                                ErrorCode.VALIDATION,
                                "Customer ID must match pattern CUST-XXX where XXX is a number"
                        ));
                    }
                    return Either.right(id);
                });
    }

    public static Either<Failure, BigDecimal> validateAmount(BigDecimal amount) {
        return Validator.notNull(amount, "amount")
                .flatMap(a -> {
                    if (a.compareTo(BigDecimal.ZERO) <= 0) {
                        return Either.left(Failure.of(
                                ErrorCode.VALIDATION,
                                "Amount must be positive"
                        ));
                    }
                    return Either.right(a);
                });
    }

    public static Either<Failure, String> validateProductCode(String productCode) {
        return Validator.notBlank(productCode, "productCode")
                .flatMap(code -> {
                    if (!code.matches("^PROD-\\d+$")) {
                        return Either.left(Failure.of(
                                ErrorCode.VALIDATION,
                                "Product code must match pattern PROD-XXX where XXX is a number"
                        ));
                    }
                    return Either.right(code);
                });
    }

    public static Either<Failure, OrderStatus> validateStatus(OrderStatus status) {
        return Validator.notNull(status, "status");
    }
}