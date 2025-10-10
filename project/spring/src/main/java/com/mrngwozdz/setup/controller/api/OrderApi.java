package com.mrngwozdz.setup.controller.api;

import com.mrngwozdz.setup.controller.model.request.CreateOrderRequest;
import com.mrngwozdz.setup.controller.model.request.UpdateOrderRequest;
import com.mrngwozdz.setup.controller.model.response.CreateOrderResponse;
import com.mrngwozdz.setup.controller.model.response.OrderResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Orders", description = "Order management API")
public interface OrderApi {

    @Operation(
            summary = "Get all orders",
            description = "Returns a list of all orders",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Success",
                            content = @Content(schema = @Schema(implementation = OrderResponse.class))
                    )
            }
    )
    @GetMapping("/orders")
    ResponseEntity<List<OrderResponse>> getAllOrders();

    @Operation(
            summary = "Get order by ID",
            description = "Returns a single order by its order ID",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Order found",
                            content = @Content(schema = @Schema(implementation = OrderResponse.class))
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "Order not found"
                    )
            }
    )
    @GetMapping("/orders/{orderId}")
    ResponseEntity<OrderResponse> getOrderById(
            @Parameter(description = "Order ID", required = true)
            @PathVariable String orderId
    );

    @Operation(
            summary = "Create new order",
            description = "Creates a new order and returns its ID. Use GET /orders/{orderId} to retrieve full order details.",
            responses = {
                    @ApiResponse(
                            responseCode = "201",
                            description = "Order created successfully. Location header contains URI of the created resource.",
                            content = @Content(schema = @Schema(implementation = CreateOrderResponse.class))
                    ),
                    @ApiResponse(
                            responseCode = "400",
                            description = "Invalid request"
                    ),
                    @ApiResponse(
                            responseCode = "409",
                            description = "Order with this ID already exists"
                    )
            }
    )
    @PostMapping("/orders")
    ResponseEntity<CreateOrderResponse> createOrder(
            @Parameter(description = "Order creation request", required = true)
            @RequestBody CreateOrderRequest request
    );

    @Operation(
            summary = "Update entire order",
            description = "Updates all fields of an existing order",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Order updated successfully",
                            content = @Content(schema = @Schema(implementation = OrderResponse.class))
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "Order not found"
                    ),
                    @ApiResponse(
                            responseCode = "400",
                            description = "Invalid request"
                    )
            }
    )
    @PutMapping("/orders/{orderId}")
    ResponseEntity<OrderResponse> updateOrder(
            @Parameter(description = "Order ID", required = true)
            @PathVariable String orderId,
            @Parameter(description = "Order update request", required = true)
            @RequestBody UpdateOrderRequest request
    );

    @Operation(
            summary = "Partially update order",
            description = "Updates specific fields of an existing order",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Order updated successfully",
                            content = @Content(schema = @Schema(implementation = OrderResponse.class))
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "Order not found"
                    ),
                    @ApiResponse(
                            responseCode = "400",
                            description = "Invalid request"
                    )
            }
    )
    @PatchMapping("/orders/{orderId}")
    ResponseEntity<OrderResponse> patchOrder(
            @Parameter(description = "Order ID", required = true)
            @PathVariable String orderId,
            @Parameter(description = "Partial order update request", required = true)
            @RequestBody UpdateOrderRequest request
    );

    @Operation(
            summary = "Delete order",
            description = "Deletes an order by its order ID",
            responses = {
                    @ApiResponse(
                            responseCode = "204",
                            description = "Order deleted successfully"
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "Order not found"
                    )
            }
    )
    @DeleteMapping("/orders/{orderId}")
    ResponseEntity<Void> deleteOrder(
            @Parameter(description = "Order ID", required = true)
            @PathVariable String orderId
    );

}