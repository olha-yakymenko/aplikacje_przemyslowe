//package src;
//
//import src.model.CompanyStatistics;
//import src.model.Employee;
//import src.model.ImportSummary;
//import src.model.Position;
//import src.service.ApiService;
//import src.service.EmployeeService;
//import src.service.ImportService;
//
//import java.util.List;
//import java.util.Map;
//import java.util.Optional;
//import java.util.OptionalDouble;
//
//public class Main {
//    public static void main(String[] args) {
//        try {
//            System.out.println("=== SYSTEM ZARZĄDZANIA PRACOWNIKAMI ROZPOCZĘTY ===\n");
//
//            EmployeeService ems = new EmployeeService();
//
//            System.out.println("=== 1. DODAWANIE PRACOWNIKÓW RĘCZNIE ===");
//            try {
//                Employee emp1 = new Employee("Jan Kowalski", "jan.kowalski@techcorp.com", "TechCorp", Position.MANAGER, 15000);
//                Employee emp2 = new Employee("Anna Nowak", "anna.nowak@techcorp.com", "TechCorp", Position.PROGRAMMER, 9000);
//                ems.addEmployee(emp1);
//                ems.addEmployee(emp2);
//                System.out.println("Dodano 2 pracowników ręcznie");
//            } catch (Exception e) {
//                System.out.println("Błąd przy dodawaniu pracowników: " + e.getMessage());
//            }
//
//            System.out.println("\n=== 2. IMPORT Z PLIKU CSV ===");
//            ImportService importService = new ImportService(ems);
//            ImportSummary summary = importService.importFromCsv("employees.csv");
//            System.out.println("Podsumowanie importu: " + summary);
//            System.out.println("Pomyślnie zaimportowano: " + summary.getImportedCount());
//            if (!summary.getErrors().isEmpty()) {
//                System.out.println("Napotkano błędów: " + summary.getErrors().size());
//                summary.getErrors().forEach(error -> System.out.println("  - " + error));
//            }
//
//            System.out.println("\n=== 3. PODSTAWOWE OPERACJE NA PRACOWNIKACH ===");
//
//            // Lista wszystkich pracowników
//            System.out.println("\n--- Wszyscy pracownicy (" + ems.getEmployeeCount() + ") ---");
//            List<Employee> allEmployees = ems.getAllEmployees();
//            allEmployees.forEach(emp -> System.out.println("  " + emp.getName() + " - " + emp.getPosition()));
//
//            // Wyszukiwanie pracownika po emailu
//            System.out.println("\n--- Wyszukiwanie pracownika ---");
//            Optional<Employee> foundEmployee = ems.findEmployeeByEmail("jan.kowalski@techcorp.com");
//            if (foundEmployee.isPresent()) {
//                System.out.println("Znaleziono: " + foundEmployee.get());
//            } else {
//                System.out.println("Pracownik nie znaleziony");
//            }
//
//            // Sprawdzanie istnienia pracownika
//            System.out.println("Czy pracownik istnieje: " + ems.employeeExists("anna.nowak@techcorp.com"));
//
//            System.out.println("\n=== 4. OPERACJE FIRMOWE ===");
//
//            // Pracownicy według firmy
//            System.out.println("\n--- Pracownicy według firmy ---");
//            Map<String, List<Employee>> byCompany = ems.groupEmployeesByCompany();
//            byCompany.forEach((company, employees) -> {
//                System.out.println("  " + company + ": " + employees.size() + " pracowników");
//            });
//
//            // Liczba pracowników w firmach
//            System.out.println("\n--- Liczba pracowników w firmach ---");
//            byCompany.keySet().forEach(company -> {
//                long count = ems.getEmployeeCountByCompany(company);
//                System.out.println("  " + company + ": " + count + " pracowników");
//            });
//
//            System.out.println("\n=== 5. OPERACJE STANOWISK ===");
//
//            // Grupowanie według stanowiska
//            System.out.println("\n--- Pracownicy według stanowiska ---");
//            Map<Position, List<Employee>> byPosition = ems.groupEmployeesByPosition();
//            byPosition.forEach((position, employees) -> {
//                System.out.println("  " + position + ": " + employees.size() + " pracowników");
//            });
//
//            // Liczba pracowników na stanowiskach
//            System.out.println("\n--- Liczba pracowników na stanowiskach ---");
//            Map<Position, Long> countByPosition = ems.countEmployeesByPosition();
//            countByPosition.forEach((position, count) -> {
//                System.out.println("  " + position + ": " + count + " pracowników");
//            });
//
//            System.out.println("\n=== 6. STATYSTYKI FINANSOWE ===");
//
//            // Średnie wynagrodzenie
//            OptionalDouble avgSalary = ems.calculateAverageSalary();
//            if (avgSalary.isPresent()) {
//                System.out.println("Średnie wynagrodzenie: " + String.format("%.2f", avgSalary.getAsDouble()));
//            }
//
//            // Średnie wynagrodzenie w firmach
//            System.out.println("\n--- Średnie wynagrodzenie w firmach ---");
//            byCompany.keySet().forEach(company -> {
//                OptionalDouble companyAvg = ems.calculateAverageSalaryByCompany(company);
//                if (companyAvg.isPresent()) {
//                    System.out.println("  " + company + ": " + String.format("%.2f", companyAvg.getAsDouble()));
//                }
//            });
//
//            // Najwyższe i najniższe wynagrodzenie
//            Optional<Employee> highestPaid = ems.findHighestPaidEmployee();
//            Optional<Employee> lowestPaid = ems.findLowestPaidEmployee();
//
//            System.out.println("\n--- Ekstrema wynagrodzeń ---");
//            highestPaid.ifPresent(emp ->
//                    System.out.println("  Najwyżej opłacany: " + emp.getName() + " - " + emp.getSalary())
//            );
//            lowestPaid.ifPresent(emp ->
//                    System.out.println("  Najniżej opłacany: " + emp.getName() + " - " + emp.getSalary())
//            );
//
//            // Całkowity koszt wynagrodzeń
//            System.out.println("\n--- Całkowity koszt wynagrodzeń ---");
//            double totalCost = ems.calculateTotalSalaryCost();
//            System.out.println("  Całkowity koszt: " + String.format("%.2f", totalCost));
//
//            byCompany.keySet().forEach(company -> {
//                double companyCost = ems.calculateTotalSalaryCostByCompany(company);
//                System.out.println("  Koszt " + company + ": " + String.format("%.2f", companyCost));
//            });
//
//            System.out.println("\n=== 7. OPERACJE ANALITYCZNE ===");
//
//            // Walidacja wynagrodzeń
//            System.out.println("\n--- Sprawdzenie spójności wynagrodzeń ---");
//            List<Employee> inconsistentSalaries = ems.validateSalaryConsistency();
//            System.out.println("  Pracownicy z wynagrodzeniem poniżej podstawy: " + inconsistentSalaries.size());
//            if (!inconsistentSalaries.isEmpty()) {
//                inconsistentSalaries.forEach(emp ->
//                        System.out.println("  " + emp.getName() + " - " + emp.getSalary() +
//                                " (podstawa: " + emp.getPosition().getBaseSalary() + ")")
//                );
//            } else {
//                System.out.println("  Wszystkie wynagrodzenia są poprawne!");
//            }
//
//            // Statystyki firm
//            System.out.println("\n--- Statystyki firm ---");
//            Map<String, CompanyStatistics> stats = ems.getCompanyStatistics();
//            if (stats.isEmpty()) {
//                System.out.println("  Brak statystyk firm");
//            } else {
//                stats.forEach((company, stat) -> {
//                    System.out.println("  " + company + ":");
//                    System.out.println("    Pracownicy: " + stat.getEmployeeCount());
//                    System.out.println("    Średnie wynagrodzenie: " + String.format("%.2f", stat.getAverageSalary()));
//                    System.out.println("    Najwyżej opłacany: " + stat.getHighestPaidEmployee());
//                });
//            }
//
//            System.out.println("\n=== 8. OPERACJE SORTOWANIA ===");
//
//            // Sortowanie według nazwiska
//            System.out.println("\n--- Pracownicy posortowani według nazwiska ---");
//            List<Employee> sortedByName = ems.sortEmployeesByName();
//            sortedByName.forEach(emp ->
//                    System.out.println("  " + emp.getLastName() + ", " + emp.getFirstName())
//            );
//
//            System.out.println("\n=== 9. INTEGRACJA Z API ===");
//            ApiService apiService = new ApiService();
//            try {
//                List<Employee> apiEmployees = apiService.fetchEmployeesFromApi();
//                System.out.println("Pobrano " + apiEmployees.size() + " pracowników z API");
//
//                // Dodanie pracowników z API do systemu
//                int addedCount = 0;
//                int skippedCount = 0;
//
//                for (Employee employee : apiEmployees) {
//                    try {
//                        if (ems.addEmployee(employee)) {
//                            addedCount++;
//                        }
//                    } catch (Exception e) {
//                        skippedCount++;
//                        System.out.println("  Pominięto " + employee.getEmail() + ": " + e.getMessage());
//                    }
//                }
//
//                System.out.println("Pomyślnie dodano " + addedCount + " pracowników z API");
//                System.out.println("Pominięto " + skippedCount + " zduplikowanych pracowników");
//
//            } catch (Exception apiError) {
//                System.out.println("Integracja z API nie powiodła się: " + apiError.getMessage());
//            }
//
//            System.out.println("\n=== 10. OPERACJE USUWANIA ===");
//
//            // Próba usunięcia nieistniejącego pracownika
//            String emailToRemove = "test@remove.com";
//            boolean removed = ems.removeEmployee(emailToRemove);
//            System.out.println("Usunięcie nieistniejącego pracownika: " + (removed ? "Sukces" : "Niepowodzenie (oczekiwane)"));
//
//            // Usunięcie istniejącego pracownika
//            if (!ems.getAllEmployees().isEmpty()) {
//                String firstEmployeeEmail = ems.getAllEmployees().get(0).getEmail();
//                boolean removedReal = ems.removeEmployee(firstEmployeeEmail);
//                System.out.println("Usunięcie prawdziwego pracownika " + firstEmployeeEmail + ": " +
//                        (removedReal ? "Sukces" : "Niepowodzenie"));
//            }
//
//            System.out.println("\n=== 11. PODSUMOWANIE KOŃCOWE ===");
//            System.out.println("Łączna liczba pracowników w systemie: " + ems.getEmployeeCount());
//            System.out.println("Łączna liczba firm: " + ems.groupEmployeesByCompany().size());
//            System.out.println("System pusty: " + ems.isEmpty());
//
//            OptionalDouble finalAvgSalary = ems.calculateAverageSalary();
//            finalAvgSalary.ifPresent(avg ->
//                    System.out.println("Końcowe średnie wynagrodzenie: " + String.format("%.2f", avg))
//            );
//
//            double finalTotalCost = ems.calculateTotalSalaryCost();
//            System.out.println("Końcowy całkowity koszt wynagrodzeń: " + String.format("%.2f", finalTotalCost));
//
//
//        } catch (Exception e) {
//            System.err.println("Błąd: " + e.getMessage());
//            e.printStackTrace();
//        }
//    }
//}