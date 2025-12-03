//package com.techcorp.employee.controller;
//
//import com.techcorp.employee.dto.EmployeeDTO;
//import com.techcorp.employee.exception.*;
//import com.techcorp.employee.model.Employee;
//import com.techcorp.employee.model.EmploymentStatus;
//import com.techcorp.employee.model.Position;
//import com.techcorp.employee.service.EmployeeService;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.http.ResponseEntity;
//import org.springframework.web.bind.annotation.*;
//import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
//
//import java.net.URI;
//import java.util.List;
//import java.util.stream.Collectors;
//
//@RestController
//@RequestMapping("/api/employees")
//public class EmployeeController {
//
//    @Autowired
//    private EmployeeService employeeService;
//
//    // Mapowanie Employee na EmployeeDTO
//    private EmployeeDTO mapToDTO(Employee employee) {
//        return new EmployeeDTO(
//                employee.getFirstName(),
//                employee.getLastName(),
//                employee.getEmail(),
//                employee.getCompany(),
//                employee.getPosition(),
//                employee.getSalary(),
//                employee.getStatus()
//        );
//    }
//
//    // Mapowanie EmployeeDTO na Employee
//    private Employee mapToEntity(EmployeeDTO employeeDTO) throws InvalidDataException {
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
//    // GET wszystkich pracowników z opcjonalnym filtrem po firmie
//    @GetMapping
//    public ResponseEntity<List<EmployeeDTO>> getAllEmployees(
//            @RequestParam(required = false) String company) {
//
//        List<Employee> employees;
//        if (company != null && !company.trim().isEmpty()) {
//            employees = employeeService.getEmployeesByCompany(company);
//        } else {
//            employees = employeeService.getAllEmployees();
//        }
//
//        List<EmployeeDTO> employeeDTOs = employees.stream()
//                .map(this::mapToDTO)
//                .collect(Collectors.toList());
//
//        return ResponseEntity.ok(employeeDTOs);
//    }
//
//    // GET pracownika po emailu
//    @GetMapping("/{email}")
//    public ResponseEntity<EmployeeDTO> getEmployeeByEmail(@PathVariable String email) {
//        Employee employee = employeeService.findEmployeeByEmail(email)
//                .orElseThrow(() -> new EmployeeNotFoundException("Employee not found with email: " + email));
//
//        return ResponseEntity.ok(mapToDTO(employee));
//    }
//
//    // POST - tworzenie nowego pracownika
//    @PostMapping
//    public ResponseEntity<EmployeeDTO> createEmployee(@RequestBody EmployeeDTO employeeDTO) {
//        try {
//            Employee employee = mapToEntity(employeeDTO);
//            employeeService.addEmployee(employee);
//
//            URI location = ServletUriComponentsBuilder
//                    .fromCurrentRequest()
//                    .path("/{email}")
//                    .buildAndExpand(employee.getEmail())
//                    .toUri();
//
//            return ResponseEntity.created(location).body(mapToDTO(employee));
//        } catch (DuplicateEmailException e) {
//            throw new DuplicateEmailException("Employee with email " + employeeDTO.getEmail() + " already exists");
//        } catch (InvalidDataException e) {
//            throw new IllegalArgumentException(e.getMessage());
//        }
//    }
//
//    @PutMapping("/{email}")
//    public ResponseEntity<EmployeeDTO> updateEmployee(@PathVariable String email,
//                                                      @RequestBody EmployeeDTO employeeDTO) {
//        try {
//            if (!email.equalsIgnoreCase(employeeDTO.getEmail())) {
//                throw new IllegalArgumentException("Email in path must match email in request body");
//            }
//
//            Employee existingEmployee = employeeService.findEmployeeByEmail(email)
//                    .orElseThrow(() -> new EmployeeNotFoundException("Employee not found with email: " + email));
//
//            String fullName = buildFullName(employeeDTO.getFirstName(), employeeDTO.getLastName());
//
//            validateUpdateData(employeeDTO, fullName);
//
//            existingEmployee.setName(fullName);
//            existingEmployee.setCompany(employeeDTO.getCompany());
//            existingEmployee.setPosition(employeeDTO.getPosition());
//            existingEmployee.setSalary(employeeDTO.getSalary());
//            existingEmployee.setStatus(employeeDTO.getStatus());
//
//            Employee updatedEmployee = employeeService.updateEmployee(existingEmployee);
//
//            return ResponseEntity.ok(mapToDTO(updatedEmployee));
//
//        } catch (IllegalArgumentException e) {
//            throw new IllegalArgumentException(e.getMessage());
//        }
//    }
//
//    // Metody pomocnicze
//    private String buildFullName(String firstName, String lastName) {
//        if (firstName == null) firstName = "";
//        if (lastName == null) lastName = "";
//        return (firstName + " " + lastName).trim();
//    }
//
//    private void validateUpdateData(EmployeeDTO dto, String fullName) {
//        if (fullName.trim().isEmpty()) {
//            throw new IllegalArgumentException("Name cannot be empty");
//        }
//        if (dto.getCompany() == null || dto.getCompany().trim().isEmpty()) {
//            throw new IllegalArgumentException("Company cannot be empty");
//        }
//        if (dto.getPosition() == null) {
//            throw new IllegalArgumentException("Position cannot be null");
//        }
//        if (dto.getSalary() < 0) {
//            throw new IllegalArgumentException("Salary cannot be negative");
//        }
//        if (dto.getStatus() == null) {
//            throw new IllegalArgumentException("Employment status cannot be null");
//        }
//    }
//
//    // DELETE - usuwanie pracownika
//    @DeleteMapping("/{email}")
//    public ResponseEntity<Void> deleteEmployee(@PathVariable String email) {
//        boolean deleted = employeeService.removeEmployee(email);
//        if (!deleted) {
//            throw new EmployeeNotFoundException("Employee not found with email: " + email);
//        }
//        return ResponseEntity.noContent().build();
//    }
//
//    // PATCH - zmiana statusu zatrudnienia
//    @PatchMapping("/{email}/status")
//    public ResponseEntity<EmployeeDTO> updateEmployeeStatus(@PathVariable String email,
//                                                            @RequestBody EmploymentStatusUpdateRequest statusRequest) {
//        Employee updatedEmployee = employeeService.updateEmployeeStatus(email, statusRequest.getStatus());
//        return ResponseEntity.ok(mapToDTO(updatedEmployee));
//    }
//
//    // GET - pracownicy według statusu
//    @GetMapping("/status/{status}")
//    public ResponseEntity<List<EmployeeDTO>> getEmployeesByStatus(@PathVariable EmploymentStatus status) {
//        List<Employee> employees = employeeService.getEmployeesByStatus(status);
//        List<EmployeeDTO> employeeDTOs = employees.stream()
//                .map(this::mapToDTO)
//                .collect(Collectors.toList());
//
//        return ResponseEntity.ok(employeeDTOs);
//    }
//
//    // Klasa pomocnicza do aktualizacji statusu
//    public static class EmploymentStatusUpdateRequest {
//        private EmploymentStatus status;
//
//        public EmploymentStatus getStatus() { return status; }
//        public void setStatus(EmploymentStatus status) { this.status = status; }
//    }
//}







