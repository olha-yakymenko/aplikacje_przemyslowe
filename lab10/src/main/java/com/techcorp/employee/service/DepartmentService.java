package com.techcorp.employee.service;

import com.techcorp.employee.dto.DepartmentDTO;
import com.techcorp.employee.model.Department;
import com.techcorp.employee.model.Employee;
import com.techcorp.employee.repository.DepartmentRepository;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
@Validated
public class DepartmentService {

    private final DepartmentRepository departmentRepository;
    private final EmployeeService employeeService;

    public DepartmentService(DepartmentRepository departmentRepository,
                             EmployeeService employeeService) {
        this.departmentRepository = departmentRepository;
        this.employeeService = employeeService;
    }

    public List<Department> getAllDepartments() {
        return departmentRepository.findAll();
    }

    public int getDepartmentCount() {
        return (int) departmentRepository.count();
    }

    public Optional<Department> getDepartmentById(Long id) {
        return departmentRepository.findById(id);
    }

    public Department createDepartment(@Valid Department department) {
        if (departmentRepository.existsByName(department.getName())) {
            throw new IllegalArgumentException(
                    "Department with name '" + department.getName() + "' already exists");
        }
        return departmentRepository.save(department);
    }

    public Department updateDepartment(
            @Min(1) Long id,
            @Valid Department department) {        // Sprawdź czy departament istnieje
        Department existing = departmentRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Department not found with id: " + id));

        // Sprawdź czy nowa nazwa nie koliduje z innym departamentem
        if (!existing.getName().equals(department.getName()) &&
                departmentRepository.existsByName(department.getName())) {
            throw new IllegalArgumentException(
                    "Department with name '" + department.getName() + "' already exists");
        }

        // Aktualizuj pola
        existing.setName(department.getName());
        existing.setLocation(department.getLocation());
        existing.setDescription(department.getDescription());
        existing.setManagerEmail(department.getManagerEmail());
        existing.setBudget(department.getBudget());

        return departmentRepository.save(existing);
    }

    public void deleteDepartment(@Min(1) Long id) {
        // Sprawdź czy departament istnieje
        Department department = departmentRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Department not found with id: " + id));

        // Znajdź pracowników w tym departamencie
        List<Employee> employeesInDepartment = employeeService.getEmployeesByDepartmentId(id);

        // Usuń departament z pracowników
        for (Employee employee : employeesInDepartment) {
            employee.setDepartment(null);
            employeeService.saveEmployee(employee);
        }

        // Usuń departament
        departmentRepository.delete(department);
    }

    public List<Department> getDepartmentsByManager(String managerEmail) {
        return departmentRepository.findByLocation(managerEmail); // UWAGA: to jest find by location, nie manager!
        // Potrzebujesz metody w repository: List<Department> findByManagerEmail(String managerEmail)
    }

    public DepartmentDTO getDepartmentDetails(@Min(1) Long id) {
        // Użyj metody z repozytorium która pobiera departament z pracownikami
        Optional<Department> departmentOpt = departmentRepository.findByIdWithEmployees(id);

        if (departmentOpt.isEmpty()) {
            return null;
        }

        Department department = departmentOpt.get();
        List<Employee> departmentEmployees = department.getEmployees();

        Optional<Employee> manager = Optional.empty();
        if (department.getManagerEmail() != null && !department.getManagerEmail().isEmpty()) {
            manager = employeeService.findEmployeeByEmail(department.getManagerEmail());
        }

        return new DepartmentDTO(department, departmentEmployees, manager);
    }

    @Transactional
    public void assignEmployeeToDepartment(
            @NotBlank(message = "Employee email cannot be blank") String employeeEmail,
            @Min(1) Long departmentId) {
        // Znajdź departament
        Department department = departmentRepository.findById(departmentId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Department not found with id: " + departmentId));

        // Znajdź pracownika przez EmployeeService
        Employee employee = employeeService.findEmployeeByEmail(employeeEmail)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Employee not found with email: " + employeeEmail));

        // Przypisz pracownika do departamentu
        employee.setDepartment(department);
        employeeService.saveEmployee(employee);
    }

    public void removeEmployeeFromDepartment(
            @NotBlank(message = "Employee email cannot be blank") String employeeEmail) {        Employee employee = employeeService.findEmployeeByEmail(employeeEmail)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Employee not found with email: " + employeeEmail));

        employee.setDepartment(null);
        employeeService.saveEmployee(employee);
    }

    public List<Employee> getEmployeesWithoutDepartment() {
        return employeeService.getEmployeesWithoutDepartment();
    }

    public List<String> getAllDepartmentNames() {
        return departmentRepository.findAllDepartmentNames();
    }
}