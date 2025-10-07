package org.example;


import org.example.Employee;
import org.example.Position;

import java.util.*;
import java.util.stream.Collectors;

public class EmployeeManagementSystem {
    private final Set<Employee> employees;

    public EmployeeManagementSystem() {
        this.employees = new HashSet<>();
    }

    // Zarządzanie pracownikami

    /**
     * Dodaje nowego pracownika do systemu z walidacją unikalności email
     */
//    public boolean addEmployee(Employee employee) {
//        if (employee == null) {
//            throw new IllegalArgumentException("Employee cannot be null");
//        }
//
////        // Sprawdź unikalność email
////        boolean emailExists = employees.stream()
////                .anyMatch(emp -> emp.getEmail().equalsIgnoreCase(employee.getEmail()));
////
////        if (emailExists) {
////            throw new IllegalArgumentException("Employee with email " + employee.getEmail() + " already exists");
////        }
//
//        // HashSet sam odrzuci duplikat email dzięki equals() i hashCode()
//
//        return employees.add(employee);
//    }

public boolean addEmployee(Employee employee) {
    if (employee == null) {
        throw new IllegalArgumentException("Employee cannot be null");
    }

    boolean added = employees.add(employee);
    if (!added) {
        // Obsługa „błędu” – pracownik z takim samym emailem już istnieje
        throw new IllegalArgumentException(
                "Employee with email " + employee.getEmail() + " already exists"
        );
    }

    return true; // dodano pomyślnie
}


    /**
     * Wyświetla listę wszystkich pracowników
     */
    public List<Employee> getAllEmployees() {
        return new ArrayList<>(employees);
    }

    // Operacje analityczne

    /**
     * Wyszukuje pracowników zatrudnionych w konkretnej firmie
     */
    public List<Employee> findEmployeesByCompany(String company) {
        if (company == null || company.trim().isEmpty()) {
            throw new IllegalArgumentException("Company name cannot be null or empty");
        }

        return employees.stream()
                .filter(employee -> employee.getCompany().equalsIgnoreCase(company.trim()))
                .collect(Collectors.toList());
    }

    /**
     * Prezentuje pracowników w kolejności alfabetycznej według nazwiska
     */
    public List<Employee> sortEmployeesByName() {
        return employees.stream()
                .sorted(Comparator.comparing(employee -> {
                    String[] nameParts = employee.getName().split(" ");
                    return nameParts[nameParts.length - 1]; // Ostatni element to nazwisko
                }))
                .collect(Collectors.toList());
    }

    /**
     * Grupuje pracowników według zajmowanego stanowiska
     */
    public Map<Position, List<Employee>> groupEmployeesByPosition() {
//        return employees.stream()
//                .collect(Collectors.groupingBy(
//                        Employee::getPosition,
//                        TreeMap::new, // Sortowanie według hierarchii stanowisk
//                        Collectors.toList()
//                ));

        return employees.stream()
                .collect(Collectors.groupingBy(Employee::getPosition));
    }

    /**
     * Zlicza liczbę pracowników na każdym stanowisku
     */
//    public Map<Position, Long> countEmployeesByPosition() {
//        return employees.stream()
//                .collect(Collectors.groupingBy(
//                        Employee::getPosition,
//                        TreeMap::new,
//                        Collectors.counting()
//                ));
//    }

    public Map<Position, Long> countEmployeesByPosition() {
        return employees.stream()
                .collect(Collectors.groupingBy(
                        Employee::getPosition,  // klucz: stanowisko
                        Collectors.counting()   // wartość: liczba pracowników
                ));
    }


    // Statystyki finansowe

    /**
     * Oblicza średnie wynagrodzenie w całej organizacji
     */
    public double calculateAverageSalary() {
        return employees.stream()
                .mapToDouble(Employee::getSalary)
                .average()
                .orElse(0.0);
    }

    /**
     * Identyfikuje pracownika z najwyższym wynagrodzeniem
     */
    public Optional<Employee> findHighestPaidEmployee() {
        return employees.stream()
                .max(Comparator.comparingDouble(Employee::getSalary));
    }

    /**
     * Zwraca liczbę pracowników w systemie
     */
    public int getEmployeeCount() {
        return employees.size();
    }
//
//    /**
//     * Sprawdza czy pracownik o podanym emailu istnieje
//     */
//    public boolean employeeExists(String email) {
//        return employees.stream()
//                .anyMatch(emp -> emp.getEmail().equalsIgnoreCase(email));
//    }
//
//    /**
//     * Usuwa pracownika na podstawie emailu
//     */
//    public boolean removeEmployee(String email) {
//        return employees.removeIf(emp -> emp.getEmail().equalsIgnoreCase(email));
//    }
}
