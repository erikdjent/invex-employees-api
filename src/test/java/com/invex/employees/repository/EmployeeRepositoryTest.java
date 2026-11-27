package com.invex.employees.repository;

import static com.invex.employees.repository.EmployeeSpecifications.nameContains;
import static com.invex.employees.support.EmployeeTestData.juan;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Sort;

import com.invex.employees.domain.Employee;

/**
 * Runs the real queries against H2 to prove the search and the automatic
 * timestamp work end to end.
 */
@DataJpaTest
class EmployeeRepositoryTest {

    @Autowired
    private EmployeeRepository repository;

    @BeforeEach
    void setUp() {
        repository.save(juan().id(null).createdAt(null).build());
        repository.save(juan().id(null).createdAt(null).firstName("Ana").middleName(null)
                .paternalLastName("García").maternalLastName("Ramírez").build());
        repository.save(juan().id(null).createdAt(null).firstName("Ramón").middleName(null)
                .paternalLastName("Soto").maternalLastName(null).build());
        repository.save(juan().id(null).createdAt(null).firstName("Mario").middleName(null)
                .paternalLastName("100%_Real").maternalLastName(null).build());
    }

    @Test
    @DisplayName("search is partial, case-insensitive and covers first name and both last names")
    void searchAcrossNameFields() {
        assertThat(search("RAM")).extracting(Employee::getFirstName).containsExactly("Ana", "Ramón");
        assertThat(search("pér")).extracting(Employee::getFirstName).containsExactly("Juan");
        assertThat(search("carl")).extracting(Employee::getFirstName).containsExactly("Juan");
    }

    @Test
    @DisplayName("returns nothing when no name matches")
    void searchWithoutMatches() {
        assertThat(search("zzz")).isEmpty();
    }

    @Test
    @DisplayName("% and _ are treated as plain text, not as wildcards")
    void wildcardsAreEscaped() {
        assertThat(search("%")).extracting(Employee::getFirstName).containsExactly("Mario");
        assertThat(search("_")).extracting(Employee::getFirstName).containsExactly("Mario");
    }

    @Test
    @DisplayName("the creation timestamp is filled automatically on insert")
    void createdAtIsAutomatic() {
        assertThat(repository.findAll()).allSatisfy(employee -> assertThat(employee.getCreatedAt()).isNotNull());
    }

    private List<Employee> search(String term) {
        return repository.findAll(nameContains(term), Sort.by("id"));
    }
}
