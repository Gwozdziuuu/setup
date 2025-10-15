package com.mrngwozdz.setup.controller;

import com.mrngwozdz.setup.controller.api.OrderApi;
import com.mrngwozdz.setup.controller.model.request.CreateOrderRequest;
import com.mrngwozdz.setup.controller.model.request.UpdateOrderRequest;
import com.mrngwozdz.setup.controller.model.response.CreateOrderResponse;
import com.mrngwozdz.setup.controller.model.response.GetAllOrdersResponse;
import com.mrngwozdz.setup.controller.model.response.OrderResponse;
import com.mrngwozdz.setup.service.order.business.OrderBusiness;
import io.micrometer.core.annotation.Timed;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;

import static com.mrngwozdz.setup.platform.http.RestResults.unwrapOrThrow;

@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping(value = "/")
public class OrderController implements OrderApi {

    private final OrderBusiness business;

    @Override
    @Timed(value = "orders.get.all", description = "Time taken to retrieve all orders")
    public ResponseEntity<GetAllOrdersResponse> getAllOrders() {
        var result = unwrapOrThrow(business.getAllOrders(), GetAllOrdersResponse::from);
        return ResponseEntity.ok(result);
    }

    @Override
    @Timed(value = "orders.get.by.id", description = "Time taken to retrieve an order by ID")
    public ResponseEntity<OrderResponse> getOrderById(String orderId) {
        var order = unwrapOrThrow(business.getOrderById(orderId), OrderResponse::from);
        return ResponseEntity.ok(order);
    }

    @Override
    @Timed(value = "orders.create", description = "Time taken to create a new order")
    public ResponseEntity<CreateOrderResponse> createOrder(CreateOrderRequest request) {
        var response = unwrapOrThrow(
                business.createOrder(request),
                order -> new CreateOrderResponse(order.getOrderId())
        );

        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(response.orderId())
                .toUri();

        return ResponseEntity.created(location).body(response);
    }

    @Override
    @Timed(value = "orders.update", description = "Time taken to update an order")
    public ResponseEntity<Void> updateOrder(String orderId, UpdateOrderRequest request) {
        unwrapOrThrow(business.updateOrder(orderId, request));
        return ResponseEntity.noContent().build();
    }

    @Override
    @Timed(value = "orders.patch", description = "Time taken to patch an order")
    public ResponseEntity<Void> patchOrder(String orderId, UpdateOrderRequest request) {
        unwrapOrThrow(business.patchOrder(orderId, request));
        return ResponseEntity.noContent().build();
    }

    @Override
    @Timed(value = "orders.delete", description = "Time taken to delete an order")
    public ResponseEntity<Void> deleteOrder(String orderId) {
        unwrapOrThrow(business.deleteOrder(orderId));
        return ResponseEntity.noContent().build();
    }
}