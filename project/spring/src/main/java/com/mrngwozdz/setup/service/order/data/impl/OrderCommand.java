package com.mrngwozdz.setup.service.order.data.impl;

import com.mrngwozdz.setup.controller.model.request.CreateOrderRequest;
import com.mrngwozdz.setup.controller.model.request.UpdateOrderRequest;
import com.mrngwozdz.setup.database.entity.Order;
import com.mrngwozdz.setup.database.entity.Order.OrderStatus;
import com.mrngwozdz.setup.platform.result.ErrorCode;
import com.mrngwozdz.setup.platform.result.Failure;
import com.mrngwozdz.setup.service.order.data.repository.command.OrderCommandRepository;
import com.mrngwozdz.setup.service.order.mapper.OrderRequestMapper;
import io.vavr.control.Either;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderCommand {

    private final OrderCommandRepository repository;

    public Either<Failure, Order> create(Order createOrder) {
        log.debug("Creating order with ID: {} in database", createOrder.getOrderId());
        try {
            var savedOrder = repository.save(createOrder);
            log.debug("Order created successfully in database: {}", savedOrder.getOrderId());
            return Either.right(savedOrder);
        } catch (Exception e) {
            log.error("Database error occurred while creating order: {}", createOrder.getOrderId(), e);
            return Either.left(
                    Failure.ofDefault(ErrorCode.DATABASE_ERROR)
                            .with("exceptionType", e.getClass().getName())
                            .with("exceptionMessage", e.getMessage())
                            .with("orderId", createOrder.getOrderId())
            );
        }
    }

    public Either<Failure, Order> update(String orderId, UpdateOrderRequest request) {
        log.debug("Updating order: {} in database", orderId);

        return repository.findByOrderId(orderId)
                .map(order -> {
                    if (request.customerId() != null) {
                        order.setCustomerId(request.customerId());
                    }
                    if (request.amount() != null) {
                        order.setAmount(request.amount());
                    }
                    if (request.productCode() != null) {
                        order.setProductCode(request.productCode());
                    }
                    if (request.status() != null) {
                        order.setStatus(request.status());
                        if (request.status() == OrderStatus.COMPLETED ||
                            request.status() == OrderStatus.FAILED) {
                            order.setProcessedAt(LocalDateTime.now());
                        }
                    }

                    try {
                        Order updatedOrder = repository.save(order);
                        log.debug("Order updated successfully in database: {}", orderId);
                        return Either.<Failure, Order>right(updatedOrder);
                    } catch (Exception e) {
                        log.error("Database error occurred while updating order: {}", orderId, e);
                        return Either.<Failure, Order>left(
                                Failure.ofDefault(ErrorCode.DATABASE_ERROR)
                                        .with("exceptionType", e.getClass().getName())
                                        .with("exceptionMessage", e.getMessage())
                                        .with("orderId", orderId)
                        );
                    }
                })
                .orElseGet(() -> {
                    log.debug("Order not found in database for update: {}", orderId);
                    return Either.left(Failure.ofDefault(ErrorCode.NOT_FOUND, "Order")
                            .with("orderId", orderId));
                });
    }

    public Either<Failure, Void> delete(String orderId) {
        log.debug("Deleting order: {} from database", orderId);

        return repository.findByOrderId(orderId)
                .map(order -> {
                    try {
                        repository.delete(order);
                        log.debug("Order deleted successfully from database: {}", orderId);
                        return Either.<Failure, Void>right(null);
                    } catch (Exception e) {
                        log.error("Database error occurred while deleting order: {}", orderId, e);
                        return Either.<Failure, Void>left(
                                Failure.ofDefault(ErrorCode.DATABASE_ERROR)
                                        .with("exceptionType", e.getClass().getName())
                                        .with("exceptionMessage", e.getMessage())
                                        .with("orderId", orderId)
                        );
                    }
                })
                .orElseGet(() -> {
                    log.debug("Order not found in database for deletion: {}", orderId);
                    return Either.left(Failure.ofDefault(ErrorCode.NOT_FOUND, "Order")
                            .with("orderId", orderId));
                });
    }
}
