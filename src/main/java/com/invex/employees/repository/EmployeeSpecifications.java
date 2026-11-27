package com.invex.employees.repository;

import java.util.List;
import java.util.Locale;

import javax.persistence.criteria.Predicate;

import org.springframework.data.jpa.domain.Specification;

import com.invex.employees.domain.Employee;

/**
 * Reusable query criteria for {@link Employee}.
 */
public final class EmployeeSpecifications {

    private static final List<String> NAME_FIELDS = List.of("firstName", "middleName", "paternalLastName",
            "maternalLastName");
    private static final char LIKE_ESCAPE = '\\';

    private EmployeeSpecifications() {
    }

    /**
     * Partial, case-insensitive match against any of the name fields.
     * "ram" finds "Ramírez" as a last name and "Ramón" as a first name.
     */
    public static Specification<Employee> nameContains(String term) {
        String pattern = "%" + escapeLikeWildcards(term.trim().toLowerCase(Locale.ROOT)) + "%";
        return (root, query, cb) -> cb.or(NAME_FIELDS.stream()
                .map(field -> cb.like(cb.lower(root.get(field)), pattern, LIKE_ESCAPE))
                .toArray(Predicate[]::new));
    }

    // Without this, searching for "%" or "_" would match every employee.
    private static String escapeLikeWildcards(String value) {
        return value.replace("\\", "\\\\").replace("%", "\\%").replace("_", "\\_");
    }
}
