import src.model.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import src.exception.InvalidDataException;
import static org.junit.jupiter.api.Assertions.*;

class EmployeeTest {

    @Test
    @DisplayName("Should create employee with valid data")
    void testEmployeeCreationWithValidData() throws InvalidDataException {
        Employee employee = new Employee("Jan Kowalski", "jan@techcorp.com", "TechCorp", Position.MANAGER, 15000.0);

        assertAll("Employee creation validation",
                () -> assertEquals("Jan Kowalski", employee.getName()),
                () -> assertEquals("jan@techcorp.com", employee.getEmail()),
                () -> assertEquals("TechCorp", employee.getCompany()),
                () -> assertEquals(Position.MANAGER, employee.getPosition()),
                () -> assertEquals(15000.0, employee.getSalary(), 0.001)
        );
    }

    @Test
    @DisplayName("Should create employee with position-only constructor")
    void testEmployeeCreationWithPositionOnly() throws InvalidDataException {
        Employee employee = new Employee("Jan Kowalski", "jan@techcorp.com", "TechCorp", Position.PROGRAMMER);

        assertAll("Position-only constructor validation",
                () -> assertEquals(Position.PROGRAMMER, employee.getPosition()),
                () -> assertEquals(Position.PROGRAMMER.getBaseSalary(), employee.getSalary(), 0.001)
        );
    }

    @Test
    @DisplayName("Should throw exception when name is null")
    void testEmployeeCreationWithNullName() {
        InvalidDataException exception = assertThrows(InvalidDataException.class, () ->
                new Employee(null, "jan@techcorp.com", "TechCorp", Position.MANAGER)
        );
        assertTrue(exception.getMessage().contains("Name cannot be null or empty"));
    }

    @Test
    @DisplayName("Should throw exception when name is empty")
    void testEmployeeCreationWithEmptyName() {
        InvalidDataException exception = assertThrows(InvalidDataException.class, () ->
                new Employee("", "jan@techcorp.com", "TechCorp", Position.MANAGER)
        );
        assertTrue(exception.getMessage().contains("Name cannot be null or empty"));
    }

    @Test
    @DisplayName("Should throw exception when email is null")
    void testEmployeeCreationWithNullEmail() {
        InvalidDataException exception = assertThrows(InvalidDataException.class, () ->
                new Employee("Jan Kowalski", null, "TechCorp", Position.MANAGER)
        );
        assertTrue(exception.getMessage().contains("Email cannot be null or empty"));
    }

    @Test
    @DisplayName("Should throw exception when email is empty")
    void testEmployeeCreationWithEmptyEmail() {
        InvalidDataException exception = assertThrows(InvalidDataException.class, () ->
                new Employee("Jan Kowalski", "", "TechCorp", Position.MANAGER)
        );
        assertTrue(exception.getMessage().contains("Email cannot be null or empty"));
    }

    @Test
    @DisplayName("Should throw exception when company is null")
    void testEmployeeCreationWithNullCompany() {
        InvalidDataException exception = assertThrows(InvalidDataException.class, () ->
                new Employee("Jan Kowalski", "jan@techcorp.com", null, Position.MANAGER)
        );
        assertTrue(exception.getMessage().contains("Company cannot be null or empty"));
    }

    @Test
    @DisplayName("Should throw exception when company is empty")
    void testEmployeeCreationWithEmptyCompany() {
        InvalidDataException exception = assertThrows(InvalidDataException.class, () ->
                new Employee("Jan Kowalski", "jan@techcorp.com", "", Position.MANAGER)
        );
        assertTrue(exception.getMessage().contains("Company cannot be null or empty"));
    }

    @Test
    @DisplayName("Should throw exception when salary is negative")
    void testEmployeeCreationWithNegativeSalary() {
        InvalidDataException exception = assertThrows(InvalidDataException.class, () ->
                new Employee("Jan Kowalski", "jan@techcorp.com", "TechCorp", Position.MANAGER, -1000.0)
        );
        assertTrue(exception.getMessage().contains("Salary cannot be negative"));
    }

    @Test
    @DisplayName("Should get first and last name from full name")
    void testGetFirstAndLastName() throws InvalidDataException {
        Employee employee = new Employee("Jan Kowalski", "jan@techcorp.com", "TechCorp", Position.MANAGER);

        assertAll("Name parsing validation",
                () -> assertEquals("Jan", employee.getFirstName()),
                () -> assertEquals("Kowalski", employee.getLastName())
        );
    }

