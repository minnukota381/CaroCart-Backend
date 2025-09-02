package com.carocart.authentication.config;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Thrown when creating/updating a resource would violate a uniqueness rule
 * (e.g., email already registered).
 */
@ResponseStatus(HttpStatus.CONFLICT) // backup: returns 409 if not caught by @ControllerAdvice
public class DuplicateResourceException extends RuntimeException {
    private final String field;

    public DuplicateResourceException(String field, String message) {
        super(message);
        this.field = field;
    }

    public String getField() {
        return field;
    }
}
