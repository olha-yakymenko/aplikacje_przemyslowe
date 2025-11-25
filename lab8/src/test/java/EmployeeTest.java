//import com.techcorp.employee.model.Employee;
//import com.techcorp.employee.model.Position;
//import org.junit.jupiter.api.Test;
//import org.junit.jupiter.api.DisplayName;
//import com.techcorp.employee.exception.InvalidDataException;
//import static org.junit.jupiter.api.Assertions.*;
//
//class EmployeeTest {
//
//    @Test
//    @DisplayName("Should create employee with valid data")
//    void testEmployeeCreationWithValidData() throws InvalidDataException {
//        Employee employee = new Employee("Jan Kowalski", "jan@techcorp.com", "TechCorp", Position.MANAGER, 15000.0);
//
//        assertAll("Employee creation validation",
//                () -> assertEquals("Jan Kowalski", employee.getName()),
//                () -> assertEquals("jan@techcorp.com", employee.getEmail()),
//                () -> assertEquals("TechCorp", employee.getCompany()),
//                () -> assertEquals(Position.MANAGER, employee.getPosition()),
//                () -> assertEquals(15000.0, employee.getSalary(), 0.001)
//        );
//    }
//
//    @Test
//    @DisplayName("Should create employee with position-only constructor")
//    void testEmployeeCreationWithPositionOnly() throws InvalidDataException {
//        Employee employee = new Employee("Jan Kowalski", "jan@techcorp.com", "TechCorp", Position.PROGRAMMER);
//
//        assertAll("Position-only constructor validation",
//                () -> assertEquals(Position.PROGRAMMER, employee.getPosition()),
//                () -> assertEquals(Position.PROGRAMMER.getBaseSalary(), employee.getSalary(), 0.001)
//        );
//    }
//
//    @Test
//    @DisplayName("Should throw exception when name is null")
//    void testEmployeeCreationWithNullName() {
//        InvalidDataException exception = assertThrows(InvalidDataException.class, () ->
//                new Employee(null, "jan@techcorp.com", "TechCorp", Position.MANAGER)
//        );
//        assertTrue(exception.getMessage().contains("Name cannot be null or empty"));
//    }
//
//    @Test
//    @DisplayName("Should throw exception when name is empty")
//    void testEmployeeCreationWithEmptyName() {
//        InvalidDataException exception = assertThrows(InvalidDataException.class, () ->
//                new Employee("", "jan@techcorp.com", "TechCorp", Position.MANAGER)
//        );
//        assertTrue(exception.getMessage().contains("Name cannot be null or empty"));
//    }
//
//    @Test
//    @DisplayName("Should throw exception when email is null")
//    void testEmployeeCreationWithNullEmail() {
//        InvalidDataException exception = assertThrows(InvalidDataException.class, () ->
//                new Employee("Jan Kowalski", null, "TechCorp", Position.MANAGER)
//        );
//        assertTrue(exception.getMessage().contains("Email cannot be null or empty"));
//    }
//
//    @Test
//    @DisplayName("Should throw exception when email is empty")
//    void testEmployeeCreationWithEmptyEmail() {
//        InvalidDataException exception = assertThrows(InvalidDataException.class, () ->
//                new Employee("Jan Kowalski", "", "TechCorp", Position.MANAGER)
//        );
//        assertTrue(exception.getMessage().contains("Email cannot be null or empty"));
//    }
//
//    @Test
//    @DisplayName("Should throw exception when company is null")
//    void testEmployeeCreationWithNullCompany() {
//        InvalidDataException exception = assertThrows(InvalidDataException.class, () ->
//                new Employee("Jan Kowalski", "jan@techcorp.com", null, Position.MANAGER)
//        );
//        assertTrue(exception.getMessage().contains("Company cannot be null or empty"));
//    }
//
//    @Test
//    @DisplayName("Should throw exception when company is empty")
//    void testEmployeeCreationWithEmptyCompany() {
//        InvalidDataException exception = assertThrows(InvalidDataException.class, () ->
//                new Employee("Jan Kowalski", "jan@techcorp.com", "", Position.MANAGER)
//        );
//        assertTrue(exception.getMessage().contains("Company cannot be null or empty"));
//    }
//
//    @Test
//    @DisplayName("Should throw exception when salary is negative")
//    void testEmployeeCreationWithNegativeSalary() {
//        InvalidDataException exception = assertThrows(InvalidDataException.class, () ->
//                new Employee("Jan Kowalski", "jan@techcorp.com", "TechCorp", Position.MANAGER, -1000.0)
//        );
//        assertTrue(exception.getMessage().contains("Salary cannot be negative"));
//    }
//
//    @Test
//    @DisplayName("Should get first and last name from full name")
//    void testGetFirstAndLastName() throws InvalidDataException {
//        Employee employee = new Employee("Jan Kowalski", "jan@techcorp.com", "TechCorp", Position.MANAGER);
//
//        assertAll("Name parsing validation",
//                () -> assertEquals("Jan", employee.getFirstName()),
//                () -> assertEquals("Kowalski", employee.getLastName())
//        );
//    }
//
//    @Test
//    @DisplayName("Should handle single name for first and last name")
//    void testSingleName() throws InvalidDataException {
//        Employee employee = new Employee("Jan", "jan@techcorp.com", "TechCorp", Position.MANAGER);
//
//        assertAll("Single name validation",
//                () -> assertEquals("Jan", employee.getFirstName()),
//                () -> assertEquals("Jan", employee.getLastName())
//        );
//    }
//
//    @Test
//    @DisplayName("Should handle multiple names")
//    void testMultipleNames() throws InvalidDataException {
//        Employee employee = new Employee("Jan Maria Kowalski", "jan@techcorp.com", "TechCorp", Position.MANAGER);
//
//        assertAll("Multiple names validation",
//                () -> assertEquals("Jan", employee.getFirstName()),
//                () -> assertEquals("Kowalski", employee.getLastName())
//        );
//    }
//
//    @Test
//    @DisplayName("Should trim spaces from inputs")
//    void testEmployeeCreationTrimsSpaces() throws InvalidDataException {
//        Employee employee = new Employee("  Jan Kowalski  ", "  JAN@TECHCORP.COM  ", "  TechCorp  ", Position.MANAGER, 15000);
//
//        assertAll("Input trimming validation",
//                () -> assertEquals("Jan Kowalski", employee.getName()),
//                () -> assertEquals("jan@techcorp.com", employee.getEmail()),
//                () -> assertEquals("TechCorp", employee.getCompany())
//        );
//    }
//
//    @Test
//    @DisplayName("Should set name with validation")
//    void testSetName() throws InvalidDataException {
//        Employee employee = new Employee("Jan Kowalski", "jan@techcorp.com", "TechCorp", Position.MANAGER);
//        employee.setName("Anna Nowak");
//
//        assertEquals("Anna Nowak", employee.getName());
//    }
//
//    @Test
//    @DisplayName("Should throw exception when setting null name")
//    void testSetNullName() throws InvalidDataException {
//        Employee employee = new Employee("Jan Kowalski", "jan@techcorp.com", "TechCorp", Position.MANAGER);
//
//        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
//                employee.setName(null)
//        );
//        assertEquals("Name cannot be null or empty", exception.getMessage());
//    }
//
//    @Test
//    @DisplayName("Should throw exception when setting empty name")
//    void testSetEmptyName() throws InvalidDataException {
//        Employee employee = new Employee("Jan Kowalski", "jan@techcorp.com", "TechCorp", Position.MANAGER);
//
//        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
//                employee.setName("")
//        );
//        assertEquals("Name cannot be null or empty", exception.getMessage());
//    }
//
//    @Test
//    @DisplayName("Should set company with validation")
//    void testSetCompany() throws InvalidDataException {
//        Employee employee = new Employee("Jan Kowalski", "jan@techcorp.com", "TechCorp", Position.MANAGER);
//        employee.setCompany("NewCorp");
//
//        assertEquals("NewCorp", employee.getCompany());
//    }
//
//    @Test
//    @DisplayName("Should throw exception when setting null company")
//    void testSetNullCompany() throws InvalidDataException {
//        Employee employee = new Employee("Jan Kowalski", "jan@techcorp.com", "TechCorp", Position.MANAGER);
//
//        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
//                employee.setCompany(null)
//        );
//        assertEquals("Company cannot be null or empty", exception.getMessage());
//    }
//
//    @Test
//    @DisplayName("Should throw exception when setting empty company")
//    void testSetEmptyCompany() throws InvalidDataException {
//        Employee employee = new Employee("Jan Kowalski", "jan@techcorp.com", "TechCorp", Position.MANAGER);
//
//        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
//                employee.setCompany("")
//        );
//        assertEquals("Company cannot be null or empty", exception.getMessage());
//    }
//
//    @Test
//    @DisplayName("Should set position and adjust salary if needed")
//    void testSetPosition() throws InvalidDataException {
//        Employee employee = new Employee("Jan Kowalski", "jan@techcorp.com", "TechCorp", Position.PROGRAMMER, 5000.0);
//        employee.setPosition(Position.MANAGER);
//
//        assertAll("Position change validation",
//                () -> assertEquals(Position.MANAGER, employee.getPosition()),
//                () -> assertEquals(Position.MANAGER.getBaseSalary(), employee.getSalary(), 0.001)
//        );
//    }
//
//    @Test
//    @DisplayName("Should keep higher salary when setting position")
//    void testSetPositionKeepsHigherSalary() throws InvalidDataException {
//        Employee employee = new Employee("Jan Kowalski", "jan@techcorp.com", "TechCorp", Position.MANAGER, 20000.0);
//        employee.setPosition(Position.VICE_PRESIDENT);
//
//        assertAll("Salary retention validation",
//                () -> assertEquals(Position.VICE_PRESIDENT, employee.getPosition()),
//                () -> assertEquals(20000.0, employee.getSalary(), 0.001)
//        );
//    }
//
//    @Test
//    @DisplayName("Should throw exception when setting null position")
//    void testSetNullPosition() throws InvalidDataException {
//        Employee employee = new Employee("Jan Kowalski", "jan@techcorp.com", "TechCorp", Position.MANAGER);
//
//        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
//                employee.setPosition(null)
//        );
//        assertEquals("Position cannot be null", exception.getMessage());
//    }
//
//    @Test
//    @DisplayName("Should set valid salary")
//    void testSetSalary() throws InvalidDataException {
//        Employee employee = new Employee("Jan Kowalski", "jan@techcorp.com", "TechCorp", Position.MANAGER, 15000.0);
//        employee.setSalary(18000.0);
//
//        assertEquals(18000.0, employee.getSalary(), 0.001);
//    }
//
//    @Test
//    @DisplayName("Should adjust salary to position base when setting lower salary")
//    void testSetSalaryBelowBase() throws InvalidDataException {
//        Employee employee = new Employee("Jan Kowalski", "jan@techcorp.com", "TechCorp", Position.MANAGER);
//        employee.setSalary(10000.0);
//
//        assertEquals(12000.0, employee.getSalary(), 0.001);
//    }
//
//    @Test
//    @DisplayName("Should throw exception when setting negative salary")
//    void testSetNegativeSalary() throws InvalidDataException {
//        Employee employee = new Employee("Jan Kowalski", "jan@techcorp.com", "TechCorp", Position.MANAGER);
//
//        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
//                employee.setSalary(-1000.0)
//        );
//        assertEquals("Salary cannot be negative", exception.getMessage());
//    }
//
//    @Test
//    @DisplayName("Should correctly implement equals based on email")
//    void testEquals() throws InvalidDataException {
//        Employee employee1 = new Employee("Jan Kowalski", "jan@techcorp.com", "TechCorp", Position.MANAGER);
//        Employee employee2 = new Employee("Jan Kowalski", "jan@techcorp.com", "DifferentCorp", Position.PROGRAMMER);
//        Employee employee3 = new Employee("Anna Nowak", "anna@techcorp.com", "TechCorp", Position.MANAGER);
//
//        assertAll("Equals implementation validation",
//                () -> assertEquals(employee1, employee2),
//                () -> assertNotEquals(employee1, employee3),
//                () -> assertNotEquals(null, employee1),
//                () -> assertNotEquals("Not an employee", employee1)
//        );
//    }
//
//    @Test
//    @DisplayName("Should have consistent hashCode")
//    void testHashCode() throws InvalidDataException {
//        Employee employee1 = new Employee("Jan Kowalski", "jan@techcorp.com", "TechCorp", Position.MANAGER);
//        Employee employee2 = new Employee("Jan Kowalski", "jan@techcorp.com", "DifferentCorp", Position.PROGRAMMER);
//
//        assertEquals(employee1.hashCode(), employee2.hashCode());
//    }
//
//    @Test
//    @DisplayName("Should return informative toString")
//    void testToString() throws InvalidDataException {
//        Employee employee = new Employee("Jan Kowalski", "jan@techcorp.com", "TechCorp", Position.MANAGER, 15000.50);
//        String result = employee.toString();
//
//        assertAll("ToString content validation",
//                () -> assertTrue(result.contains("Jan Kowalski")),
//                () -> assertTrue(result.contains("jan@techcorp.com")),
//                () -> assertTrue(result.contains("TechCorp")),
//                () -> assertTrue(result.contains("MANAGER")),
//                () -> assertTrue(result.contains("15000.50"))
//        );
//    }
//
//    @Test
//    @DisplayName("Should handle email case insensitivity in equals")
//    void testEmailCaseInsensitivity() throws InvalidDataException {
//        Employee employee1 = new Employee("Jan Kowalski", "JAN@techcorp.com", "TechCorp", Position.MANAGER);
//        Employee employee2 = new Employee("Jan Kowalski", "jan@TECHCORP.COM", "TechCorp", Position.MANAGER);
//
//        assertAll("Email case insensitivity validation",
//                () -> assertEquals(employee1, employee2),
//                () -> assertEquals(employee1.hashCode(), employee2.hashCode())
//        );
//    }
//}












