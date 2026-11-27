package com.invex.employees.exception;

import java.time.OffsetDateTime;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

/**
 * Error body inspired by RFC 7807 (Problem Details), which Spring Boot 2.7 does
 * not ship natively.
 * {@code requestId} matches the X-Request-Id header and the logs, so a client
 * can report it and we can trace it.
 */
@Builder
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@Schema(description = "Standard error response")
public record ErrorResponse(
                @Schema(example = "Not Found") String title,
                @Schema(example = "404") int status,
                @Schema(example = "Employee with id 99 was not found") String detail,
                @Schema(example = "/employees/99") String instance,
                @Schema(example = "6f1c2a9e-3c1d-4b8e-9f3a-2d4c5b6a7e8f") String requestId,
                OffsetDateTime timestamp,
                List<FieldError> errors) {

        @Schema(description = "A single invalid field")
        public record FieldError(
                        @Schema(example = "firstName") String field,
                        @Schema(example = "is required") String message) {
        }
}
