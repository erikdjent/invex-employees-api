package com.invex.employees.mapper;

import static com.invex.employees.support.EmployeeTestData.*;
import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.invex.employees.domain.Employee;
import com.invex.employees.domain.Sex;
import com.invex.employees.dto.EmployeeUpdateRequest;

class EmployeeMapperTest {

    private final EmployeeMapper mapper = new EmployeeMapper();

    @Test
    @DisplayName("a new employee is active by default and its text fields are trimmed")
    void toEntityDefaultsActiveAndTrims() {
        Employee employee = mapper.toEntity(juanRequest().firstName("  Juan ").position(" Backend Developer ").build());

        assertThat(employee.getActive()).isTrue();
        assertThat(employee.getFirstName()).isEqualTo("Juan");
        assertThat(employee.getPosition()).isEqualTo("Backend Developer");
        assertThat(employee.getId()).isNull();
        assertThat(employee.getCreatedAt()).isNull();
    }

    @Test
    @DisplayName("an explicit active=false is respected")
    void toEntityKeepsExplicitInactive() {
        assertThat(mapper.toEntity(juanRequest().active(false).build()).getActive()).isFalse();
    }

    @Test
    @DisplayName("partial update changes only the fields that were sent")
    void applyChangesOnlyTouchesSentFields() {
        Employee employee = juan().build();

        mapper.applyChanges(EmployeeUpdateRequest.builder().position("Tech Lead").active(false).build(), employee);

        assertThat(employee.getPosition()).isEqualTo("Tech Lead");
        assertThat(employee.getActive()).isFalse();
        assertThat(employee.getFirstName()).isEqualTo("Juan");
        assertThat(employee.getAge()).isEqualTo(30);
        assertThat(employee.getSex()).isEqualTo(Sex.MALE);
        assertThat(employee.getBirthDate()).isEqualTo(BIRTH_DATE);
        assertThat(employee.getCreatedAt()).isEqualTo(CREATED_AT);
    }

    @Test
    @DisplayName("an empty update leaves the employee exactly as it was")
    void emptyUpdateChangesNothing() {
        Employee employee = juan().build();
        String before = employee.toString();

        mapper.applyChanges(EmployeeUpdateRequest.builder().build(), employee);

        assertThat(employee).hasToString(before);
    }

    @Test
    @DisplayName("response carries every field of the entity")
    void toResponseCopiesEverything() {
        assertThat(mapper.toResponse(juan().build())).isEqualTo(juanResponse().build());
    }
}