package com.techcorp.employee.model;

import com.techcorp.employee.exception.InvalidDataException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

class EmployeeTest {

    @Test
    @DisplayName("Should create employee with valid data using constructor with salary")
    void testEmployeeCreationWithValidData() throws InvalidDataException {
        Employee employee = new Employee("Jan Kowalski", "jan@techcorp.com", "TechCorp", Position.MANAGER, 15000.0);

        assertAll("Employee creation validation",
                () -> assertEquals("Jan Kowalski", employee.getName()),
                () -> assertEquals("jan@techcorp.com", employee.getEmail()),
                () -> assertEquals("TechCorp", employee.getCompany()),
                () -> assertEquals(Position.MANAGER, employee.getPosition()),
                () -> assertEquals(15000.0, employee.getSalary(), 0.001),
                () -> assertEquals(EmploymentStatus.ACTIVE, employee.getStatus())
        );
    }

    @Test
    @DisplayName("Should create employee with position-only constructor")
    void testEmployeeCreationWithPositionOnly() throws InvalidDataException {
        Employee employee = new Employee("Jan Kowalski", "jan@techcorp.com", "TechCorp", Position.PROGRAMMER);

        assertAll("Position-only constructor validation",
                () -> assertEquals(Position.PROGRAMMER, employee.getPosition()),
                () -> assertEquals(Position.PROGRAMMER.getBaseSalary(), employee.getSalary(), 0.001),
                () -> assertEquals(EmploymentStatus.ACTIVE, employee.getStatus())
        );
    }

