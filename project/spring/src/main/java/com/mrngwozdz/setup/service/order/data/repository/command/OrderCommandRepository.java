package com.mrngwozdz.setup.service.order.data.repository.command;

import com.mrngwozdz.setup.database.config.repository.WriteRepository;
import com.mrngwozdz.setup.database.entity.Order;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface OrderCommandRepository extends WriteRepository<Order, Long> {
    Optional<Order> findByOrderId(String orderId);
    boolean existsByOrderId(String orderId);
}