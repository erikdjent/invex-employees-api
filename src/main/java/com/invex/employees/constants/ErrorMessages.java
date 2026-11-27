package com.invex.employees.constants;

/**
 * Human readable messages returned in error responses. They never include stack
 * traces or internals.
 */
public final class ErrorMessages {

    public static final String EMPLOYEE_NOT_FOUND = "Employee with id %d was not found";
    public static final String VALIDATION_FAILED = "One or more fields are invalid";
    public static final String MALFORMED_BODY = "The request body is malformed or contains invalid values. "
            + "Dates must use the format " + ApiConstants.DATE_FORMAT + " and sex must be MALE, FEMALE or OTHER";
    public static final String INVALID_PARAMETER = "Parameter '%s' has an invalid value: '%s'";
    public static final String MISSING_PARAMETER = "Required parameter '%s' is missing";
    public static final String METHOD_NOT_ALLOWED = "Method %s is not supported for this endpoint";
    public static final String UNSUPPORTED_MEDIA_TYPE = "Content type '%s' is not supported, use application/json";
    public static final String RESOURCE_NOT_FOUND = "The requested resource does not exist";
    public static final String DATA_CONFLICT = "The operation conflicts with the current state of the data";
    public static final String UNEXPECTED_ERROR = "An unexpected error occurred. Please contact support with the request id";

    private ErrorMessages() {
    }
}
