package com.mrngwozdz.setup.controller.model.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Response for order creation containing only the ID")
public record CreateOrderResponse(
        @Schema(description = "Unique order identifier", example = "ORD-001")
        String orderId
) {
}