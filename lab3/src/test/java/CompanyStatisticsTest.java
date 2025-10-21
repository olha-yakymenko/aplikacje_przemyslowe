import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import src.model.CompanyStatistics;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("CompanyStatistics Unit Tests")
class CompanyStatisticsTest {

    private CompanyStatistics defaultStats;
    private static final double DELTA = 0.001;

    @BeforeEach
    void setUp() {
        // Arrange - przygotowanie wspólnych danych testowych
        defaultStats = new CompanyStatistics(10, 5000.0, "John Doe");
    }

    @Test
    @DisplayName("Should create CompanyStatistics with valid parameters")
    void constructor_WithValidParameters_ShouldCreateObject() {
        // Act & Assert
        assertAll("CompanyStatistics constructor validation",
                () -> assertEquals(10, defaultStats.getEmployeeCount(),
                        "Employee count should match constructor parameter"),
                () -> assertEquals(5000.0, defaultStats.getAverageSalary(), DELTA,
                        "Average salary should match constructor parameter"),
                () -> assertEquals("John Doe", defaultStats.getHighestPaidEmployee(),
                        "Highest paid employee should match constructor parameter")
        );
    }

    @Test
    @DisplayName("Should handle zero employee count")
    void constructor_WithZeroEmployees_ShouldCreateObject() {
        // Arrange
        CompanyStatistics stats = new CompanyStatistics(0, 0.0, "No employees");

        // Act & Assert
        assertAll("Zero employee statistics",
                () -> assertEquals(0, stats.getEmployeeCount()),
                () -> assertEquals(0.0, stats.getAverageSalary(), DELTA),
                () -> assertEquals("No employees", stats.getHighestPaidEmployee())
        );
    }

    @ParameterizedTest
    @CsvSource({
            "5, 7500.50, 'Jane Smith'",
            "1, 10000.0, 'Single Employee'",
            "100, 3500.75, 'Large Company CEO'"
    })
    @DisplayName("Should create CompanyStatistics with various parameters")
    void constructor_WithVariousParameters_ShouldCreateObject(
            int employeeCount, double averageSalary, String highestPaidEmployee) {

        // Act
        CompanyStatistics stats = new CompanyStatistics(employeeCount, averageSalary, highestPaidEmployee);

        // Assert
        assertAll("Parameterized statistics validation",
                () -> assertEquals(employeeCount, stats.getEmployeeCount()),
                () -> assertEquals(averageSalary, stats.getAverageSalary(), DELTA),
                () -> assertEquals(highestPaidEmployee, stats.getHighestPaidEmployee())
        );
    }

    @Test
    @DisplayName("Should generate correct string representation")
    void toString_ShouldReturnFormattedString() {
        // Arrange
        CompanyStatistics stats = new CompanyStatistics(5, 7500.50, "Jane Smith");

        // Act
        String result = stats.toString();

        // Assert - bardziej elastyczna weryfikacja
        assertAll("String representation validation",
                () -> assertTrue(result.startsWith("Employees: 5"),
                        "Should start with employee count"),
                () -> assertTrue(result.contains("Avg Salary:"),
                        "Should contain salary label"),
                () -> assertTrue(result.contains("7500.50") || result.contains("7500,50"),
                        "Should contain salary value (with . or , separator)"),
                () -> assertTrue(result.endsWith("Highest Paid: Jane Smith") ||
                                result.contains("Highest Paid: Jane Smith"),
                        "Should contain highest paid employee name")
        );
    }

    @Test
    @DisplayName("Should handle null highest paid employee")
    void constructor_WithNullHighestPaidEmployee_ShouldCreateObject() {
        // Arrange & Act
        CompanyStatistics stats = new CompanyStatistics(3, 4000.0, null);

        // Assert
        assertNull(stats.getHighestPaidEmployee(),
                "Highest paid employee should be null");
    }

    @Test
    @DisplayName("Should handle empty highest paid employee name")
    void constructor_WithEmptyHighestPaidEmployee_ShouldCreateObject() {
        // Arrange & Act
        CompanyStatistics stats = new CompanyStatistics(2, 4500.0, "");

        // Assert
        assertEquals("", stats.getHighestPaidEmployee(),
                "Highest paid employee should be empty string");
    }

    @ParameterizedTest
    @ValueSource(doubles = {0.0, 0.001, 999999.999, Double.MAX_VALUE})
    @DisplayName("Should handle various salary values")
    void constructor_WithVariousSalaryValues_ShouldCreateObject(double salary) {
        // Arrange & Act
        CompanyStatistics stats = new CompanyStatistics(1, salary, "Test Employee");

        // Assert
        assertEquals(salary, stats.getAverageSalary(), DELTA,
                "Should handle salary value: " + salary);
    }

    @Test
    @DisplayName("Equals should return true for identical objects")
    void equals_WithIdenticalObjects_ShouldReturnTrue() {
        // Arrange
        CompanyStatistics stats1 = new CompanyStatistics(10, 5000.0, "John Doe");
        CompanyStatistics stats2 = new CompanyStatistics(10, 5000.0, "John Doe");

        // Act & Assert
        assertEquals(stats1, stats2, "Identical objects should be equal");
        assertEquals(stats1.hashCode(), stats2.hashCode(),
                "Equal objects should have same hash code");
    }

    @Test
    @DisplayName("Equals should return false for different objects")
    void equals_WithDifferentObjects_ShouldReturnFalse() {
        // Arrange
        CompanyStatistics stats1 = new CompanyStatistics(10, 5000.0, "John Doe");
        CompanyStatistics stats2 = new CompanyStatistics(11, 5000.0, "John Doe");

        // Act & Assert
        assertNotEquals(stats1, stats2, "Different objects should not be equal");
    }

    @Test
    @DisplayName("Equals should return false for null")
    void equals_WithNull_ShouldReturnFalse() {
        // Act & Assert
        assertFalse(defaultStats.equals(null), "Should return false for null");
    }

    @Test
    @DisplayName("Equals should return false for different class")
    void equals_WithDifferentClass_ShouldReturnFalse() {
        // Act & Assert
        assertFalse(defaultStats.equals("Not a CompanyStatistics"),
                "Should return false for different class");
    }

    @Test
    @DisplayName("Hash code should be consistent")
    void hashCode_ShouldBeConsistent() {
        // Arrange
        int initialHashCode = defaultStats.hashCode();

        // Act & Assert
        assertEquals(initialHashCode, defaultStats.hashCode(),
                "Hash code should be consistent across multiple calls");
    }

    @Test
    @DisplayName("Should handle negative employee count")
    void constructor_WithNegativeEmployeeCount_ShouldCreateObject() {
        // Arrange & Act
        CompanyStatistics stats = new CompanyStatistics(-1, 5000.0, "Test");

        // Assert
        assertEquals(-1, stats.getEmployeeCount(),
                "Should preserve negative employee count");
    }

    @Test
    @DisplayName("Should handle negative salary")
    void constructor_WithNegativeSalary_ShouldCreateObject() {
        // Arrange & Act
        CompanyStatistics stats = new CompanyStatistics(5, -1000.0, "Test");

        // Assert
        assertEquals(-1000.0, stats.getAverageSalary(), DELTA,
                "Should preserve negative salary");
    }
}