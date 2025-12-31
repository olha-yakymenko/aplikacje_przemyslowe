//package com.techcorp.employee;
//
//import com.techcorp.employee.model.CompanyStatistics;
//import com.techcorp.employee.model.Employee;
//import com.techcorp.employee.model.ImportSummary;
//import com.techcorp.employee.model.Position;
//import com.techcorp.employee.service.ApiService;
//import com.techcorp.employee.service.EmployeeService;
//import com.techcorp.employee.service.ImportService;
//import org.springframework.boot.CommandLineRunner;
//import org.springframework.boot.SpringApplication;
//import org.springframework.boot.autoconfigure.SpringBootApplication;
//import org.springframework.context.annotation.ImportResource;
package com.techcorp.employee;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class EmployeeManagementApplication {
    public static void main(String[] args) {
        SpringApplication.run(EmployeeManagementApplication.class, args);
    }
}

//import org.springframework.beans.factory.annotation.Qualifier;
//
//import java.util.List;
//import java.util.Map;
//
//@SpringBootApplication
//@ImportResource("classpath:employees-beans.xml")
//public class EmployeeManagementApplication implements CommandLineRunner {
//
//    private final EmployeeService employeeService;
//    private final ImportService importService;
//    private final ApiService apiService;
//    private final List<Employee> xmlEmployees;
//
//    public EmployeeManagementApplication(
//            EmployeeService employeeService,
//            ImportService importService,
//            ApiService apiService,
//            @Qualifier("xmlEmployees") List<Employee> xmlEmployees)
//    {
//        this.employeeService = employeeService;
//        this.importService = importService;
//        this.apiService = apiService;
//        this.xmlEmployees = xmlEmployees;
//    }
//
//    public static void main(String[] args) {
//        SpringApplication.run(EmployeeManagementApplication.class);
//    }
//
//    @Override
//    public void run(String... args) throws Exception {
//        System.out.println("=== SYSTEM ZARZĄDZANIA PRACOWNIKAMI SPRING BOOT ROZPOCZĘTY ===\n");
//
//        // 1. Import pracowników z pliku CSV
//        System.out.println("=== 1. IMPORT Z PLIKU CSV ===");
//        try {
//            ImportSummary csvSummary = importService.importFromCsv();
//            System.out.println("Zaimportowano " + csvSummary.getImportedCount() +
//                    " pracowników, błędów: " + csvSummary.getErrors().size());
//            if (!csvSummary.getErrors().isEmpty()) {
//                System.out.println("Szczegóły błędów:");
//                csvSummary.getErrors().forEach(error -> System.out.println("  - " + error));
//            }
//        } catch (Exception e) {
//            System.out.println("Błąd podczas importu CSV: " + e.getMessage());
//        }
//
//        // 2. Dodanie pracowników z XML
//        System.out.println("\n=== 2. DODAWANIE PRACOWNIKÓW Z XML ===");
//        employeeService.addAllEmployees(xmlEmployees);
//        System.out.println("Dodano " + xmlEmployees.size() + " pracowników z konfiguracji XML");
//        xmlEmployees.forEach(emp ->
//                System.out.println("  - " + emp.getName() +
//                        " (" + emp.getPosition() + ", " + emp.getSalary() + ")")
//        );
//
//        // 3. Pobieranie danych z REST API
//        System.out.println("\n=== 3. POBIERANIE DANYCH Z REST API ===");
//        try {
//            List<Employee> apiEmployees = apiService.fetchEmployeesFromApi();
//            employeeService.addAllEmployees(apiEmployees);
//            System.out.println("Dodano " + apiEmployees.size() + " pracowników z API");
//        } catch (Exception e) {
//            System.out.println("Błąd podczas pobierania z API: " + e.getMessage());
//        }
//
//        // 4. Wyświetlenie wszystkich pracowników
//        System.out.println("\n=== 4. WSZYSCY PRACOWNICY W SYSTEMIE ===");
//        List<Employee> allEmployees = employeeService.getAllEmployees();
//        System.out.println("Łączna liczba pracowników: " + allEmployees.size());
//        allEmployees.forEach(emp ->
//                System.out.println("  - " + emp.getName() +
//                        " | " + emp.getEmail() + " | " + emp.getCompany() +
//                        " | " + emp.getPosition() + " | " + emp.getSalary())
//        );
//
//        // 5. Statystyki dla firmy TechCorp
//        System.out.println("\n=== 5. STATYSTYKI DLA FIRMY TECHCORP ===");
//        CompanyStatistics stats = employeeService.getCompanyStatistics("TechCorp");
//        System.out.println("Firma: TechCorp");
//        System.out.println("Liczba pracowników: " + stats.getEmployeeCount());
//        System.out.println("Średnie wynagrodzenie: " + String.format("%.2f", stats.getAverageSalary()));
//        System.out.println("Najwyżej opłacany: " + stats.getHighestPaidEmployee());
//
//        // 6. Średnie wynagrodzenia według stanowisk
//        System.out.println("\n=== 6. ŚREDNIE WYNAGRODZENIA WEDŁUG STANOWISK ===");
//        Map<Position, Double> salaryByPosition = employeeService.getAverageSalaryByPosition();
//        if (salaryByPosition.isEmpty()) {
//            System.out.println("Brak danych o wynagrodzeniach");
//        } else {
//            salaryByPosition.forEach((position, avgSalary) ->
//                    System.out.println("  " + position + ": " + String.format("%.2f", avgSalary))
//            );
//        }
//
//        // 7. Grupowanie pracowników według firmy
//        System.out.println("\n=== 7. PRACOWNICY WEDŁUG FIRMY ===");
//        Map<String, List<Employee>> byCompany = employeeService.groupEmployeesByCompany();
//        byCompany.forEach((company, employees) -> {
//            System.out.println("  " + company + ": " + employees.size() + " pracowników");
//        });
//
//        // 8. Grupowanie pracowników według stanowiska
//        System.out.println("\n=== 8. PRACOWNICY WEDŁUG STANOWISKA ===");
//        Map<Position, List<Employee>> byPosition = employeeService.groupEmployeesByPosition();
//        byPosition.forEach((position, employees) -> {
//            System.out.println("  " + position + ": " + employees.size() + " pracowników");
//        });
//
//        // 9. Walidacja spójności wynagrodzeń
//        System.out.println("\n=== 9. WALIDACJA SPÓJNOŚCI WYNAGRODZEŃ (bazowa stawka: 6000) ===");
//        List<Employee> underpaidEmployees = employeeService.validateSalaryConsistency(6000);
//        if (underpaidEmployees.isEmpty()) {
//            System.out.println("Brak pracowników z wynagrodzeniem poniżej bazowej stawki");
//        } else {
//            System.out.println("Pracownicy z wynagrodzeniem poniżej bazowej stawki:");
//            underpaidEmployees.forEach(emp ->
//                    System.out.println("  - " + emp.getName() +
//                            " (" + emp.getPosition() + "): " + emp.getSalary() +
//                            " (podstawa: " + emp.getPosition().getBaseSalary() + ")")
//            );
//        }
//
//        // 10. Statystyki finansowe
//        System.out.println("\n=== 10. STATYSTYKI FINANSOWE ===");
//
//        // Średnie wynagrodzenie
//        employeeService.calculateAverageSalary().ifPresent(avgSalary ->
//                System.out.println("Średnie wynagrodzenie w organizacji: " + String.format("%.2f", avgSalary))
//        );
//
//        // Najwyższe i najniższe wynagrodzenie
//        employeeService.findHighestPaidEmployee().ifPresent(emp ->
//                System.out.println("Najwyżej opłacany: " + emp.getName() +
//                        " - " + emp.getSalary())
//        );
//
//        employeeService.findLowestPaidEmployee().ifPresent(emp ->
//                System.out.println("Najniżej opłacany: " + emp.getName() +
//                        " - " + emp.getSalary())
//        );
//
//        // Całkowity koszt wynagrodzeń
//        double totalCost = employeeService.calculateTotalSalaryCost();
//        System.out.println("Całkowity koszt wynagrodzeń: " + String.format("%.2f", totalCost));
//
//        // 11. Statystyki dla wszystkich firm
//        System.out.println("\n=== 11. STATYSTYKI WSZYSTKICH FIRM ===");
//        Map<String, CompanyStatistics> allStats = employeeService.getCompanyStatistics();
//        if (allStats.isEmpty()) {
//            System.out.println("Brak statystyk firm");
//        } else {
//            allStats.forEach((company, companyStats) -> {
//                System.out.println("  " + company + ":");
//                System.out.println("    Pracownicy: " + companyStats.getEmployeeCount());
//                System.out.println("    Średnie wynagrodzenie: " + String.format("%.2f", companyStats.getAverageSalary()));
//                System.out.println("    Najwyżej opłacany: " + companyStats.getHighestPaidEmployee());
//            });
//        }
//
//        // 12. Sortowanie pracowników według nazwiska
//        System.out.println("\n=== 12. PRACOWNICY POSORTOWANI WEDŁUG NAZWISKA ===");
//        List<Employee> sortedEmployees = employeeService.sortEmployeesByName();
//        sortedEmployees.forEach(emp ->
//                System.out.println("  " + emp.getLastName() + ", " + emp.getFirstName())
//        );
//
//        // 13. Podsumowanie końcowe
//        System.out.println("\n=== 13. PODSUMOWANIE KOŃCOWE ===");
//        System.out.println("Łączna liczba pracowników w systemie: " + employeeService.getEmployeeCount());
//        System.out.println("Łączna liczba firm: " + employeeService.groupEmployeesByCompany().size());
//        System.out.println("Liczba stanowisk: " + employeeService.groupEmployeesByPosition().size());
//        System.out.println("System pusty: " + employeeService.isEmpty());
//
//        employeeService.calculateAverageSalary().ifPresent(avg ->
//                System.out.println("Końcowe średnie wynagrodzenie: " + String.format("%.2f", avg))
//        );
//
//        double finalTotalCost = employeeService.calculateTotalSalaryCost();
//        System.out.println("Końcowy całkowity koszt wynagrodzeń: " + String.format("%.2f", finalTotalCost));
//
//        System.out.println("\n=== KONIEC DEMONSTRACJI SYSTEMU SPRING BOOT ===");
//    }
//}