    @Test
    @DisplayName("Should create employee with full constructor")
    void testEmployeeCreationWithFullConstructor() throws InvalidDataException {
        Employee employee = new Employee("Jan Kowalski", "jan@techcorp.com", "TechCorp",
                Position.MANAGER, 15000.0, EmploymentStatus.ON_LEAVE);

        assertAll("Full constructor validation",
                () -> assertEquals("Jan Kowalski", employee.getName()),
                () -> assertEquals("jan@techcorp.com", employee.getEmail()),
                () -> assertEquals("TechCorp", employee.getCompany()),
                () -> assertEquals(Position.MANAGER, employee.getPosition()),
                () -> assertEquals(15000.0, employee.getSalary(), 0.001),
                () -> assertEquals(EmploymentStatus.ON_LEAVE, employee.getStatus())
        );
    }

    @Test
    @DisplayName("Should create employee with no-args constructor")
    void testNoArgsConstructor() {
        Employee employee = new Employee();

        assertNotNull(employee);
        assertNull(employee.getId());
        assertNull(employee.getName());
        assertNull(employee.getEmail());
        assertNull(employee.getCompany());
        assertNull(employee.getPosition());
        assertNull(employee.getStatus());
        assertEquals(0.0, employee.getSalary(), 0.001);
        assertNull(employee.getDepartmentId());
        assertNull(employee.getPhotoFileName());
    }

