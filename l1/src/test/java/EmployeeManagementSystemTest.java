package org.example;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.OptionalDouble;

class EmployeeManagementSystemTest {

    private EmployeeManagementSystem system;
    private Employee emp1, emp2, emp3, emp4, emp5;

    @BeforeEach
    void setUp() {
        system = new EmployeeManagementSystem();
        emp1 = new Employee("Jan Kowalski", "jan.kowalski@techcorp.com", "TechCorp", Position.MANAGER, 15000);
        emp2 = new Employee("Anna Nowak", "anna.nowak@techcorp.com", "TechCorp", Position.PROGRAMMER, 9000);
        emp3 = new Employee("Piotr Wiśniewski", "piotr.wisniewski@other.com", "OtherCorp", Position.INTERN);
        emp4 = new Employee("Maria Zielińska", "maria.zielinska@techcorp.com", "TechCorp", Position.VICE_PRESIDENT, 20000);
        emp5 = new Employee("Krzysztof Lewandowski", "krzysztof.lewandowski@techcorp.com", "TechCorp", Position.PROGRAMMER, 8500);

        system.addEmployee(emp1);
        system.addEmployee(emp2);
        system.addEmployee(emp3);
        system.addEmployee(emp4);
        system.addEmployee(emp5);
    }

    @Test
    void testRemoveEmployeeByEmail() {
        // Test usuwania istniejącego pracownika
        assertTrue(system.employeeExists("piotr.wisniewski@other.com"));
        boolean removed = system.removeEmployee("piotr.wisniewski@other.com");

        assertTrue(removed);
        assertFalse(system.employeeExists("piotr.wisniewski@other.com"));
        assertEquals(4, system.getEmployeeCount());
    }

    @Test
    void testRemoveNonExistentEmployee() {
        // Test usuwania nieistniejącego pracownika
        boolean removed = system.removeEmployee("nieistniejacy@email.com");

        assertFalse(removed);
        assertEquals(5, system.getEmployeeCount());
    }

    @Test
    void testRemoveEmployeeWithInvalidEmail() {
        // Test usuwania z nieprawidłowym emailem
        assertThrows(IllegalArgumentException.class, () -> system.removeEmployee(null));
        assertThrows(IllegalArgumentException.class, () -> system.removeEmployee(""));
        assertThrows(IllegalArgumentException.class, () -> system.removeEmployee("   "));
    }

    @Test
    void testFindEmployeeByEmail() {
        // Test znalezienia pracownika po emailu
        Optional<Employee> found = system.findEmployeeByEmail("anna.nowak@techcorp.com");

        assertTrue(found.isPresent());
        assertEquals(emp2, found.get());
        assertEquals("Anna Nowak", found.get().getName());
    }

    @Test
    void testFindEmployeeByEmailCaseInsensitive() {
        // Test case-insensitive wyszukiwania
        Optional<Employee> found = system.findEmployeeByEmail("ANNA.NOWAK@TECHCORP.COM");

        assertTrue(found.isPresent());
        assertEquals(emp2, found.get());
    }

    @Test
    void testFindNonExistentEmployeeByEmail() {
        // Test wyszukiwania nieistniejącego pracownika
        Optional<Employee> found = system.findEmployeeByEmail("nieistniejacy@email.com");

        assertFalse(found.isPresent());
    }

    @Test
    void testFindEmployeeByEmailWithInvalidInput() {
        // Test wyszukiwania z nieprawidłowym wejściem
        assertThrows(IllegalArgumentException.class, () -> system.findEmployeeByEmail(null));
        assertThrows(IllegalArgumentException.class, () -> system.findEmployeeByEmail(""));
        assertThrows(IllegalArgumentException.class, () -> system.findEmployeeByEmail("   "));
    }

    @Test
    void testEmployeeExists() {
        // Test sprawdzania istnienia pracownika
        assertTrue(system.employeeExists("jan.kowalski@techcorp.com"));
        assertTrue(system.employeeExists("JAN.KOWALSKI@TECHCORP.COM")); // case insensitive
        assertFalse(system.employeeExists("nieistniejacy@email.com"));
    }

