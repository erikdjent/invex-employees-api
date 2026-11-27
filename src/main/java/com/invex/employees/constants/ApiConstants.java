package com.invex.employees.constants;

/**
 * Values shared across the API layer: paths, formats and HTTP headers.
 */
public final class ApiConstants {

    public static final String EMPLOYEES_PATH = "/employees";
    public static final String ID_PATH = "/{id}";
    public static final String SEARCH_PATH = "/search";

    public static final String DATE_FORMAT = "dd-MM-yyyy";
    public static final String DATE_TIME_FORMAT = "dd-MM-yyyy HH:mm:ss";

    public static final String REQUEST_ID_HEADER = "X-Request-Id";
    public static final String REQUEST_ID_MDC_KEY = "requestId";

    public static final int MAX_EMPLOYEES_PER_REQUEST = 100;

    private ApiConstants() {
    }
}