    @Test
    @DisplayName("Should handle single name for first and last name")
    void testSingleName() throws InvalidDataException {
        Employee employee = new Employee("Jan", "jan@techcorp.com", "TechCorp", Position.MANAGER);

        assertAll("Single name validation",
                () -> assertEquals("Jan", employee.getFirstName()),
                () -> assertEquals("Jan", employee.getLastName())
        );
    }

    @Test
    @DisplayName("Should handle multiple names")
    void testMultipleNames() throws InvalidDataException {
        Employee employee = new Employee("Jan Maria Kowalski", "jan@techcorp.com", "TechCorp", Position.MANAGER);

        assertAll("Multiple names validation",
                () -> assertEquals("Jan", employee.getFirstName()),
                () -> assertEquals("Kowalski", employee.getLastName())
        );
    }

    @Test
    @DisplayName("Should trim spaces from inputs")
    void testEmployeeCreationTrimsSpaces() throws InvalidDataException {
        Employee employee = new Employee("  Jan Kowalski  ", "  JAN@TECHCORP.COM  ", "  TechCorp  ", Position.MANAGER, 15000);

        assertAll("Input trimming validation",
                () -> assertEquals("Jan Kowalski", employee.getName()),
                () -> assertEquals("jan@techcorp.com", employee.getEmail()),
                () -> assertEquals("TechCorp", employee.getCompany())
        );
    }

    @Test
    @DisplayName("Should set name with validation")
    void testSetName() throws InvalidDataException {
        Employee employee = new Employee("Jan Kowalski", "jan@techcorp.com", "TechCorp", Position.MANAGER);
        employee.setName("Anna Nowak");

        assertEquals("Anna Nowak", employee.getName());
    }

