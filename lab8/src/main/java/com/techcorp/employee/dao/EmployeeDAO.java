package com.techcorp.employee.dao;

import com.techcorp.employee.model.Employee;
import com.techcorp.employee.model.CompanyStatistics;

import java.util.List;
import java.util.Optional;

public interface EmployeeDAO {
    List<Employee> findAll();
    Optional<Employee> findById(Long id);
    Optional<Employee> findByEmail(String email);
    Employee save(Employee employee);
    void deleteByEmail(String email);
    void deleteAll();
    List<Employee> findByCompany(String company);
    List<Employee> findByDepartmentId(Long departmentId);
    List<Employee> findEmployeesWithoutDepartment();
    List<CompanyStatistics> getCompanyStatistics();
    boolean existsByEmail(String email);
}