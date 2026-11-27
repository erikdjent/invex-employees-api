package com.invex.employees.constants;

/**
 * Validation rules and their messages, kept in one place so the request DTOs
 * stay readable.
 */
public final class ValidationConstants {

    /**
     * Must start with a letter; allows accents, spaces, apostrophes, dots and
     * hyphens ("María José", "O'Neil").
     */
    public static final String NAME_PATTERN = "^\\p{L}[\\p{L} '.-]*$";

    public static final int NAME_MAX_LENGTH = 50;
    public static final int POSITION_MAX_LENGTH = 100;
    public static final int MIN_AGE = 18;
    public static final int MAX_AGE = 100;

    public static final String NAME_REQUIRED = "is required";
    public static final String NAME_INVALID = "must contain only letters, spaces, apostrophes, dots or hyphens";
    public static final String NAME_TOO_LONG = "must not exceed " + NAME_MAX_LENGTH + " characters";
    public static final String POSITION_REQUIRED = "is required";
    public static final String POSITION_TOO_LONG = "must not exceed " + POSITION_MAX_LENGTH + " characters";
    public static final String AGE_REQUIRED = "is required";
    public static final String AGE_MIN = "must be at least " + MIN_AGE;
    public static final String AGE_MAX = "must be at most " + MAX_AGE;
    public static final String SEX_REQUIRED = "is required (MALE, FEMALE or OTHER)";
    public static final String BIRTH_DATE_REQUIRED = "is required (format " + ApiConstants.DATE_FORMAT + ")";
    public static final String BIRTH_DATE_PAST = "must be a date in the past";
    public static final String EMPLOYEES_REQUIRED = "at least one employee is required";
    public static final String EMPLOYEES_TOO_MANY = "no more than " + ApiConstants.MAX_EMPLOYEES_PER_REQUEST
            + " employees can be created per request";
    public static final String SEARCH_NAME_REQUIRED = "the 'name' parameter must not be blank";

    private ValidationConstants() {
    }
}
