package com.techcorp.employee.service;

import com.techcorp.employee.exception.*;
import com.techcorp.employee.model.*;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class EmployeeService {
    private final Set<Employee> employees;

    public EmployeeService() {
        this.employees = new HashSet<>();
    }

    // ===== ZARZĄDZANIE PRACOWNIKAMI =====

    public boolean addEmployee(Employee employee) throws InvalidDataException {
        validateEmployee(employee);

        if (!employees.add(employee)) {
            throw new DuplicateEmailException(
                    "Employee with email " + employee.getEmail() + " already exists"
            );
        }
        return true;
    }

    public void addAllEmployees(List<Employee> newEmployees) {
        for (Employee employee : newEmployees) {
            try {
                addEmployee(employee);
            } catch (InvalidDataException e) {
                // Pomijanie duplikatów
                System.out.println("Pominięto duplikat: " + employee.getEmail());
            }
        }
    }

    public List<Employee> getAllEmployees() {
        return new ArrayList<>(employees);
    }

    public boolean removeEmployee(String email) {
        validateEmail(email);

        Optional<Employee> employee = findEmployeeByEmail(email);
        if (employee.isPresent()) {
            employees.remove(employee.get());
            return true;
        }
        return false;
    }

    public Optional<Employee> findEmployeeByEmail(String email) {
        validateEmail(email);

        return employees.stream()
                .filter(emp -> emp.getEmail().equalsIgnoreCase(email.trim()))
                .findFirst();
    }

    public boolean employeeExists(String email) {
        return findEmployeeByEmail(email).isPresent();
    }

    // ===== OPERACJE ZE STATUSEM =====

    public Employee updateEmployeeStatus(String email, EmploymentStatus newStatus) {
        validateEmail(email);

        Employee employee = findEmployeeByEmail(email)
                .orElseThrow(() -> new EmployeeNotFoundException("Employee not found with email: " + email));

        employee.setStatus(newStatus);
        return employee;
    }

    public List<Employee> getEmployeesByStatus(EmploymentStatus status) {
        if (status == null) {
            throw new IllegalArgumentException("Status cannot be null");
        }

        return employees.stream()
                .filter(employee -> employee.getStatus() == status)
                .collect(Collectors.toList());
    }

    public Map<EmploymentStatus, Long> getEmploymentStatusDistribution() {
        return employees.stream()
                .collect(Collectors.groupingBy(
                        Employee::getStatus,
                        Collectors.counting()
                ));
    }

    // ===== OPERACJE ANALITYCZNE =====

    public List<Employee> getEmployeesByCompany(String company) {
        validateCompany(company);

        return employees.stream()
                .filter(employee -> employee.getCompany().equalsIgnoreCase(company.trim()))
                .collect(Collectors.toList());
    }

    public List<Employee> sortEmployeesByName() {
        return employees.stream()
                .sorted(Comparator.comparing(Employee::getLastName)
                        .thenComparing(Employee::getFirstName))
                .collect(Collectors.toList());
    }

    public Map<Position, List<Employee>> groupEmployeesByPosition() {
        return employees.stream()
                .collect(Collectors.groupingBy(
                        Employee::getPosition,
                        TreeMap::new,
                        Collectors.toList()
                ));
    }

    public Map<Position, Long> countEmployeesByPosition() {
        return employees.stream()
                .collect(Collectors.groupingBy(
                        Employee::getPosition,
                        TreeMap::new,
                        Collectors.counting()
                ));
    }

    public Map<String, List<Employee>> groupEmployeesByCompany() {
        return employees.stream()
                .collect(Collectors.groupingBy(
                        Employee::getCompany,
                        TreeMap::new,
                        Collectors.toList()
                ));
    }

    // ===== STATYSTYKI FINANSOWE =====

    public OptionalDouble calculateAverageSalary() {
        if (employees.isEmpty()) {
            return OptionalDouble.empty();
        }
        return OptionalDouble.of(
                employees.stream()
                        .mapToDouble(Employee::getSalary)
                        .average()
                        .orElse(0.0)
        );
    }

    public OptionalDouble calculateAverageSalaryByCompany(String company) {
        validateCompany(company);

        return employees.stream()
                .filter(emp -> emp.getCompany().equalsIgnoreCase(company.trim()))
                .mapToDouble(Employee::getSalary)
                .average();
    }

    public Optional<Employee> findHighestPaidEmployee() {
        return employees.stream()
                .max(Comparator.comparingDouble(Employee::getSalary));
    }

    public Optional<Employee> findLowestPaidEmployee() {
        return employees.stream()
                .min(Comparator.comparingDouble(Employee::getSalary));
    }

    public double calculateTotalSalaryCost() {
        return employees.stream()
                .mapToDouble(Employee::getSalary)
                .sum();
    }

    public double calculateTotalSalaryCostByCompany(String company) {
        validateCompany(company);

        return employees.stream()
                .filter(emp -> emp.getCompany().equalsIgnoreCase(company.trim()))
                .mapToDouble(Employee::getSalary)
                .sum();
    }

    // ===== METODY POMOCNICZE =====

    public int getEmployeeCount() {
        return employees.size();
    }

    public long getEmployeeCountByCompany(String company) {
        validateCompany(company);

        return employees.stream()
                .filter(emp -> emp.getCompany().equalsIgnoreCase(company.trim()))
                .count();
    }

    public boolean isEmpty() {
        return employees.isEmpty();
    }

    // ===== STATYSTYKI FIRMY =====

    public CompanyStatistics getCompanyStatistics(String company) {
        List<Employee> companyEmployees = getEmployeesByCompany(company);

        if (companyEmployees.isEmpty()) {
            return new CompanyStatistics(0, 0.0, "None");
        }

        double avgSalary = companyEmployees.stream()
                .mapToDouble(Employee::getSalary)
                .average()
                .orElse(0.0);

        String highestPaidEmployee = companyEmployees.stream()
                .max(Comparator.comparingDouble(Employee::getSalary))
                .map(Employee::getName)
                .orElse("None");

        return new CompanyStatistics(companyEmployees.size(), avgSalary, highestPaidEmployee);
    }

    public Map<Position, Double> getAverageSalaryByPosition() {
        return employees.stream()
                .collect(Collectors.groupingBy(
                        Employee::getPosition,
                        Collectors.averagingDouble(Employee::getSalary)
                ));
    }

    public List<Employee> validateSalaryConsistency(double baseSalary) {
        return employees.stream()
                .filter(employee -> employee.getSalary() < baseSalary)
                .collect(Collectors.toList());
    }

    public Map<String, CompanyStatistics> getCompanyStatistics() {
        return employees.stream()
                .collect(Collectors.groupingBy(
                        Employee::getCompany,
                        TreeMap::new,
                        Collectors.collectingAndThen(
                                Collectors.toList(),
                                this::calculateCompanyStatistics
                        )
                ));
    }

    private CompanyStatistics calculateCompanyStatistics(List<Employee> companyEmployees) {
        int employeeCount = companyEmployees.size();

        double averageSalary = companyEmployees.stream()
                .mapToDouble(Employee::getSalary)
                .average()
                .orElse(0.0);

        String highestPaidEmployee = companyEmployees.stream()
                .max(Comparator.comparingDouble(Employee::getSalary))
                .map(Employee::getName)
                .orElse("None");

        return new CompanyStatistics(employeeCount, averageSalary, highestPaidEmployee);
    }


    public Employee updateEmployee(Employee updatedEmployee) {
        Employee existing = findEmployeeByEmail(updatedEmployee.getEmail())
                .orElseThrow(() -> new EmployeeNotFoundException(
                        "Employee not found with email: " + updatedEmployee.getEmail()));

        if (!existing.getEmail().equals(updatedEmployee.getEmail())) {
            employees.remove(existing);
            employees.add(updatedEmployee);
        }
        return updatedEmployee;
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
}