package com.techcorp.employee.controller;

import com.techcorp.employee.dto.CompanyStatisticsDTO;
import com.techcorp.employee.dto.EmployeeDTO;
import com.techcorp.employee.dto.EmployeeListView;
import com.techcorp.employee.exception.*;
import com.techcorp.employee.model.CompanyStatistics;
import com.techcorp.employee.model.Employee;
import com.techcorp.employee.model.EmploymentStatus;
import com.techcorp.employee.model.Position;
import com.techcorp.employee.service.EmployeeService;
import com.techcorp.employee.specification.EmployeeSpecification;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
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
                employee.getStatus());
    }

    // GET wszystkich pracowników z PAGINACJĄ i OPTYMALIZACJĄ
//    @GetMapping
//    public ResponseEntity<Page<EmployeeListView>> getAllEmployees(
//            @PageableDefault(size = 20, sort = "name") Pageable pageable) {
//
//        Page<EmployeeListView> employees = employeeService.getAllEmployeesSummary(pageable);
//        return ResponseEntity.ok(employees);
//    }

    // GET z ZAAWANSOWANYM WYSZUKIWANIEM i PAGINACJĄ
    @GetMapping("/search")
    public ResponseEntity<Page<EmployeeListView>> searchEmployees(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String company,
            @RequestParam(required = false) Position position,
            @RequestParam(required = false) EmploymentStatus status,
            @RequestParam(required = false) Double minSalary,
            @RequestParam(required = false) Double maxSalary,
            @RequestParam(required = false) String departmentName,
            @PageableDefault(size = 20, sort = "name") Pageable pageable) {

        Page<EmployeeListView> employees = employeeService.searchEmployeesWithFilters(
                name, company, position, status, minSalary, maxSalary, departmentName, pageable);
        return ResponseEntity.ok(employees);
    }

