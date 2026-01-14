package com.techcorp.employee.service;

import com.techcorp.employee.model.Employee;
import com.techcorp.employee.repository.EmployeeViewRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional(readOnly = true)
public class EmployeeViewService {

    @Autowired
    private EmployeeViewRepository employeeViewRepository;

    public List<Employee> findAll() {
        return employeeViewRepository.findAll();
    }

    public Optional<Employee> findById(Long id) {
        return employeeViewRepository.findById(id);
    }

    public List<Employee> findByCompany(String company) {
        return employeeViewRepository.findByCompany(company);
    }

    public Page<Employee> findAll(Pageable pageable) {
        List<Employee> allEmployees = employeeViewRepository.findAll();

        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), allEmployees.size());

        if (start > allEmployees.size()) {
            return Page.empty();
        }

        List<Employee> pageContent = allEmployees.subList(start, end);
        return new PageImpl<>(pageContent, pageable, allEmployees.size());
    }


    public long count() {
        return employeeViewRepository.findAll().size();
    }
}