package com.techcorp.employee.service;

import com.techcorp.employee.dto.EmployeeDTO;
import com.techcorp.employee.exception.InvalidDataException;
import com.techcorp.employee.model.Department;
import com.techcorp.employee.model.Employee;
import com.techcorp.employee.model.Position;
import com.techcorp.employee.model.EmploymentStatus;
import com.techcorp.employee.repository.DepartmentRepository;
import jakarta.validation.Valid;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Service
@Validated
public class EmployeeFormService {

    private final EmployeeService employeeService;
    private final DepartmentRepository departmentRepository; // ✅ DODAJ

    // ✅ DODAJ DepartmentRepository DO KONSTRUKTORA
    public EmployeeFormService(EmployeeService employeeService, DepartmentRepository departmentRepository) {
        this.employeeService = employeeService;
        this.departmentRepository = departmentRepository; // ✅ DODAJ
    }

    public EmployeeFormData getFormData() {
        return new EmployeeFormData(
                Arrays.asList(Position.values()),
                Arrays.asList(EmploymentStatus.values())
        );
    }

    public Employee convertToEntity(
            @Valid EmployeeDTO employeeDTO) throws InvalidDataException {        String fullName = employeeDTO.getFirstName() + " " + employeeDTO.getLastName();
        Employee employee = new Employee(
                fullName,
                employeeDTO.getEmail(),
                employeeDTO.getCompany(),
                employeeDTO.getPosition(),
                employeeDTO.getSalary(),
                employeeDTO.getStatus()
        );

        // ✅ TERAZ departmentRepository JEST DOSTĘPNE
        if (employeeDTO.getDepartmentId() != null) {
            Department department = departmentRepository.findById(employeeDTO.getDepartmentId())
                    .orElseThrow(() -> new IllegalArgumentException("Department not found with ID: " + employeeDTO.getDepartmentId()));
            employee.setDepartment(department);
        }

        return employee;
    }

    public EmployeeDTO convertToDTO(Employee employee) {
        EmployeeDTO dto = new EmployeeDTO();
        String[] nameParts = employee.getName().split(" ", 2);
        dto.setFirstName(nameParts[0]);
        dto.setLastName(nameParts.length > 1 ? nameParts[1] : "");
        dto.setEmail(employee.getEmail());
        dto.setCompany(employee.getCompany());
        dto.setPosition(employee.getPosition());
        dto.setSalary(employee.getSalary());
        dto.setStatus(employee.getStatus());

        // ✅ POPRAWIONE: Sprawdź czy department nie jest null
        if (employee.getDepartment() != null) {
            dto.setDepartmentId(employee.getDepartment().getId());
        }

        return dto;
    }

    public FormValidationResult validateEmployee(
            @Valid EmployeeDTO employeeDTO) {
        FormValidationResult result = new FormValidationResult();

        if (employeeDTO.getSalary() == null || employeeDTO.getSalary() <= 0) {
            result.addError("salary", "Wynagrodzenie musi być większe niż 0");
        }

        return result;
    }

    // Klasy pomocnicze DTO
    public static class EmployeeFormData {
        private final List<Position> positions;
        private final List<EmploymentStatus> statuses;

        public EmployeeFormData(List<Position> positions, List<EmploymentStatus> statuses) {
            this.positions = positions;
            this.statuses = statuses;
        }

        // getters
        public List<Position> getPositions() { return positions; }
        public List<EmploymentStatus> getStatuses() { return statuses; }
    }

    public static class FormValidationResult {
        private boolean valid = true;
        private String field;
        private String message;

        public void addError(String field, String message) {
            this.valid = false;
            this.field = field;
            this.message = message;
        }

        // getters
        public boolean isValid() { return valid; }
        public String getField() { return field; }
        public String getMessage() { return message; }
    }
}