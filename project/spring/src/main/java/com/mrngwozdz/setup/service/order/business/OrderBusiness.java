package com.mrngwozdz.setup.service.order.business;

import com.mrngwozdz.setup.controller.model.request.CreateOrderRequest;
import com.mrngwozdz.setup.controller.model.request.UpdateOrderRequest;
import com.mrngwozdz.setup.database.config.datasource.ReadOperation;
import com.mrngwozdz.setup.database.config.datasource.WriteOperation;
import com.mrngwozdz.setup.database.entity.Order;
import com.mrngwozdz.setup.platform.result.Failure;
import com.mrngwozdz.setup.service.order.business.createorder.CreateOrderHelper;
import com.mrngwozdz.setup.service.order.business.updateorder.UpdateOrderHelper;
import com.mrngwozdz.setup.service.order.data.impl.OrderCommand;
import com.mrngwozdz.setup.service.order.data.impl.OrderQuery;
import com.mrngwozdz.setup.service.order.mapper.OrderRequestMapper;
import io.vavr.control.Either;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;

import java.util.List;

/**
 * Business layer for Order operations.
 * Uses @ReadOperation and @WriteOperation annotations to route database operations
 * to the appropriate DataSource (READ or WRITE).
 */
@Slf4j
@Service
public class OrderBusiness {

    private final OrderQuery orderQuery;
    private final OrderCommand orderCommand;
    private final OrderBusiness self;  // Self-injection for internal method calls to use Spring proxy

    public OrderBusiness(OrderQuery orderQuery, OrderCommand orderCommand, @Lazy OrderBusiness self) {
        this.orderQuery = orderQuery;
        this.orderCommand = orderCommand;
        this.self = self;
    }

    /**
     * Retrieves all orders from the database.
     * Uses READ DataSource for query operations.
     */
    @ReadOperation
    @Transactional(readOnly = true)
    public Either<Failure, List<Order>> getAllOrders() {
        return orderQuery.findAll();
    }

    /**
     * Retrieves a single order by its ID.
     * Uses READ DataSource for query operations.
     */
    @ReadOperation
    @Transactional(readOnly = true)
    public Either<Failure, Order> getOrderById(String orderId) {
        return orderQuery.findById(orderId);
    }

    /**
     * Creates a new order.
     * Uses WRITE DataSource for command operations.
     */
    @WriteOperation
    @Transactional
    public Either<Failure, Order> createOrder(CreateOrderRequest request) {
        var helper = new CreateOrderHelper();
        var result = request.validate().map(helper::setValidatedCreateOrderRequest)
                .flatMap(h -> orderQuery.ensureOrderDoesNotExist(h.getValidatedCreateOrderRequest().orderId()).map(h::setOrderDoesNotExist))
                .flatMap(h -> OrderRequestMapper.INSTANCE.toOrderSafely(h.getValidatedCreateOrderRequest()).map(h::setCreateOrder))
                .flatMap(h -> orderCommand.create(h.getCreateOrder()).map(h::setCreatedOrder));
        if (result.isLeft()) {
            log.error("Create order failed with request: {}, process: {}", request, helper);
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
        }
        return result.map(CreateOrderHelper::getCreatedOrder);
    }

    /**
     * Updates an existing order (full update).
     * Uses WRITE DataSource for command operations.
     */
    @WriteOperation
    @Transactional
    public Either<Failure, Order> updateOrder(String orderId, UpdateOrderRequest request) {
        var helper = new UpdateOrderHelper();
        var result = Either.<Failure, UpdateOrderHelper>right(helper.setOrderId(orderId))
                .flatMap(h -> request.validate().map(h::setValidatedUpdateOrderRequest))
                .flatMap(h -> orderQuery.findById(h.getOrderId()).map(h::setExistingOrder))
                .flatMap(h -> orderCommand.update(h.getOrderId(), h.getValidatedUpdateOrderRequest()).map(h::setUpdatedOrder));
        if (result.isLeft()) {
            log.error("Update order failed with orderId: {}, request: {}, process: {}", orderId, request, helper);
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
        }
        return result.map(UpdateOrderHelper::getUpdatedOrder);
    }

    /**
     * Partially updates an existing order.
     * Uses WRITE DataSource for command operations.
     */
    public Either<Failure, Order> patchOrder(String orderId, UpdateOrderRequest request) {
        return self.updateOrder(orderId, request);
    }

    /**
     * Deletes an order by its ID.
     * Uses WRITE DataSource for command operations.
     */
    @WriteOperation
    @Transactional
    public Either<Failure, Void> deleteOrder(String orderId) {
        return orderCommand.delete(orderId);
    }
}