//    // GET z OPTYMALIZOWANYM WYSZUKIWANIEM (Projekcja)
//    @GetMapping("/search/optimized")
//    public ResponseEntity<Page<EmployeeListView>> searchEmployeesOptimized(
//            @RequestParam(required = false) String name,
//            @RequestParam(required = false) String company,
//            @RequestParam(required = false) String position,
//            @RequestParam(required = false) Double minSalary,
//            @RequestParam(required = false) Double maxSalary,
//            @PageableDefault(size = 20, sort = "name") Pageable pageable) {
//
//        Page<EmployeeListView> employees = employeeService.findEmployeesWithFiltersOptimized(
//                name, company, position, minSalary, maxSalary, pageable
//        );
//        return ResponseEntity.ok(employees);
//    }

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
            Employee savedEmployee = employeeService.saveEmployee(employee);

            URI location = ServletUriComponentsBuilder
                    .fromCurrentRequest()
                    .path("/{email}")
                    .buildAndExpand(savedEmployee.getEmail())
                    .toUri();

            return ResponseEntity.created(location).body(mapToDTO(savedEmployee));
        } catch (DuplicateEmailException e) {
            throw new DuplicateEmailException("Employee with email " + employeeDTO.getEmail() + " already exists");
        } catch (InvalidDataException e) {
            throw new IllegalArgumentException(e.getMessage());
        }
    }

    // Mapowanie EmployeeDTO na Employee (zaktualizowane)
    private Employee mapToEntity(EmployeeDTO employeeDTO) throws InvalidDataException {
        String fullName = employeeDTO.getFirstName() + " " + employeeDTO.getLastName();
        Employee employee = new Employee(
                fullName,
                employeeDTO.getEmail(),
                employeeDTO.getCompany(),
                employeeDTO.getPosition(),
                employeeDTO.getSalary(),
                employeeDTO.getStatus()
        );

        // Jeśli potrzebujesz ustawić department, zrób to tutaj poprzez service
        return employee;
    }

    // PUT - aktualizacja pracownika
    @PutMapping("/{email}")
    public ResponseEntity<EmployeeDTO> updateEmployee(@PathVariable String email,
                                                      @RequestBody EmployeeDTO employeeDTO) {
        try {
            if (!email.equalsIgnoreCase(employeeDTO.getEmail())) {
                throw new IllegalArgumentException("Email in path must match email in request body");
            }

            Employee existingEmployee = employeeService.findEmployeeByEmail(email)
                    .orElseThrow(() -> new EmployeeNotFoundException("Employee not found with email: " + email));

            // Aktualizacja pól
            updateEmployeeFromDTO(existingEmployee, employeeDTO);

            Employee updatedEmployee = employeeService.saveEmployee(existingEmployee);

            return ResponseEntity.ok(mapToDTO(updatedEmployee));

        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException(e.getMessage());
        } catch (InvalidDataException e) {
            throw new InvalidDataException("Error");
        }
    }

    private void updateEmployeeFromDTO(Employee employee, EmployeeDTO dto) {
        String fullName = buildFullName(dto.getFirstName(), dto.getLastName());

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

        employee.setName(fullName);
        employee.setCompany(dto.getCompany());
        employee.setPosition(dto.getPosition());
        employee.setSalary(dto.getSalary());
        employee.setStatus(dto.getStatus());
    }

    private String buildFullName(String firstName, String lastName) {
        if (firstName == null) firstName = "";
        if (lastName == null) lastName = "";
        return (firstName + " " + lastName).trim();
    }

    // DELETE - usuwanie pracownika
    @DeleteMapping("/{email}")
    public ResponseEntity<Void> deleteEmployee(@PathVariable String email) {
        employeeService.deleteEmployeeByEmail(email);
        return ResponseEntity.noContent().build();
    }

    // PATCH - zmiana statusu zatrudnienia
    @PatchMapping("/{email}/status")
    public ResponseEntity<EmployeeDTO> updateEmployeeStatus(@PathVariable String email,
                                                            @RequestBody EmploymentStatusUpdateRequest statusRequest) {
        Employee updatedEmployee = employeeService.updateEmployeeStatus(email, statusRequest.getStatus());
        return ResponseEntity.ok(mapToDTO(updatedEmployee));
    }

    // GET - pracownicy według statusu z PAGINACJĄ
    @GetMapping("/status/{status}")
    public ResponseEntity<Page<EmployeeListView>> getEmployeesByStatus(
            @PathVariable EmploymentStatus status,
            @PageableDefault(size = 20) Pageable pageable) {

        Page<EmployeeListView> employees = employeeService.getEmployeesByStatusProjection(status, pageable);
        return ResponseEntity.ok(employees);
    }


    // GET - pracownicy według firmy z PAGINACJĄ
    @GetMapping("/company/{company}")
    public ResponseEntity<Page<EmployeeListView>> getEmployeesByCompany(
            @PathVariable String company,
            @PageableDefault(size = 20) Pageable pageable) {

        Page<EmployeeListView> employees = employeeService.getEmployeesByCompanyProjection(company, pageable);
        return ResponseEntity.ok(employees);
    }

    // GET - statystyki firmy
    @GetMapping("/statistics/company")
    public ResponseEntity<List<CompanyStatisticsDTO>> getCompanyStatistics() {
        List<CompanyStatisticsDTO> statistics = employeeService.getAllCompanyStatisticsDTO();
        return ResponseEntity.ok(statistics);
    }

    // Klasa pomocnicza do aktualizacji statusu
    public static class EmploymentStatusUpdateRequest {
        private EmploymentStatus status;

        public EmploymentStatus getStatus() { return status; }
        public void setStatus(EmploymentStatus status) { this.status = status; }
    }
}