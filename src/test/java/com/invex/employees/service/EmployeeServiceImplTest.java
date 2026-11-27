package com.invex.employees.service;

import static com.invex.employees.support.EmployeeTestData.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.tuple;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;

import com.invex.employees.domain.Employee;
import com.invex.employees.dto.EmployeeRequest;
import com.invex.employees.dto.EmployeeResponse;
import com.invex.employees.dto.EmployeeUpdateRequest;
import com.invex.employees.exception.EmployeeNotFoundException;
import com.invex.employees.mapper.EmployeeMapper;
import com.invex.employees.repository.EmployeeRepository;

@ExtendWith(MockitoExtension.class)
class EmployeeServiceImplTest {

    @Mock
    private EmployeeRepository repository;

    @Captor
    private ArgumentCaptor<List<Employee>> employeesCaptor;

    private EmployeeServiceImpl service;

    @BeforeEach
    void setUp() {
        // The real mapper is used on purpose: it is pure logic and mocking it would
        // only hide bugs.
        service = new EmployeeServiceImpl(repository, new EmployeeMapper());
    }

    @Nested
    @DisplayName("findAll")
    class FindAll {

        @Test
        @DisplayName("returns every employee ordered by id")
        void returnsAll() {
            when(repository.findAll(Sort.by("id"))).thenReturn(List.of(juan().build(), juan().id(2L).build()));

            List<EmployeeResponse> result = service.findAll();

            assertThat(result).extracting(EmployeeResponse::id).containsExactly(1L, 2L);
        }

        @Test
        @DisplayName("returns an empty list when there are no employees")
        void returnsEmpty() {
            when(repository.findAll(any(Sort.class))).thenReturn(List.of());

            assertThat(service.findAll()).isEmpty();
        }
    }

    @Nested
    @DisplayName("findById")
    class FindById {

        @Test
        @DisplayName("returns the employee when it exists")
        void found() {
            when(repository.findById(1L)).thenReturn(Optional.of(juan().build()));

            assertThat(service.findById(1L)).isEqualTo(juanResponse().build());
        }

        @Test
        @DisplayName("throws a 404 exception when it does not exist")
        void notFound() {
            when(repository.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.findById(99L))
                    .isInstanceOf(EmployeeNotFoundException.class)
                    .hasMessage("Employee with id 99 was not found")
                    .extracting("status").isEqualTo(HttpStatus.NOT_FOUND);
        }
    }

    @Nested
    @DisplayName("createAll")
    class CreateAll {

        @Test
        @DisplayName("saves every employee in a single call and returns them with their ids")
        void savesBatch() {
            List<EmployeeRequest> requests = List.of(juanRequest().build(), juanRequest().firstName("Ana").build());
            when(repository.saveAll(anyList()))
                    .thenReturn(List.of(juan().build(), juan().id(2L).firstName("Ana").build()));

            List<EmployeeResponse> result = service.createAll(requests);

            verify(repository).saveAll(employeesCaptor.capture());
            assertThat(employeesCaptor.getValue())
                    .extracting(Employee::getFirstName, Employee::getActive)
                    .containsExactly(tuple("Juan", true), tuple("Ana", true));
            assertThat(result).extracting(EmployeeResponse::id).containsExactly(1L, 2L);
        }
    }

    @Nested
    @DisplayName("update")
    class Update {

        @Test
        @DisplayName("applies only the sent fields and saves")
        void partialUpdate() {
            Employee existing = juan().build();
            when(repository.findById(1L)).thenReturn(Optional.of(existing));
            when(repository.saveAndFlush(existing)).thenReturn(existing);

            EmployeeResponse result = service.update(1L, EmployeeUpdateRequest.builder().age(31).build());

            assertThat(result.age()).isEqualTo(31);
            assertThat(result.firstName()).isEqualTo("Juan");
        }

        @Test
        @DisplayName("throws 404 and saves nothing when the employee does not exist")
        void notFound() {
            when(repository.findById(99L)).thenReturn(Optional.empty());
            EmployeeUpdateRequest changes = EmployeeUpdateRequest.builder().age(31).build();

            assertThatThrownBy(() -> service.update(99L, changes)).isInstanceOf(EmployeeNotFoundException.class);
            verify(repository, never()).saveAndFlush(any());
        }
    }

    @Nested
    @DisplayName("delete")
    class Delete {

        @Test
        @DisplayName("removes the employee when it exists")
        void deletes() {
            Employee existing = juan().build();
            when(repository.findById(1L)).thenReturn(Optional.of(existing));

            service.delete(1L);

            verify(repository).delete(existing);
        }

        @Test
        @DisplayName("throws 404 and deletes nothing when the employee does not exist")
        void notFound() {
            when(repository.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.delete(99L)).isInstanceOf(EmployeeNotFoundException.class);
            verify(repository, never()).delete(any());
        }
    }

    @Nested
    @DisplayName("searchByName")
    class SearchByName {

        @Test
        @SuppressWarnings("unchecked")
        @DisplayName("delegates to the name specification and maps the results")
        void searches() {
            when(repository.findAll(any(Specification.class), any(Sort.class))).thenReturn(List.of(juan().build()));

            assertThat(service.searchByName("per")).containsExactly(juanResponse().build());
        }
    }
}
