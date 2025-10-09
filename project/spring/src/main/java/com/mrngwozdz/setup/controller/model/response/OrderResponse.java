package com.mrngwozdz.setup.controller.model.response;

import com.mrngwozdz.setup.controller.model.dto.OrderData;
import com.mrngwozdz.setup.database.entity.Order;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Order response")
public record OrderResponse(
        @Schema(description = "Order data")
        OrderData order
) {
    public static OrderResponse from(Order order) {
        return new OrderResponse(OrderData.from(order));
    }
}