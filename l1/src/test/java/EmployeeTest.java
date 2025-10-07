import org.example.Employee;
import org.example.Position;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class EmployeeTest {

    @Test
    void testGetFirstName() {
        Employee employee = new Employee("Jan Kowalski", "jan@techcorp.com", "TechCorp", Position.MANAGER);
        assertEquals("Jan", employee.getFirstName());
    }

    @Test
    void testGetLastName() {
        Employee employee = new Employee("Jan Kowalski", "jan@techcorp.com", "TechCorp", Position.MANAGER);
        assertEquals("Kowalski", employee.getLastName());
    }

    @Test
    void testGetFirstNameWithMultipleNames() {
        Employee employee = new Employee("Jan Maria Kowalski", "jan@techcorp.com", "TechCorp", Position.MANAGER);
        assertEquals("Jan", employee.getFirstName());
    }

    @Test
    void testGetLastNameWithMultipleNames() {
        Employee employee = new Employee("Jan Maria Kowalski", "jan@techcorp.com", "TechCorp", Position.MANAGER);
        assertEquals("Kowalski", employee.getLastName());
    }

    @Test
    void testGetFirstNameWithSingleName() {
        Employee employee = new Employee("Jan", "jan@techcorp.com", "TechCorp", Position.MANAGER);
        assertEquals("Jan", employee.getFirstName());
    }

    @Test
    void testGetLastNameWithSingleName() {
        Employee employee = new Employee("Jan", "jan@techcorp.com", "TechCorp", Position.MANAGER);
        assertEquals("Jan", employee.getLastName());
    }

    @Test
    void testEmployeeWithMinimalSalary() {
        Employee employee = new Employee("Jan Kowalski", "jan@techcorp.com", "TechCorp", Position.PROGRAMMER, 0.0);
        assertEquals(0.0, employee.getSalary(), 0.001);
    }

    @Test
    void testEmployeeWithHighSalary() {
        Employee employee = new Employee("Jan Kowalski", "jan@techcorp.com", "TechCorp", Position.PROGRAMMER, 1000000.0);
        assertEquals(1000000.0, employee.getSalary(), 0.001);
    }

    @Test
    void testEmployeeWithExactBaseSalary() {
        Employee employee = new Employee("Jan Kowalski", "jan@techcorp.com", "TechCorp", Position.MANAGER, 12000.0);
        assertEquals(12000.0, employee.getSalary(), 0.001);
    }

    @Test
    void testEmployeeWithSalaryAboveBase() {
        Employee employee = new Employee("Jan Kowalski", "jan@techcorp.com", "TechCorp", Position.PROGRAMMER, 15000.0);
        assertEquals(15000.0, employee.getSalary(), 0.001);
    }

    @Test
    void testEmployeeCreationTrimsSpaces() {
        Employee employee = new Employee("  Jan Kowalski  ", "  JAN@TECHCORP.COM  ", "  TechCorp  ", Position.MANAGER, 15000);

        assertEquals("Jan Kowalski", employee.getName());
        assertEquals("jan@techcorp.com", employee.getEmail());
        assertEquals("TechCorp", employee.getCompany());
    }

    @Test
    void testToStringContainsAllInformation() {
        Employee employee = new Employee("Jan Kowalski", "jan@techcorp.com", "TechCorp", Position.MANAGER, 15000.50);
        String toString = employee.toString();

        assertTrue(toString.contains("Jan Kowalski"));
        assertTrue(toString.contains("jan@techcorp.com"));
        assertTrue(toString.contains("TechCorp"));
        assertTrue(toString.contains("MANAGER"));
        assertTrue(toString.contains("15000.50"));
    }

    @Test
    void testEqualsWithNull() {
        Employee employee = new Employee("Jan Kowalski", "jan@techcorp.com", "TechCorp", Position.MANAGER);
        assertNotEquals(null, employee);
    }

    @Test
    void testEqualsWithDifferentClass() {
        Employee employee = new Employee("Jan Kowalski", "jan@techcorp.com", "TechCorp", Position.MANAGER);
        assertNotEquals("Not an employee", employee);
    }
}