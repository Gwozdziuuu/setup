package com.mrngwozdz.setup.service.order.mapper;

import com.mrngwozdz.setup.controller.model.request.CreateOrderRequest;
import com.mrngwozdz.setup.database.entity.Order;
import com.mrngwozdz.setup.platform.result.ErrorCode;
import com.mrngwozdz.setup.platform.result.Failure;
import io.vavr.control.Either;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper
public interface OrderRequestMapper {

    OrderRequestMapper INSTANCE = Mappers.getMapper(OrderRequestMapper.class);

    @Mapping(target = "id", ignore = true)
    @Mapping(source = "orderId", target = "orderId")
    @Mapping(source = "customerId", target = "customerId")
    @Mapping(source = "amount", target = "amount")
    @Mapping(source = "productCode", target = "productCode")
    @Mapping(target = "status", expression = "java(com.mrngwozdz.setup.database.entity.Order.OrderStatus.PENDING)")
    @Mapping(target = "createdAt", expression = "java(java.time.LocalDateTime.now())")
    @Mapping(target = "processedAt", ignore = true)
    Order toOrder(CreateOrderRequest request);

    default Either<Failure, Order> toOrderSafely(CreateOrderRequest request) {
        try {
            Order order = toOrder(request);
            return Either.right(order);
        } catch (Exception e) {
            return Either.left(
                    Failure.ofDefault(ErrorCode.UNKNOWN)
                            .with("exceptionType", e.getClass().getName())
                            .with("exceptionMessage", e.getMessage())
            );
        }
    }
}