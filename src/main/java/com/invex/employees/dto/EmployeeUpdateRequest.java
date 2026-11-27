package com.invex.employees.dto;

import static com.invex.employees.constants.ValidationConstants.*;

import java.time.LocalDate;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.Past;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.invex.employees.constants.ApiConstants;
import com.invex.employees.domain.Sex;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

/**
 * Payload for PUT. Every field is optional: only the ones sent (non-null) are
 * applied, the rest keep their value.
 * Fields that are present still go through the same rules as on creation.
 */
@Builder
@Schema(description = "Fields to update. Omitted or null fields are left unchanged")
public record EmployeeUpdateRequest(

                @Schema(example = "Juan") @Size(max = NAME_MAX_LENGTH, message = NAME_TOO_LONG) @Pattern(regexp = NAME_PATTERN, message = NAME_INVALID) String firstName,
                @Schema(example = "Carlos") @Size(max = NAME_MAX_LENGTH, message = NAME_TOO_LONG) @Pattern(regexp = NAME_PATTERN, message = NAME_INVALID) String middleName,
                @Schema(example = "Pérez") @Size(max = NAME_MAX_LENGTH, message = NAME_TOO_LONG) @Pattern(regexp = NAME_PATTERN, message = NAME_INVALID) String paternalLastName,
                @Schema(example = "López") @Size(max = NAME_MAX_LENGTH, message = NAME_TOO_LONG) @Pattern(regexp = NAME_PATTERN, message = NAME_INVALID) String maternalLastName,
                @Schema(example = "31") @Min(value = MIN_AGE, message = AGE_MIN) @Max(value = MAX_AGE, message = AGE_MAX) Integer age,
                @Schema(example = "MALE") Sex sex,
                @Schema(type = "string", pattern = "dd-MM-yyyy", example = "15-03-1994") @Past(message = BIRTH_DATE_PAST) @JsonFormat(pattern = ApiConstants.DATE_FORMAT) LocalDate birthDate,
                @Schema(example = "Senior Backend Developer") @Size(max = POSITION_MAX_LENGTH, message = POSITION_TOO_LONG) @Pattern(regexp = ".*\\S.*", message = POSITION_REQUIRED) String position,
                @Schema(example = "false") Boolean active) {
}
