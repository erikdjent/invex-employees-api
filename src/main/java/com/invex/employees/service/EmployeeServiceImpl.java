package com.invex.employees.service;

import java.util.List;

import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.invex.employees.domain.Employee;
import com.invex.employees.dto.EmployeeRequest;
import com.invex.employees.dto.EmployeeResponse;
import com.invex.employees.dto.EmployeeUpdateRequest;
import com.invex.employees.exception.EmployeeNotFoundException;
import com.invex.employees.mapper.EmployeeMapper;
import com.invex.employees.repository.EmployeeRepository;
import com.invex.employees.repository.EmployeeSpecifications;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class EmployeeServiceImpl implements EmployeeService {

    private static final Sort BY_ID = Sort.by("id");

    private final EmployeeRepository repository;
    private final EmployeeMapper mapper;

    @Override
    public List<EmployeeResponse> findAll() {
        return toResponses(repository.findAll(BY_ID));
    }

    @Override
    public EmployeeResponse findById(Long id) {
        return mapper.toResponse(getExisting(id));
    }

    /** All or nothing: if one employee fails to persist, none of the batch is saved. */
    @Override
    @Transactional
    public List<EmployeeResponse> createAll(List<EmployeeRequest> requests) {
        List<Employee> saved = repository.saveAll(requests.stream().map(mapper::toEntity).toList());
        log.info("Created {} employee(s) with ids {}", saved.size(), saved.stream().map(Employee::getId).toList());
        return toResponses(saved);
    }

    @Override
    @Transactional
    public EmployeeResponse update(Long id, EmployeeUpdateRequest changes) {
        Employee employee = getExisting(id);
        mapper.applyChanges(changes, employee);
        // saveAndFlush so the returned data reflects exactly what the database accepted.
        EmployeeResponse response = mapper.toResponse(repository.saveAndFlush(employee));
        log.info("Updated employee {}", id);
        return response;
    }

    @Override
    @Transactional
    public void delete(Long id) {
        repository.delete(getExisting(id));
        log.info("Deleted employee {}", id);
    }

    @Override
    public List<EmployeeResponse> searchByName(String name) {
        List<EmployeeResponse> matches =
                toResponses(repository.findAll(EmployeeSpecifications.nameContains(name), BY_ID));
        log.debug("Search by name '{}' returned {} result(s)", name, matches.size());
        return matches;
    }

    private Employee getExisting(Long id) {
        return repository.findById(id).orElseThrow(() -> new EmployeeNotFoundException(id));
    }

    private List<EmployeeResponse> toResponses(List<Employee> employees) {
        return employees.stream().map(mapper::toResponse).toList();
    }
}