    @Test
    void testEmployeeExistsWithInvalidInput() {
        // Test z nieprawidłowym wejściem
        assertThrows(IllegalArgumentException.class, () -> system.employeeExists(null));
        assertThrows(IllegalArgumentException.class, () -> system.employeeExists(""));
        assertThrows(IllegalArgumentException.class, () -> system.employeeExists("   "));
    }

    @Test
    void testGetEmployeeCount() {
        // Test liczenia pracowników
        assertEquals(5, system.getEmployeeCount());

        system.removeEmployee("piotr.wisniewski@other.com");
        assertEquals(4, system.getEmployeeCount());

        system.addEmployee(new Employee("Nowy Pracownik", "nowy@test.com", "TestCorp", Position.PROGRAMMER));
        assertEquals(5, system.getEmployeeCount());
    }

    @Test
    void testGetEmployeeCountByCompany() {
        // Test liczenia pracowników według firmy
        assertEquals(4, system.getEmployeeCountByCompany("TechCorp"));
        assertEquals(1, system.getEmployeeCountByCompany("OtherCorp"));
        assertEquals(0, system.getEmployeeCountByCompany("NieistniejacaFirma"));
    }

    @Test
    void testGetEmployeeCountByCompanyCaseInsensitive() {
        // Test case-insensitive liczenia
        assertEquals(4, system.getEmployeeCountByCompany("techcorp"));
        assertEquals(4, system.getEmployeeCountByCompany("TECHCORP"));
    }

    @Test
    void testGetEmployeeCountByCompanyWithInvalidInput() {
        // Test z nieprawidłowym wejściem
        assertThrows(IllegalArgumentException.class, () -> system.getEmployeeCountByCompany(null));
        assertThrows(IllegalArgumentException.class, () -> system.getEmployeeCountByCompany(""));
        assertThrows(IllegalArgumentException.class, () -> system.getEmployeeCountByCompany("   "));
    }

    @Test
    void testIsEmpty() {
        // Test sprawdzania czy system jest pusty
        assertFalse(system.isEmpty());

        EmployeeManagementSystem emptySystem = new EmployeeManagementSystem();
        assertTrue(emptySystem.isEmpty());

        emptySystem.addEmployee(emp1);
        assertFalse(emptySystem.isEmpty());
    }

    @Test
    void testCalculateAverageSalaryByCompany() {
        // Test średniego wynagrodzenia w firmie
        OptionalDouble techCorpAvg = system.calculateAverageSalaryByCompany("TechCorp");
        OptionalDouble otherCorpAvg = system.calculateAverageSalaryByCompany("OtherCorp");
        OptionalDouble unknownCorpAvg = system.calculateAverageSalaryByCompany("UnknownCorp");

        double expectedTechCorpAvg = (15000 + 9000 + 20000 + 8500) / 4.0;
        double expectedOtherCorpAvg = 3000; // tylko stażysta

        assertTrue(techCorpAvg.isPresent());
        assertEquals(expectedTechCorpAvg, techCorpAvg.getAsDouble(), 0.001);

        assertTrue(otherCorpAvg.isPresent());
        assertEquals(expectedOtherCorpAvg, otherCorpAvg.getAsDouble(), 0.001);

        assertFalse(unknownCorpAvg.isPresent());
    }

    @Test
    void testCalculateAverageSalaryByCompanyWithInvalidInput() {
        // Test z nieprawidłowym wejściem
        assertThrows(IllegalArgumentException.class, () -> system.calculateAverageSalaryByCompany(null));
        assertThrows(IllegalArgumentException.class, () -> system.calculateAverageSalaryByCompany(""));
        assertThrows(IllegalArgumentException.class, () -> system.calculateAverageSalaryByCompany("   "));
    }

