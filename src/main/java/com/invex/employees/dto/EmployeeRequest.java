package com.invex.employees.dto;

import static com.invex.employees.constants.ValidationConstants.*;

import java.time.LocalDate;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Past;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.invex.employees.constants.ApiConstants;
import com.invex.employees.domain.Sex;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

/**
 * Payload used to create an employee. Optional fields: middle name, maternal
 * last name and active (defaults to true).
 */
@Builder
@Schema(description = "Data required to register a new employee")
public record EmployeeRequest(

                @Schema(example = "Juan", requiredMode = Schema.RequiredMode.REQUIRED) @NotBlank(message = NAME_REQUIRED) @Size(max = NAME_MAX_LENGTH, message = NAME_TOO_LONG) @Pattern(regexp = NAME_PATTERN, message = NAME_INVALID) String firstName,
                @Schema(example = "Carlos") @Size(max = NAME_MAX_LENGTH, message = NAME_TOO_LONG) @Pattern(regexp = NAME_PATTERN, message = NAME_INVALID) String middleName,
                @Schema(example = "Pérez", requiredMode = Schema.RequiredMode.REQUIRED) @NotBlank(message = NAME_REQUIRED) @Size(max = NAME_MAX_LENGTH, message = NAME_TOO_LONG) @Pattern(regexp = NAME_PATTERN, message = NAME_INVALID) String paternalLastName,
                @Schema(example = "López") @Size(max = NAME_MAX_LENGTH, message = NAME_TOO_LONG) @Pattern(regexp = NAME_PATTERN, message = NAME_INVALID) String maternalLastName,
                @Schema(example = "30", requiredMode = Schema.RequiredMode.REQUIRED) @NotNull(message = AGE_REQUIRED) @Min(value = MIN_AGE, message = AGE_MIN) @Max(value = MAX_AGE, message = AGE_MAX) Integer age,
                @Schema(example = "MALE", requiredMode = Schema.RequiredMode.REQUIRED) @NotNull(message = SEX_REQUIRED) Sex sex,
                @Schema(type = "string", pattern = "dd-MM-yyyy", example = "15-03-1994", requiredMode = Schema.RequiredMode.REQUIRED) @NotNull(message = BIRTH_DATE_REQUIRED) @Past(message = BIRTH_DATE_PAST) @JsonFormat(pattern = ApiConstants.DATE_FORMAT) LocalDate birthDate,
                @Schema(example = "Backend Developer", requiredMode = Schema.RequiredMode.REQUIRED) @NotBlank(message = POSITION_REQUIRED) @Size(max = POSITION_MAX_LENGTH, message = POSITION_TOO_LONG) String position,
                @Schema(example = "true", description = "Defaults to true when omitted") Boolean active) {
}
