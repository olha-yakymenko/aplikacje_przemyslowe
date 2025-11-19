//package com.techcorp.employee.service;
//
//import com.techcorp.employee.dto.DepartmentDetails;
//import com.techcorp.employee.model.Department;
//import org.springframework.stereotype.Service;
//
//import java.util.*;
//import java.util.concurrent.atomic.AtomicLong;
//
//@Service
//public class DepartmentService {
//    private final Map<Long, Department> departments = new HashMap<>();
//    private final AtomicLong idCounter = new AtomicLong(1);
//
//    public List<Department> getAllDepartments() {
//        return new ArrayList<>(departments.values());
//    }
//
//    public Optional<Department> getDepartmentById(Long id) {
//        return Optional.ofNullable(departments.get(id));
//    }
//
//    public Department createDepartment(Department department) {
//        Long newId = idCounter.getAndIncrement();
//        department.setId(newId);
//        departments.put(newId, department);
//        return department;
//    }
//
//    public Department updateDepartment(Long id, Department department) {
//        if (departments.containsKey(id)) {
//            department.setId(id);
//            departments.put(id, department);
//            return department;
//        }
//        return null;
//    }
//
//    public boolean deleteDepartment(Long id) {
//        return departments.remove(id) != null;
//    }
//
//    public List<Department> getDepartmentsByManager(String managerEmail) {
//        return departments.values().stream()
//                .filter(dept -> managerEmail.equals(dept.getManagerEmail()))
//                .toList();
//    }
//
//    public DepartmentDetails getDepartmentDetails(Long id) {
//        Optional<Department> department = getDepartmentById(id);
//        if (department.isEmpty()) {
//            return null;
//        }
//
//        List<Employee> departmentEmployees = employeeService.getEmployeesByDepartmentId(id);
//
//        Optional<Employee> manager = Optional.empty();
//        if (department.get().getManagerEmail() != null && !department.get().getManagerEmail().isEmpty()) {
//            manager = employeeService.findEmployeeByEmail(department.get().getManagerEmail());
//        }
//
//        return new DepartmentDetails(department.get(), departmentEmployees, manager);
//}







package com.techcorp.employee.service;

import com.techcorp.employee.dto.DepartmentDTO;
import com.techcorp.employee.model.Department;
import com.techcorp.employee.model.Employee;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class DepartmentService {
    private final Map<Long, Department> departments = new HashMap<>();
    private final AtomicLong idCounter = new AtomicLong(1);
    private final EmployeeService employeeService;

    // Konstruktor powinien być na początku metod
    public DepartmentService(EmployeeService employeeService) {
        this.employeeService = employeeService;
    }

    public List<Department> getAllDepartments() {
        return new ArrayList<>(departments.values());
    }

    public int getDepartmentCount() {
        return getAllDepartments().size();
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

    public DepartmentDTO getDepartmentDetails(Long id) {
        Optional<Department> department = getDepartmentById(id);
        if (department.isEmpty()) {
            return null;
        }

        List<Employee> departmentEmployees = employeeService.getEmployeesByDepartmentId(id);

        Optional<Employee> manager = Optional.empty();
        if (department.get().getManagerEmail() != null && !department.get().getManagerEmail().isEmpty()) {
            manager = employeeService.findEmployeeByEmail(department.get().getManagerEmail());
        }

        return new DepartmentDTO(department.get(), departmentEmployees, manager);
    }

}