package com.invex.employees.controller;

import static com.invex.employees.constants.ApiConstants.*;
import static com.invex.employees.constants.ValidationConstants.*;

import java.util.List;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Size;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.invex.employees.dto.EmployeeRequest;
import com.invex.employees.dto.EmployeeResponse;
import com.invex.employees.dto.EmployeeUpdateRequest;
import com.invex.employees.exception.ErrorResponse;
import com.invex.employees.service.EmployeeService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

/**
 * HTTP entry point for employees. It only deals with HTTP concerns (status
 * codes, validation, docs);
 * the business logic lives in {@link EmployeeService}.
 */
@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping(value = EMPLOYEES_PATH, produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Employees", description = "Create, read, update, delete and search employees")
@SecurityRequirement(name = "basicAuth")
@ApiResponses({
                @ApiResponse(responseCode = "401", description = "Missing or invalid credentials", content = @Content),
                @ApiResponse(responseCode = "500", description = "Unexpected error", content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ErrorResponse.class)))
})
public class EmployeeController {

        private final EmployeeService employeeService;

        @GetMapping
        @Operation(summary = "List all employees", description = "Returns every registered employee ordered by id")
        @ApiResponse(responseCode = "200", description = "Employees found (empty list if there are none)")
        public List<EmployeeResponse> findAll() {
                return employeeService.findAll();
        }

        @GetMapping(ID_PATH)
        @Operation(summary = "Get an employee by id")
        @ApiResponse(responseCode = "200", description = "Employee found")
        @ApiResponse(responseCode = "400", description = "The id is not a number", content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ErrorResponse.class)))
        @ApiResponse(responseCode = "404", description = "Employee not found", content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ErrorResponse.class)))
        public EmployeeResponse findById(@Parameter(description = "Employee id", example = "1") @PathVariable Long id) {
                return employeeService.findById(id);
        }

        @GetMapping(SEARCH_PATH)
        @Operation(summary = "Search employees by name", description = "Partial, case-insensitive match on first name, middle name or either last name")
        @ApiResponse(responseCode = "200", description = "Matching employees (empty list if none match)")
        @ApiResponse(responseCode = "400", description = "The name parameter is missing or blank", content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ErrorResponse.class)))
        public List<EmployeeResponse> searchByName(
                        @Parameter(description = "Text to look for", example = "per") @RequestParam @NotBlank(message = SEARCH_NAME_REQUIRED) String name) {
                return employeeService.searchByName(name);
        }

        @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
        @ResponseStatus(HttpStatus.CREATED)
        @Operation(summary = "Create one or several employees", description = "Accepts a single employee object or an array of employees. "
                        + "The batch is atomic: if any employee is invalid, none is created.")
        @io.swagger.v3.oas.annotations.parameters.RequestBody(content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, array = @ArraySchema(schema = @Schema(implementation = EmployeeRequest.class)), examples = {
                        @ExampleObject(name = "Several employees", value = """
                                        [
                                          {"firstName":"Juan","middleName":"Carlos","paternalLastName":"Pérez",
                                           "maternalLastName":"López","age":30,"sex":"MALE","birthDate":"15-03-1994",
                                           "position":"Backend Developer"},
                                          {"firstName":"Ana","paternalLastName":"García","age":28,"sex":"FEMALE",
                                           "birthDate":"02-07-1996","position":"QA Engineer","active":true}
                                        ]"""),
                        @ExampleObject(name = "Single employee", value = """
                                        {"firstName":"Luis","paternalLastName":"Ramírez","maternalLastName":"Soto","age":41,
                                         "sex":"MALE","birthDate":"20-11-1983","position":"Tech Lead"}""")
        }))
        @ApiResponse(responseCode = "201", description = "Employees created")
        @ApiResponse(responseCode = "400", description = "Invalid or malformed data", content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ErrorResponse.class)))
        public List<EmployeeResponse> create(
                        @RequestBody @NotEmpty(message = EMPLOYEES_REQUIRED) @Size(max = MAX_EMPLOYEES_PER_REQUEST, message = EMPLOYEES_TOO_MANY) List<@Valid EmployeeRequest> requests) {
                return employeeService.createAll(requests);
        }

        @PutMapping(value = ID_PATH, consumes = MediaType.APPLICATION_JSON_VALUE)
        @Operation(summary = "Update an employee", description = "Updates all or some fields. Fields not sent (or sent as null) keep their current value. "
                        + "The id and the creation timestamp cannot be changed.")
        @ApiResponse(responseCode = "200", description = "Employee updated")
        @ApiResponse(responseCode = "400", description = "Invalid or malformed data", content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ErrorResponse.class)))
        @ApiResponse(responseCode = "404", description = "Employee not found", content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ErrorResponse.class)))
        public EmployeeResponse update(@Parameter(description = "Employee id", example = "1") @PathVariable Long id,
                        @Valid @RequestBody EmployeeUpdateRequest changes) {
                return employeeService.update(id, changes);
        }

        @DeleteMapping(ID_PATH)
        @ResponseStatus(HttpStatus.NO_CONTENT)
        @Operation(summary = "Delete an employee")
        @ApiResponse(responseCode = "204", description = "Employee deleted")
        @ApiResponse(responseCode = "404", description = "Employee not found", content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ErrorResponse.class)))
        public void delete(@Parameter(description = "Employee id", example = "1") @PathVariable Long id) {
                employeeService.delete(id);
        }
}
