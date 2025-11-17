package com.techcorp.employee.controller;

import com.techcorp.employee.dto.EmployeeDTO;
import com.techcorp.employee.exception.*;
import com.techcorp.employee.model.Employee;
import com.techcorp.employee.model.EmploymentStatus;
import com.techcorp.employee.model.Position;
import com.techcorp.employee.service.EmployeeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/employees")
public class EmployeeController {

    @Autowired
    private EmployeeService employeeService;

    // Mapowanie Employee na EmployeeDTO
    private EmployeeDTO mapToDTO(Employee employee) {
        return new EmployeeDTO(
                employee.getFirstName(),
                employee.getLastName(),
                employee.getEmail(),
                employee.getCompany(),
                employee.getPosition(),
                employee.getSalary(),
                employee.getStatus()
        );
    }

    // Mapowanie EmployeeDTO na Employee
    private Employee mapToEntity(EmployeeDTO employeeDTO) throws InvalidDataException {
        String fullName = employeeDTO.getFirstName() + " " + employeeDTO.getLastName();
        return new Employee(
                fullName,
                employeeDTO.getEmail(),
                employeeDTO.getCompany(),
                employeeDTO.getPosition(),
                employeeDTO.getSalary(),
                employeeDTO.getStatus()
        );
    }

    // GET wszystkich pracowników z opcjonalnym filtrem po firmie
    @GetMapping
    public ResponseEntity<List<EmployeeDTO>> getAllEmployees(
            @RequestParam(required = false) String company) {

        List<Employee> employees;
        if (company != null && !company.trim().isEmpty()) {
            employees = employeeService.getEmployeesByCompany(company);
        } else {
            employees = employeeService.getAllEmployees();
        }

        List<EmployeeDTO> employeeDTOs = employees.stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());

        return ResponseEntity.ok(employeeDTOs);
    }

    // GET pracownika po emailu
    @GetMapping("/{email}")
    public ResponseEntity<EmployeeDTO> getEmployeeByEmail(@PathVariable String email) {
        Employee employee = employeeService.findEmployeeByEmail(email)
                .orElseThrow(() -> new EmployeeNotFoundException("Employee not found with email: " + email));

        return ResponseEntity.ok(mapToDTO(employee));
    }

    // POST - tworzenie nowego pracownika
    @PostMapping
    public ResponseEntity<EmployeeDTO> createEmployee(@RequestBody EmployeeDTO employeeDTO) {
        try {
            Employee employee = mapToEntity(employeeDTO);
            employeeService.addEmployee(employee);

            URI location = ServletUriComponentsBuilder
                    .fromCurrentRequest()
                    .path("/{email}")
                    .buildAndExpand(employee.getEmail())
                    .toUri();

            return ResponseEntity.created(location).body(mapToDTO(employee));
        } catch (DuplicateEmailException e) {
            throw new DuplicateEmailException("Employee with email " + employeeDTO.getEmail() + " already exists");
        } catch (InvalidDataException e) {
            throw new IllegalArgumentException(e.getMessage());
        }
    }

    @PutMapping("/{email}")
    public ResponseEntity<EmployeeDTO> updateEmployee(@PathVariable String email,
                                                      @RequestBody EmployeeDTO employeeDTO) {
        try {
            if (!email.equalsIgnoreCase(employeeDTO.getEmail())) {
                throw new IllegalArgumentException("Email in path must match email in request body");
            }

            Employee existingEmployee = employeeService.findEmployeeByEmail(email)
                    .orElseThrow(() -> new EmployeeNotFoundException("Employee not found with email: " + email));

            String fullName = buildFullName(employeeDTO.getFirstName(), employeeDTO.getLastName());

            validateUpdateData(employeeDTO, fullName);

            existingEmployee.setName(fullName);
            existingEmployee.setCompany(employeeDTO.getCompany());
            existingEmployee.setPosition(employeeDTO.getPosition());
            existingEmployee.setSalary(employeeDTO.getSalary());
            existingEmployee.setStatus(employeeDTO.getStatus());

            Employee updatedEmployee = employeeService.updateEmployee(existingEmployee);

            return ResponseEntity.ok(mapToDTO(updatedEmployee));

        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException(e.getMessage());
        }
    }

    // Metody pomocnicze
    private String buildFullName(String firstName, String lastName) {
        if (firstName == null) firstName = "";
        if (lastName == null) lastName = "";
        return (firstName + " " + lastName).trim();
    }

    private void validateUpdateData(EmployeeDTO dto, String fullName) {
        if (fullName.trim().isEmpty()) {
            throw new IllegalArgumentException("Name cannot be empty");
        }
        if (dto.getCompany() == null || dto.getCompany().trim().isEmpty()) {
            throw new IllegalArgumentException("Company cannot be empty");
        }
        if (dto.getPosition() == null) {
            throw new IllegalArgumentException("Position cannot be null");
        }
        if (dto.getSalary() < 0) {
            throw new IllegalArgumentException("Salary cannot be negative");
        }
        if (dto.getStatus() == null) {
            throw new IllegalArgumentException("Employment status cannot be null");
        }
    }

    // DELETE - usuwanie pracownika
    @DeleteMapping("/{email}")
    public ResponseEntity<Void> deleteEmployee(@PathVariable String email) {
        boolean deleted = employeeService.removeEmployee(email);
        if (!deleted) {
            throw new EmployeeNotFoundException("Employee not found with email: " + email);
        }
        return ResponseEntity.noContent().build();
    }

    // PATCH - zmiana statusu zatrudnienia
    @PatchMapping("/{email}/status")
    public ResponseEntity<EmployeeDTO> updateEmployeeStatus(@PathVariable String email,
                                                            @RequestBody EmploymentStatusUpdateRequest statusRequest) {
        Employee updatedEmployee = employeeService.updateEmployeeStatus(email, statusRequest.getStatus());
        return ResponseEntity.ok(mapToDTO(updatedEmployee));
    }

    // GET - pracownicy według statusu
    @GetMapping("/status/{status}")
    public ResponseEntity<List<EmployeeDTO>> getEmployeesByStatus(@PathVariable EmploymentStatus status) {
        List<Employee> employees = employeeService.getEmployeesByStatus(status);
        List<EmployeeDTO> employeeDTOs = employees.stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());

        return ResponseEntity.ok(employeeDTOs);
    }

    // Klasa pomocnicza do aktualizacji statusu
    public static class EmploymentStatusUpdateRequest {
        private EmploymentStatus status;

        public EmploymentStatus getStatus() { return status; }
        public void setStatus(EmploymentStatus status) { this.status = status; }
    }
}