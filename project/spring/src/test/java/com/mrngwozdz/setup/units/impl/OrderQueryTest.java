package com.mrngwozdz.setup.units.impl;

import com.mrngwozdz.setup.service.order.data.impl.OrderQuery;
import com.mrngwozdz.setup.service.order.data.repository.query.OrderQueryRepository;
import com.mrngwozdz.setup.units.base.DataLayerUnitTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrderQueryTest extends DataLayerUnitTest {

    @Mock
    private OrderQueryRepository repository;

    @InjectMocks
    private OrderQuery orderQuery;

    @Test
    void shouldReturnDatabaseErrorWhenUnexpectedExceptionOccurs() {
        // given
        var expectedException = new RuntimeException("Database connection failed");
        when(repository.findAll()).thenThrow(expectedException);

        // when
        var result = orderQuery.findAll();

        // then
        assertDatabaseError(result, expectedException);
    }

    @Test
    void shouldReturnDatabaseErrorWhenFindByIdThrowsException() {
        // given
        var orderId = "ORD-123";
        var expectedException = new RuntimeException("Database connection timeout");
        when(repository.findByOrderId(orderId)).thenThrow(expectedException);

        // when
        var result = orderQuery.findById(orderId);

        // then
        assertDatabaseError(result, expectedException, "orderId", orderId);
    }
}