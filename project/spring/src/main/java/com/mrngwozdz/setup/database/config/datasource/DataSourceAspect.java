package com.mrngwozdz.setup.database.config.datasource;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * AOP Aspect that intercepts methods annotated with @ReadOperation or @WriteOperation
 * and sets the appropriate DataSource context before method execution.
 *
 * This aspect runs BEFORE the @Transactional aspect (Order = 0) to ensure the
 * correct DataSource is selected before the transaction begins.
 */
@Slf4j
@Aspect
@Component
@Order(0)  // Must run before @Transactional aspect
public class DataSourceAspect {

    /**
     * Pointcut for methods annotated with @ReadOperation
     */
    @Pointcut("@annotation(com.mrngwozdz.setup.database.config.datasource.ReadOperation)")
    public void readOperation() {
    }

    /**
     * Pointcut for methods annotated with @WriteOperation
     */
    @Pointcut("@annotation(com.mrngwozdz.setup.database.config.datasource.WriteOperation)")
    public void writeOperation() {
    }

    /**
     * Around advice for @ReadOperation methods.
     * Sets the DataSource context to READ before method execution.
     */
    @Around("readOperation()")
    public Object routeReadOperation(ProceedingJoinPoint joinPoint) throws Throwable {
        DataSourceType previous = DataSourceContextHolder.getDataSourceType();

        try {
            log.debug("Setting DataSource to READ for method: {}", joinPoint.getSignature().getName());
            DataSourceContextHolder.setDataSourceType(DataSourceType.READ);
            return joinPoint.proceed();
        } finally {
            // Restore previous context (in case of nested calls)
            if (previous != null) {
                DataSourceContextHolder.setDataSourceType(previous);
            } else {
                DataSourceContextHolder.clear();
            }
            log.debug("DataSource context cleared for method: {}", joinPoint.getSignature().getName());
        }
    }

    /**
     * Around advice for @WriteOperation methods.
     * Sets the DataSource context to WRITE before method execution.
     */
    @Around("writeOperation()")
    public Object routeWriteOperation(ProceedingJoinPoint joinPoint) throws Throwable {
        DataSourceType previous = DataSourceContextHolder.getDataSourceType();

        try {
            log.debug("Setting DataSource to WRITE for method: {}", joinPoint.getSignature().getName());
            DataSourceContextHolder.setDataSourceType(DataSourceType.WRITE);
            return joinPoint.proceed();
        } finally {
            // Restore previous context (in case of nested calls)
            if (previous != null) {
                DataSourceContextHolder.setDataSourceType(previous);
            } else {
                DataSourceContextHolder.clear();
            }
            log.debug("DataSource context cleared for method: {}", joinPoint.getSignature().getName());
        }
    }
}