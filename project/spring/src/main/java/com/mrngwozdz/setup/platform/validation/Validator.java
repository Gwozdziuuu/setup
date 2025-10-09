package com.mrngwozdz.setup.platform.validation;

import com.mrngwozdz.setup.platform.result.ErrorCode;
import com.mrngwozdz.setup.platform.result.Failure;
import io.vavr.control.Either;

import java.util.function.Function;

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

    /**
     * Validates an optional field - if value is null, returns success; otherwise applies validator.
     * Useful for optional fields in update requests.
     *
     * @param value the value to validate (can be null)
     * @param validator the validation function to apply if value is present
     * @param context the context object to return on success
     * @param <T> type of the value being validated
     * @param <R> type of the context object
     * @return Either containing Failure or the context object
     */
    public static <T, R> Either<Failure, R> validateIfPresent(
            T value,
            Function<T, Either<Failure, T>> validator,
            R context
    ) {
        if (value == null) {
            return Either.right(context);
        }
        return validator.apply(value).map(v -> context);
    }

}