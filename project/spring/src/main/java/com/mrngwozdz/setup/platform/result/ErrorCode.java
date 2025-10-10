package com.mrngwozdz.setup.platform.result;

public enum ErrorCode {
    VALIDATION("Validation failed"),
    INVALID_FILE_TYPE("Invalid file type"),
    NOT_FOUND("%s not found"),
    CONFLICT("%s already exists"),
    IO_ERROR("IO operation failed"),
    DATABASE_ERROR("Unexpected database error occurred"),
    TIMEOUT("Operation timed out"),
    UNAVAILABLE("Service unavailable"),
    UNKNOWN("Unknown error occurred");

    private final String defaultMessage;

    ErrorCode(String defaultMessage) {
        this.defaultMessage = defaultMessage;
    }

    public String getDefaultMessage() {
        return defaultMessage;
    }

    public String formatMessage(Object... args) {
        return String.format(defaultMessage, args);
    }
}