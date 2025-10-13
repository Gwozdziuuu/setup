package com.mrngwozdz.setup.platform.exception;

import com.mrngwozdz.setup.platform.http.ResponseProblem;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.HashMap;
import java.util.Map;

/**
 * Global exception handler that catches unexpected exceptions not handled by the functional Either pattern.
 * This serves as a safety net for:
 * - BusinessException (functional Either converted to exception flow)
 * - Unexpected runtime exceptions
 * - Spring framework exceptions (validation, deserialization, etc.)
 * - Any other unhandled exceptions that escape the business layer
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Handles BusinessException thrown by controllers when converting functional Either to exception flow.
     * This enables clean controller code with proper type safety while maintaining functional Either in business layer.
     */
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ResponseProblem> handleBusinessException(BusinessException ex) {
        return com.mrngwozdz.setup.platform.http.RestResults.toResponse(ex.getFailure());
    }

    /**
     * Handles all unexpected exceptions that are not caught by the application logic.
     * This is the last line of defense to prevent unformatted error responses.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ResponseProblem> handleUnexpectedException(Exception e) {
        log.error("Unexpected exception occurred", e);
        var problem = ResponseProblem.of(
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "INTERNAL_SERVER_ERROR",
                "An unexpected error occurred. Please contact support if the problem persists.",
                Map.of(
                        "exceptionType", e.getClass().getName(),
                        "exceptionMessage", e.getMessage() != null ? e.getMessage() : "No message available"
                )
        );
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(problem);
    }

    /**
     * Handles invalid JSON or request body deserialization errors.
     * Occurs when the client sends malformed JSON or incorrect data types.
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ResponseProblem> handleInvalidJson(HttpMessageNotReadableException e) {
        log.warn("Invalid JSON in request body: {}", e.getMessage());
        var problem = ResponseProblem.of(
                HttpStatus.BAD_REQUEST.value(),
                "INVALID_REQUEST_BODY",
                "The request body is invalid or malformed. Please check your JSON syntax.",
                Map.of(
                        "error", e.getMessage() != null ? e.getMessage() : "Invalid JSON format",
                        "hint", "Verify that all required fields are present and have correct data types"
                )
        );
        return ResponseEntity.badRequest().body(problem);
    }

    /**
     * Handles Spring validation errors when using @Valid annotation.
     * Collects all field errors and returns them in a structured format.
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ResponseProblem> handleValidationErrors(MethodArgumentNotValidException e) {
        log.warn("Validation failed for request: {}", e.getBindingResult());

        Map<String, Object> extensions = new HashMap<>();
        Map<String, String> fieldErrors = new HashMap<>();

        e.getBindingResult().getFieldErrors().forEach(error ->
            fieldErrors.put(error.getField(), error.getDefaultMessage())
        );

        extensions.put("fieldErrors", fieldErrors);
        extensions.put("errorCount", fieldErrors.size());

        var problem = ResponseProblem.of(
                HttpStatus.BAD_REQUEST.value(),
                "VALIDATION_ERROR",
                "One or more fields failed validation. Please check the fieldErrors for details.",
                extensions
        );
        return ResponseEntity.badRequest().body(problem);
    }

    /**
     * Handles type mismatch errors for path variables and request parameters.
     * Occurs when a path variable or request parameter cannot be converted to the expected type.
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ResponseProblem> handleTypeMismatch(MethodArgumentTypeMismatchException e) {
        log.warn("Type mismatch for parameter '{}': expected type {}, got value '{}'",
                e.getName(), e.getRequiredType(), e.getValue());

        String expectedType = e.getRequiredType() != null ? e.getRequiredType().getSimpleName() : "unknown";

        var problem = ResponseProblem.of(
                HttpStatus.BAD_REQUEST.value(),
                "INVALID_PARAMETER_TYPE",
                String.format("Parameter '%s' should be of type %s", e.getName(), expectedType),
                Map.of(
                        "parameter", e.getName(),
                        "expectedType", expectedType,
                        "providedValue", e.getValue() != null ? e.getValue().toString() : "null"
                )
        );
        return ResponseEntity.badRequest().body(problem);
    }

    /**
     * Handles IllegalArgumentException which may be thrown by the application code.
     * This provides a more user-friendly response than the generic exception handler.
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ResponseProblem> handleIllegalArgument(IllegalArgumentException e) {
        log.warn("Illegal argument: {}", e.getMessage());
        var problem = ResponseProblem.of(
                HttpStatus.BAD_REQUEST.value(),
                "ILLEGAL_ARGUMENT",
                e.getMessage() != null ? e.getMessage() : "Invalid argument provided",
                Map.of("exceptionType", e.getClass().getName())
        );
        return ResponseEntity.badRequest().body(problem);
    }

    /**
     * Handles NullPointerException which should rarely occur but provides better error messages when it does.
     * Note: NPE should be prevented by proper null checks in the code.
     */
    @ExceptionHandler(NullPointerException.class)
    public ResponseEntity<ResponseProblem> handleNullPointer(NullPointerException e) {
        log.error("NullPointerException occurred - this indicates a bug in the code", e);
        var problem = ResponseProblem.of(
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "NULL_POINTER_ERROR",
                "An internal error occurred due to missing data. This has been logged and will be investigated.",
                Map.of(
                        "exceptionType", e.getClass().getName(),
                        "message", e.getMessage() != null ? e.getMessage() : "Null value encountered unexpectedly"
                )
        );
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(problem);
    }
}