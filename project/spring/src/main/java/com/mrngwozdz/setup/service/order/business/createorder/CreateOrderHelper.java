package com.mrngwozdz.setup.service.order.business.createorder;

import com.mrngwozdz.setup.controller.model.request.CreateOrderRequest;
import com.mrngwozdz.setup.database.entity.Order;
import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
public class CreateOrderHelper {

    private CreateOrderRequest validatedCreateOrderRequest;
    private Boolean orderDoesNotExist;
    private Order createOrder;
    private Order createdOrder;

    public CreateOrderHelper setValidatedCreateOrderRequest(CreateOrderRequest validatedCreateOrderRequest) {
        this.validatedCreateOrderRequest = validatedCreateOrderRequest;
        return this;
    }

    public CreateOrderHelper setOrderDoesNotExist(Boolean orderDoesNotExist) {
        this.orderDoesNotExist = orderDoesNotExist;
        return this;
    }

    public CreateOrderHelper setCreateOrder(Order createOrder) {
        this.createOrder = createOrder;
        return this;
    }

    public CreateOrderHelper setCreatedOrder(Order createdOrder) {
        this.createdOrder = createdOrder;
        return this;
    }
}
