package com.mrngwozdz.setup.database.config.datasource;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a method as a write database operation.
 * Methods annotated with @WriteOperation will use the WRITE DataSource (command connection pool).
 *
 * This annotation should be used in the Business Layer for operations that modify data
 * (INSERT, UPDATE, DELETE).
 *
 * Example:
 * <pre>
 * {@code
 * @WriteOperation
 * @Transactional
 * public Either<Failure, Order> createOrder(CreateOrderRequest request) {
 *     return orderCommand.create(request);
 * }
 * }
 * </pre>
 *
 * Note: This annotation works in conjunction with @Transactional.
 * Do not use readOnly = true with @WriteOperation.
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface WriteOperation {
}