package com.mrngwozdz.setup.service.order.data.impl;

import com.mrngwozdz.setup.database.entity.Order;
import com.mrngwozdz.setup.platform.result.ErrorCode;
import com.mrngwozdz.setup.platform.result.Failure;
import com.mrngwozdz.setup.platform.result.Success;
import com.mrngwozdz.setup.service.order.data.repository.query.OrderQueryRepository;
import io.vavr.control.Either;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderQuery {

    private final OrderQueryRepository repository;

    public Either<Failure, List<Order>> findAll() {
        log.debug("Retrieving all orders from database");
        try {
            var orders = repository.findAll();
            log.debug("Successfully retrieved {} orders from database", orders.size());
            return Either.right(orders);
        } catch (Exception e) {
            log.error("Database error occurred while retrieving orders", e);
            return Either.left(
                    Failure.ofDefault(ErrorCode.DATABASE_ERROR)
                            .with("exceptionType", e.getClass().getName())
                            .with("exceptionMessage", e.getMessage())
            );
        }
    }

    public Either<Failure, Order> findById(String orderId) {
        log.debug("Retrieving order with ID: {} from database", orderId);
        Optional<Order> orderOptional;
        try {
            orderOptional = repository.findByOrderId(orderId);
        } catch (Exception e) {
            log.error("Database error occurred while retrieving order: {}", orderId, e);
            return Either.left(
                    Failure.ofDefault(ErrorCode.DATABASE_ERROR)
                            .with("exceptionType", e.getClass().getName())
                            .with("exceptionMessage", e.getMessage())
                            .with("orderId", orderId)
            );
        }
        return orderOptional.map(order -> {
                    log.debug("Order found in database: {}", orderId);
                    return Either.<Failure, Order>right(order);
                })
                .orElseGet(() -> {
                    log.debug("Order not found in database: {}", orderId);
                    return Either.left(Failure.ofDefault(ErrorCode.NOT_FOUND, "Order")
                            .with("orderId", orderId));
                });
    }

    public Either<Failure, Boolean> ensureOrderDoesNotExist(String orderId) {
        log.debug("Checking if order exists in database: {}", orderId);
        boolean exists;
        try {
            exists = repository.existsByOrderId(orderId);
        } catch (Exception e) {
            log.error("Database error occurred while checking order existence: {}", orderId, e);
            return Either.left(
                    Failure.ofDefault(ErrorCode.DATABASE_ERROR)
                            .with("exceptionType", e.getClass().getName())
                            .with("exceptionMessage", e.getMessage())
                            .with("orderId", orderId)
            );
        }
        if (exists) {
            log.debug("Order already exists in database: {}", orderId);
            return Either.left(Failure.ofDefault(ErrorCode.CONFLICT, "Order")
                    .with("orderId", orderId));
        }
        log.debug("Order does not exist in database: {}", orderId);
        return Either.right(true);
    }
}
