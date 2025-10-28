
package com.techcorp.employee.service;

import com.techcorp.employee.exception.InvalidDataException;
import com.techcorp.employee.model.CompanyStatistics;
import com.techcorp.employee.model.Employee;
import com.techcorp.employee.model.Position;
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

    /**
     * Dodaje nowego pracownika do systemu z walidacją unikalności adresu email
     */
    public boolean addEmployee(Employee employee) throws InvalidDataException {
        validateEmployee(employee);

        if (!employees.add(employee)) {
            throw new InvalidDataException(
                    "Employee with email " + employee.getEmail() + " already exists"
            );
        }
        return true;
    }

    /**
     * Dodaje listę pracowników (dla XML i API)
     */
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

    /**
     * Wyświetla listę wszystkich pracowników w systemie
     */
    public List<Employee> getAllEmployees() {
        return new ArrayList<>(employees);
    }

    /**
     * Usuwa pracownika na podstawie emailu
     */
    public boolean removeEmployee(String email) {
        validateEmail(email);

        Optional<Employee> employee = findEmployeeByEmail(email);
        if (employee.isPresent()) {
            employees.remove(employee.get());
            return true;
        }
        return false;
    }

    /**
     * Znajduje pracownika po emailu
     */
    public Optional<Employee> findEmployeeByEmail(String email) {
        validateEmail(email);

        return employees.stream()
                .filter(emp -> emp.getEmail().equalsIgnoreCase(email.trim()))
                .findFirst();
    }

    /**
     * Sprawdza czy pracownik o podanym emailu istnieje
     */
    public boolean employeeExists(String email) {
        return findEmployeeByEmail(email).isPresent();
    }

    // ===== OPERACJE ANALITYCZNE =====

    /**
     * Wyszukuje pracowników zatrudnionych w konkretnej firmie
     */
    public List<Employee> getEmployeesByCompany(String company) {
        validateCompany(company);

        return employees.stream()
                .filter(employee -> employee.getCompany().equalsIgnoreCase(company.trim()))
                .collect(Collectors.toList());
    }

    /**
     * Prezentuje pracowników w kolejności alfabetycznej według nazwiska
     */
    public List<Employee> sortEmployeesByName() {
        return employees.stream()
                .sorted(Comparator.comparing(Employee::getLastName)
                        .thenComparing(Employee::getFirstName))
                .collect(Collectors.toList());
    }

    /**
     * Grupuje pracowników według zajmowanego stanowiska
     */
    public Map<Position, List<Employee>> groupEmployeesByPosition() {
        return employees.stream()
                .collect(Collectors.groupingBy(
                        Employee::getPosition,
                        TreeMap::new, // Sortowanie według hierarchii stanowisk
                        Collectors.toList()
                ));
    }

    /**
     * Zlicza liczbę pracowników na każdym stanowisku
     */
    public Map<Position, Long> countEmployeesByPosition() {
        return employees.stream()
                .collect(Collectors.groupingBy(
                        Employee::getPosition,
                        TreeMap::new, // Sortowanie według hierarchii
                        Collectors.counting()
                ));
    }

    /**
     * Grupuje pracowników według firmy
     */
    public Map<String, List<Employee>> groupEmployeesByCompany() {
        return employees.stream()
                .collect(Collectors.groupingBy(
                        Employee::getCompany,
                        TreeMap::new, // Sortowanie alfabetyczne firm
                        Collectors.toList()
                ));
    }

    // ===== STATYSTYKI FINANSOWE =====

    /**
     * Oblicza średnie wynagrodzenie w całej organizacji
     */
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

    /**
     * Oblicza średnie wynagrodzenie w konkretnej firmie
     */
    public OptionalDouble calculateAverageSalaryByCompany(String company) {
        validateCompany(company);

        return employees.stream()
                .filter(emp -> emp.getCompany().equalsIgnoreCase(company.trim()))
                .mapToDouble(Employee::getSalary)
                .average();
    }

    /**
     * Identyfikuje pracownika z najwyższym wynagrodzeniem
     */
    public Optional<Employee> findHighestPaidEmployee() {
        return employees.stream()
                .max(Comparator.comparingDouble(Employee::getSalary));
    }

    /**
     * Identyfikuje pracownika z najniższym wynagrodzeniem
     */
    public Optional<Employee> findLowestPaidEmployee() {
        return employees.stream()
                .min(Comparator.comparingDouble(Employee::getSalary));
    }

    /**
     * Oblicza całkowity koszt wynagrodzeń
     */
    public double calculateTotalSalaryCost() {
        return employees.stream()
                .mapToDouble(Employee::getSalary)
                .sum();
    }

    /**
     * Oblicza całkowity koszt wynagrodzeń w firmie
     */
    public double calculateTotalSalaryCostByCompany(String company) {
        validateCompany(company);

        return employees.stream()
                .filter(emp -> emp.getCompany().equalsIgnoreCase(company.trim()))
                .mapToDouble(Employee::getSalary)
                .sum();
    }

    // ===== METODY POMOCNICZE =====

    /**
     * Zwraca liczbę pracowników w systemie
     */
    public int getEmployeeCount() {
        return employees.size();
    }

    /**
     * Zwraca liczbę pracowników w konkretnej firmie
     */
    public long getEmployeeCountByCompany(String company) {
        validateCompany(company);

        return employees.stream()
                .filter(emp -> emp.getCompany().equalsIgnoreCase(company.trim()))
                .count();
    }

    /**
     * Sprawdza czy system jest pusty
     */
    public boolean isEmpty() {
        return employees.isEmpty();
    }

    // ===== NOWE METODY DLA SPRING BOOT =====

    /**
     * Zwraca statystyki dla konkretnej firmy
     */
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

    /**
     * Zwraca średnie wynagrodzenia według stanowisk
     */
    public Map<Position, Double> getAverageSalaryByPosition() {
        return employees.stream()
                .collect(Collectors.groupingBy(
                        Employee::getPosition,
                        Collectors.averagingDouble(Employee::getSalary)
                ));
    }

    /**
     * Zwraca listę pracowników z wynagrodzeniem niższym niż podana wartość
     */
    public List<Employee> validateSalaryConsistency(double baseSalary) {
        return employees.stream()
                .filter(employee -> employee.getSalary() < baseSalary)
                .collect(Collectors.toList());
    }

    /**
     * Zwraca statystyki dla każdej firmy
     */
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