    @Test
    void testFindLowestPaidEmployee() {
        // Test znalezienia najgorzej zarabiającego pracownika
        Optional<Employee> lowestPaid = system.findLowestPaidEmployee();

        assertTrue(lowestPaid.isPresent());
        assertEquals(emp3, lowestPaid.get());
        assertEquals(3000, lowestPaid.get().getSalary(), 0.001);
    }

    @Test
    void testFindLowestPaidEmployeeWithEmptySystem() {
        // Test z pustym systemem
        EmployeeManagementSystem emptySystem = new EmployeeManagementSystem();
        Optional<Employee> lowestPaid = emptySystem.findLowestPaidEmployee();

        assertFalse(lowestPaid.isPresent());
    }

    @Test
    void testCalculateTotalSalaryCost() {
        // Test obliczania całkowitego kosztu wynagrodzeń
        double expectedTotal = 15000 + 9000 + 3000 + 20000 + 8500;
        double actualTotal = system.calculateTotalSalaryCost();

        assertEquals(expectedTotal, actualTotal, 0.001);
    }

    @Test
    void testCalculateTotalSalaryCostWithEmptySystem() {
        // Test z pustym systemem
        EmployeeManagementSystem emptySystem = new EmployeeManagementSystem();
        double total = emptySystem.calculateTotalSalaryCost();

        assertEquals(0.0, total, 0.001);
    }

    @Test
    void testCalculateTotalSalaryCostByCompany() {
        // Test kosztu wynagrodzeń w firmie
        double expectedTechCorpTotal = 15000 + 9000 + 20000 + 8500;
        double expectedOtherCorpTotal = 3000;

        double techCorpTotal = system.calculateTotalSalaryCostByCompany("TechCorp");
        double otherCorpTotal = system.calculateTotalSalaryCostByCompany("OtherCorp");
        double unknownCorpTotal = system.calculateTotalSalaryCostByCompany("UnknownCorp");

        assertEquals(expectedTechCorpTotal, techCorpTotal, 0.001);
        assertEquals(expectedOtherCorpTotal, otherCorpTotal, 0.001);
        assertEquals(0.0, unknownCorpTotal, 0.001);
    }

    @Test
    void testCalculateTotalSalaryCostByCompanyWithInvalidInput() {
        // Test z nieprawidłowym wejściem
        assertThrows(IllegalArgumentException.class, () -> system.calculateTotalSalaryCostByCompany(null));
        assertThrows(IllegalArgumentException.class, () -> system.calculateTotalSalaryCostByCompany(""));
        assertThrows(IllegalArgumentException.class, () -> system.calculateTotalSalaryCostByCompany("   "));
    }

    @Test
    void testGroupEmployeesByCompany() {
        // Test grupowania pracowników według firmy
        Map<String, List<Employee>> groupedByCompany = system.groupEmployeesByCompany();

        assertEquals(2, groupedByCompany.size());
        assertTrue(groupedByCompany.containsKey("TechCorp"));
        assertTrue(groupedByCompany.containsKey("OtherCorp"));

        assertEquals(4, groupedByCompany.get("TechCorp").size());
        assertEquals(1, groupedByCompany.get("OtherCorp").size());

        // Sprawdź czy pracownicy są w odpowiednich firmach
        assertTrue(groupedByCompany.get("TechCorp").contains(emp1));
        assertTrue(groupedByCompany.get("TechCorp").contains(emp2));
        assertTrue(groupedByCompany.get("TechCorp").contains(emp4));
        assertTrue(groupedByCompany.get("TechCorp").contains(emp5));
        assertTrue(groupedByCompany.get("OtherCorp").contains(emp3));
    }

