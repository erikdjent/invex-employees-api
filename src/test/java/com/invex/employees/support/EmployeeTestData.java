package com.invex.employees.support;

import java.time.LocalDate;
import java.time.LocalDateTime;

import com.invex.employees.domain.Employee;
import com.invex.employees.domain.Sex;
import com.invex.employees.dto.EmployeeRequest;
import com.invex.employees.dto.EmployeeResponse;

/** Small, readable fixtures shared by the tests. */
public final class EmployeeTestData {

    public static final LocalDate BIRTH_DATE = LocalDate.of(1994, 3, 15);
    public static final LocalDateTime CREATED_AT = LocalDateTime.of(2026, 11, 26, 10, 15, 30);

    private EmployeeTestData() {
    }

    public static EmployeeRequest.EmployeeRequestBuilder juanRequest() {
        return EmployeeRequest.builder()
                .firstName("Juan")
                .middleName("Carlos")
                .paternalLastName("Pérez")
                .maternalLastName("López")
                .age(30)
                .sex(Sex.MALE)
                .birthDate(BIRTH_DATE)
                .position("Backend Developer");
    }

    public static Employee.EmployeeBuilder juan() {
        return Employee.builder()
                .id(1L)
                .firstName("Juan")
                .middleName("Carlos")
                .paternalLastName("Pérez")
                .maternalLastName("López")
                .age(30)
                .sex(Sex.MALE)
                .birthDate(BIRTH_DATE)
                .position("Backend Developer")
                .createdAt(CREATED_AT)
                .active(true);
    }

    public static EmployeeResponse.EmployeeResponseBuilder juanResponse() {
        return EmployeeResponse.builder()
                .id(1L)
                .firstName("Juan")
                .middleName("Carlos")
                .paternalLastName("Pérez")
                .maternalLastName("López")
                .age(30)
                .sex(Sex.MALE)
                .birthDate(BIRTH_DATE)
                .position("Backend Developer")
                .createdAt(CREATED_AT)
                .active(true);
    }
}
