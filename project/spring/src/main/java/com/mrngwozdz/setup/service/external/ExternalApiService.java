package com.mrngwozdz.setup.service.external;

import com.mrngwozdz.setup.database.entity.Order;
import com.mrngwozdz.setup.platform.result.ErrorCode;
import com.mrngwozdz.setup.platform.result.Failure;
import com.mrngwozdz.setup.platform.result.Success;
import io.vavr.control.Either;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Random;

/**
 * Mock external API service for demonstration purposes.
 * Simulates various failure scenarios.
 */
@Slf4j
@Service
public class ExternalApiService {

    private final Random random = new Random();

    /**
     * Simulates calling an external payment processing API.
     * Has a chance to fail with TIMEOUT or UNAVAILABLE errors.
     */
    public Either<Failure, Success<String>> processPayment(Order order) {
        log.info("║ [EXTERNAL API] Processing payment for order: {}", order.getOrderId());

        // Simulate random failures for demonstration
        int scenario = random.nextInt(10);

        if (scenario < 7) {
            // 70% success rate
            String transactionId = "TXN-" + System.currentTimeMillis();
            log.info("║ [EXTERNAL API] Payment processed successfully: {}", transactionId);
            return Either.right(Success.of(transactionId));
        } else if (scenario < 9) {
            // 20% timeout
            log.warn("║ [EXTERNAL API] Payment processing timeout");
            return Either.left(Failure.of(ErrorCode.TIMEOUT, "Payment API timeout"));
        } else {
            // 10% unavailable
            log.error("║ [EXTERNAL API] Payment API unavailable");
            return Either.left(Failure.of(ErrorCode.UNAVAILABLE, "Payment API unavailable"));
        }
    }
}