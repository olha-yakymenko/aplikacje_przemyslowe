package com.techcorp.employee.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.math.BigDecimal;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

class EmployeeTest {

    private Department testDepartment;

    @BeforeEach
    void setUp() {
        testDepartment = new Department("IT", "Warszawa", "Dział technologii", "manager@example.com",
                100000.00);
        testDepartment.setId(1L);
    }

    // ===== TESTY KONSTRUKTORÓW =====

    @Test
    void constructor_WithAllParameters_ShouldCreateEmployee() {
        // When
        Employee employee = new Employee(
                "Jan Kowalski",
                "jan@example.com",
                "TechCorp",
                Position.PROGRAMMER,
                new BigDecimal("5000.00"),
                EmploymentStatus.ACTIVE
        );

        // Then - assertAll dla grupowania asercji
        assertAll(
                () -> assertNotNull(employee, "Employee should not be null"),
                () -> assertEquals("Jan Kowalski", employee.getName()),
                () -> assertEquals("jan@example.com", employee.getEmail()),
                () -> assertEquals("TechCorp", employee.getCompany()),
                () -> assertEquals(Position.PROGRAMMER, employee.getPosition()),
                () -> assertEquals(0, new BigDecimal("5000.00").compareTo(employee.getSalary()),
                        "Salary should be 5000.00"),
                () -> assertEquals(EmploymentStatus.ACTIVE, employee.getStatus()),
                () -> assertNull(employee.getDepartment(), "Department should be null"),
                () -> assertNull(employee.getPhotoFileName(), "Photo should be null")
        );
    }

    @Test
    void constructor_WithDepartment_ShouldCreateEmployeeWithDepartment() {
        // When
        Employee employee = new Employee(
                "Jan Kowalski",
                "jan@example.com",
                "TechCorp",
                Position.PROGRAMMER,
                new BigDecimal("5000.00"),
                EmploymentStatus.ACTIVE,
                testDepartment
        );

        // Then - jeden assert z assertAll
        assertAll(
                () -> assertNotNull(employee),
                () -> assertEquals("Jan Kowalski", employee.getName()),
                () -> assertEquals(testDepartment, employee.getDepartment())
        );
    }

    @Test
    void constructor_WithSalaryOnly_ShouldSetDefaultStatusToActive() {
        // When
        Employee employee = new Employee(
                "Jan Kowalski",
                "jan@example.com",
                "TechCorp",
                Position.PROGRAMMER,
                new BigDecimal("5000.00")
        );

        // Then - pojedynczy assert
        assertEquals(EmploymentStatus.ACTIVE, employee.getStatus(),
                "Default status should be ACTIVE");
    }

    @Test
    void constructor_ShouldTrimAndLowerCaseEmail() {
        // When
        Employee employee = new Employee(
                "Jan Kowalski",
                "  JAN@EXAMPLE.COM  ",
                "TechCorp",
                Position.PROGRAMMER,
                new BigDecimal("5000.00"),
                EmploymentStatus.ACTIVE
        );

        // Then - jeden assert
        assertEquals("jan@example.com", employee.getEmail(),
                "Email should be trimmed and lowercased");
    }

    // ===== TESTY SETTERÓW =====

    @Test
    void setName_ValidName_ShouldSetTrimmed() {
        // Given
        Employee employee = new Employee();

        // When
        employee.setName("  Jan Kowalski  ");

        // Then - jeden assert
        assertEquals("Jan Kowalski", employee.getName(),
                "Name should be trimmed");
    }

    @Test
    void setName_Null_ShouldThrowException() {
        // Given
        Employee employee = new Employee();

        // When & Then - assertThrows zwraca wyjątek do dalszych asercji
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> employee.setName(null));

