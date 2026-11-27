package com.invex.employees.mapper;

import java.util.Optional;
import java.util.function.Consumer;

import org.springframework.stereotype.Component;

import com.invex.employees.domain.Employee;
import com.invex.employees.dto.EmployeeRequest;
import com.invex.employees.dto.EmployeeResponse;
import com.invex.employees.dto.EmployeeUpdateRequest;

/**
 * Translates between the API contract (DTOs) and the persistence model, so
 * neither leaks into the other.
 */
@Component
public class EmployeeMapper {

    public Employee toEntity(EmployeeRequest request) {
        return Employee.builder()
                .firstName(clean(request.firstName()))
                .middleName(clean(request.middleName()))
                .paternalLastName(clean(request.paternalLastName()))
                .maternalLastName(clean(request.maternalLastName()))
                .age(request.age())
                .sex(request.sex())
                .birthDate(request.birthDate())
                .position(clean(request.position()))
                .active(Optional.ofNullable(request.active()).orElse(Boolean.TRUE))
                .build();
    }

    public EmployeeResponse toResponse(Employee employee) {
        return EmployeeResponse.builder()
                .id(employee.getId())
                .firstName(employee.getFirstName())
                .middleName(employee.getMiddleName())
                .paternalLastName(employee.getPaternalLastName())
                .maternalLastName(employee.getMaternalLastName())
                .age(employee.getAge())
                .sex(employee.getSex())
                .birthDate(employee.getBirthDate())
                .position(employee.getPosition())
                .createdAt(employee.getCreatedAt())
                .active(employee.getActive())
                .build();
    }

    /**
     * Applies only the fields the client actually sent; id and createdAt are never
     * touched.
     */
    public void applyChanges(EmployeeUpdateRequest changes, Employee employee) {
        applyIfPresent(clean(changes.firstName()), employee::setFirstName);
        applyIfPresent(clean(changes.middleName()), employee::setMiddleName);
        applyIfPresent(clean(changes.paternalLastName()), employee::setPaternalLastName);
        applyIfPresent(clean(changes.maternalLastName()), employee::setMaternalLastName);
        applyIfPresent(changes.age(), employee::setAge);
        applyIfPresent(changes.sex(), employee::setSex);
        applyIfPresent(changes.birthDate(), employee::setBirthDate);
        applyIfPresent(clean(changes.position()), employee::setPosition);
        applyIfPresent(changes.active(), employee::setActive);
    }

    private static <T> void applyIfPresent(T value, Consumer<T> setter) {
        Optional.ofNullable(value).ifPresent(setter);
    }

    private static String clean(String value) {
        return Optional.ofNullable(value).map(String::trim).orElse(null);
    }
}
