
import org.example.Employee;
import org.example.Position;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class EmployeeTest {

    @Test
    void testEmployeeCreationWithValidData() {
        Employee employee = new Employee("Jan Kowalski", "jan@techcorp.com", "TechCorp", Position.MANAGER, 15000);

        assertEquals("Jan Kowalski", employee.getName());
        assertEquals("jan@techcorp.com", employee.getEmail());
        assertEquals("TechCorp", employee.getCompany());
        assertEquals(Position.MANAGER, employee.getPosition());
        assertEquals(15000, employee.getSalary(), 0.001);
    }

    @Test
    void testEmployeeCreationWithBaseSalary() {
        Employee employee = new Employee("Anna Nowak", "anna@techcorp.com", "TechCorp", Position.PROGRAMMER);

        assertEquals(Position.PROGRAMMER.getBaseSalary(), employee.getSalary(), 0.001);
    }

    @Test
    void testEmployeeCreationThrowsExceptionForInvalidData() {
        assertThrows(IllegalArgumentException.class, () ->
                new Employee(null, "test@test.com", "TechCorp", Position.PROGRAMMER, 5000));

        assertThrows(IllegalArgumentException.class, () ->
                new Employee("Jan Kowalski", null, "TechCorp", Position.PROGRAMMER, 5000));

        assertThrows(IllegalArgumentException.class, () ->
                new Employee("Jan Kowalski", "test@test.com", null, Position.PROGRAMMER, 5000));

        assertThrows(IllegalArgumentException.class, () ->
                new Employee("Jan Kowalski", "test@test.com", "TechCorp", null, 5000));

        assertThrows(IllegalArgumentException.class, () ->
                new Employee("Jan Kowalski", "test@test.com", "TechCorp", Position.PROGRAMMER, -1000));
    }

    @Test
    void testEmailIsNormalizedToLowerCase() {
        Employee employee = new Employee("Jan Kowalski", "JAN@TECHCORP.COM", "TechCorp", Position.MANAGER, 15000);
        assertEquals("jan@techcorp.com", employee.getEmail());
    }

    @Test
    void testEqualsAndHashCodeBasedOnEmail() {
        Employee emp1 = new Employee("Jan Kowalski", "jan@techcorp.com", "TechCorp", Position.MANAGER, 15000);
        Employee emp2 = new Employee("Jan Nowak", "jan@techcorp.com", "OtherCorp", Position.PROGRAMMER, 8000);
        Employee emp3 = new Employee("Anna Kowalski", "anna@techcorp.com", "TechCorp", Position.MANAGER, 15000);

        assertEquals(emp1, emp2);
        assertNotEquals(emp1, emp3);
        assertEquals(emp1.hashCode(), emp2.hashCode());
        assertNotEquals(emp1.hashCode(), emp3.hashCode());
    }

}