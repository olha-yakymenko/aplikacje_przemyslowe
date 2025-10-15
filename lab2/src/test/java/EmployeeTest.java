import src.exception.InvalidDataException;
import src.model.Position;
import org.junit.jupiter.api.Test;
import src.model.Employee;

import static org.junit.jupiter.api.Assertions.*;

class EmployeeTest {

    @Test
    void testGetFirstName() throws InvalidDataException {
        Employee employee = new Employee("Jan Kowalski", "jan@techcorp.com", "TechCorp", Position.MANAGER);
        assertEquals("Jan", employee.getFirstName());
    }

    @Test
    void testGetLastName() throws InvalidDataException {
        Employee employee = new Employee("Jan Kowalski", "jan@techcorp.com", "TechCorp", Position.MANAGER);
        assertEquals("Kowalski", employee.getLastName());
    }

    @Test
    void testGetFirstNameWithMultipleNames() throws InvalidDataException {
        Employee employee = new Employee("Jan Maria Kowalski", "jan@techcorp.com", "TechCorp", Position.MANAGER);
        assertEquals("Jan", employee.getFirstName());
    }

    @Test
    void testGetLastNameWithMultipleNames() throws InvalidDataException {
        Employee employee = new Employee("Jan Maria Kowalski", "jan@techcorp.com", "TechCorp", Position.MANAGER);
        assertEquals("Kowalski", employee.getLastName());
    }

    @Test
    void testGetFirstNameWithSingleName() throws InvalidDataException {
        Employee employee = new Employee("Jan", "jan@techcorp.com", "TechCorp", Position.MANAGER);
        assertEquals("Jan", employee.getFirstName());
    }

    @Test
    void testGetLastNameWithSingleName() throws InvalidDataException {
        Employee employee = new Employee("Jan", "jan@techcorp.com", "TechCorp", Position.MANAGER);
        assertEquals("Jan", employee.getLastName());
    }

    @Test
    void testEmployeeWithMinimalSalary() throws InvalidDataException {
        Employee employee = new Employee("Jan Kowalski", "jan@techcorp.com", "TechCorp", Position.PROGRAMMER, 0.0);
        assertEquals(0.0, employee.getSalary(), 0.001);
    }

    @Test
    void testEmployeeWithHighSalary() throws InvalidDataException {
        Employee employee = new Employee("Jan Kowalski", "jan@techcorp.com", "TechCorp", Position.PROGRAMMER, 1000000.0);
        assertEquals(1000000.0, employee.getSalary(), 0.001);
    }

    @Test
    void testEmployeeWithExactBaseSalary() throws InvalidDataException {
        Employee employee = new Employee("Jan Kowalski", "jan@techcorp.com", "TechCorp", Position.MANAGER, 12000.0);
        assertEquals(12000.0, employee.getSalary(), 0.001);
    }

    @Test
    void testEmployeeWithSalaryAboveBase() throws InvalidDataException {
        Employee employee = new Employee("Jan Kowalski", "jan@techcorp.com", "TechCorp", Position.PROGRAMMER, 15000.0);
        assertEquals(15000.0, employee.getSalary(), 0.001);
    }

    @Test
    void testEmployeeCreationTrimsSpaces() throws InvalidDataException {
        Employee employee = new Employee("  Jan Kowalski  ", "  JAN@TECHCORP.COM  ", "  TechCorp  ", Position.MANAGER, 15000);

        assertEquals("Jan Kowalski", employee.getName());
        assertEquals("jan@techcorp.com", employee.getEmail());
        assertEquals("TechCorp", employee.getCompany());
    }

    @Test
    void testToStringContainsAllInformation() throws InvalidDataException {
        Employee employee = new Employee("Jan Kowalski", "jan@techcorp.com", "TechCorp", Position.MANAGER, 15000.50);
        String toString = employee.toString();

        assertTrue(toString.contains("Jan Kowalski"));
        assertTrue(toString.contains("jan@techcorp.com"));
        assertTrue(toString.contains("TechCorp"));
        assertTrue(toString.contains("MANAGER"));
        assertTrue(toString.contains("15000.50"));
    }

    @Test
    void testEqualsWithNull() throws InvalidDataException {
        Employee employee = new Employee("Jan Kowalski", "jan@techcorp.com", "TechCorp", Position.MANAGER);
        assertNotEquals(null, employee);
    }

    @Test
    void testEqualsWithDifferentClass() throws InvalidDataException {
        Employee employee = new Employee("Jan Kowalski", "jan@techcorp.com", "TechCorp", Position.MANAGER);
        assertNotEquals("Not an employee", employee);
    }
}