package com.mrngwozdz.setup.database.config.datasource;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a method as a read-only database operation.
 * Methods annotated with @ReadOperation will use the READ DataSource (query connection pool).
 *
 * This annotation should be used in the Business Layer for operations that only read data
 * and do not perform any modifications.
 *
 * Example:
 * <pre>
 * {@code
 * @ReadOperation
 * @Transactional(readOnly = true)
 * public Either<Failure, Order> getOrderById(String orderId) {
 *     return orderQuery.findById(orderId);
 * }
 * }
 * </pre>
 *
 * Note: This annotation works in conjunction with @Transactional.
 * Use @Transactional(readOnly = true) for read operations.
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface ReadOperation {
}