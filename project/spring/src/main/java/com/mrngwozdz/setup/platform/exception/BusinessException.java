package com.mrngwozdz.setup.platform.exception;

import com.mrngwozdz.setup.platform.result.Failure;
import lombok.Getter;

/**
 * Runtime exception that wraps a business Failure.
 * Used to convert functional Either<Failure, T> to exception-based flow in controllers.
 */
@Getter
public class BusinessException extends RuntimeException {
    private final Failure failure;

    public BusinessException(Failure failure) {
        super(failure.message());
        this.failure = failure;
    }
}