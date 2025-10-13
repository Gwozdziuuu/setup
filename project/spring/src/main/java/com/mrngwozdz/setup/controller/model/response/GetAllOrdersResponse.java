package com.mrngwozdz.setup.controller.model.response;

import com.mrngwozdz.setup.controller.model.dto.OrderData;
import com.mrngwozdz.setup.database.entity.Order;

import java.util.List;

public record GetAllOrdersResponse(
        List<OrderData> orders
) {
    public static GetAllOrdersResponse from(List<Order> orders) {
        return new GetAllOrdersResponse(
                orders.stream().map(OrderData::from).toList()
        );
    }
}
