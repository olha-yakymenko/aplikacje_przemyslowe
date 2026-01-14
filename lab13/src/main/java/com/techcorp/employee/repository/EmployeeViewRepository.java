package com.techcorp.employee.repository;

import com.techcorp.employee.model.Employee;
import org.springframework.data.repository.Repository;

import java.util.List;
import java.util.Optional;

public interface EmployeeViewRepository extends Repository<Employee, Long> {

    List<Employee> findAll();
    Optional<Employee> findById(Long id);

    List<Employee> findByCompany(String company);
}