    @Test
    @DisplayName("Should create employee with email-only constructor")
    void testEmailOnlyConstructor() {
        Employee employee = new Employee("jan@techcorp.com");

        assertEquals("jan@techcorp.com", employee.getEmail());
        assertNull(employee.getName());
        assertNull(employee.getCompany());
        assertNull(employee.getPosition());
        assertNull(employee.getStatus());
    }

    @ParameterizedTest
    @NullAndEmptySource
    @DisplayName("Should throw exception when name is null or empty")
    void testEmployeeCreationWithInvalidName(String invalidName) {
        InvalidDataException exception = assertThrows(InvalidDataException.class, () ->
                new Employee(invalidName, "jan@techcorp.com", "TechCorp", Position.MANAGER)
        );
        assertTrue(exception.getMessage().contains("Name cannot be null or empty"));
    }

    @ParameterizedTest
    @NullAndEmptySource
    @DisplayName("Should throw exception when email is null or empty")
    void testEmployeeCreationWithInvalidEmail(String invalidEmail) {
        InvalidDataException exception = assertThrows(InvalidDataException.class, () ->
                new Employee("Jan Kowalski", invalidEmail, "TechCorp", Position.MANAGER)
        );
        assertTrue(exception.getMessage().contains("Email cannot be null or empty"));
    }

