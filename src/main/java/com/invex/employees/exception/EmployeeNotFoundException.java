package com.invex.employees.exception;

import org.springframework.http.HttpStatus;

import com.invex.employees.constants.ErrorMessages;

public class EmployeeNotFoundException extends ApiException {

    public EmployeeNotFoundException(Long id) {
        super(HttpStatus.NOT_FOUND, String.format(ErrorMessages.EMPLOYEE_NOT_FOUND, id));
    }
}
