package com.techcorp.employee.dto;

import com.techcorp.employee.model.Department;
import com.techcorp.employee.model.Employee;

import java.util.List;
import java.util.Optional;

public class DepartmentDTO {
    private final Department department;
    private final List<Employee> employees;
    private final Optional<Employee> manager;

    public DepartmentDTO(Department department, List<Employee> employees, Optional<Employee> manager) {
        this.department = department;
        this.employees = employees;
        this.manager = manager;
    }

    // gettery
    public Department getDepartment() { return department; }
    public List<Employee> getEmployees() { return employees; }
    public Optional<Employee> getManager() { return manager; }

}