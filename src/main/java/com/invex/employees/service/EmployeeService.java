package com.invex.employees.service;

import java.util.List;

import com.invex.employees.dto.EmployeeRequest;
import com.invex.employees.dto.EmployeeResponse;
import com.invex.employees.dto.EmployeeUpdateRequest;

public interface EmployeeService {

    List<EmployeeResponse> findAll();

    EmployeeResponse findById(Long id);

    List<EmployeeResponse> createAll(List<EmployeeRequest> requests);

    EmployeeResponse update(Long id, EmployeeUpdateRequest changes);

    void delete(Long id);

    List<EmployeeResponse> searchByName(String name);
}
