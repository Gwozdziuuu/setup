package com.mrngwozdz.setup.integration.order;

import com.mrngwozdz.setup.AbstractIntegrationTest;
import com.mrngwozdz.setup.controller.model.request.CreateOrderRequest;
import com.mrngwozdz.setup.controller.model.response.CreateOrderResponse;
import com.mrngwozdz.setup.controller.model.response.OrderResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.jdbc.Sql;

import java.math.BigDecimal;
import java.util.stream.Stream;

import static com.mrngwozdz.setup.controller.OrderControllerUtils.createOrder;
import static com.mrngwozdz.setup.controller.OrderControllerUtils.getOrderById;
import static org.assertj.core.api.Assertions.assertThat;

class CreateOrderTest extends AbstractIntegrationTest {

    @Test
    void shouldCreateOrderSuccessfully() {
        // given
        var request = new CreateOrderRequest(
                "ORD-001",
                "CUST-123",
                new BigDecimal("99.99"),
                "PROD-456"
        );

        // when
        var createResponse = createOrder(request)
                .statusCode(HttpStatus.CREATED.value())
                .header("Location", org.hamcrest.Matchers.endsWith("/orders/ORD-001"))
                .extract()
                .as(CreateOrderResponse.class);

        // then - verify create response contains only ID
        assertThat(createResponse).isNotNull();
        assertThat(createResponse.orderId()).isEqualTo("ORD-001");

        // when - fetch full order details via GET
        var fullOrder = getOrderById("ORD-001")
                .statusCode(HttpStatus.OK.value())
                .extract()
                .as(OrderResponse.class);

        // then - verify full order details
        assertThat(fullOrder).isNotNull();
        assertThat(fullOrder.order()).isNotNull();
        assertThat(fullOrder.order().orderId()).isEqualTo("ORD-001");
        assertThat(fullOrder.order().customerId()).isEqualTo("CUST-123");
        assertThat(fullOrder.order().amount()).isEqualByComparingTo(new BigDecimal("99.99"));
        assertThat(fullOrder.order().productCode()).isEqualTo("PROD-456");
    }

    @Test
    @Sql(scripts = {"/sql/order/create_order_test_init.sql"})
    void shouldReturnConflictWhenOrderIdAlreadyExists() {
        // given - order with ID 'ORD-999' already exists in database (from SQL init script)
        var request = new CreateOrderRequest(
                "ORD-999",
                "CUST-123",
                new BigDecimal("99.99"),
                "PROD-456"
        );

        // when & then - attempt to create order with duplicate ID should return 409 CONFLICT
        createOrder(request)
                .statusCode(HttpStatus.CONFLICT.value());
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("provideInvalidOrderData")
    void shouldReturnBadRequestForInvalidData(String testDescription, String jsonRequest) {
        createOrder(jsonRequest).statusCode(HttpStatus.BAD_REQUEST.value());
    }

    private static Stream<Arguments> provideInvalidOrderData() {
        return Stream.of(
                Arguments.of(
                        "Null orderId should return BAD_REQUEST",
                        """
                        {
                            "orderId": null,
                            "customerId": "CUST-123",
                            "amount": 99.99,
                            "productCode": "PROD-456"
                        }
                        """
                ),
                Arguments.of(
                        "Blank orderId should return BAD_REQUEST",
                        """
                        {
                            "orderId": "",
                            "customerId": "CUST-123",
                            "amount": 99.99,
                            "productCode": "PROD-456"
                        }
                        """
                ),
                Arguments.of(
                        "Null customerId should return BAD_REQUEST",
                        """
                        {
                            "orderId": "ORD-002",
                            "customerId": null,
                            "amount": 99.99,
                            "productCode": "PROD-456"
                        }
                        """
                ),
                Arguments.of(
                        "Blank customerId should return BAD_REQUEST",
                        """
                        {
                            "orderId": "ORD-003",
                            "customerId": "",
                            "amount": 99.99,
                            "productCode": "PROD-456"
                        }
                        """
                ),
                Arguments.of(
                        "Null amount should return BAD_REQUEST",
                        """
                        {
                            "orderId": "ORD-004",
                            "customerId": "CUST-123",
                            "amount": null,
                            "productCode": "PROD-456"
                        }
                        """
                ),
                Arguments.of(
                        "Negative amount should return BAD_REQUEST",
                        """
                        {
                            "orderId": "ORD-005",
                            "customerId": "CUST-123",
                            "amount": -10.00,
                            "productCode": "PROD-456"
                        }
                        """
                ),
                Arguments.of(
                        "Zero amount should return BAD_REQUEST",
                        """
                        {
                            "orderId": "ORD-006",
                            "customerId": "CUST-123",
                            "amount": 0,
                            "productCode": "PROD-456"
                        }
                        """
                ),
                Arguments.of(
                        "Null productCode should return BAD_REQUEST",
                        """
                        {
                            "orderId": "ORD-007",
                            "customerId": "CUST-123",
                            "amount": 99.99,
                            "productCode": null
                        }
                        """
                ),
                Arguments.of(
                        "Blank productCode should return BAD_REQUEST",
                        """
                        {
                            "orderId": "ORD-008",
                            "customerId": "CUST-123",
                            "amount": 99.99,
                            "productCode": ""
                        }
                        """
                )
        );
    }

}