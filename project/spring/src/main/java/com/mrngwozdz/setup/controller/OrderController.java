package com.mrngwozdz.setup.controller;

import com.mrngwozdz.setup.controller.api.OrderApi;
import com.mrngwozdz.setup.controller.model.request.CreateOrderRequest;
import com.mrngwozdz.setup.controller.model.request.UpdateOrderRequest;
import com.mrngwozdz.setup.controller.model.response.CreateOrderResponse;
import com.mrngwozdz.setup.controller.model.response.OrderResponse;
import com.mrngwozdz.setup.database.entity.Order;
import com.mrngwozdz.setup.platform.http.RestResults;
import com.mrngwozdz.setup.service.order.business.OrderBusiness;
import io.micrometer.core.annotation.Timed;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import static com.mrngwozdz.setup.platform.http.RestResults.*;

@RestController
@RequiredArgsConstructor
@RequestMapping(value = "/")
public class OrderController implements OrderApi {

    private final OrderBusiness business;

    @Override
    @Timed(value = "orders.get.all", description = "Time taken to retrieve all orders")
    public ResponseEntity<List<OrderResponse>> getAllOrders() {
        return toResponseEntity(
                business.getAllOrders(),
                orders -> orders.stream().map(OrderResponse::from).toList()
        );
    }

    @Override
    @Timed(value = "orders.get.by.id", description = "Time taken to retrieve an order by ID")
    public ResponseEntity<OrderResponse> getOrderById(String orderId) {
        return toResponseEntity(business.getOrderById(orderId), OrderResponse::from);
    }

    @Override
    @Timed(value = "orders.create", description = "Time taken to create a new order")
    public ResponseEntity<?> createOrder(CreateOrderRequest request) {
        return business.createOrder(request)
                .fold(
                        RestResults::toResponse,
                        order -> ResponseEntity.status(HttpStatus.CREATED)
                                .body(new CreateOrderResponse(order.getOrderId()))
                );
    }

    @Override
    @Timed(value = "orders.update", description = "Time taken to update an order")
    public ResponseEntity<OrderResponse> updateOrder(String orderId, UpdateOrderRequest request) {
        return toResponseEntity(business.updateOrder(orderId, request), OrderResponse::from);
    }

    @Override
    @Timed(value = "orders.patch", description = "Time taken to patch an order")
    public ResponseEntity<OrderResponse> patchOrder(String orderId, UpdateOrderRequest request) {
        return toResponseEntity(business.patchOrder(orderId, request), OrderResponse::from);
    }

    @Override
    @Timed(value = "orders.delete", description = "Time taken to delete an order")
    public ResponseEntity<Void> deleteOrder(String orderId) {
        return business.deleteOrder(orderId)
                .fold(
                        failure -> {
                            var response = toResponse(failure);
                            return ResponseEntity.status(response.getStatusCode()).build();
                        },
                        success -> ResponseEntity.noContent().build()
                );
    }
}