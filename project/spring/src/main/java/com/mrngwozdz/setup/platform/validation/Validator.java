package com.mrngwozdz.setup.platform.validation;

import com.mrngwozdz.setup.platform.result.ErrorCode;
import com.mrngwozdz.setup.platform.result.Failure;
import io.vavr.control.Either;

public class Validator {

    private Validator() {}

    public static <T> Either<Failure, T> notNull(T value, String fieldName) {
        if (value == null) {
            return Either.left(Failure.of(
                    ErrorCode.VALIDATION,
                    String.format("%s cannot be null", fieldName)
            ));
        }
        return Either.right(value);
    }

    public static Either<Failure, String> notBlank(String value, String fieldName) {
        return notNull(value, fieldName)
                .flatMap(v -> {
                    if (v.isBlank()) {
                        return Either.left(Failure.of(
                                ErrorCode.VALIDATION,
                                String.format("%s cannot be blank", fieldName)
                        ));
                    }
                    return Either.right(v);
                });
    }

}