    @ParameterizedTest
    @NullAndEmptySource
    @DisplayName("Should throw exception when company is null or empty")
    void testEmployeeCreationWithInvalidCompany(String invalidCompany) {
        InvalidDataException exception = assertThrows(InvalidDataException.class, () ->
                new Employee("Jan Kowalski", "jan@techcorp.com", invalidCompany, Position.MANAGER)
        );
        assertTrue(exception.getMessage().contains("Company cannot be null or empty"));
    }


    @Test
    @DisplayName("Should throw exception when status is null")
    void testEmployeeCreationWithNullStatus() {
        InvalidDataException exception = assertThrows(InvalidDataException.class, () ->
                new Employee("Jan Kowalski", "jan@techcorp.com", "TechCorp", Position.MANAGER, 15000.0, null)
        );
        assertTrue(exception.getMessage().contains("Employment status cannot be null"));
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
                () -> assertEquals("", employee.getLastName()) // Now returns empty string for single name
        );
    }

    @Test
    @DisplayName("Should handle multiple names correctly")
    void testMultipleNames() throws InvalidDataException {
        Employee employee = new Employee("Jan Maria Kowalski", "jan@techcorp.com", "TechCorp", Position.MANAGER);

        assertAll("Multiple names validation",
                () -> assertEquals("Jan", employee.getFirstName()),
                () -> assertEquals("Maria Kowalski", employee.getLastName()) // Now returns all parts after first
        );
    }

    @Test
    @DisplayName("Should handle empty name for first and last name")
    void testEmptyName() {
        Employee employee = new Employee();

        assertEquals("", employee.getFirstName());
        assertEquals("", employee.getLastName());
    }

    @Test
    @DisplayName("Should trim spaces from inputs during construction")
    void testEmployeeCreationTrimsSpaces() throws InvalidDataException {
        Employee employee = new Employee("  Jan Kowalski  ", "  JAN@TECHCORP.COM  ", "  TechCorp  ", Position.MANAGER, 15000);

        assertAll("Input trimming validation",
                () -> assertEquals("Jan Kowalski", employee.getName()),
                () -> assertEquals("jan@techcorp.com", employee.getEmail()),
                () -> assertEquals("TechCorp", employee.getCompany())
        );
    }

    @Test
    @DisplayName("Should set and get ID")
    void testIdSetterAndGetter() {
        Employee employee = new Employee();
        employee.setId(1L);

        assertEquals(1L, employee.getId());
    }

    @Test
    @DisplayName("Should set name with validation")
    void testSetName() throws InvalidDataException {
        Employee employee = new Employee("Jan Kowalski", "jan@techcorp.com", "TechCorp", Position.MANAGER);
        employee.setName("Anna Nowak");

        assertEquals("Anna Nowak", employee.getName());
    }

    @ParameterizedTest
    @NullAndEmptySource
    @DisplayName("Should throw exception when setting null or empty name")
    void testSetInvalidName(String invalidName) throws InvalidDataException {
        Employee employee = new Employee("Jan Kowalski", "jan@techcorp.com", "TechCorp", Position.MANAGER);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                employee.setName(invalidName)
        );
        assertEquals("Name cannot be null or empty", exception.getMessage());
    }

    @Test
    @DisplayName("Should set email with validation and normalization")
    void testSetEmail() {
        Employee employee = new Employee();
        employee.setEmail("  TEST@TECHCORP.COM  ");

        assertEquals("test@techcorp.com", employee.getEmail());
    }

    @ParameterizedTest
    @NullAndEmptySource
    @DisplayName("Should throw exception when setting null or empty email")
    void testSetInvalidEmail(String invalidEmail) {
        Employee employee = new Employee();

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                employee.setEmail(invalidEmail)
        );
        assertEquals("Email cannot be null or empty", exception.getMessage());
    }

    @Test
    @DisplayName("Should set company with validation")
    void testSetCompany() throws InvalidDataException {
        Employee employee = new Employee("Jan Kowalski", "jan@techcorp.com", "TechCorp", Position.MANAGER);
        employee.setCompany("NewCorp");

        assertEquals("NewCorp", employee.getCompany());
    }

    @ParameterizedTest
    @NullAndEmptySource
    @DisplayName("Should throw exception when setting null or empty company")
    void testSetInvalidCompany(String invalidCompany) throws InvalidDataException {
        Employee employee = new Employee("Jan Kowalski", "jan@techcorp.com", "TechCorp", Position.MANAGER);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                employee.setCompany(invalidCompany)
        );
        assertEquals("Company cannot be null or empty", exception.getMessage());
    }

    @Test
    @DisplayName("Should set position with validation")
    void testSetPosition() throws InvalidDataException {
        Employee employee = new Employee("Jan Kowalski", "jan@techcorp.com", "TechCorp", Position.PROGRAMMER, 5000.0);
        employee.setPosition(Position.MANAGER);

        assertEquals(Position.MANAGER, employee.getPosition());
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
    @DisplayName("Should throw exception when setting negative salary")
    void testSetNegativeSalary() throws InvalidDataException {
        Employee employee = new Employee("Jan Kowalski", "jan@techcorp.com", "TechCorp", Position.MANAGER);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                employee.setSalary(-1000.0)
        );
        assertEquals("Salary cannot be negative", exception.getMessage());
    }

    @Test
    @DisplayName("Should set status with validation")
    void testSetStatus() throws InvalidDataException {
        Employee employee = new Employee("Jan Kowalski", "jan@techcorp.com", "TechCorp", Position.MANAGER);
        employee.setStatus(EmploymentStatus.ON_LEAVE);

        assertEquals(EmploymentStatus.ON_LEAVE, employee.getStatus());
    }

    @Test
    @DisplayName("Should throw exception when setting null status")
    void testSetNullStatus() throws InvalidDataException {
        Employee employee = new Employee("Jan Kowalski", "jan@techcorp.com", "TechCorp", Position.MANAGER);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                employee.setStatus(null)
        );
        assertEquals("Status cannot be null", exception.getMessage());
    }

    @Test
    @DisplayName("Should set department ID")
    void testSetDepartmentId() {
        Employee employee = new Employee();
        employee.setDepartmentId(5L);

        assertEquals(5L, employee.getDepartmentId());
    }

    @Test
    @DisplayName("Should set null department ID")
    void testSetNullDepartmentId() {
        Employee employee = new Employee();
        employee.setDepartmentId(null);

        assertNull(employee.getDepartmentId());
    }

    @Test
    @DisplayName("Should set photo file name")
    void testSetPhotoFileName() {
        Employee employee = new Employee();
        employee.setPhotoFileName("photo.jpg");

        assertEquals("photo.jpg", employee.getPhotoFileName());
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
                () -> assertNotEquals("Not an employee", employee1),
                () -> assertEquals(employee1, employee1) // reflexivity
        );
    }

    @Test
    @DisplayName("Should have consistent hashCode based on email")
    void testHashCode() throws InvalidDataException {
        Employee employee1 = new Employee("Jan Kowalski", "jan@techcorp.com", "TechCorp", Position.MANAGER);
        Employee employee2 = new Employee("Jan Kowalski", "jan@techcorp.com", "DifferentCorp", Position.PROGRAMMER);

        assertEquals(employee1.hashCode(), employee2.hashCode());
    }

    @Test
    @DisplayName("Should return informative toString")
    void testToString() throws InvalidDataException {
        Employee employee = new Employee("Jan Kowalski", "jan@techcorp.com", "TechCorp", Position.MANAGER, 15000.50);
        employee.setId(1L);
        employee.setStatus(EmploymentStatus.ACTIVE);

        String result = employee.toString();

        assertAll("ToString content validation",
                () -> assertTrue(result.contains("id=1")),
                () -> assertTrue(result.contains("name='Jan Kowalski'")),
                () -> assertTrue(result.contains("email='jan@techcorp.com'")),
                () -> assertTrue(result.contains("company='TechCorp'")),
                () -> assertTrue(result.contains("position=MANAGER")),
                () -> assertTrue(result.contains("salary=15000.50")),
                () -> assertTrue(result.contains("status=ACTIVE"))
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

    @Test
    @DisplayName("Should handle complex name with multiple spaces")
    void testComplexNameWithMultipleSpaces() throws InvalidDataException {
        Employee employee = new Employee("Jan Maria Kowalski  ", "jan@techcorp.com", "TechCorp", Position.MANAGER);

        assertAll("Complex name validation",
                () -> assertEquals("Jan Maria Kowalski", employee.getName()),
                () -> assertEquals("Jan", employee.getFirstName()),
                () -> assertEquals("Maria Kowalski", employee.getLastName())
        );
    }

    @Test
    @DisplayName("Should handle all employment statuses")
    void testAllEmploymentStatuses() throws InvalidDataException {
        for (EmploymentStatus status : EmploymentStatus.values()) {
            Employee employee = new Employee("Test Employee", "test@techcorp.com", "TechCorp",
                    Position.MANAGER, 10000.0, status);

            assertEquals(status, employee.getStatus());
        }
    }

    @Test
    @DisplayName("Should handle all positions")
    void testAllPositions() throws InvalidDataException {
        for (Position position : Position.values()) {
            Employee employee = new Employee("Test Employee", "test@techcorp.com", "TechCorp", position);

            assertEquals(position, employee.getPosition());
            assertEquals(position.getBaseSalary(), employee.getSalary(), 0.001);
        }
    }
}