package org.example;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

class EmployeeManagementSystemTest {

    private EmployeeManagementSystem system;
    private Employee emp1;
    private Employee emp2;
    private Employee emp3;

    @BeforeEach
    void setUp() {
        system = new EmployeeManagementSystem();
        emp1 = new Employee("Jan Kowalski", "jan.kowalski@techcorp.com", "TechCorp", Position.MANAGER, 15000);
        emp2 = new Employee("Anna Nowak", "anna.nowak@techcorp.com", "TechCorp", Position.PROGRAMMER, 9000);
        emp3 = new Employee("Piotr Wiśniewski", "piotr.wisniewski@other.com", "OtherCorp", Position.INTERN);

        system.addEmployee(emp1);
        system.addEmployee(emp2);
        system.addEmployee(emp3);
    }

    @Test
    void testAddEmployee() {
        assertEquals(3, system.getEmployeeCount());

        Employee newEmp = new Employee("Maria Zielińska", "maria@techcorp.com", "TechCorp", Position.VICE_PRESIDENT);
        assertTrue(system.addEmployee(newEmp));
        assertEquals(4, system.getEmployeeCount());
    }

    @Test
    void testAddEmployeeWithDuplicateEmailThrowsException() {
        Employee duplicateEmailEmp = new Employee("Jan Nowak", "jan.kowalski@techcorp.com", "OtherCorp", Position.PROGRAMMER);

        Exception exception = assertThrows(IllegalArgumentException.class, () ->
                system.addEmployee(duplicateEmailEmp));

        assertTrue(exception.getMessage().contains("already exists"));
    }

    @Test
    void testAddNullEmployeeThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> system.addEmployee(null));
    }

    @Test
    void testGetAllEmployees() {
        List<Employee> allEmployees = system.getAllEmployees();

        assertEquals(3, allEmployees.size());
        assertTrue(allEmployees.contains(emp1));
        assertTrue(allEmployees.contains(emp2));
        assertTrue(allEmployees.contains(emp3));
    }

    @Test
    void testFindEmployeesByCompany() {
        List<Employee> techCorpEmployees = system.findEmployeesByCompany("TechCorp");
        List<Employee> otherCorpEmployees = system.findEmployeesByCompany("OtherCorp");
        List<Employee> unknownCorpEmployees = system.findEmployeesByCompany("UnknownCorp");

        assertEquals(2, techCorpEmployees.size());
        assertEquals(1, otherCorpEmployees.size());
        assertEquals(0, unknownCorpEmployees.size());

        assertTrue(techCorpEmployees.contains(emp1));
        assertTrue(techCorpEmployees.contains(emp2));
        assertTrue(otherCorpEmployees.contains(emp3));
    }

    @Test
    void testFindEmployeesByCompanyCaseInsensitive() {
        List<Employee> employees = system.findEmployeesByCompany("techcorp");

        assertEquals(2, employees.size());
    }

    @Test
    void testFindEmployeesByCompanyWithInvalidInput() {
        assertThrows(IllegalArgumentException.class, () -> system.findEmployeesByCompany(null));
        assertThrows(IllegalArgumentException.class, () -> system.findEmployeesByCompany(""));
        assertThrows(IllegalArgumentException.class, () -> system.findEmployeesByCompany("   "));
    }

    @Test
    void testSortEmployeesByName() {
        Employee empZ = new Employee("Zofia Kowalska", "zofia@test.com", "TechCorp", Position.PROGRAMMER);

        system.addEmployee(empZ);

        List<Employee> sorted = system.sortEmployeesByName();
        
        // Powinno być posortowane według nazwiska: Kowalska, Kowalski, Nowak, Wiśniewski
        assertEquals("Kowalska", sorted.get(0).getName().split(" ")[1]);
        assertEquals("Kowalski", sorted.get(1).getName().split(" ")[1]);
        assertEquals("Nowak", sorted.get(2).getName().split(" ")[1]);
        assertEquals("Wiśniewski", sorted.get(3).getName().split(" ")[1]);
    }

    @Test
    void testGroupEmployeesByPosition() {
        Map<Position, List<Employee>> grouped = system.groupEmployeesByPosition();

        assertEquals(3, grouped.size());
        assertTrue(grouped.containsKey(Position.MANAGER));
        assertTrue(grouped.containsKey(Position.PROGRAMMER));
        assertTrue(grouped.containsKey(Position.INTERN));

        assertEquals(1, grouped.get(Position.MANAGER).size());
        assertEquals(1, grouped.get(Position.PROGRAMMER).size());
        assertEquals(1, grouped.get(Position.INTERN).size());

        assertEquals(emp1, grouped.get(Position.MANAGER).get(0));
        assertEquals(emp2, grouped.get(Position.PROGRAMMER).get(0));
        assertEquals(emp3, grouped.get(Position.INTERN).get(0));
    }

    @Test
    void testCountEmployeesByPosition() {
        // Dodajemy kolejnego programistę
        Employee anotherProgrammer = new Employee("Krzysztof Lewandowski", "krzysztof@techcorp.com", "TechCorp", Position.PROGRAMMER);
        system.addEmployee(anotherProgrammer);

        Map<Position, Long> counts = system.countEmployeesByPosition();

        assertEquals(1, counts.get(Position.MANAGER));
        assertEquals(2, counts.get(Position.PROGRAMMER)); // Teraz mamy 2 programistów
        assertEquals(1, counts.get(Position.INTERN));
        assertNull(counts.get(Position.PRESIDENT)); // Nie ma prezesa
    }

    @Test
    void testCalculateAverageSalary() {
        // emp1: 15000, emp2: 9000, emp3: 3000 (base salary for INTERN)
        double expectedAverage = (15000 + 9000 + 3000) / 3.0;

        assertEquals(expectedAverage, system.calculateAverageSalary(), 0.001);
    }

    @Test
    void testCalculateAverageSalaryWithNoEmployees() {
        EmployeeManagementSystem emptySystem = new EmployeeManagementSystem();

        assertEquals(0.0, emptySystem.calculateAverageSalary(), 0.001);
    }

    @Test
    void testFindHighestPaidEmployee() {
        Optional<Employee> highestPaid = system.findHighestPaidEmployee();

        assertTrue(highestPaid.isPresent());
        assertEquals(emp1, highestPaid.get());
        assertEquals(15000, highestPaid.get().getSalary(), 0.001);
    }

    @Test
    void testFindHighestPaidEmployeeWithEmptySystem() {
        EmployeeManagementSystem emptySystem = new EmployeeManagementSystem();
        Optional<Employee> highestPaid = emptySystem.findHighestPaidEmployee();

        assertFalse(highestPaid.isPresent());
    }


}