
package com.techcorp.employee.service;

import com.techcorp.employee.dao.EmployeeDAO;
import com.techcorp.employee.exception.*;
import com.techcorp.employee.model.*;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.core.io.UrlResource;
import org.springframework.beans.factory.annotation.Autowired;

import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class EmployeeService {
    private final EmployeeDAO employeeDAO;
    private final FileStorageService fileStorageService;

    @Autowired
    public EmployeeService(EmployeeDAO employeeDAO, FileStorageService fileStorageService) {
        this.employeeDAO = employeeDAO;
        this.fileStorageService = fileStorageService;
    }

    // ===== ZARZĄDZANIE PRACOWNIKAMI =====

    public boolean addEmployee(Employee employee) throws InvalidDataException {
        validateEmployee(employee);

        if (employeeDAO.existsByEmail(employee.getEmail())) {
            throw new DuplicateEmailException(
                    "Employee with email " + employee.getEmail() + " already exists"
            );
        }

        employeeDAO.save(employee);
        return true;
    }

    @Transactional
    public void addAllEmployees(List<Employee> newEmployees) {
        // Opcja 1: Usuń wszystkie istniejące rekordy przed importem
        // employeeDAO.deleteAll();

        // Opcja 2: Upsert - aktualizuj istniejące lub dodawaj nowe (obecna implementacja)
        for (Employee employee : newEmployees) {
            try {
                if (!employeeDAO.existsByEmail(employee.getEmail())) {
                    employeeDAO.save(employee);
                } else {
                    // Opcjonalnie: aktualizuj istniejącego pracownika
                    Employee existing = employeeDAO.findByEmail(employee.getEmail()).get();
                    employee.setId(existing.getId());
                    employeeDAO.save(employee);
                }
            } catch (Exception e) {
                System.out.println("Pominięto duplikat: " + employee.getEmail());
            }
        }
    }

    public List<Employee> getAllEmployees() {
        return employeeDAO.findAll();
    }

    public boolean removeEmployee(String email) {
        validateEmail(email);

        if (employeeDAO.existsByEmail(email)) {
            employeeDAO.deleteByEmail(email);
            return true;
        }
        return false;
    }

    public Optional<Employee> findEmployeeByEmail(String email) {
        validateEmail(email);
        return employeeDAO.findByEmail(email);
    }

    public boolean employeeExists(String email) {
        return employeeDAO.existsByEmail(email);
    }

    // ===== OPERACJE ZE STATUSEM =====

    @Transactional
    public Employee updateEmployeeStatus(String email, EmploymentStatus newStatus) {
        validateEmail(email);

        Employee employee = employeeDAO.findByEmail(email)
                .orElseThrow(() -> new EmployeeNotFoundException("Employee not found with email: " + email));

        employee.setStatus(newStatus);
        return employeeDAO.save(employee);
    }

    public List<Employee> getEmployeesByStatus(EmploymentStatus status) {
        if (status == null) {
            throw new IllegalArgumentException("Status cannot be null");
        }

        // Filtrowanie w Javie po pobraniu wszystkich danych
        return employeeDAO.findAll().stream()
                .filter(employee -> employee.getStatus() == status)
                .collect(Collectors.toList());
    }

    public Map<EmploymentStatus, Long> getEmploymentStatusDistribution() {
        // Obliczenia w Javie
        return employeeDAO.findAll().stream()
                .collect(Collectors.groupingBy(
                        Employee::getStatus,
                        Collectors.counting()
                ));
    }

    // ===== OPERACJE ANALITYCZNE =====

    public List<Employee> getEmployeesByCompany(String company) {
        validateCompany(company);
        // Użycie metody DAO - bezpośrednio w bazie
        return employeeDAO.findByCompany(company);
    }

    public List<Employee> sortEmployeesByName() {
        // Sortowanie w Javie
        return employeeDAO.findAll().stream()
                .sorted(Comparator.comparing(Employee::getLastName)
                        .thenComparing(Employee::getFirstName))
                .collect(Collectors.toList());
    }

    public Map<Position, List<Employee>> groupEmployeesByPosition() {
        // Grupowanie w Javie
        return employeeDAO.findAll().stream()
                .collect(Collectors.groupingBy(
                        Employee::getPosition,
                        TreeMap::new,
                        Collectors.toList()
                ));
    }

    public Map<Position, Long> countEmployeesByPosition() {
        // Obliczenia w Javie
        return employeeDAO.findAll().stream()
                .collect(Collectors.groupingBy(
                        Employee::getPosition,
                        TreeMap::new,
                        Collectors.counting()
                ));
    }

    public Map<String, List<Employee>> groupEmployeesByCompany() {
        // Grupowanie w Javie
        return employeeDAO.findAll().stream()
                .collect(Collectors.groupingBy(
                        Employee::getCompany,
                        TreeMap::new,
                        Collectors.toList()
                ));
    }

    // ===== STATYSTYKI FINANSOWE =====

    public OptionalDouble calculateAverageSalary() {
        List<Employee> allEmployees = employeeDAO.findAll();
        if (allEmployees.isEmpty()) {
            return OptionalDouble.empty();
        }
        // Obliczenia w Javie
        return OptionalDouble.of(
                allEmployees.stream()
                        .mapToDouble(Employee::getSalary)
                        .average()
                        .orElse(0.0)
        );
    }

    public List<Employee> getEmployeesWithoutDepartment() {
        // Użycie metody DAO - bezpośrednio w bazie
        return employeeDAO.findEmployeesWithoutDepartment();
    }

    @Transactional
    public boolean assignEmployeeToDepartment(String employeeEmail, Long departmentId) {
        Optional<Employee> employeeOpt = employeeDAO.findByEmail(employeeEmail);
        if (employeeOpt.isPresent()) {
            Employee employee = employeeOpt.get();
            employee.setDepartmentId(departmentId);
            employeeDAO.save(employee); // Bezpośrednio w bazie
            return true;
        }
        return false;
    }

    @Transactional
    public boolean removeEmployeeFromDepartment(String employeeEmail) {
        Optional<Employee> employeeOpt = employeeDAO.findByEmail(employeeEmail);
        if (employeeOpt.isPresent()) {
            Employee employee = employeeOpt.get();
            employee.setDepartmentId(null);
            employeeDAO.save(employee); // Bezpośrednio w bazie
            return true;
        }
        return false;
    }

    public List<Employee> getEmployeesByDepartment(Long departmentId) {
        // Użycie metody DAO - bezpośrednio w bazie
        return employeeDAO.findByDepartmentId(departmentId);
    }

    public OptionalDouble calculateAverageSalaryByCompany(String company) {
        validateCompany(company);
        // Obliczenia w Javie
        return employeeDAO.findByCompany(company).stream()
                .mapToDouble(Employee::getSalary)
                .average();
    }

    public Optional<Employee> findHighestPaidEmployee() {
        // Obliczenia w Javie
        return employeeDAO.findAll().stream()
                .max(Comparator.comparingDouble(Employee::getSalary));
    }

    public double findHighestSalary() {
        // Obliczenia w Javie
        return employeeDAO.findAll().stream()
                .mapToDouble(Employee::getSalary)
                .max()
                .orElse(0.0);
    }

    public double findHighestSalaryByCompany(String company) {
        validateCompany(company);
        // Obliczenia w Javie
        return employeeDAO.findByCompany(company).stream()
                .mapToDouble(Employee::getSalary)
                .max()
                .orElse(0.0);
    }

    public Optional<Employee> findLowestPaidEmployee() {
        // Obliczenia w Javie
        return employeeDAO.findAll().stream()
                .min(Comparator.comparingDouble(Employee::getSalary));
    }

    public double calculateTotalSalaryCost() {
        // Obliczenia w Javie
        return employeeDAO.findAll().stream()
                .mapToDouble(Employee::getSalary)
                .sum();
    }

    public double calculateTotalSalaryCostByCompany(String company) {
        validateCompany(company);
        // Obliczenia w Javie
        return employeeDAO.findByCompany(company).stream()
                .mapToDouble(Employee::getSalary)
                .sum();
    }

    // ===== METODY POMOCNICZE =====

    public int getEmployeeCount() {
        return employeeDAO.findAll().size();
    }

    public long getEmployeeCountByCompany(String company) {
        validateCompany(company);
        return employeeDAO.findByCompany(company).size();
    }

    public boolean isEmpty() {
        return employeeDAO.findAll().isEmpty();
    }

    // ===== STATYSTYKI FIRMY =====

    public CompanyStatistics getCompanyStatistics(String company) {
        List<Employee> companyEmployees = employeeDAO.findByCompany(company);

        if (companyEmployees.isEmpty()) {
            // 👇 NOWY KONSTRUKTOR + ustawienie highestPaidEmployee
            CompanyStatistics stats = new CompanyStatistics(company, 0, 0.0, 0.0);
            stats.setHighestPaidEmployee(null); // → Optional.empty()
            return stats;
        }

        // 👇 OBLICZENIA WSZYSTKICH STATYSTYK
        double avgSalary = companyEmployees.stream()
                .mapToDouble(Employee::getSalary)
                .average()
                .orElse(0.0);

        double maxSalary = companyEmployees.stream()
                .mapToDouble(Employee::getSalary)
                .max()
                .orElse(0.0);

        Optional<String> highestPaidEmployee = companyEmployees.stream()
                .max(Comparator.comparingDouble(Employee::getSalary))
                .map(Employee::getName);

        CompanyStatistics stats = new CompanyStatistics(
                company,
                companyEmployees.size(),
                avgSalary,
                maxSalary
        );
        stats.setHighestPaidEmployee(highestPaidEmployee.orElse(null)); // null → Optional.empty()
        return stats;
    }

    public Map<Position, Double> getAverageSalaryByPosition() {
        // Obliczenia w Javie
        return employeeDAO.findAll().stream()
                .collect(Collectors.groupingBy(
                        Employee::getPosition,
                        Collectors.averagingDouble(Employee::getSalary)
                ));
    }

    public List<Employee> validateSalaryConsistency(double baseSalary) {
        // Filtrowanie w Javie
        return employeeDAO.findAll().stream()
                .filter(employee -> employee.getSalary() < baseSalary)
                .collect(Collectors.toList());
    }

    // POPRAWIONE - używa metody DAO z obliczeniami w SQL
    public Map<String, CompanyStatistics> getCompanyStatistics() {
        // Użycie metody DAO - obliczenia w bazie danych
        List<CompanyStatistics> statsFromDb = employeeDAO.getCompanyStatistics();

        return statsFromDb.stream()
                .collect(Collectors.toMap(
                        CompanyStatistics::getCompanyName,
                        stat -> stat,
                        (existing, replacement) -> existing,
                        TreeMap::new
                ));
    }

    // Usunięto metodę calculateCompanyStatistics - nie jest już potrzebna

    @Transactional
    public Employee updateEmployee(Employee updatedEmployee) {
        Employee existing = employeeDAO.findByEmail(updatedEmployee.getEmail())
                .orElseThrow(() -> new EmployeeNotFoundException(
                        "Employee not found with email: " + updatedEmployee.getEmail()));

        // Zachowaj ID z istniejącego rekordu
        updatedEmployee.setId(existing.getId());
        return employeeDAO.save(updatedEmployee); // Bezpośrednio w bazie
    }

    public List<Employee> getEmployeesByDepartmentId(Long departmentId) {
        return employeeDAO.findByDepartmentId(departmentId);
    }

    // ===== WALIDACJA =====

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

    // ===== ZARZĄDZANIE ZDJĘCIAMI PRACOWNIKÓW =====

    @Transactional
    public ResponseEntity<String> uploadEmployeePhoto(String email, MultipartFile file) {
        if (!employeeDAO.existsByEmail(email)) {
            throw new EmployeeNotFoundException("Employee not found with email: " + email);
        }

        fileStorageService.validateFile(file);
        fileStorageService.validateImageFile(file);
        fileStorageService.validateFileSize(file, 2 * 1024 * 1024);

        String fileExtension = getFileExtension(file.getOriginalFilename());
        String customFileName = email.replace("@", "_").replace(".", "_") + fileExtension;

        String savedFileName = fileStorageService.storeFileWithCustomName(file, "photos", customFileName);

        Optional<Employee> employeeOpt = employeeDAO.findByEmail(email);
        if (employeeOpt.isPresent()) {
            Employee employee = employeeOpt.get();
            employee.setPhotoFileName(savedFileName);
            employeeDAO.save(employee); // Bezpośrednio w bazie
        }

        return ResponseEntity.ok("Photo uploaded successfully: " + savedFileName);
    }

    public ResponseEntity<org.springframework.core.io.Resource> getEmployeePhoto(String email) {
        try {
            String photoFileName = employeeDAO.findByEmail(email)
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
            Optional<Employee> employeeOpt = employeeDAO.findByEmail(email);
            if (employeeOpt.isPresent() && employeeOpt.get().getPhotoFileName() != null) {
                String photoFileName = employeeOpt.get().getPhotoFileName();
                fileStorageService.deleteFile(photoFileName, "photos");

                Employee employee = employeeOpt.get();
                employee.setPhotoFileName(null);
                employeeDAO.save(employee); // Bezpośrednio w bazie
            }
        } catch (Exception e) {
            throw new FileStorageException("Could not delete photo", e);
        }
    }

    // ===== METODY POMOCNICZE =====

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

    public List<Employee> getAvailableManagers() {
        // Filtrowanie w Javie
        return employeeDAO.findAll().stream()
                .filter(emp -> emp.getPosition() != null &&
                        (emp.getPosition() == Position.MANAGER ||
                                emp.getPosition() == Position.VICE_PRESIDENT ||
                                emp.getPosition() == Position.PRESIDENT))
                .collect(Collectors.toList());
    }
}