        assertEquals("Name cannot be null or empty", exception.getMessage());
    }

    @Test
    void setSalary_Negative_ShouldThrowException() {
        // Given
        Employee employee = new Employee();

        // When & Then - jeden assert dla wyjątku
        assertThrows(IllegalArgumentException.class,
                () -> employee.setSalary(new BigDecimal("-100.00")),
                "Should throw exception for negative salary");
    }

    @Test
    void setSalary_ValidSalary_ShouldSet() {
        // Given
        Employee employee = new Employee();
        BigDecimal expectedSalary = new BigDecimal("5000.50");

        // When
        employee.setSalary(expectedSalary);

        // Then - compareTo zwraca 0 gdy równe
        assertEquals(0, expectedSalary.compareTo(employee.getSalary()),
                "Salary should be set correctly");
    }

    @ParameterizedTest
    @MethodSource("validSalariesProvider")
    void setSalary_ValidSalaries_ShouldSet(BigDecimal salary) {
        // Given
        Employee employee = new Employee();

        // When
        employee.setSalary(salary);

        // Then - jeden assert dla każdego przypadku
        assertEquals(0, salary.compareTo(employee.getSalary()),
                () -> String.format("Salary %s should be set correctly", salary));
    }

    private static Stream<BigDecimal> validSalariesProvider() {
        return Stream.of(
                new BigDecimal("0.00"),
                new BigDecimal("1000.00"),
                new BigDecimal("10000.00"),
                new BigDecimal("999999.99"),
                new BigDecimal("5000.50"),
                new BigDecimal("0.01")
        );
    }

    // ===== TESTY METOD POMOCNICZYCH =====

    @ParameterizedTest
    @CsvSource({
            "Jan Kowalski, Jan, Kowalski",
            "Anna Maria Nowak, Anna, Maria Nowak",
            "John, John, ''",
            "  Jan  Kowalski  , Jan, Kowalski"
    })
    void getFirstNameAndLastName_ShouldSplitCorrectly(String fullName, String expectedFirstName, String expectedLastName) {
        // Given
        Employee employee = new Employee();
        employee.setName(fullName);

        // When
        String firstName = employee.getFirstName();
        String lastName = employee.getLastName();

        // Then - assertAll dla dwóch powiązanych asercji
        assertAll(
                () -> assertEquals(expectedFirstName, firstName,
                        "First name should be extracted correctly"),
                () -> assertEquals(expectedLastName, lastName,
                        "Last name should be extracted correctly")
        );
    }

    // ===== TESTY EQUALS I HASHCODE =====

    @Test
    void equals_SameEmail_ShouldReturnTrue() {
        // Given
        Employee employee1 = new Employee("Jan Kowalski", "jan@example.com", "TechCorp",
                Position.PROGRAMMER, new BigDecimal("5000.00"), EmploymentStatus.ACTIVE);
        Employee employee2 = new Employee("Jan Nowak", "jan@example.com", "OtherCorp",
                Position.MANAGER, new BigDecimal("8000.00"), EmploymentStatus.ON_LEAVE);

        // When & Then - jeden assert
        assertEquals(employee1, employee2,
                "Employees with same email should be equal");
    }

    @Test
    void hashCode_SameEmail_ShouldReturnSameHashCode() {
        // Given
        Employee employee1 = new Employee("Jan Kowalski", "jan@example.com", "TechCorp",
                Position.PROGRAMMER, new BigDecimal("5000.00"), EmploymentStatus.ACTIVE);
        Employee employee2 = new Employee("Jan Nowak", "jan@example.com", "OtherCorp",
                Position.MANAGER, new BigDecimal("8000.00"), EmploymentStatus.ON_LEAVE);

        // When & Then - jeden assert
        assertEquals(employee1.hashCode(), employee2.hashCode(),
                "Employees with same email should have same hashCode");
    }

    // ===== TESTY TOString =====

    @Test
    void toString_ShouldContainAllFields() {
        // Given
        Employee employee = new Employee("Jan Kowalski", "jan@example.com", "TechCorp",
                Position.PROGRAMMER, new BigDecimal("5000.00"), EmploymentStatus.ACTIVE);
        employee.setId(1L);

        // When
        String result = employee.toString();

        // Then - assertAll dla wielu warunków
        assertAll(
                () -> assertNotNull(result, "toString should not return null"),
                () -> assertTrue(result.contains("id=1"), "Should contain id"),
                () -> assertTrue(result.contains("name='Jan Kowalski'"), "Should contain name"),
                () -> assertTrue(result.contains("email='jan@example.com'"), "Should contain email"),
                () -> assertTrue(result.contains("company='TechCorp'"), "Should contain company"),
                () -> assertTrue(result.contains("position=PROGRAMMER"), "Should contain position"),
                () -> assertTrue(result.contains("salary=5000.00"), "Should contain salary"),
                () -> assertTrue(result.contains("status=ACTIVE"), "Should contain status")
        );
    }

    // ===== TESTY DEFAULTOWEGO KONSTRUKTORA =====

