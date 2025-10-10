package com.mrngwozdz.setup.platform.result;

import org.springframework.http.HttpStatus;

public record Failure(
        ErrorCode code,
        String message,
        java.util.Map<String, Object> context
) {
    public static Failure of(ErrorCode code, String message) {
        return new Failure(code, message, java.util.Map.of());
    }

    public static Failure ofDefault(ErrorCode code) {
        return new Failure(code, code.getDefaultMessage(), java.util.Map.of());
    }

    public static Failure ofDefault(ErrorCode code, Object... args) {
        return new Failure(code, code.formatMessage(args), java.util.Map.of());
    }

    public Failure with(String key, Object value) {
        var copy = new java.util.HashMap<>(context);
        copy.put(key, value);
        return new Failure(code, message, java.util.Map.copyOf(copy));
    }

    public HttpStatus getHttpStatus() {
        return switch (code) {
            case VALIDATION, INVALID_FILE_TYPE -> HttpStatus.BAD_REQUEST;
            case NOT_FOUND -> HttpStatus.NOT_FOUND;
            case CONFLICT -> HttpStatus.CONFLICT;
            case TIMEOUT -> HttpStatus.GATEWAY_TIMEOUT;
            case UNAVAILABLE -> HttpStatus.SERVICE_UNAVAILABLE;
            case DATABASE_ERROR -> HttpStatus.INTERNAL_SERVER_ERROR;
            default -> HttpStatus.INTERNAL_SERVER_ERROR;
        };
    }
}