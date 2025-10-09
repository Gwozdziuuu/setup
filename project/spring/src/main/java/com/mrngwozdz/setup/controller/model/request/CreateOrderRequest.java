package com.mrngwozdz.setup.controller.model.request;

import com.mrngwozdz.setup.platform.result.Failure;
import com.mrngwozdz.setup.platform.validation.Validator;
import io.swagger.v3.oas.annotations.media.Schema;
import io.vavr.control.Either;

import java.math.BigDecimal;

import static com.mrngwozdz.setup.controller.model.valdation.OrderValidators.*;


@Schema(description = "Request to create a new order")
public record CreateOrderRequest(
        @Schema(description = "Unique order identifier", example = "ORD-001", requiredMode = Schema.RequiredMode.REQUIRED)
        String orderId,

        @Schema(description = "Customer identifier", example = "CUST-123", requiredMode = Schema.RequiredMode.REQUIRED)
        String customerId,

        @Schema(description = "Order amount", example = "99.99", requiredMode = Schema.RequiredMode.REQUIRED)
        BigDecimal amount,

        @Schema(description = "Product code", example = "PROD-456", requiredMode = Schema.RequiredMode.REQUIRED)
        String productCode
) {
    /**
     * Validates the CreateOrderRequest instance.
     * @return Either containing Failure on validation error or validated request on success
     */
    public Either<Failure, CreateOrderRequest> validate() {
        return Validator.notNull(this, "CreateOrderRequest")
                .flatMap(request -> validateOrderId(request.orderId()).map(id -> request))
                .flatMap(request -> validateCustomerId(request.customerId()).map(id -> request))
                .flatMap(request -> validateAmount(request.amount()).map(amount -> request))
                .flatMap(request -> validateProductCode(request.productCode()).map(code -> request));
    }

}