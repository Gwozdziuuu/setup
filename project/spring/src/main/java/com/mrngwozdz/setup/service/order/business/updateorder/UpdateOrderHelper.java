package com.mrngwozdz.setup.service.order.business.updateorder;

import com.mrngwozdz.setup.controller.model.request.UpdateOrderRequest;
import com.mrngwozdz.setup.database.entity.Order;
import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
public class UpdateOrderHelper {

    private String orderId;
    private UpdateOrderRequest validatedUpdateOrderRequest;
    private Order existingOrder;
    private Order updatedOrder;

    public UpdateOrderHelper setOrderId(String orderId) {
        this.orderId = orderId;
        return this;
    }

    public UpdateOrderHelper setValidatedUpdateOrderRequest(UpdateOrderRequest validatedUpdateOrderRequest) {
        this.validatedUpdateOrderRequest = validatedUpdateOrderRequest;
        return this;
    }

    public UpdateOrderHelper setExistingOrder(Order existingOrder) {
        this.existingOrder = existingOrder;
        return this;
    }

    public UpdateOrderHelper setUpdatedOrder(Order updatedOrder) {
        this.updatedOrder = updatedOrder;
        return this;
    }
}