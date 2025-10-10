package com.mrngwozdz.setup.units.base;

import com.mrngwozdz.setup.platform.result.ErrorCode;
import com.mrngwozdz.setup.platform.result.Failure;
import io.vavr.control.Either;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Base class for data layer unit tests providing common assertion methods
 * for database error handling verification.
 */
public abstract class DataLayerUnitTest {

    /**
     * Verifies that the result is a Left containing a DATABASE_ERROR failure
     * with proper exception details in the context.
     *
     * @param result the Either result to verify
     * @param expectedException the exception that was thrown
     * @param <T> the type of the Right value
     */
    protected <T> void assertDatabaseError(Either<Failure, T> result, Exception expectedException) {
        assertThat(result.isLeft()).isTrue();

        var failure = result.getLeft();
        assertThat(failure.code()).isEqualTo(ErrorCode.DATABASE_ERROR);
        assertThat(failure.message()).isEqualTo("Unexpected database error occurred");
        assertThat(failure.context())
                .containsEntry("exceptionType", expectedException.getClass().getName())
                .containsEntry("exceptionMessage", expectedException.getMessage());
    }

    /**
     * Verifies that the result is a Left containing a DATABASE_ERROR failure
     * with proper exception details and additional context entries.
     *
     * @param result the Either result to verify
     * @param expectedException the exception that was thrown
     * @param additionalContextKey additional context key to verify
     * @param additionalContextValue additional context value to verify
     * @param <T> the type of the Right value
     */
    protected <T> void assertDatabaseError(
            Either<Failure, T> result,
            Exception expectedException,
            String additionalContextKey,
            Object additionalContextValue) {

        assertThat(result.isLeft()).isTrue();

        var failure = result.getLeft();
        assertThat(failure.code()).isEqualTo(ErrorCode.DATABASE_ERROR);
        assertThat(failure.message()).isEqualTo("Unexpected database error occurred");
        assertThat(failure.context())
                .containsEntry("exceptionType", expectedException.getClass().getName())
                .containsEntry("exceptionMessage", expectedException.getMessage())
                .containsEntry(additionalContextKey, additionalContextValue);
    }

    /**
     * Verifies that the result is a Left containing a NOT_FOUND failure
     * with the expected entity name in the message.
     *
     * @param result the Either result to verify
     * @param entityName the name of the entity that was not found
     * @param <T> the type of the Right value
     */
    protected <T> void assertNotFound(Either<Failure, T> result, String entityName) {
        assertThat(result.isLeft()).isTrue();

        var failure = result.getLeft();
        assertThat(failure.code()).isEqualTo(ErrorCode.NOT_FOUND);
        assertThat(failure.message()).isEqualTo(entityName + " not found");
    }

    /**
     * Verifies that the result is a Left containing a CONFLICT failure
     * with the expected entity name in the message.
     *
     * @param result the Either result to verify
     * @param entityName the name of the entity that already exists
     * @param <T> the type of the Right value
     */
    protected <T> void assertConflict(Either<Failure, T> result, String entityName) {
        assertThat(result.isLeft()).isTrue();

        var failure = result.getLeft();
        assertThat(failure.code()).isEqualTo(ErrorCode.CONFLICT);
        assertThat(failure.message()).isEqualTo(entityName + " already exists");
    }
}