    @Test
    void testSortEmployeesByNameWithMultipleSameLastNames() {
        // Test sortowania z wieloma pracownikami o tym samym nazwisku
        Employee emp6 = new Employee("Adam Kowalski", "adam.kowalski@test.com", "TestCorp", Position.PROGRAMMER);
        Employee emp7 = new Employee("Zofia Kowalski", "zofia.kowalski@test.com", "TestCorp", Position.PROGRAMMER);

        system.addEmployee(emp6);
        system.addEmployee(emp7);

        List<Employee> sorted = system.sortEmployeesByName();

        // Powinno być posortowane: Adam Kowalski, Jan Kowalski, Zofia Kowalski, Krzysztof Lewandowski, Anna Nowak, Piotr Wiśniewski, Maria Zielińska
        assertEquals("Adam", sorted.get(0).getFirstName());
        assertEquals("Jan", sorted.get(1).getFirstName());
        assertEquals("Zofia", sorted.get(2).getFirstName());
        assertEquals("Krzysztof", sorted.get(3).getFirstName());
        assertEquals("Anna", sorted.get(4).getFirstName());
        assertEquals("Piotr", sorted.get(5).getFirstName());
        assertEquals("Maria", sorted.get(6).getFirstName());
    }

    @Test
    void testCountEmployeesByPositionWithMultipleEmployees() {
        // Test zliczania z wieloma pracownikami na tym samym stanowisku
        Map<Position, Long> counts = system.countEmployeesByPosition();

        assertEquals(2, counts.get(Position.PROGRAMMER)); // emp2 i emp5
        assertEquals(1, counts.get(Position.MANAGER));    // emp1
        assertEquals(1, counts.get(Position.INTERN));     // emp3
        assertEquals(1, counts.get(Position.VICE_PRESIDENT)); // emp4
        assertNull(counts.get(Position.PRESIDENT));       // brak prezesa
    }

    @Test
    void testSystemWithNoEmployees() {
        // Test wszystkich operacji na pustym systemie
        EmployeeManagementSystem emptySystem = new EmployeeManagementSystem();

        assertTrue(emptySystem.isEmpty());
        assertEquals(0, emptySystem.getEmployeeCount());
        assertEquals(0, emptySystem.getAllEmployees().size());
        assertEquals(0, emptySystem.findEmployeesByCompany("TechCorp").size());
        assertEquals(0, emptySystem.sortEmployeesByName().size());
        assertTrue(emptySystem.groupEmployeesByPosition().isEmpty());
        assertTrue(emptySystem.countEmployeesByPosition().isEmpty());
        assertFalse(emptySystem.calculateAverageSalary().isPresent());
        assertFalse(emptySystem.findHighestPaidEmployee().isPresent());
        assertFalse(emptySystem.findLowestPaidEmployee().isPresent());
        assertEquals(0.0, emptySystem.calculateTotalSalaryCost(), 0.001);
        assertTrue(emptySystem.groupEmployeesByCompany().isEmpty());
    }

    @Test
    void testEmployeeEmailUniquenessAcrossDifferentCompanies() {
        // Test unikalności emaila across różnych firm
        Employee duplicateEmail = new Employee("Inny Jan", "jan.kowalski@techcorp.com", "DifferentCorp", Position.PROGRAMMER);

        Exception exception = assertThrows(IllegalArgumentException.class, () ->
                system.addEmployee(duplicateEmail));

        assertTrue(exception.getMessage().contains("already exists"));
    }

    @Test
    void testMultipleOperationsConsistency() {
        // Test spójności po wielu operacjach
        assertEquals(5, system.getEmployeeCount());

        // Usuń pracownika
        system.removeEmployee("piotr.wisniewski@other.com");
        assertEquals(4, system.getEmployeeCount());
        assertFalse(system.employeeExists("piotr.wisniewski@other.com"));

        // Dodaj nowego
        Employee newEmp = new Employee("Nowy Pracownik", "nowy@test.com", "TestCorp", Position.MANAGER, 12000);
        system.addEmployee(newEmp);
        assertEquals(5, system.getEmployeeCount());
        assertTrue(system.employeeExists("nowy@test.com"));

        // Sprawdź czy statystyki są aktualne
        assertEquals(5, system.getAllEmployees().size());
        assertEquals(1, system.getEmployeeCountByCompany("TestCorp"));
    }
}