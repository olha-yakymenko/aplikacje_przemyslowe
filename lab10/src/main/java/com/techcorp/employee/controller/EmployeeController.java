package com.techcorp.employee.controller;

import com.techcorp.employee.dto.CompanyStatisticsDTO;
import com.techcorp.employee.dto.EmployeeDTO;
import com.techcorp.employee.dto.EmployeeListView;
import com.techcorp.employee.exception.EmployeeNotFoundException;
import com.techcorp.employee.model.Employee;
import com.techcorp.employee.model.EmploymentStatus;
import com.techcorp.employee.service.EmployeeService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/employees")
@Validated
public class EmployeeController {

    @Autowired
    private EmployeeService employeeService;

    private EmployeeDTO mapToDTO(Employee employee) {
        return new EmployeeDTO(
                employee.getFirstName(),
                employee.getLastName(),
                employee.getEmail(),
                employee.getCompany(),
                employee.getPosition(),
                employee.getSalary(),
                employee.getStatus());
    }

    @GetMapping("/{email}")
    public ResponseEntity<EmployeeDTO> getEmployeeByEmail(
            @PathVariable @NotBlank(message = "Email cannot be blank") String email) {
        Employee employee = employeeService.findEmployeeByEmail(email)
                .orElseThrow(() -> new EmployeeNotFoundException("Employee not found with email: " + email));
        return ResponseEntity.ok(mapToDTO(employee));
    }

    @PostMapping
    public ResponseEntity<EmployeeDTO> createEmployee(@Valid @RequestBody EmployeeDTO employeeDTO) {
        Employee employee = mapToEntity(employeeDTO);
        Employee savedEmployee = employeeService.saveEmployee(employee);

        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{email}")
                .buildAndExpand(savedEmployee.getEmail())
                .toUri();

        return ResponseEntity.created(location).body(mapToDTO(savedEmployee));
    }

