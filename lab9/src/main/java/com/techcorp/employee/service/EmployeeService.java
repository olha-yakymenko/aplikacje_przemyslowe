package com.techcorp.employee.service;

import com.techcorp.employee.dto.EmployeeListView;
import com.techcorp.employee.dto.CompanyStatisticsDTO;
import com.techcorp.employee.exception.*;
import com.techcorp.employee.model.*;
import com.techcorp.employee.repository.EmployeeRepository;
import com.techcorp.employee.repository.DepartmentRepository;
import com.techcorp.employee.specification.EmployeeSpecification;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.core.io.UrlResource;

import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class EmployeeService {

    private final EmployeeRepository employeeRepository;
    private final DepartmentRepository departmentRepository;
    private final FileStorageService fileStorageService;

    @Autowired
    public EmployeeService(EmployeeRepository employeeRepository,
                           DepartmentRepository departmentRepository,
                           FileStorageService fileStorageService) {
        this.employeeRepository = employeeRepository;
        this.departmentRepository = departmentRepository;
        this.fileStorageService = fileStorageService;
    }

    // ===== OPERACJE MATEMATYCZNE PRZEZ SQL =====

    public Double calculateAverageSalary() {
        return employeeRepository.findAverageSalary();
    }

    public Double calculateAverageSalaryByCompany(String company) {
        validateCompany(company);
        return employeeRepository.findAverageSalaryByCompany(company);
    }

    public Double findMaxSalary() {
        return employeeRepository.findMaxSalary();
    }

    public Double findMaxSalaryByCompany(String company) {
        validateCompany(company);
        return employeeRepository.findMaxSalaryByCompany(company);
    }

    public Double findMinSalary() {
        return employeeRepository.findMinSalary();
    }

    public Double calculateTotalSalaryCost() {
        return employeeRepository.findTotalSalaryCost();
    }

    public Double calculateTotalSalaryCostByCompany(String company) {
        validateCompany(company);
        return employeeRepository.findTotalSalaryCostByCompany(company);
    }

    public Long getEmployeeCount() {
        return employeeRepository.countAllEmployees();
    }

    public Long getEmployeeCountByCompany(String company) {
        validateCompany(company);
        return employeeRepository.countEmployeesByCompany(company);
    }

    public Long getEmployeeCountByStatus(EmploymentStatus status) {
        return employeeRepository.countEmployeesByStatus(status);
    }

    public Long getEmployeeCountByPosition(Position position) {
        return employeeRepository.countEmployeesByPosition(position);
    }

    public Long getEmployeeCountByDepartment(Long departmentId) {
        return employeeRepository.countEmployeesByDepartment(departmentId);
    }

    public Long getEmployeeCountWithoutDepartment() {
        return employeeRepository.countEmployeesWithoutDepartment();
    }

    // ===== STATYSTYKI FIRM =====


    public List<CompanyStatisticsDTO> getAllCompanyStatisticsDTO() {
        return employeeRepository.getCompanyStatisticsDTO();
    }


    public Optional<CompanyStatisticsDTO> getCompanyStatisticsDTO(String company) {
        validateCompany(company);
        return employeeRepository.getCompanyStatisticsDTO(company);
    }

    public CompanyStatistics getCompanyStatistics(String company) {
        validateCompany(company);
        Optional<CompanyStatisticsDTO> dtoOpt = employeeRepository.getCompanyStatisticsDTO(company);

        if (dtoOpt.isPresent()) {
            CompanyStatisticsDTO dto = dtoOpt.get();
            CompanyStatistics stats = new CompanyStatistics(
                    dto.getCompanyName(),
                    dto.getEmployeeCount(),
                    dto.getAverageSalary(),
                    dto.getHighestSalary()
            );
            stats.setHighestPaidEmployee(dto.getTopEarnerName());
            return stats;
        } else {
            return new CompanyStatistics(company, 0, 0.0, 0.0);
        }
    }


    public List<CompanyStatistics> getAllCompanyStatistics() {
        List<CompanyStatisticsDTO> dtos = employeeRepository.getCompanyStatisticsDTO();
        return dtos.stream()
                .map(dto -> {
                    CompanyStatistics stats = new CompanyStatistics(
                            dto.getCompanyName(),
                            dto.getEmployeeCount(),
                            dto.getAverageSalary(),
                            dto.getHighestSalary()
                    );
                    stats.setHighestPaidEmployee(dto.getTopEarnerName());
                    return stats;
                })
                .collect(Collectors.toList());
    }


    public Map<String, CompanyStatistics> getCompanyStatisticsMap() {
        List<CompanyStatisticsDTO> dtos = employeeRepository.getCompanyStatisticsDTO();
        return dtos.stream()
                .collect(Collectors.toMap(
                        CompanyStatisticsDTO::getCompanyName,
                        dto -> {
                            CompanyStatistics stats = new CompanyStatistics(
                                    dto.getCompanyName(),
                                    dto.getEmployeeCount(),
                                    dto.getAverageSalary(),
                                    dto.getHighestSalary()
                            );
                            stats.setHighestPaidEmployee(dto.getTopEarnerName());
                            return stats;
                        },
                        (existing, replacement) -> existing,
                        TreeMap::new
                ));
    }

    public Map<String, Object> getPositionStatistics() {
        List<Object[]> results = employeeRepository.getPositionStatistics();
        return results.stream()
                .collect(Collectors.toMap(
                        result -> ((Position) result[0]).name(),
                        result -> Map.of(
                                "count", result[1],
                                "averageSalary", result[2],
                                "maxSalary", result[3],
                                "minSalary", result[4]
                        )
                ));
    }

    public Map<String, Object> getStatusStatistics() {
        List<Object[]> results = employeeRepository.getStatusStatistics();
        return results.stream()
                .collect(Collectors.toMap(
                        result -> ((EmploymentStatus) result[0]).name(),
                        result -> Map.of(
                                "count", result[1],
                                "averageSalary", result[2]
                        )
                ));
    }

    public Map<String, Double> getAverageSalaryByPosition() {
        List<Object[]> results = employeeRepository.getPositionStatistics();
        return results.stream()
                .collect(Collectors.toMap(
                        result -> ((Position) result[0]).name(),
                        result -> (Double) result[2]
                ));
    }

    // ===== ZAAWANSOWANE ZAPYTANIA SQL =====

    public Optional<Employee> findHighestPaidEmployee() {
        List<Employee> highestPaid = employeeRepository.findHighestPaidEmployees();
        return highestPaid.isEmpty() ? Optional.empty() : Optional.of(highestPaid.get(0));
    }

    public Optional<Employee> findHighestPaidEmployeeByCompany(String company) {
        validateCompany(company);
        List<Employee> highestPaid = employeeRepository.findHighestPaidEmployeesByCompany(company);
        return highestPaid.isEmpty() ? Optional.empty() : Optional.of(highestPaid.get(0));
    }

    public List<Employee> findEmployeesBelowAverageSalary() {
        return employeeRepository.findEmployeesBelowAverageSalary();
    }

    public List<Employee> findTop10HighestPaidEmployees() {
        return employeeRepository.findTop10HighestPaidEmployees(Pageable.ofSize(10));
    }

    public Map<EmploymentStatus, Long> getEmploymentStatusDistribution() {
        List<Object[]> results = employeeRepository.getStatusStatistics();
        return results.stream()
                .collect(Collectors.toMap(
                        result -> (EmploymentStatus) result[0],
                        result -> (Long) result[1]
                ));
    }

    // ===== METODY Z PAGINACJĄ =====

    public Page<Employee> getAllEmployees(Pageable pageable) {
        return employeeRepository.findAll(pageable);
    }

    // ===== METODY Z PAGINACJĄ I PROJEKCJĄ =====

    public Page<EmployeeListView> getEmployeesByStatusProjection(EmploymentStatus status, Pageable pageable) {
        return employeeRepository.findByStatusProjection(status, pageable);
    }

    public Page<EmployeeListView> getEmployeesByCompanyProjection(String company, Pageable pageable) {
        validateCompany(company);
        return employeeRepository.findByCompanyProjection(company, pageable);
    }

    public Page<Employee> getEmployeesByStatus(EmploymentStatus status, Pageable pageable) {
        return employeeRepository.findByStatus(status, pageable);
    }

    public Page<Employee> getEmployeesByCompany(String company, Pageable pageable) {
        validateCompany(company);
        return employeeRepository.findByCompany(company, pageable);
    }


    public void deleteEmployeeByEmail(String email) {
        validateEmail(email);
        employeeRepository.deleteByEmail(email);
    }

    public Optional<Employee> findEmployeeByEmail(String email) {
        validateEmail(email);
        return employeeRepository.findByEmail(email);
    }

    public boolean employeeExists(String email) {
        return employeeRepository.existsByEmail(email);
    }

    @Transactional
    public Employee updateEmployeeStatus(String email, EmploymentStatus newStatus) {
        validateEmail(email);

        Employee employee = employeeRepository.findByEmail(email)
                .orElseThrow(() -> new EmployeeNotFoundException("Employee not found with email: " + email));

        employee.setStatus(newStatus);
        return employeeRepository.save(employee);
    }

    // ===== KOMPATYBILNOŚĆ ZE STARNYMI METODAMI =====

    public List<Employee> getEmployeesByStatus(EmploymentStatus status) {
        if (status == null) {
            throw new IllegalArgumentException("Status cannot be null");
        }
        return employeeRepository.findByStatus(status);
    }

    public List<Employee> getEmployeesByCompany(String company) {
        validateCompany(company);
        return employeeRepository.findByCompany(company);
    }

    public List<Employee> getAllEmployees() {
        return employeeRepository.findAll();
    }

    public List<Employee> getEmployeesWithoutDepartment() {
        return employeeRepository.findByDepartmentIsNull();
    }

    public List<Employee> getEmployeesByDepartment(Long departmentId) {
        return employeeRepository.findByDepartmentId(departmentId);
    }

    public List<Employee> getEmployeesByDepartmentId(Long departmentId) {
        return employeeRepository.findByDepartmentId(departmentId);
    }

    public boolean assignEmployeeToDepartment(String employeeEmail, Long departmentId) {
        System.out.println("=== DEBUG: assignEmployeeToDepartment ===");
        System.out.println("Employee email: '" + employeeEmail + "'");
        System.out.println("Department ID: " + departmentId);

        String normalizedEmail = employeeEmail.trim().toLowerCase();
        System.out.println("Normalized email: '" + normalizedEmail + "'");

        Optional<Employee> employeeOpt = employeeRepository.findByEmail(normalizedEmail);
        System.out.println("Employee found: " + employeeOpt.isPresent());

        if (employeeOpt.isEmpty()) {
            List<String> existingEmails = employeeRepository.findAll().stream()
                    .map(Employee::getEmail)
                    .limit(20)
                    .collect(Collectors.toList());
            System.out.println("First 20 existing emails: " + existingEmails);

            List<Employee> allEmployees = employeeRepository.findAll();
            Optional<Employee> caseInsensitiveMatch = allEmployees.stream()
                    .filter(e -> e.getEmail().equalsIgnoreCase(normalizedEmail))
                    .findFirst();

            if (caseInsensitiveMatch.isPresent()) {
                System.out.println("Found with different case: " + caseInsensitiveMatch.get().getEmail());
            }

            return false;
        }

        Optional<Department> departmentOpt = departmentRepository.findById(departmentId);
        System.out.println("Department found: " + departmentOpt.isPresent());

        if (departmentOpt.isEmpty()) {
            return false;
        }

        Employee employee = employeeOpt.get();
        Department department = departmentOpt.get();

        employee.setDepartment(department);
        employeeRepository.save(employee);

        System.out.println("Successfully assigned " + employee.getName() +
                " to department " + department.getName());
        System.out.println("=== DEBUG END ===");

        return true;
    }

    @Transactional
    public boolean removeEmployeeFromDepartment(String employeeEmail) {
        Optional<Employee> employeeOpt = employeeRepository.findByEmail(employeeEmail);
        if (employeeOpt.isPresent()) {
            Employee employee = employeeOpt.get();
            employee.setDepartment(null);
            employeeRepository.save(employee);
            return true;
        }
        return false;
    }

    public boolean isEmpty() {
        return employeeRepository.findAll().isEmpty();
    }

    @Transactional
    public ResponseEntity<String> uploadEmployeePhoto(String email, MultipartFile file) {
        if (!employeeRepository.existsByEmail(email)) {
            throw new EmployeeNotFoundException("Employee not found with email: " + email);
        }

        fileStorageService.validateFile(file);
        fileStorageService.validateImageFile(file);
        fileStorageService.validateFileSize(file, 2 * 1024 * 1024);

        String fileExtension = getFileExtension(file.getOriginalFilename());
        String customFileName = email.replace("@", "_").replace(".", "_") + fileExtension;

        String savedFileName = fileStorageService.storeFileWithCustomName(file, "photos", customFileName);

        Optional<Employee> employeeOpt = employeeRepository.findByEmail(email);
        if (employeeOpt.isPresent()) {
            Employee employee = employeeOpt.get();
            employee.setPhotoFileName(savedFileName);
            employeeRepository.save(employee);
        }

        return ResponseEntity.ok("Photo uploaded successfully: " + savedFileName);
    }

    public ResponseEntity<org.springframework.core.io.Resource> getEmployeePhoto(String email) {
        try {
            String photoFileName = employeeRepository.findByEmail(email)
                    .map(Employee::getPhotoFileName)
                    .orElseThrow(() -> new FileNotFoundException("Employee not found or has no photo"));

            Path photosDir = fileStorageService.getPhotosStorageLocation();
            Path photoPath = photosDir.resolve(photoFileName);

            org.springframework.core.io.Resource resource = new UrlResource(photoPath.toUri());
            if (!resource.exists()) {
                return ResponseEntity.notFound().build();
            }

            String contentType = determineImageContentType(photoFileName);

            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + photoFileName + "\"")
                    .body(resource);

        } catch (FileNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @Transactional
    public void deleteEmployeePhoto(String email) {
        try {
            Optional<Employee> employeeOpt = employeeRepository.findByEmail(email);
            if (employeeOpt.isPresent() && employeeOpt.get().getPhotoFileName() != null) {
                String photoFileName = employeeOpt.get().getPhotoFileName();
                fileStorageService.deleteFile(photoFileName, "photos");

                Employee employee = employeeOpt.get();
                employee.setPhotoFileName(null);
                employeeRepository.save(employee);
            }
        } catch (Exception e) {
            throw new FileStorageException("Could not delete photo", e);
        }
    }

    // ===== METODY POMOCNICZE =====

    public List<Employee> getAvailableManagers() {
        return employeeRepository.findAll().stream()
                .filter(emp -> emp.getPosition() != null &&
                        (emp.getPosition() == Position.MANAGER ||
                                emp.getPosition() == Position.VICE_PRESIDENT ||
                                emp.getPosition() == Position.PRESIDENT))
                .collect(Collectors.toList());
    }

    // Zachowaj kompatybilność ze starym kodem
    @Transactional
    public boolean addEmployee(Employee employee) throws InvalidDataException {
        saveEmployee(employee);
        return true;
    }

    @Transactional
    public void addAllEmployees(List<Employee> newEmployees) {
        for (Employee employee : newEmployees) {
            try {
                if (!employeeRepository.existsByEmail(employee.getEmail())) {
                    employeeRepository.save(employee);
                } else {
                    // Aktualizuj istniejącego pracownika
                    Employee existing = employeeRepository.findByEmail(employee.getEmail()).get();
                    employee.setId(existing.getId());
                    employeeRepository.save(employee);
                }
            } catch (Exception e) {
                System.out.println("Pominięto duplikat: " + employee.getEmail());
            }
        }
    }

    public Employee createEmployee(Employee employee) throws InvalidDataException {
        validateEmployee(employee);

        if (employeeRepository.existsByEmail(employee.getEmail())) {
            throw new DuplicateEmailException(
                    "Employee with email " + employee.getEmail() + " already exists"
            );
        }

        return employeeRepository.save(employee);
    }

    @Transactional
    public Employee updateEmployee(Employee updatedEmployee) throws InvalidDataException {
        System.out.println("=== DEBUG: updateEmployee ===");
        System.out.println("Employee ID: " + updatedEmployee.getId());
        System.out.println("Employee email: " + updatedEmployee.getEmail());

        if (updatedEmployee.getId() == null) {
            System.out.println("ID is null, trying to find by email...");
            Optional<Employee> existingByEmail = employeeRepository.findByEmail(updatedEmployee.getEmail());
            if (existingByEmail.isPresent()) {
                updatedEmployee.setId(existingByEmail.get().getId());
                System.out.println("Found existing employee by email, ID: " + updatedEmployee.getId());
            } else {
                throw new IllegalArgumentException("Cannot update non-existing employee. No ID provided and email not found.");
            }
        }

        validateEmployee(updatedEmployee);

        Optional<Employee> existingOpt = employeeRepository.findById(updatedEmployee.getId());

        if (existingOpt.isEmpty()) {
            System.out.println("ERROR: Employee not found with ID: " + updatedEmployee.getId());
            throw new IllegalArgumentException("Cannot update non-existing employee");
        }

        Employee existing = existingOpt.get();
        System.out.println("Found existing employee: " + existing.getName());
        System.out.println("Existing department: " +
                (existing.getDepartment() != null ? existing.getDepartment().getName() : "null"));

        // Sprawdź email - czy nie zmienia się na istniejący inny email
        if (!existing.getEmail().equalsIgnoreCase(updatedEmployee.getEmail()) &&
                employeeRepository.existsByEmail(updatedEmployee.getEmail())) {
            throw new DuplicateEmailException(
                    "Another employee with email " + updatedEmployee.getEmail() + " already exists");
        }

        // Aktualizuj
        existing.setName(updatedEmployee.getName());
        existing.setEmail(updatedEmployee.getEmail());
        existing.setCompany(updatedEmployee.getCompany());
        existing.setPosition(updatedEmployee.getPosition());
        existing.setSalary(updatedEmployee.getSalary());
        existing.setStatus(updatedEmployee.getStatus());

        System.out.println("=== DEBUG END ===");

        return employeeRepository.save(existing);
    }

    public Employee saveEmployee(Employee employee) throws InvalidDataException {
        if (employee.getId() == null) {
            return createEmployee(employee);
        } else {
            return updateEmployee(employee);
        }
    }

    @Transactional
    public boolean removeEmployee(String email) {
        if (employeeRepository.existsByEmail(email)) {
            employeeRepository.deleteByEmail(email);
            return true;
        }
        return false;
    }

    private void validateEmployee(Employee employee) throws InvalidDataException {
        if (employee == null) {
            throw new InvalidDataException("Employee cannot be null");
        }
    }

    private void validateEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            throw new IllegalArgumentException("Email cannot be null or empty");
        }
    }

    private void validateCompany(String company) {
        if (company == null || company.trim().isEmpty()) {
            throw new IllegalArgumentException("Company name cannot be null or empty");
        }
    }

    private String determineImageContentType(String filename) {
        if (filename.toLowerCase().endsWith(".png")) {
            return "image/png";
        } else if (filename.toLowerCase().endsWith(".gif")) {
            return "image/gif";
        } else {
            return "image/jpeg";
        }
    }

    private String getFileExtension(String fileName) {
        if (fileName == null) return ".jpg";
        int lastDotIndex = fileName.lastIndexOf(".");
        return lastDotIndex > 0 ? fileName.substring(lastDotIndex) : ".jpg";
    }

    public List<String> getAllUniqueCompanies() {
        return employeeRepository.findDistinctCompanies();
    }


    public Page<Employee> searchEmployeesDynamic(
            String name, String company, Position position, EmploymentStatus status,
            Double minSalary, Double maxSalary, String departmentName, Pageable pageable) {

        Specification<Employee> spec = Specification.where(null);

        if (name != null && !name.trim().isEmpty()) {
            spec = spec.and(EmployeeSpecification.hasName(name));
        }

        if (company != null && !company.trim().isEmpty()) {
            spec = spec.and(EmployeeSpecification.fromCompany(company));
        }

        if (position != null) {
            spec = spec.and(EmployeeSpecification.withPosition(position));
        }

        if (status != null) {
            spec = spec.and(EmployeeSpecification.withStatus(status));
        }

        if (minSalary != null) {
            spec = spec.and(EmployeeSpecification.salaryGreaterThanOrEqual(minSalary));
        }

        if (maxSalary != null) {
            spec = spec.and(EmployeeSpecification.salaryLessThanOrEqual(maxSalary));
        }

        if (departmentName != null && !departmentName.trim().isEmpty()) {
            spec = spec.and(EmployeeSpecification.inDepartment(departmentName));
        }

        return employeeRepository.findAll(spec, pageable);
    }


    public Page<Employee> searchEmployeesWithSpecifications(
            String name, String company, Position position, EmploymentStatus status,
            Double minSalary, Double maxSalary, String departmentName, Pageable pageable) {

        return searchEmployeesDynamic(name, company, position, status,
                minSalary, maxSalary, departmentName, pageable);
    }

    public Page<Employee> searchEmployeesFull(
            String name, String company, Position position, EmploymentStatus status,
            Double minSalary, Double maxSalary, String departmentName, Pageable pageable) {

        return searchEmployeesDynamic(name, company, position, status,
                minSalary, maxSalary, departmentName, pageable);
    }


    public List<Employee> findHighEarnersInIT() {
        Specification<Employee> spec = Specification
                .where(EmployeeSpecification.fromCompany("IT"))
                .and(EmployeeSpecification.salaryGreaterThanOrEqual(5000.0));

        return employeeRepository.findAll(spec);
    }


    public Page<Employee> findActiveManagers(Pageable pageable) {
        Specification<Employee> spec = Specification
                .where(EmployeeSpecification.withPosition(Position.MANAGER))
                .and(EmployeeSpecification.withStatus(EmploymentStatus.ACTIVE));

        return employeeRepository.findAll(spec, pageable);
    }


    public List<Employee> findLowPaidWithoutDepartment() {
        Specification<Employee> spec = Specification
                .where(EmployeeSpecification.hasNoDepartment())
                .and(EmployeeSpecification.salaryLessThanOrEqual(3000.0));

        return employeeRepository.findAll(spec);
    }

    // ===== METODY Z PROJEKCJĄ =====

    public Page<EmployeeListView> searchEmployeesAdvanced(
            String name, String company, Position position, EmploymentStatus status,
            Double minSalary, Double maxSalary, String departmentName, Pageable pageable) {

        if (areAllFiltersEmpty(name, company, position, status,
                minSalary, maxSalary, departmentName)) {
            return employeeRepository.findAllProjection(pageable);
        } else {
            return employeeRepository.findWithFiltersProjection(
                    name, company, position, status, minSalary, maxSalary,
                    departmentName, pageable);
        }
    }

    private boolean areAllFiltersEmpty(String name, String company, Position position,
                                       EmploymentStatus status, Double minSalary,
                                       Double maxSalary, String departmentName) {
        return (name == null || name.trim().isEmpty()) &&
                (company == null || company.trim().isEmpty()) &&
                position == null &&
                status == null &&
                minSalary == null &&
                maxSalary == null &&
                (departmentName == null || departmentName.trim().isEmpty() ||
                        "null".equalsIgnoreCase(departmentName));
    }

    public Page<EmployeeListView> searchEmployeesWithProjection(
            String name, String company, Position position, EmploymentStatus status,
            Double minSalary, Double maxSalary, String departmentName, Pageable pageable) {

        return searchEmployeesAdvanced(name, company, position, status,
                minSalary, maxSalary, departmentName, pageable);
    }

    public Page<EmployeeListView> getAllEmployeesProjection(Pageable pageable) {
        return employeeRepository.findAllProjection(pageable);
    }
}







