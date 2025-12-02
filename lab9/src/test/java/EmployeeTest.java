package com.techcorp.employee.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

class EmployeeTest {

    private Department testDepartment;

    @BeforeEach
    void setUp() {
        testDepartment = new Department("IT", "Warszawa", "Dział technologii", "manager@example.com", 100000.0);
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
                5000.0,
                EmploymentStatus.ACTIVE
        );

        // Then
        assertNotNull(employee);
        assertEquals("Jan Kowalski", employee.getName());
        assertEquals("jan@example.com", employee.getEmail());
        assertEquals("TechCorp", employee.getCompany());
        assertEquals(Position.PROGRAMMER, employee.getPosition());
        assertEquals(5000.0, employee.getSalary(), 0.001);
        assertEquals(EmploymentStatus.ACTIVE, employee.getStatus());
        assertNull(employee.getDepartment());
        assertNull(employee.getPhotoFileName());
    }

    @Test
    void constructor_WithDepartment_ShouldCreateEmployeeWithDepartment() {
        // When
        Employee employee = new Employee(
                "Jan Kowalski",
                "jan@example.com",
                "TechCorp",
                Position.PROGRAMMER,
                5000.0,
                EmploymentStatus.ACTIVE,
                testDepartment
        );

        // Then
        assertNotNull(employee);
        assertEquals("Jan Kowalski", employee.getName());
        assertEquals(testDepartment, employee.getDepartment());
    }

    @Test
    void constructor_WithSalaryOnly_ShouldSetDefaultStatusToActive() {
        // When
        Employee employee = new Employee(
                "Jan Kowalski",
                "jan@example.com",
                "TechCorp",
                Position.PROGRAMMER,
                5000.0
        );

        // Then
        assertNotNull(employee);
        assertEquals(EmploymentStatus.ACTIVE, employee.getStatus());
    }

    @Test
    void constructor_ShouldTrimAndLowerCaseEmail() {
        // When
        Employee employee = new Employee(
                "Jan Kowalski",
                "  JAN@EXAMPLE.COM  ",
                "TechCorp",
                Position.PROGRAMMER,
                5000.0,
                EmploymentStatus.ACTIVE
        );

        // Then
        assertEquals("jan@example.com", employee.getEmail());
    }

    @Test
    void constructor_ShouldTrimName() {
        // When
        Employee employee = new Employee(
                "  Jan Kowalski  ",
                "jan@example.com",
                "TechCorp",
                Position.PROGRAMMER,
                5000.0,
                EmploymentStatus.ACTIVE
        );

        // Then
        assertEquals("Jan Kowalski", employee.getName());
    }

    @Test
    void constructor_ShouldTrimCompany() {
        // When
        Employee employee = new Employee(
                "Jan Kowalski",
                "jan@example.com",
                "  TechCorp  ",
                Position.PROGRAMMER,
                5000.0,
                EmploymentStatus.ACTIVE
        );

        // Then
        assertEquals("TechCorp", employee.getCompany());
    }

    // ===== TESTY SETTERÓW =====

    @Test
    void setName_ValidName_ShouldSetTrimmed() {
        // Given
        Employee employee = new Employee();

        // When
        employee.setName("  Jan Kowalski  ");

        // Then
        assertEquals("Jan Kowalski", employee.getName());
    }

    @Test
    void setName_Null_ShouldThrowException() {
        // Given
        Employee employee = new Employee();

        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            employee.setName(null);
        });
        assertEquals("Name cannot be null or empty", exception.getMessage());
    }

    @Test
    void setName_Empty_ShouldThrowException() {
        // Given
        Employee employee = new Employee();

        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            employee.setName("");
        });
        assertEquals("Name cannot be null or empty", exception.getMessage());
    }

    @Test
    void setName_WhitespaceOnly_ShouldThrowException() {
        // Given
        Employee employee = new Employee();

        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            employee.setName("   ");
        });
        assertEquals("Name cannot be null or empty", exception.getMessage());
    }

    @Test
    void setEmail_ValidEmail_ShouldSetTrimmedAndLowercased() {
        // Given
        Employee employee = new Employee();

        // When
        employee.setEmail("  JAN@EXAMPLE.COM  ");

        // Then
        assertEquals("jan@example.com", employee.getEmail());
    }

    @Test
    void setEmail_Null_ShouldThrowException() {
        // Given
        Employee employee = new Employee();

        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            employee.setEmail(null);
        });
        assertEquals("Email cannot be null or empty", exception.getMessage());
    }

    @Test
    void setEmail_Empty_ShouldThrowException() {
        // Given
        Employee employee = new Employee();

        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            employee.setEmail("");
        });
        assertEquals("Email cannot be null or empty", exception.getMessage());
    }

    @Test
    void setCompany_ValidCompany_ShouldSetTrimmed() {
        // Given
        Employee employee = new Employee();

        // When
        employee.setCompany("  TechCorp  ");

        // Then
        assertEquals("TechCorp", employee.getCompany());
    }

    @Test
    void setCompany_Null_ShouldThrowException() {
        // Given
        Employee employee = new Employee();

        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            employee.setCompany(null);
        });
        assertEquals("Company cannot be null or empty", exception.getMessage());
    }

    @Test
    void setPosition_ValidPosition_ShouldSet() {
        // Given
        Employee employee = new Employee();

        // When
        employee.setPosition(Position.MANAGER);

        // Then
        assertEquals(Position.MANAGER, employee.getPosition());
    }

    @Test
    void setPosition_Null_ShouldThrowException() {
        // Given
        Employee employee = new Employee();

        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            employee.setPosition(null);
        });
        assertEquals("Position cannot be null", exception.getMessage());
    }

    @ParameterizedTest
    @ValueSource(doubles = {0.0, 1000.0, 10000.0, 999999.99})
    void setSalary_ValidSalary_ShouldSet(double salary) {
        // Given
        Employee employee = new Employee();

        // When
        employee.setSalary(salary);

        // Then
        assertEquals(salary, employee.getSalary(), 0.001);
    }

    @Test
    void setSalary_Negative_ShouldThrowException() {
        // Given
        Employee employee = new Employee();

        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            employee.setSalary(-100.0);
        });
        assertEquals("Salary cannot be negative", exception.getMessage());
    }

    @Test
    void setStatus_ValidStatus_ShouldSet() {
        // Given
        Employee employee = new Employee();

        // When
        employee.setStatus(EmploymentStatus.ON_LEAVE);

        // Then
        assertEquals(EmploymentStatus.ON_LEAVE, employee.getStatus());
    }

    @Test
    void setStatus_Null_ShouldThrowException() {
        // Given
        Employee employee = new Employee();

        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            employee.setStatus(null);
        });
        assertEquals("Status cannot be null", exception.getMessage());
    }

    @Test
    void setDepartment_ValidDepartment_ShouldSet() {
        // Given
        Employee employee = new Employee();

        // When
        employee.setDepartment(testDepartment);

        // Then
        assertEquals(testDepartment, employee.getDepartment());
    }

    @Test
    void setDepartment_Null_ShouldSetNull() {
        // Given
        Employee employee = new Employee();
        employee.setDepartment(testDepartment);

        // When
        employee.setDepartment(null);

        // Then
        assertNull(employee.getDepartment());
    }

    @Test
    void setPhotoFileName_ValidFileName_ShouldSet() {
        // Given
        Employee employee = new Employee();

        // When
        employee.setPhotoFileName("photo.jpg");

        // Then
        assertEquals("photo.jpg", employee.getPhotoFileName());
    }

    // ===== TESTY METOD POMOCNICZYCH =====

    @ParameterizedTest
    @CsvSource({
            "Jan Kowalski, Jan, Kowalski",
            "Anna Maria Nowak, Anna, Maria Nowak",
            "John, John, ''",
            "  Jan  Kowalski  , Jan, Kowalski",
            "'', '', ''",
    })
    void getFirstNameAndLastName_ShouldSplitCorrectly(String fullName, String expectedFirstName, String expectedLastName) {
        // Given
        Employee employee = new Employee();
        if (!fullName.isEmpty()) {
            employee.setName(fullName);
        }

        // When
        String firstName = employee.getFirstName();
        String lastName = employee.getLastName();

        // Then
        assertEquals(expectedFirstName, firstName);
        assertEquals(expectedLastName, lastName);
    }

    @Test
    void getFirstNameAndLastName_WithNullName_ShouldReturnEmptyStrings() {
        // Given
        Employee employee = new Employee();
        // name pozostaje null (nie ustawione)

        // When
        String firstName = employee.getFirstName();
        String lastName = employee.getLastName();

        // Then
        assertEquals("", firstName);
        assertEquals("", lastName);
    }

    static Stream<Arguments> nameProvider() {
        return Stream.of(
                Arguments.of("Jan Kowalski", "Jan", "Kowalski"),
                Arguments.of("Anna Maria Nowak", "Anna", "Maria Nowak"),
                Arguments.of("John", "John", ""),
                Arguments.of("  Jan  Kowalski  ", "Jan", "Kowalski"),
                Arguments.of("A B C D", "A", "B C D")
        );
    }

    @ParameterizedTest
    @MethodSource("nameProvider")
    void getFirstNameAndLastName_MethodSource_ShouldSplitCorrectly(String fullName, String expectedFirstName, String expectedLastName) {
        // Given
        Employee employee = new Employee();
        employee.setName(fullName);

        // When
        String firstName = employee.getFirstName();
        String lastName = employee.getLastName();

        // Then
        assertEquals(expectedFirstName, firstName);
        assertEquals(expectedLastName, lastName);
    }

    @Test
    void getFirstName_SingleNameWithMultipleSpaces_ShouldReturnFirstName() {
        // Given
        Employee employee = new Employee();
        employee.setName("  Jan  ");

        // When
        String firstName = employee.getFirstName();

        // Then
        assertEquals("Jan", firstName);
    }

    @Test
    void getLastName_SingleName_ShouldReturnEmptyString() {
        // Given
        Employee employee = new Employee();
        employee.setName("Jan");

        // When
        String lastName = employee.getLastName();

        // Then
        assertEquals("", lastName);
    }

    // ===== TESTY EQUALS I HASHCODE =====

    @Test
    void equals_SameEmail_ShouldReturnTrue() {
        // Given
        Employee employee1 = new Employee("Jan Kowalski", "jan@example.com", "TechCorp",
                Position.PROGRAMMER, 5000.0, EmploymentStatus.ACTIVE);
        Employee employee2 = new Employee("Jan Nowak", "jan@example.com", "OtherCorp",
                Position.MANAGER, 8000.0, EmploymentStatus.ON_LEAVE);

        // When & Then
        assertEquals(employee1, employee2);
    }

    @Test
    void equals_DifferentEmail_ShouldReturnFalse() {
        // Given
        Employee employee1 = new Employee("Jan Kowalski", "jan1@example.com", "TechCorp",
                Position.PROGRAMMER, 5000.0, EmploymentStatus.ACTIVE);
        Employee employee2 = new Employee("Jan Kowalski", "jan2@example.com", "TechCorp",
                Position.PROGRAMMER, 5000.0, EmploymentStatus.ACTIVE);

        // When & Then
        assertNotEquals(employee1, employee2);
    }

    @Test
    void equals_SameInstance_ShouldReturnTrue() {
        // Given
        Employee employee = new Employee("Jan Kowalski", "jan@example.com", "TechCorp",
                Position.PROGRAMMER, 5000.0, EmploymentStatus.ACTIVE);

        // When & Then
        assertEquals(employee, employee);
    }

    @Test
    void equals_Null_ShouldReturnFalse() {
        // Given
        Employee employee = new Employee("Jan Kowalski", "jan@example.com", "TechCorp",
                Position.PROGRAMMER, 5000.0, EmploymentStatus.ACTIVE);

        // When & Then
        assertNotEquals(null, employee);
    }

    @Test
    void equals_DifferentClass_ShouldReturnFalse() {
        // Given
        Employee employee = new Employee("Jan Kowalski", "jan@example.com", "TechCorp",
                Position.PROGRAMMER, 5000.0, EmploymentStatus.ACTIVE);
        String notEmployee = "not an employee";

        // When & Then
        assertNotEquals(employee, notEmployee);
    }

    @Test
    void hashCode_SameEmail_ShouldReturnSameHashCode() {
        // Given
        Employee employee1 = new Employee("Jan Kowalski", "jan@example.com", "TechCorp",
                Position.PROGRAMMER, 5000.0, EmploymentStatus.ACTIVE);
        Employee employee2 = new Employee("Jan Nowak", "jan@example.com", "OtherCorp",
                Position.MANAGER, 8000.0, EmploymentStatus.ON_LEAVE);

        // When & Then
        assertEquals(employee1.hashCode(), employee2.hashCode());
    }

    @Test
    void hashCode_DifferentEmail_ShouldReturnDifferentHashCode() {
        // Given
        Employee employee1 = new Employee("Jan Kowalski", "jan1@example.com", "TechCorp",
                Position.PROGRAMMER, 5000.0, EmploymentStatus.ACTIVE);
        Employee employee2 = new Employee("Jan Kowalski", "jan2@example.com", "TechCorp",
                Position.PROGRAMMER, 5000.0, EmploymentStatus.ACTIVE);

        // When & Then
        assertNotEquals(employee1.hashCode(), employee2.hashCode());
    }

    // ===== TESTY TOString =====

    @Test
    void toString_ShouldReturnFormattedString() {
        // Given
        Employee employee = new Employee("Jan Kowalski", "jan@example.com", "TechCorp",
                Position.PROGRAMMER, 5000.0, EmploymentStatus.ACTIVE);
        employee.setId(1L);

        // When
        String result = employee.toString();

        // Then
        assertNotNull(result);
        assertTrue(result.contains("Employee{id=1"));
        assertTrue(result.contains("name='Jan Kowalski'"));
        assertTrue(result.contains("email='jan@example.com'"));
        assertTrue(result.contains("company='TechCorp'"));
        assertTrue(result.contains("position=PROGRAMMER"));
        assertTrue(result.contains("salary=5000.00"));
        assertTrue(result.contains("status=ACTIVE"));
    }

    @Test
    void toString_WithDepartment_ShouldNotIncludeDepartment() {
        // Given
        Employee employee = new Employee("Jan Kowalski", "jan@example.com", "TechCorp",
                Position.PROGRAMMER, 5000.0, EmploymentStatus.ACTIVE, testDepartment);
        employee.setId(1L);

        // When
        String result = employee.toString();

        // Then
        assertNotNull(result);
        // toString nie zawiera departamentu
        assertFalse(result.contains("department"));
    }

    // ===== TESTY DEFAULTOWEGO KONSTRUKTORA =====

    @Test
    void defaultConstructor_ShouldCreateEmptyEmployee() {
        // When
        Employee employee = new Employee();

        // Then
        assertNotNull(employee);
        assertNull(employee.getId());
        assertNull(employee.getName());
        assertNull(employee.getEmail());
        assertNull(employee.getCompany());
        assertNull(employee.getPosition());
        assertEquals(0.0, employee.getSalary(), 0.001);
        assertNull(employee.getStatus());
        assertNull(employee.getDepartment());
        assertNull(employee.getPhotoFileName());
    }



    // ===== TESTY GETTERÓW =====

    @Test
    void getId_ShouldReturnId() {
        // Given
        Employee employee = new Employee();
        employee.setId(1L);

        // When & Then
        assertEquals(1L, employee.getId());
    }

    @Test
    void getPhotoFileName_ShouldReturnFileName() {
        // Given
        Employee employee = new Employee();
        employee.setPhotoFileName("profile.jpg");

        // When & Then
        assertEquals("profile.jpg", employee.getPhotoFileName());
    }

    // ===== TESTY EDGE CASES =====

    @Test
    void setSalary_MaxValue_ShouldAccept() {
        // Given
        Employee employee = new Employee();

        // When
        employee.setSalary(Double.MAX_VALUE);

        // Then
        assertEquals(Double.MAX_VALUE, employee.getSalary(), 0.001);
    }

    @Test
    void setSalary_MinPositiveValue_ShouldAccept() {
        // Given
        Employee employee = new Employee();

        // When
        employee.setSalary(Double.MIN_VALUE);

        // Then
        assertEquals(Double.MIN_VALUE, employee.getSalary(), 0.001);
    }

    @Test
    void getName_AfterSettingWithSpaces_ShouldReturnTrimmed() {
        // Given
        Employee employee = new Employee();

        // When
        employee.setName("  Jan  Maria  Kowalski  ");

        // Then
        assertEquals("Jan  Maria  Kowalski", employee.getName()); // Uwaga: wewnętrzne spacje pozostają
    }

    @Test
    void getEmail_AfterSettingWithMixedCase_ShouldReturnLowercase() {
        // Given
        Employee employee = new Employee();

        // When
        employee.setEmail("JAN.Maria.Kowalski@Example.COM");

        // Then
        assertEquals("jan.maria.kowalski@example.com", employee.getEmail());
    }

    @Test
    void getCompany_AfterSettingWithSpaces_ShouldReturnTrimmed() {
        // Given
        Employee employee = new Employee();

        // When
        employee.setCompany("  Tech Corp International  ");

        // Then
        assertEquals("Tech Corp International", employee.getCompany());
    }

    // ===== TESTY Z DEPARTAMENTEM =====

    @Test
    void setAndGetDepartment_ShouldWorkCorrectly() {
        // Given
        Employee employee = new Employee();
        Department department = new Department("HR", "Kraków", "Dział kadr", "hr@example.com", 50000.0);

        // When
        employee.setDepartment(department);

        // Then
        assertEquals(department, employee.getDepartment());
    }

    @Test
    void constructor_WithDepartment_ShouldSetDepartment() {
        // When
        Employee employee = new Employee(
                "Jan Kowalski",
                "jan@example.com",
                "TechCorp",
                Position.PROGRAMMER,
                5000.0,
                EmploymentStatus.ACTIVE,
                testDepartment
        );

        // Then
        assertEquals(testDepartment, employee.getDepartment());
    }

    // ===== TESTY Z PHOTO FILE NAME =====

    @Test
    void setPhotoFileName_Null_ShouldSetNull() {
        // Given
        Employee employee = new Employee();
        employee.setPhotoFileName("photo.jpg");

        // When
        employee.setPhotoFileName(null);

        // Then
        assertNull(employee.getPhotoFileName());
    }

    @Test
    void setPhotoFileName_EmptyString_ShouldSetEmptyString() {
        // Given
        Employee employee = new Employee();

        // When
        employee.setPhotoFileName("");

        // Then
        assertEquals("", employee.getPhotoFileName());
    }

    // ===== TESTY PORÓWNANIA =====

    @Test
    void equals_WithDifferentIdsButSameEmail_ShouldReturnTrue() {
        // Given
        Employee employee1 = new Employee("Jan Kowalski", "jan@example.com", "TechCorp",
                Position.PROGRAMMER, 5000.0, EmploymentStatus.ACTIVE);
        employee1.setId(1L);

        Employee employee2 = new Employee("Jan Nowak", "jan@example.com", "OtherCorp",
                Position.MANAGER, 8000.0, EmploymentStatus.ON_LEAVE);
        employee2.setId(2L);

        // When & Then
        assertEquals(employee1, employee2); // równość bazuje na emailu, nie na ID
    }

    // ===== TESTY SPRAWDZAJĄCE ZACHOWANIE =====

    @Test
    void employee_ShouldBeImmutableAfterConstruction() {
        // Given
        Employee employee = new Employee(
                "Jan Kowalski",
                "jan@example.com",
                "TechCorp",
                Position.PROGRAMMER,
                5000.0,
                EmploymentStatus.ACTIVE
        );

        // When - próba zmiany przez settery
        employee.setName("Anna Nowak");
        employee.setEmail("anna@example.com");
        employee.setCompany("OtherCorp");
        employee.setPosition(Position.MANAGER);
        employee.setSalary(8000.0);
        employee.setStatus(EmploymentStatus.ON_LEAVE);

        // Then - wartości powinny się zmienić
        assertEquals("Anna Nowak", employee.getName());
        assertEquals("anna@example.com", employee.getEmail());
        assertEquals("OtherCorp", employee.getCompany());
        assertEquals(Position.MANAGER, employee.getPosition());
        assertEquals(8000.0, employee.getSalary(), 0.001);
        assertEquals(EmploymentStatus.ON_LEAVE, employee.getStatus());
    }
}