    private Employee mapToEntity(EmployeeDTO employeeDTO) {
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

    @PutMapping("/{email}")
    public ResponseEntity<EmployeeDTO> updateEmployee(
            @PathVariable @NotBlank(message = "Email cannot be blank") String email,
            @Valid @RequestBody EmployeeDTO employeeDTO) {

        validateEmailMatch(email, employeeDTO.getEmail());

        Employee existingEmployee = employeeService.findEmployeeByEmail(email)
                .orElseThrow(() -> new EmployeeNotFoundException("Employee not found with email: " + email));

        updateEmployeeFromDTO(existingEmployee, employeeDTO);
        Employee updatedEmployee = employeeService.saveEmployee(existingEmployee);

        return ResponseEntity.ok(mapToDTO(updatedEmployee));
    }

    private void validateEmailMatch(String pathEmail, String bodyEmail) {
        if (!pathEmail.equalsIgnoreCase(bodyEmail)) {
            throw new IllegalArgumentException("Email in path must match email in request body");
        }
    }

    private void updateEmployeeFromDTO(Employee employee, EmployeeDTO dto) {
        String fullName = buildFullName(dto.getFirstName(), dto.getLastName());
        employee.setName(fullName);
        employee.setCompany(dto.getCompany());
        employee.setPosition(dto.getPosition());
        employee.setSalary(dto.getSalary());
        employee.setStatus(dto.getStatus());
    }

    private String buildFullName(String firstName, String lastName) {
        return (firstName + " " + lastName).trim();
    }

    @DeleteMapping("/{email}")
    public ResponseEntity<Void> deleteEmployee(
            @PathVariable @NotBlank(message = "Email cannot be blank") String email) {
        employeeService.deleteEmployeeByEmail(email);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{email}/status")
    public ResponseEntity<EmployeeDTO> updateEmployeeStatus(
            @PathVariable @NotBlank(message = "Email cannot be blank") String email,
            @Valid @RequestBody EmploymentStatusUpdateRequest statusRequest) {
        Employee updatedEmployee = employeeService.updateEmployeeStatus(email, statusRequest.getStatus());
        return ResponseEntity.ok(mapToDTO(updatedEmployee));
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<Page<EmployeeListView>> getEmployeesByStatus(
            @PathVariable EmploymentStatus status,
            @PageableDefault(size = 20) Pageable pageable) {
        Page<EmployeeListView> employees = employeeService.getEmployeesByStatusProjection(status, pageable);
        return ResponseEntity.ok(employees);
    }

    @GetMapping("/company/{company}")
    public ResponseEntity<Page<EmployeeListView>> getEmployeesByCompany(
            @PathVariable @NotBlank(message = "Company name cannot be blank") String company,
            @PageableDefault(size = 20) Pageable pageable) {
        Page<EmployeeListView> employees = employeeService.getEmployeesByCompanyProjection(company, pageable);
        return ResponseEntity.ok(employees);
    }

    @GetMapping("/statistics/company")
    public ResponseEntity<List<CompanyStatisticsDTO>> getCompanyStatistics() {
        List<CompanyStatisticsDTO> statistics = employeeService.getAllCompanyStatisticsDTO();
        return ResponseEntity.ok(statistics);
    }

    public static class EmploymentStatusUpdateRequest {
        @NotNull(message = "Status cannot be null")
        private EmploymentStatus status;

        public EmploymentStatus getStatus() { return status; }
        public void setStatus(EmploymentStatus status) { this.status = status; }
    }
}


//
//
//
//package com.techcorp.employee.controller;
//
//import com.techcorp.employee.dto.CompanyStatisticsDTO;
//import com.techcorp.employee.dto.EmployeeDTO;
//import com.techcorp.employee.dto.EmployeeListView;
//import com.techcorp.employee.exception.*;
//import com.techcorp.employee.model.Employee;
//import com.techcorp.employee.model.EmploymentStatus;
//import com.techcorp.employee.service.EmployeeService;
//import jakarta.validation.Valid;
//import jakarta.validation.constraints.NotBlank;
//import jakarta.validation.constraints.NotNull;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.data.domain.Page;
//import org.springframework.data.domain.Pageable;
//import org.springframework.data.web.PageableDefault;
//import org.springframework.http.ResponseEntity;
//import org.springframework.validation.annotation.Validated;
//import org.springframework.web.bind.annotation.*;
//import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
//
//import java.net.URI;
//import java.util.List;
//
//@RestController
//@RequestMapping("/api/employees")
//@Validated
//public class EmployeeController {
//
//    @Autowired
//    private EmployeeService employeeService;
//
//    private EmployeeDTO mapToDTO(Employee employee) {
//        return new EmployeeDTO(
//                employee.getFirstName(),
//                employee.getLastName(),
//                employee.getEmail(),
//                employee.getCompany(),
//                employee.getPosition(),
//                employee.getSalary(),
//                employee.getStatus());
//    }
//
//    // GET pracownika po emailu
//    @GetMapping("/{email}")
//    public ResponseEntity<EmployeeDTO> getEmployeeByEmail(
//            @PathVariable @NotBlank(message = "Email cannot be blank") String email) {
//
//        Employee employee = employeeService.findEmployeeByEmail(email)
//                .orElseThrow(() -> new EmployeeNotFoundException("Employee not found with email: " + email));
//
//        return ResponseEntity.ok(mapToDTO(employee));
//    }
//
//    @PostMapping
//    public ResponseEntity<EmployeeDTO> createEmployee(@Valid @RequestBody EmployeeDTO employeeDTO) {
//        try {
//            Employee employee = mapToEntity(employeeDTO);
//            Employee savedEmployee = employeeService.saveEmployee(employee);
//
//            URI location = ServletUriComponentsBuilder
//                    .fromCurrentRequest()
//                    .path("/{email}")
//                    .buildAndExpand(savedEmployee.getEmail())
//                    .toUri();
//
//            return ResponseEntity.created(location).body(mapToDTO(savedEmployee));
//        } catch (DuplicateEmailException e) {
//            throw new DuplicateEmailException("Employee with email " + employeeDTO.getEmail() + " already exists");
//        }
//    }
//
//    private Employee mapToEntity(EmployeeDTO employeeDTO) {
//        String fullName = employeeDTO.getFirstName() + " " + employeeDTO.getLastName();
//        return new Employee(
//                fullName,
//                employeeDTO.getEmail(),
//                employeeDTO.getCompany(),
//                employeeDTO.getPosition(),
//                employeeDTO.getSalary(),
//                employeeDTO.getStatus()
//        );
//    }
//
//    @PutMapping("/{email}")
//    public ResponseEntity<EmployeeDTO> updateEmployee(
//            @PathVariable @NotBlank(message = "Email cannot be blank") String email,
//            @Valid @RequestBody EmployeeDTO employeeDTO) {
//
//        if (!email.equalsIgnoreCase(employeeDTO.getEmail())) {
//            throw new IllegalArgumentException("Email in path must match email in request body");
//        }
//
//        Employee existingEmployee = employeeService.findEmployeeByEmail(email)
//                .orElseThrow(() -> new EmployeeNotFoundException("Employee not found with email: " + email));
//
//        // Walidacja DTO już wykonana przez @Valid, więc można bezpiecznie aktualizować
//        updateEmployeeFromDTO(existingEmployee, employeeDTO);
//        Employee updatedEmployee = employeeService.saveEmployee(existingEmployee);
//
//        return ResponseEntity.ok(mapToDTO(updatedEmployee));
//    }
//
//    private void updateEmployeeFromDTO(Employee employee, EmployeeDTO dto) {
//        String fullName = buildFullName(dto.getFirstName(), dto.getLastName());
//        employee.setName(fullName);
//        employee.setCompany(dto.getCompany());
//        employee.setPosition(dto.getPosition());
//        employee.setSalary(dto.getSalary());
//        employee.setStatus(dto.getStatus());
//    }
//
//    private String buildFullName(String firstName, String lastName) {
//        return (firstName + " " + lastName).trim();
//    }
//
//    @DeleteMapping("/{email}")
//    public ResponseEntity<Void> deleteEmployee(
//            @PathVariable @NotBlank(message = "Email cannot be blank") String email) {
//        employeeService.deleteEmployeeByEmail(email);
//        return ResponseEntity.noContent().build();
//    }
//
//    @PatchMapping("/{email}/status")
//    public ResponseEntity<EmployeeDTO> updateEmployeeStatus(
//            @PathVariable @NotBlank(message = "Email cannot be blank") String email,
//            @Valid @RequestBody EmploymentStatusUpdateRequest statusRequest) {
//
//        Employee updatedEmployee = employeeService.updateEmployeeStatus(email, statusRequest.getStatus());
//        return ResponseEntity.ok(mapToDTO(updatedEmployee));
//    }
//
//    @GetMapping("/status/{status}")
//    public ResponseEntity<Page<EmployeeListView>> getEmployeesByStatus(
//            @PathVariable EmploymentStatus status,
//            @PageableDefault(size = 20) Pageable pageable) {
//
//        Page<EmployeeListView> employees = employeeService.getEmployeesByStatusProjection(status, pageable);
//        return ResponseEntity.ok(employees);
//    }
//
//    @GetMapping("/company/{company}")
//    public ResponseEntity<Page<EmployeeListView>> getEmployeesByCompany(
//            @PathVariable @NotBlank(message = "Company name cannot be blank") String company,
//            @PageableDefault(size = 20) Pageable pageable) {
//
//        Page<EmployeeListView> employees = employeeService.getEmployeesByCompanyProjection(company, pageable);
//        return ResponseEntity.ok(employees);
//    }
//
//    @GetMapping("/statistics/company")
//    public ResponseEntity<List<CompanyStatisticsDTO>> getCompanyStatistics() {
//        List<CompanyStatisticsDTO> statistics = employeeService.getAllCompanyStatisticsDTO();
//        return ResponseEntity.ok(statistics);
//    }
//
//    // Klasa pomocnicza z adnotacjami walidacyjnymi
//    public static class EmploymentStatusUpdateRequest {
//        @NotNull(message = "Status cannot be null")
//        private EmploymentStatus status;
//
//        public EmploymentStatus getStatus() { return status; }
//        public void setStatus(EmploymentStatus status) { this.status = status; }
//    }
//}