package com.invex.employees.exception;

import org.springframework.http.HttpStatus;

import lombok.Getter;

/**
 * Base class for business errors that already know which HTTP status they map
 * to.
 * New domain errors only need to extend it; the global handler picks them up
 * automatically.
 */
@Getter
public abstract class ApiException extends RuntimeException {

    private final HttpStatus status;

    protected ApiException(HttpStatus status, String message) {
        super(message);
        this.status = status;
    }
}