    @Test
    @DisplayName("Should throw exception when setting null name")
    void testSetNullName() throws InvalidDataException {
        Employee employee = new Employee("Jan Kowalski", "jan@techcorp.com", "TechCorp", Position.MANAGER);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                employee.setName(null)
        );
        assertEquals("Name cannot be null or empty", exception.getMessage());
    }

    @Test
    @DisplayName("Should throw exception when setting empty name")
    void testSetEmptyName() throws InvalidDataException {
        Employee employee = new Employee("Jan Kowalski", "jan@techcorp.com", "TechCorp", Position.MANAGER);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                employee.setName("")
        );
        assertEquals("Name cannot be null or empty", exception.getMessage());
    }

    @Test
    @DisplayName("Should set company with validation")
    void testSetCompany() throws InvalidDataException {
        Employee employee = new Employee("Jan Kowalski", "jan@techcorp.com", "TechCorp", Position.MANAGER);
        employee.setCompany("NewCorp");

        assertEquals("NewCorp", employee.getCompany());
    }

    @Test
    @DisplayName("Should throw exception when setting null company")
    void testSetNullCompany() throws InvalidDataException {
        Employee employee = new Employee("Jan Kowalski", "jan@techcorp.com", "TechCorp", Position.MANAGER);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                employee.setCompany(null)
        );
        assertEquals("Company cannot be null or empty", exception.getMessage());
    }

    @Test
    @DisplayName("Should throw exception when setting empty company")
    void testSetEmptyCompany() throws InvalidDataException {
        Employee employee = new Employee("Jan Kowalski", "jan@techcorp.com", "TechCorp", Position.MANAGER);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                employee.setCompany("")
        );
        assertEquals("Company cannot be null or empty", exception.getMessage());
    }

    @Test
    @DisplayName("Should set position and adjust salary if needed")
    void testSetPosition() throws InvalidDataException {
        Employee employee = new Employee("Jan Kowalski", "jan@techcorp.com", "TechCorp", Position.PROGRAMMER, 5000.0);
        employee.setPosition(Position.MANAGER);

        assertAll("Position change validation",
                () -> assertEquals(Position.MANAGER, employee.getPosition()),
                () -> assertEquals(Position.MANAGER.getBaseSalary(), employee.getSalary(), 0.001)
        );
    }

    @Test
    @DisplayName("Should keep higher salary when setting position")
    void testSetPositionKeepsHigherSalary() throws InvalidDataException {
        Employee employee = new Employee("Jan Kowalski", "jan@techcorp.com", "TechCorp", Position.MANAGER, 20000.0);
        employee.setPosition(Position.VICE_PRESIDENT);

        assertAll("Salary retention validation",
                () -> assertEquals(Position.VICE_PRESIDENT, employee.getPosition()),
                () -> assertEquals(20000.0, employee.getSalary(), 0.001)
        );
    }

    @Test
    @DisplayName("Should throw exception when setting null position")
    void testSetNullPosition() throws InvalidDataException {
        Employee employee = new Employee("Jan Kowalski", "jan@techcorp.com", "TechCorp", Position.MANAGER);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                employee.setPosition(null)
        );
        assertEquals("Position cannot be null", exception.getMessage());
    }

    @Test
    @DisplayName("Should set valid salary")
    void testSetSalary() throws InvalidDataException {
        Employee employee = new Employee("Jan Kowalski", "jan@techcorp.com", "TechCorp", Position.MANAGER, 15000.0);
        employee.setSalary(18000.0);

        assertEquals(18000.0, employee.getSalary(), 0.001);
    }

    @Test
    @DisplayName("Should adjust salary to position base when setting lower salary")
    void testSetSalaryBelowBase() throws InvalidDataException {
        Employee employee = new Employee("Jan Kowalski", "jan@techcorp.com", "TechCorp", Position.MANAGER);
        employee.setSalary(10000.0);

        assertEquals(12000.0, employee.getSalary(), 0.001);
    }

    @Test
    @DisplayName("Should throw exception when setting negative salary")
    void testSetNegativeSalary() throws InvalidDataException {
        Employee employee = new Employee("Jan Kowalski", "jan@techcorp.com", "TechCorp", Position.MANAGER);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                employee.setSalary(-1000.0)
        );
        assertEquals("Salary cannot be negative", exception.getMessage());
    }

    @Test
    @DisplayName("Should correctly implement equals based on email")
    void testEquals() throws InvalidDataException {
        Employee employee1 = new Employee("Jan Kowalski", "jan@techcorp.com", "TechCorp", Position.MANAGER);
        Employee employee2 = new Employee("Jan Kowalski", "jan@techcorp.com", "DifferentCorp", Position.PROGRAMMER);
        Employee employee3 = new Employee("Anna Nowak", "anna@techcorp.com", "TechCorp", Position.MANAGER);

        assertAll("Equals implementation validation",
                () -> assertEquals(employee1, employee2),
                () -> assertNotEquals(employee1, employee3),
                () -> assertNotEquals(null, employee1),
                () -> assertNotEquals("Not an employee", employee1)
        );
    }

    @Test
    @DisplayName("Should have consistent hashCode")
    void testHashCode() throws InvalidDataException {
        Employee employee1 = new Employee("Jan Kowalski", "jan@techcorp.com", "TechCorp", Position.MANAGER);
        Employee employee2 = new Employee("Jan Kowalski", "jan@techcorp.com", "DifferentCorp", Position.PROGRAMMER);

        assertEquals(employee1.hashCode(), employee2.hashCode());
    }

    @Test
    @DisplayName("Should return informative toString")
    void testToString() throws InvalidDataException {
        Employee employee = new Employee("Jan Kowalski", "jan@techcorp.com", "TechCorp", Position.MANAGER, 15000.50);
        String result = employee.toString();

        assertAll("ToString content validation",
                () -> assertTrue(result.contains("Jan Kowalski")),
                () -> assertTrue(result.contains("jan@techcorp.com")),
                () -> assertTrue(result.contains("TechCorp")),
                () -> assertTrue(result.contains("MANAGER")),
                () -> assertTrue(result.contains("15000.50"))
        );
    }

    @Test
    @DisplayName("Should handle email case insensitivity in equals")
    void testEmailCaseInsensitivity() throws InvalidDataException {
        Employee employee1 = new Employee("Jan Kowalski", "JAN@techcorp.com", "TechCorp", Position.MANAGER);
        Employee employee2 = new Employee("Jan Kowalski", "jan@TECHCORP.COM", "TechCorp", Position.MANAGER);

        assertAll("Email case insensitivity validation",
                () -> assertEquals(employee1, employee2),
                () -> assertEquals(employee1.hashCode(), employee2.hashCode())
        );
    }
}