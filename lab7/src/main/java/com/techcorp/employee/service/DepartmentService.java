package com.techcorp.employee.service;

import com.techcorp.employee.model.Department;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class DepartmentService {
    private final Map<Long, Department> departments = new HashMap<>();
    private final AtomicLong idCounter = new AtomicLong(1);

    public List<Department> getAllDepartments() {
        return new ArrayList<>(departments.values());
    }

    public Optional<Department> getDepartmentById(Long id) {
        return Optional.ofNullable(departments.get(id));
    }

    public Department createDepartment(Department department) {
        Long newId = idCounter.getAndIncrement();
        department.setId(newId);
        departments.put(newId, department);
        return department;
    }

    public Department updateDepartment(Long id, Department department) {
        if (departments.containsKey(id)) {
            department.setId(id);
            departments.put(id, department);
            return department;
        }
        return null;
    }

    public boolean deleteDepartment(Long id) {
        return departments.remove(id) != null;
    }

    public List<Department> getDepartmentsByManager(String managerEmail) {
        return departments.values().stream()
                .filter(dept -> managerEmail.equals(dept.getManagerEmail()))
                .toList();
    }
}