/*
    @Test
    void defaultConstructor_ShouldCreateEmptyEmployee() {
        // When
        Employee employee = new Employee();

        // Then - assertAll dla wszystkich pól
        assertAll(
                () -> assertNotNull(employee, "Employee should not be null"),
                () -> assertNull(employee.getId(), "Id should be null"),
                () -> assertNull(employee.getName(), "Name should be null"),
                () -> assertNull(employee.getEmail(), "Email should be null"),
                () -> assertNull(employee.getCompany(), "Company should be null"),
                () -> assertNull(employee.getPosition(), "Position should be null"),
                () -> assertEquals(0, BigDecimal.ZERO.compareTo(employee.getSalary()),
                        "Salary should be 0"),
                () -> assertNull(employee.getStatus(), "Status should be null"),
                () -> assertNull(employee.getDepartment(), "Department should be null"),
                () -> assertNull(employee.getPhotoFileName(), "Photo should be null")
        );
    }
*/

    // ===== TESTY GETTERÓW =====

    @Test
    void getId_ShouldReturnId() {
        // Given
        Employee employee = new Employee();
        employee.setId(1L);

        // When & Then - jeden assert
        assertEquals(1L, employee.getId(), "Should return correct id");
    }

    // ===== TESTY EDGE CASES =====

    @Test
    void setSalary_BigDecimalPrecision_ShouldMaintainScale() {
        // Given
        Employee employee = new Employee();
        BigDecimal salaryWithScale = new BigDecimal("5000.123");

        // When
        employee.setSalary(salaryWithScale);

        // Then - assertAll dla różnych aspektów BigDecimal
        BigDecimal actualSalary = employee.getSalary();
        assertAll(
                () -> assertEquals(0, salaryWithScale.compareTo(actualSalary),
                        "Salary values should be equal"),
                () -> assertEquals(3, actualSalary.scale(),
                        "Should maintain 3 decimal places"),
                () -> assertEquals("5000.123", actualSalary.toPlainString(),
                        "String representation should match")
        );
    }

    @Test
    void setSalary_Zero_ShouldBeAccepted() {
        // Given
        Employee employee = new Employee();

        // When
        employee.setSalary(BigDecimal.ZERO);

        // Then - jeden assert
        assertEquals(0, BigDecimal.ZERO.compareTo(employee.getSalary()),
                "Zero salary should be accepted");
    }

    @Test
    void setSalary_LargeValue_ShouldBeAccepted() {
        // Given
        Employee employee = new Employee();
        BigDecimal largeSalary = new BigDecimal("999999.99");

        // When
        employee.setSalary(largeSalary);

        // Then - jeden assert
        assertEquals(0, largeSalary.compareTo(employee.getSalary()),
                "Large salary should be accepted");
    }

    // ===== TESTY Z DEPARTAMENTEM =====

    @Test
    void setAndGetDepartment_ShouldWorkCorrectly() {
        // Given
        Employee employee = new Employee();
        Department department = new Department("HR", "Kraków", "Dział kadr",
                "hr@example.com",50000.00);

        // When
        employee.setDepartment(department);

        // Then - jeden assert
        assertEquals(department, employee.getDepartment(),
                "Department should be set correctly");
    }

    // ===== TESTY PORÓWNANIA =====

    @Test
    void equals_WithDifferentIdsButSameEmail_ShouldReturnTrue() {
        // Given
        Employee employee1 = new Employee("Jan Kowalski", "jan@example.com", "TechCorp",
                Position.PROGRAMMER, new BigDecimal("5000.00"), EmploymentStatus.ACTIVE);
        employee1.setId(1L);

        Employee employee2 = new Employee("Jan Nowak", "jan@example.com", "OtherCorp",
                Position.MANAGER, new BigDecimal("8000.00"), EmploymentStatus.ON_LEAVE);
        employee2.setId(2L);

        // When & Then - jeden assert
        assertEquals(employee1, employee2,
                "Equality should be based on email, not ID");
    }

    // ===== KOMPLEKSOWY TEST =====

    @Test
    void employee_CompleteLifecycle() {
        // Given
        BigDecimal initialSalary = new BigDecimal("5000.00");
        BigDecimal updatedSalary = new BigDecimal("6000.50");

        Employee employee = new Employee(
                "  Jan Kowalski  ",
                "  JAN@EXAMPLE.COM  ",
                "  TechCorp  ",
                Position.PROGRAMMER,
                initialSalary,
                EmploymentStatus.ACTIVE,
                testDepartment
        );

        employee.setId(1L);
        employee.setPhotoFileName("photo.jpg");

        // Then - kompleksowy assertAll
        assertAll("Complete employee lifecycle test",
                () -> assertNotNull(employee, "Employee should exist"),
                () -> assertEquals(1L, employee.getId(), "ID should be 1"),
                () -> assertEquals("Jan Kowalski", employee.getName(),
                        "Name should be trimmed"),
                () -> assertEquals("jan@example.com", employee.getEmail(),
                        "Email should be trimmed and lowercased"),
                () -> assertEquals("TechCorp", employee.getCompany(),
                        "Company should be trimmed"),
                () -> assertEquals(Position.PROGRAMMER, employee.getPosition(),
                        "Position should be PROGRAMMER"),
                () -> assertEquals(0, initialSalary.compareTo(employee.getSalary()),
                        "Salary should match initial value"),
                () -> assertEquals(EmploymentStatus.ACTIVE, employee.getStatus(),
                        "Status should be ACTIVE"),
                () -> assertEquals(testDepartment, employee.getDepartment(),
                        "Department should be set"),
                () -> assertEquals("photo.jpg", employee.getPhotoFileName(),
                        "Photo filename should be set")
        );

        // When - aktualizacja
        employee.setSalary(updatedSalary);
        employee.setStatus(EmploymentStatus.ON_LEAVE);

        // Then - assertAll dla aktualizacji
        assertAll("After update",
                () -> assertEquals(0, updatedSalary.compareTo(employee.getSalary()),
                        "Salary should be updated"),
                () -> assertEquals(EmploymentStatus.ON_LEAVE, employee.getStatus(),
                        "Status should be updated")
        );
    }
}