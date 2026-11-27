package com.invex.employees.exception;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import javax.servlet.http.HttpServletRequest;
import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.validation.Path;

import org.slf4j.MDC;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import com.invex.employees.constants.ApiConstants;
import com.invex.employees.constants.ErrorMessages;

import lombok.extern.slf4j.Slf4j;

/**
 * Single place where exceptions become HTTP responses. Clients always get the
 * same JSON shape
 * and never see stack traces; the full detail stays in the logs, linked by the
 * request id.
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final MediaType PROBLEM_JSON = MediaType.APPLICATION_PROBLEM_JSON;

    @ExceptionHandler(ApiException.class)
    public ResponseEntity<ErrorResponse> handleApiException(ApiException ex, HttpServletRequest request) {
        log.warn("{} {} -> {}", request.getMethod(), request.getRequestURI(), ex.getMessage());
        return build(ex.getStatus(), ex.getMessage(), request, List.of());
    }

    /** @Valid on a single request body (PUT). */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleInvalidBody(MethodArgumentNotValidException ex,
            HttpServletRequest request) {
        List<ErrorResponse.FieldError> errors = ex.getBindingResult().getFieldErrors().stream()
                .map(error -> new ErrorResponse.FieldError(error.getField(), error.getDefaultMessage()))
                .toList();
        return validationError(errors, request);
    }

    /** Validation on lists (POST) and on query/path parameters (search). */
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponse> handleConstraintViolation(ConstraintViolationException ex,
            HttpServletRequest request) {
        List<ErrorResponse.FieldError> errors = ex.getConstraintViolations().stream()
                .map(violation -> new ErrorResponse.FieldError(fieldName(violation), violation.getMessage()))
                .sorted((a, b) -> a.field().compareTo(b.field()))
                .toList();
        return validationError(errors, request);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleUnreadableBody(HttpMessageNotReadableException ex,
            HttpServletRequest request) {
        log.warn("Unreadable request body on {}: {}", request.getRequestURI(), ex.getMostSpecificCause().getMessage());
        return build(HttpStatus.BAD_REQUEST, ErrorMessages.MALFORMED_BODY, request, List.of());
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> handleTypeMismatch(MethodArgumentTypeMismatchException ex,
            HttpServletRequest request) {
        String detail = String.format(ErrorMessages.INVALID_PARAMETER, ex.getName(), ex.getValue());
        return build(HttpStatus.BAD_REQUEST, detail, request, List.of());
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ErrorResponse> handleMissingParameter(MissingServletRequestParameterException ex,
            HttpServletRequest request) {
        String detail = String.format(ErrorMessages.MISSING_PARAMETER, ex.getParameterName());
        return build(HttpStatus.BAD_REQUEST, detail, request, List.of());
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ErrorResponse> handleMethodNotSupported(HttpRequestMethodNotSupportedException ex,
            HttpServletRequest request) {
        String detail = String.format(ErrorMessages.METHOD_NOT_ALLOWED, ex.getMethod());
        return build(HttpStatus.METHOD_NOT_ALLOWED, detail, request, List.of());
    }

    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public ResponseEntity<ErrorResponse> handleMediaType(HttpMediaTypeNotSupportedException ex,
            HttpServletRequest request) {
        String detail = String.format(ErrorMessages.UNSUPPORTED_MEDIA_TYPE, ex.getContentType());
        return build(HttpStatus.UNSUPPORTED_MEDIA_TYPE, detail, request, List.of());
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponse> handleDataIntegrity(DataIntegrityViolationException ex,
            HttpServletRequest request) {
        log.error("Data integrity violation on {}", request.getRequestURI(), ex);
        return build(HttpStatus.CONFLICT, ErrorMessages.DATA_CONFLICT, request, List.of());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleUnexpected(Exception ex, HttpServletRequest request) {
        log.error("Unexpected error on {} {}", request.getMethod(), request.getRequestURI(), ex);
        return build(HttpStatus.INTERNAL_SERVER_ERROR, ErrorMessages.UNEXPECTED_ERROR, request, List.of());
    }

    private ResponseEntity<ErrorResponse> validationError(List<ErrorResponse.FieldError> errors,
            HttpServletRequest request) {
        log.warn("Validation failed on {} {}: {}", request.getMethod(), request.getRequestURI(), errors);
        return build(HttpStatus.BAD_REQUEST, ErrorMessages.VALIDATION_FAILED, request, errors);
    }

    private ResponseEntity<ErrorResponse> build(HttpStatus status, String detail, HttpServletRequest request,
            List<ErrorResponse.FieldError> errors) {
        ErrorResponse body = ErrorResponse.builder()
                .title(status.getReasonPhrase())
                .status(status.value())
                .detail(detail)
                .instance(request.getRequestURI())
                .requestId(MDC.get(ApiConstants.REQUEST_ID_MDC_KEY))
                .timestamp(OffsetDateTime.now())
                .errors(errors)
                .build();
        return ResponseEntity.status(status).contentType(PROBLEM_JSON).body(body);
    }

    /**
     * Turns "create.requests[1].firstName" into "requests[1].firstName" and
     * "search.name" into "name":
     * the method name is noise for the client.
     */
    private static String fieldName(ConstraintViolation<?> violation) {
        List<Path.Node> nodes = StreamSupport.stream(violation.getPropertyPath().spliterator(), false).toList();
        return nodes.stream()
                .skip(1)
                .map(node -> node.getIndex() != null ? "[" + node.getIndex() + "]." + node.getName() : node.getName())
                .filter(name -> name != null && !name.isBlank())
                .collect(Collectors.joining("."))
                .replace(".[", "[");
    }
}
