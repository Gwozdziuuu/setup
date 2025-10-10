package com.mrngwozdz.setup.service.order.data.repository.query;

import com.mrngwozdz.setup.database.config.repository.ReadOnlyRepository;
import com.mrngwozdz.setup.database.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface OrderQueryRepository extends ReadOnlyRepository<Order, Long> {
    Optional<Order> findByOrderId(String orderId);
    boolean existsByOrderId(String orderId);
}