package org.example;

import java.util.*;
import java.util.stream.Collectors;

public class EmployeeManagementSystem {
    private final Set<Employee> employees;

    public EmployeeManagementSystem() {
        this.employees = new HashSet<>();
    }

    // ===== ZARZĄDZANIE PRACOWNIKAMI =====

    /**
     * Dodaje nowego pracownika do systemu z walidacją unikalności adresu email
     */
    public boolean addEmployee(Employee employee) {
        validateEmployee(employee);

        if (!employees.add(employee)) {
            throw new IllegalArgumentException(
                    "Employee with email " + employee.getEmail() + " already exists"
            );
        }
        return true;
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
    public List<Employee> findEmployeesByCompany(String company) {
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

    // ===== WALIDACJA =====

    private void validateEmployee(Employee employee) {
        if (employee == null) {
            throw new IllegalArgumentException("Employee cannot be null");
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