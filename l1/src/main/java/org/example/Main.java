package org.example;

import java.util.*;

public class Main {
    public static void main(String[] args) {
        EmployeeManagementSystem system = new EmployeeManagementSystem();

        // Dodawanie pracowników
        system.addEmployee(new Employee("Jan Kowalski", "jan.kowalski@techcorp.com", "TechCorp", Position.MANAGER, 15000));
        system.addEmployee(new Employee("Anna Nowak", "anna.nowak@techcorp.com", "TechCorp", Position.PROGRAMMER, 9000));
        system.addEmployee(new Employee("Piotr Wiśniewski", "piotr.wisniewski@other.com", "OtherCorp", Position.INTERN));
        system.addEmployee(new Employee("Maria Zielińska", "maria.zielinska@techcorp.com", "TechCorp", Position.VICE_PRESIDENT, 20000));
        system.addEmployee(new Employee("Krzysztof Lewandowski", "krzysztof.lewandowski@techcorp.com", "TechCorp", Position.PROGRAMMER, 8500));

        // Wyświetlanie wszystkich pracowników
        System.out.println("=== WSZYSCY PRACOWNICY (" + system.getEmployeeCount() + ") ===");
        system.getAllEmployees().forEach(System.out::println);

        // Wyszukiwanie pracowników TechCorp
        System.out.println("\n=== PRACOWNICY TECHCORP (" + system.getEmployeeCountByCompany("TechCorp") + ") ===");
        system.findEmployeesByCompany("TechCorp").forEach(System.out::println);

        // Sortowanie według nazwiska
        System.out.println("\n=== POSORTOWANI WEDŁUG NAZWISKA ===");
        system.sortEmployeesByName().forEach(System.out::println);

        // Grupowanie według stanowiska
        System.out.println("\n=== GRUPOWANIE WEDŁUG STANOWISKA ===");
        Map<Position, List<Employee>> groupedByPosition = system.groupEmployeesByPosition();
        groupedByPosition.forEach((position, employees) -> {
            System.out.println(position + " (" + employees.size() + "):");
            employees.forEach(emp -> System.out.println("  - " + emp.getName()));
        });

        // Liczba pracowników na stanowisku
        System.out.println("\n=== LICZBA PRACOWNIKÓW NA STANOWISKU ===");
        Map<Position, Long> counts = system.countEmployeesByPosition();
        counts.forEach((position, count) -> {
            System.out.println(position + ": " + count);
        });

        // Grupowanie według firmy
        System.out.println("\n=== GRUPOWANIE WEDŁUG FIRMY ===");
        Map<String, List<Employee>> groupedByCompany = system.groupEmployeesByCompany();
        groupedByCompany.forEach((company, employees) -> {
            System.out.println(company + " (" + employees.size() + "):");
            employees.forEach(emp -> System.out.println("  - " + emp.getName() + " - " + emp.getPosition()));
        });

        // Statystyki finansowe
        System.out.println("\n=== STATYSTYKI FINANSOWE ===");
        System.out.printf("Całkowity koszt wynagrodzeń: %.2f PLN\n", system.calculateTotalSalaryCost());
        System.out.printf("Średnie wynagrodzenie: %.2f PLN\n", system.calculateAverageSalary().orElse(0.0));

        system.findHighestPaidEmployee().ifPresent(employee -> {
            System.out.printf("Najlepiej zarabiający: %s (%.2f PLN)\n",
                    employee.getName(), employee.getSalary());
        });

        system.findLowestPaidEmployee().ifPresent(employee -> {
            System.out.printf("Najgorzej zarabiający: %s (%.2f PLN)\n",
                    employee.getName(), employee.getSalary());
        });

        // Statystyki dla TechCorp
        System.out.println("\n=== STATYSTYKI TECHCORP ===");
        System.out.printf("Koszt wynagrodzeń w TechCorp: %.2f PLN\n",
                system.calculateTotalSalaryCostByCompany("TechCorp"));
        System.out.printf("Średnie wynagrodzenie w TechCorp: %.2f PLN\n",
                system.calculateAverageSalaryByCompany("TechCorp").orElse(0.0));

        // Wyszukiwanie po emailu
        System.out.println("\n=== WYSZUKIWANIE PO EMAILU ===");
        system.findEmployeeByEmail("anna.nowak@techcorp.com")
                .ifPresentOrElse(
                        employee -> System.out.println("Znaleziono: " + employee),
                        () -> System.out.println("Nie znaleziono pracownika")
                );

        // Sprawdzanie istnienia
        System.out.println("\n=== SPRAWDZANIE ISTNIENIA ===");
        String testEmail = "jan.kowalski@techcorp.com";
        System.out.println("Czy pracownik z emailem " + testEmail + " istnieje? " +
                system.employeeExists(testEmail));
    }
}