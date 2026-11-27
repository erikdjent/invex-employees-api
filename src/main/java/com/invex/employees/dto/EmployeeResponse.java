package com.invex.employees.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.invex.employees.constants.ApiConstants;
import com.invex.employees.domain.Sex;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

@Builder
@Schema(description = "Employee as returned by the API")
public record EmployeeResponse(

                @Schema(example = "1") Long id,
                @Schema(example = "Juan") String firstName,
                @Schema(example = "Carlos") String middleName,
                @Schema(example = "Pérez") String paternalLastName,
                @Schema(example = "López") String maternalLastName,
                @Schema(example = "30") Integer age,
                @Schema(example = "MALE") Sex sex,
                @Schema(type = "string", pattern = "dd-MM-yyyy", example = "15-03-1994") @JsonFormat(pattern = ApiConstants.DATE_FORMAT) LocalDate birthDate,
                @Schema(example = "Backend Developer") String position,
                @Schema(type = "string", pattern = "dd-MM-yyyy HH:mm:ss", example = "26-11-2026 10:15:30", description = "Set automatically by the system when the employee is created") @JsonFormat(pattern = ApiConstants.DATE_TIME_FORMAT) LocalDateTime createdAt,
                @Schema(example = "true") Boolean active) {
}
