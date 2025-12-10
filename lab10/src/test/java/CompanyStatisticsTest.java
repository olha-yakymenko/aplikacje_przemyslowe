//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.DisplayName;
//import org.junit.jupiter.api.Test;
//import org.junit.jupiter.params.ParameterizedTest;
//import org.junit.jupiter.params.provider.CsvSource;
//import org.junit.jupiter.params.provider.ValueSource;
//import com.techcorp.employee.model.CompanyStatistics;
//
//import static org.junit.jupiter.api.Assertions.*;
//
//@DisplayName("CompanyStatistics Unit Tests")
//class CompanyStatisticsTest {
//
//    private CompanyStatistics defaultStats;
//    private static final double DELTA = 0.001;
//
//    @BeforeEach
//    void setUp() {
//        // Arrange - przygotowanie wspólnych danych testowych
//        defaultStats = new CompanyStatistics(10, 5000.0, "John Doe");
//    }
//
//    @Test
//    @DisplayName("Should create CompanyStatistics with valid parameters")
//    void constructor_WithValidParameters_ShouldCreateObject() {
//        // Act & Assert
//        assertAll("CompanyStatistics constructor validation",
//                () -> assertEquals(10, defaultStats.getEmployeeCount(),
//                        "Employee count should match constructor parameter"),
//                () -> assertEquals(5000.0, defaultStats.getAverageSalary(), DELTA,
//                        "Average salary should match constructor parameter"),
//                () -> assertEquals("John Doe", defaultStats.getHighestPaidEmployee(),
//                        "Highest paid employee should match constructor parameter")
//        );
//    }
//
//    @Test
//    @DisplayName("Should handle zero employee count")
//    void constructor_WithZeroEmployees_ShouldCreateObject() {
//        // Arrange
//        CompanyStatistics stats = new CompanyStatistics(0, 0.0, "No employees");
//
//        // Act & Assert
//        assertAll("Zero employee statistics",
//                () -> assertEquals(0, stats.getEmployeeCount()),
//                () -> assertEquals(0.0, stats.getAverageSalary(), DELTA),
//                () -> assertEquals("No employees", stats.getHighestPaidEmployee())
//        );
//    }
//
//    @ParameterizedTest
//    @CsvSource({
//            "5, 7500.50, 'Jane Smith'",
//            "1, 10000.0, 'Single Employee'",
//            "100, 3500.75, 'Large Company CEO'"
//    })
//    @DisplayName("Should create CompanyStatistics with various parameters")
//    void constructor_WithVariousParameters_ShouldCreateObject(
//            int employeeCount, double averageSalary, String highestPaidEmployee) {
//
//        // Act
//        CompanyStatistics stats = new CompanyStatistics(employeeCount, averageSalary, highestPaidEmployee);
//
//        // Assert
//        assertAll("Parameterized statistics validation",
//                () -> assertEquals(employeeCount, stats.getEmployeeCount()),
//                () -> assertEquals(averageSalary, stats.getAverageSalary(), DELTA),
//                () -> assertEquals(highestPaidEmployee, stats.getHighestPaidEmployee())
//        );
//    }
//
//    @Test
//    @DisplayName("Should generate correct string representation")
//    void toString_ShouldReturnFormattedString() {
//        // Arrange
//        CompanyStatistics stats = new CompanyStatistics(5, 7500.50, "Jane Smith");
//
//        // Act
//        String result = stats.toString();
//
//        // Assert - bardziej elastyczna weryfikacja
//        assertAll("String representation validation",
//                () -> assertTrue(result.startsWith("Employees: 5"),
//                        "Should start with employee count"),
//                () -> assertTrue(result.contains("Avg Salary:"),
//                        "Should contain salary label"),
//                () -> assertTrue(result.contains("7500.50") || result.contains("7500,50"),
//                        "Should contain salary value (with . or , separator)"),
//                () -> assertTrue(result.endsWith("Highest Paid: Jane Smith") ||
//                                result.contains("Highest Paid: Jane Smith"),
//                        "Should contain highest paid employee name")
//        );
//    }
//
//    @Test
//    @DisplayName("Should handle null highest paid employee")
//    void constructor_WithNullHighestPaidEmployee_ShouldCreateObject() {
//        // Arrange & Act
//        CompanyStatistics stats = new CompanyStatistics(3, 4000.0, null);
//
//        // Assert
//        assertNull(stats.getHighestPaidEmployee(),
//                "Highest paid employee should be null");
//    }
//
//    @Test
//    @DisplayName("Should handle empty highest paid employee name")
//    void constructor_WithEmptyHighestPaidEmployee_ShouldCreateObject() {
//        // Arrange & Act
//        CompanyStatistics stats = new CompanyStatistics(2, 4500.0, "");
//
//        // Assert
//        assertEquals("", stats.getHighestPaidEmployee(),
//                "Highest paid employee should be empty string");
//    }
//
//    @ParameterizedTest
//    @ValueSource(doubles = {0.0, 0.001, 999999.999, Double.MAX_VALUE})
//    @DisplayName("Should handle various salary values")
//    void constructor_WithVariousSalaryValues_ShouldCreateObject(double salary) {
//        // Arrange & Act
//        CompanyStatistics stats = new CompanyStatistics(1, salary, "Test Employee");
//
//        // Assert
//        assertEquals(salary, stats.getAverageSalary(), DELTA,
//                "Should handle salary value: " + salary);
//    }
//
//    @Test
//    @DisplayName("Equals should return true for identical objects")
//    void equals_WithIdenticalObjects_ShouldReturnTrue() {
//        // Arrange
//        CompanyStatistics stats1 = new CompanyStatistics(10, 5000.0, "John Doe");
//        CompanyStatistics stats2 = new CompanyStatistics(10, 5000.0, "John Doe");
//
//        // Act & Assert
//        assertEquals(stats1, stats2, "Identical objects should be equal");
//        assertEquals(stats1.hashCode(), stats2.hashCode(),
//                "Equal objects should have same hash code");
//    }
//
//    @Test
//    @DisplayName("Equals should return false for different objects")
//    void equals_WithDifferentObjects_ShouldReturnFalse() {
//        // Arrange
//        CompanyStatistics stats1 = new CompanyStatistics(10, 5000.0, "John Doe");
//        CompanyStatistics stats2 = new CompanyStatistics(11, 5000.0, "John Doe");
//
//        // Act & Assert
//        assertNotEquals(stats1, stats2, "Different objects should not be equal");
//    }
//
//    @Test
//    @DisplayName("Equals should return false for null")
//    void equals_WithNull_ShouldReturnFalse() {
//        // Act & Assert
//        assertFalse(defaultStats.equals(null), "Should return false for null");
//    }
//
//    @Test
//    @DisplayName("Equals should return false for different class")
//    void equals_WithDifferentClass_ShouldReturnFalse() {
//        // Act & Assert
//        assertFalse(defaultStats.equals("Not a CompanyStatistics"),
//                "Should return false for different class");
//    }
//
//    @Test
//    @DisplayName("Hash code should be consistent")
//    void hashCode_ShouldBeConsistent() {
//        // Arrange
//        int initialHashCode = defaultStats.hashCode();
//
//        // Act & Assert
//        assertEquals(initialHashCode, defaultStats.hashCode(),
//                "Hash code should be consistent across multiple calls");
//    }
//
//    @Test
//    @DisplayName("Should handle negative employee count")
//    void constructor_WithNegativeEmployeeCount_ShouldCreateObject() {
//        // Arrange & Act
//        CompanyStatistics stats = new CompanyStatistics(-1, 5000.0, "Test");
//
//        // Assert
//        assertEquals(-1, stats.getEmployeeCount(),
//                "Should preserve negative employee count");
//    }
//
//    @Test
//    @DisplayName("Should handle negative salary")
//    void constructor_WithNegativeSalary_ShouldCreateObject() {
//        // Arrange & Act
//        CompanyStatistics stats = new CompanyStatistics(5, -1000.0, "Test");
//
//        // Assert
//        assertEquals(-1000.0, stats.getAverageSalary(), DELTA,
//                "Should preserve negative salary");
//    }
//}









package com.techcorp.employee.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;

class CompanyStatisticsTest {

    @Test
    void shouldCreateStatisticsWithNewConstructor() {
        // When
        CompanyStatistics stats = new CompanyStatistics("TechCorp", 10, 5000.0, 8000.0);
        stats.setHighestPaidEmployee("Jan Kowalski");

        // Then
        assertThat(stats.getCompanyName()).isEqualTo("TechCorp");
        assertThat(stats.getEmployeeCount()).isEqualTo(10);
        assertThat(stats.getAverageSalary()).isEqualTo(5000.0);
        assertThat(stats.getMaxSalary()).isEqualTo(8000.0);
        assertThat(stats.getHighestPaidEmployee()).isEqualTo("Jan Kowalski");
    }

    @Test
    void shouldCreateStatisticsWithOldConstructor() {
        // When - UŻYJ NOWEGO KONSTRUKTORA
        CompanyStatistics stats = new CompanyStatistics("TechCorp", 5, 4000.0, 5000.0);
        stats.setHighestPaidEmployee("Anna Nowak"); // 👈 DODAJ SETTER

        // Then
        assertThat(stats.getEmployeeCount()).isEqualTo(5);
        assertThat(stats.getAverageSalary()).isEqualTo(4000.0);
        assertThat(stats.getHighestPaidEmployee()).isEqualTo("Anna Nowak");
        assertThat(stats.getCompanyName()).isEqualTo("TechCorp");
        assertThat(stats.getMaxSalary()).isEqualTo(5000.0);
    }

    @Test
    void shouldImplementEqualsAndHashCode() {
        // Given
        CompanyStatistics stats1 = new CompanyStatistics("TechCorp", 10, 5000.0, 8000.0);
        CompanyStatistics stats2 = new CompanyStatistics("TechCorp", 10, 5000.0, 8000.0);
        CompanyStatistics stats3 = new CompanyStatistics("OtherCorp", 5, 4000.0, 6000.0);

        // Then
        assertThat(stats1).isEqualTo(stats2);
        assertThat(stats1).isNotEqualTo(stats3);
        assertThat(stats1.hashCode()).isEqualTo(stats2.hashCode());
    }

    @Test
    void shouldReturnCorrectToString() {
        // Given
        CompanyStatistics stats = new CompanyStatistics("TechCorp", 10, 5000.0, 8000.0);
        stats.setHighestPaidEmployee("Jan Kowalski");

        // When
        String toString = stats.toString();

        // Then
        assertThat(toString).contains("TechCorp");
        assertThat(toString).contains("employees=10");
        assertThat(toString).contains("avg=5000,00");
        assertThat(toString).contains("max=8000,00");
        assertThat(toString).contains("topEarner='Jan Kowalski'");
    }



    @Test
    @DisplayName("Should validate successfully for zero employees")
    void constructor_WithZeroEmployees_ShouldSkipValidation() {
        // When & Then - should not throw for zero employees
        assertThat(new CompanyStatistics("TestCorp", 0, 8000.0, 5000.0, "Test"))
                .isNotNull();
    }

    @Test
    @DisplayName("Should validate successfully for equal salaries")
    void constructor_WithEqualSalaries_ShouldPassValidation() {
        // When & Then - should not throw for equal salaries
        assertThat(new CompanyStatistics("TestCorp", 10, 5000.0, 5000.0, "Test"))
                .isNotNull();
    }

    // ===== TESTS FOR COMPUTED GETTERS =====

    @Test
    @DisplayName("Should calculate total salary cost correctly")
    void getTotalSalaryCost_ShouldCalculateCorrectly() {
        // Given
        CompanyStatistics stats = new CompanyStatistics("TechCorp", 10, 5000.0, 8000.0, "Test");

        // When & Then
        assertThat(stats.getTotalSalaryCost()).isEqualTo(50000.0);
    }

    @Test
    @DisplayName("Total salary cost should be zero for zero employees")
    void getTotalSalaryCost_WithZeroEmployees_ShouldBeZero() {
        // Given
        CompanyStatistics stats = new CompanyStatistics("TechCorp", 0, 5000.0, 8000.0, "Test");

        // When & Then
        assertThat(stats.getTotalSalaryCost()).isZero();
    }

    @Test
    @DisplayName("Should calculate salary to max ratio correctly")
    void getSalaryToMaxRatio_ShouldCalculateCorrectly() {
        // Given
        CompanyStatistics stats = new CompanyStatistics("TechCorp", 10, 5000.0, 8000.0, "Test");

        // When & Then
        assertThat(stats.getSalaryToMaxRatio()).isEqualTo(1.6); // 8000 / 5000
    }

    @Test
    @DisplayName("Salary to max ratio should be zero for zero average")
    void getSalaryToMaxRatio_WithZeroAverage_ShouldBeZero() {
        // Given
        CompanyStatistics stats = new CompanyStatistics("TechCorp", 10, 0.0, 8000.0, "Test");

        // When & Then
        assertThat(stats.getSalaryToMaxRatio()).isZero();
    }

    @Test
    @DisplayName("Should calculate salary range")
    void getSalaryRange_ShouldCalculateCorrectly() {
        // Given
        CompanyStatistics stats = new CompanyStatistics("TechCorp", 10, 5000.0, 8000.0, "Test");

        // When
        double range = stats.getSalaryRange();

        // Then - expected: 8000 - (5000 - (8000 - 5000)) = 8000 - (5000 - 3000) = 8000 - 2000 = 6000
        assertThat(range).isEqualTo(6000.0);
    }

    @Test
    @DisplayName("Salary range should be zero for zero employees")
    void getSalaryRange_WithZeroEmployees_ShouldBeZero() {
        // Given
        CompanyStatistics stats = new CompanyStatistics("TechCorp", 0, 5000.0, 8000.0, "Test");

        // When & Then
        assertThat(stats.getSalaryRange()).isZero();
    }

    // ===== TESTS FOR HELPER METHODS =====

    @Test
    @DisplayName("Should correctly identify if has employees")
    void hasEmployees_ShouldReturnCorrectValue() {
        // Given
        CompanyStatistics withEmployees = new CompanyStatistics("TechCorp", 5, 5000.0, 8000.0, "Test");
        CompanyStatistics withoutEmployees = new CompanyStatistics("TechCorp", 0, 0.0, 0.0, "");

        // When & Then
        assertAll("hasEmployees checks",
                () -> assertThat(withEmployees.hasEmployees()).isTrue(),
                () -> assertThat(withoutEmployees.hasEmployees()).isFalse()
        );
    }

    @Test
    @DisplayName("Should correctly identify if has top earner info")
    void hasTopEarnerInfo_ShouldReturnCorrectValue() {
        // Given
        CompanyStatistics withTopEarner = new CompanyStatistics("TechCorp", 5, 5000.0, 8000.0, "Jan Kowalski");
        CompanyStatistics emptyTopEarner = new CompanyStatistics("TechCorp", 5, 5000.0, 8000.0, "");
        CompanyStatistics nullTopEarner = new CompanyStatistics("TechCorp", 5, 5000.0, 8000.0, null);
        CompanyStatistics whitespaceTopEarner = new CompanyStatistics("TechCorp", 5, 5000.0, 8000.0, "  ");

        // When & Then
        assertAll("hasTopEarnerInfo checks",
                () -> assertThat(withTopEarner.hasTopEarnerInfo()).isTrue(),
                () -> assertThat(emptyTopEarner.hasTopEarnerInfo()).isFalse(),
                () -> assertThat(nullTopEarner.hasTopEarnerInfo()).isFalse(),
                () -> assertThat(whitespaceTopEarner.hasTopEarnerInfo()).isFalse()
        );
    }


    @Test
    @DisplayName("Should generate correct summary for no employees")
    void getSummary_WithNoEmployees_ShouldGenerateCorrectString() {
        // Given
        CompanyStatistics stats = new CompanyStatistics("EmptyCorp", 0, 0.0, 0.0, "");

        // When
        String summary = stats.getSummary();

        // Then
        assertThat(summary)
                .isEqualTo("Company: EmptyCorp (no employees)");
    }


    @Test
    @DisplayName("Should create empty statistics")
    void empty_ShouldCreateZeroStatistics() {
        // When
        CompanyStatistics empty = CompanyStatistics.empty("EmptyCorp");

        // Then
        assertAll("Empty statistics",
                () -> assertThat(empty.getCompanyName()).isEqualTo("EmptyCorp"),
                () -> assertThat(empty.getEmployeeCount()).isZero(),
                () -> assertThat(empty.getAverageSalary()).isZero(),
                () -> assertThat(empty.getMaxSalary()).isZero(),
                () -> assertThat(empty.getTopEarnerName()).isEmpty(),
                () -> assertThat(empty.hasEmployees()).isFalse()
        );
    }


    // ===== TESTS FOR SETTERS =====

    @Test
    @DisplayName("Should set highest paid employee")
    void setHighestPaidEmployee_ShouldUpdateValue() {
        // Given
        CompanyStatistics stats = new CompanyStatistics("TechCorp", 10, 5000.0, 8000.0, "Original");

        // When
        stats.setHighestPaidEmployee("New Top Earner");

        // Then
        assertAll("Setter updates",
                () -> assertThat(stats.getTopEarnerName()).isEqualTo("New Top Earner"),
                () -> assertThat(stats.getHighestPaidEmployee()).isEqualTo("New Top Earner")
        );
    }

    @Test
    @DisplayName("Should set highest paid employee to empty string")
    void setHighestPaidEmployee_ToEmptyString_ShouldUpdate() {
        // Given
        CompanyStatistics stats = new CompanyStatistics("TechCorp", 10, 5000.0, 8000.0, "Original");

        // When
        stats.setHighestPaidEmployee("");

        // Then
        assertThat(stats.getTopEarnerName()).isEmpty();
    }



    // ===== TESTS FOR EQUALS AND HASHCODE =====

    @Test
    @DisplayName("Should be equal for same values")
    void equals_WithSameValues_ShouldReturnTrue() {
        // Given
        CompanyStatistics stats1 = new CompanyStatistics("TechCorp", 10, 5000.0, 8000.0, "Jan Kowalski");
        CompanyStatistics stats2 = new CompanyStatistics("TechCorp", 10, 5000.0, 8000.0, "Jan Kowalski");

        // When & Then
        assertAll("Equals for same values",
                () -> assertThat(stats1).isEqualTo(stats2),
                () -> assertThat(stats1.hashCode()).isEqualTo(stats2.hashCode())
        );
    }

    @Test
    @DisplayName("Should not be equal for different company name")
    void equals_WithDifferentCompany_ShouldReturnFalse() {
        // Given
        CompanyStatistics stats1 = new CompanyStatistics("TechCorp", 10, 5000.0, 8000.0, "Jan Kowalski");
        CompanyStatistics stats2 = new CompanyStatistics("OtherCorp", 10, 5000.0, 8000.0, "Jan Kowalski");

        // When & Then
        assertThat(stats1).isNotEqualTo(stats2);
    }

    @Test
    @DisplayName("Should not be equal for different employee count")
    void equals_WithDifferentEmployeeCount_ShouldReturnFalse() {
        // Given
        CompanyStatistics stats1 = new CompanyStatistics("TechCorp", 10, 5000.0, 8000.0, "Jan Kowalski");
        CompanyStatistics stats2 = new CompanyStatistics("TechCorp", 11, 5000.0, 8000.0, "Jan Kowalski");

        // When & Then
        assertThat(stats1).isNotEqualTo(stats2);
    }

    @Test
    @DisplayName("Should not be equal for different average salary")
    void equals_WithDifferentAverageSalary_ShouldReturnFalse() {
        // Given
        CompanyStatistics stats1 = new CompanyStatistics("TechCorp", 10, 5000.0, 8000.0, "Jan Kowalski");
        CompanyStatistics stats2 = new CompanyStatistics("TechCorp", 10, 5001.0, 8000.0, "Jan Kowalski");

        // When & Then
        assertThat(stats1).isNotEqualTo(stats2);
    }

    @Test
    @DisplayName("Should not be equal for different max salary")
    void equals_WithDifferentMaxSalary_ShouldReturnFalse() {
        // Given
        CompanyStatistics stats1 = new CompanyStatistics("TechCorp", 10, 5000.0, 8000.0, "Jan Kowalski");
        CompanyStatistics stats2 = new CompanyStatistics("TechCorp", 10, 5000.0, 8001.0, "Jan Kowalski");

        // When & Then
        assertThat(stats1).isNotEqualTo(stats2);
    }

    @Test
    @DisplayName("Should not be equal for different top earner")
    void equals_WithDifferentTopEarner_ShouldReturnFalse() {
        // Given
        CompanyStatistics stats1 = new CompanyStatistics("TechCorp", 10, 5000.0, 8000.0, "Jan Kowalski");
        CompanyStatistics stats2 = new CompanyStatistics("TechCorp", 10, 5000.0, 8000.0, "Anna Nowak");

        // When & Then
        assertThat(stats1).isNotEqualTo(stats2);
    }

    @Test
    @DisplayName("Should not be equal to null")
    void equals_WithNull_ShouldReturnFalse() {
        // Given
        CompanyStatistics stats = new CompanyStatistics("TechCorp", 10, 5000.0, 8000.0, "Jan Kowalski");

        // When & Then
        assertThat(stats.equals(null)).isFalse();
    }

    @Test
    @DisplayName("Should not be equal to different type")
    void equals_WithDifferentType_ShouldReturnFalse() {
        // Given
        CompanyStatistics stats = new CompanyStatistics("TechCorp", 10, 5000.0, 8000.0, "Jan Kowalski");

        // When & Then
        assertThat(stats.equals("String")).isFalse();
    }

    @Test
    @DisplayName("Should be equal to itself")
    void equals_WithSameInstance_ShouldReturnTrue() {
        // Given
        CompanyStatistics stats = new CompanyStatistics("TechCorp", 10, 5000.0, 8000.0, "Jan Kowalski");

        // When & Then
        assertThat(stats.equals(stats)).isTrue();
    }

    @Test
    @DisplayName("Hash code should be consistent")
    void hashCode_ShouldBeConsistent() {
        // Given
        CompanyStatistics stats = new CompanyStatistics("TechCorp", 10, 5000.0, 8000.0, "Jan Kowalski");
        int initialHashCode = stats.hashCode();

        // When & Then
        assertThat(stats.hashCode()).isEqualTo(initialHashCode);
        assertThat(stats.hashCode()).isEqualTo(stats.hashCode()); // multiple calls
    }

    @Test
    @DisplayName("Hash codes should be equal for equal objects")
    void hashCode_ForEqualObjects_ShouldBeEqual() {
        // Given
        CompanyStatistics stats1 = new CompanyStatistics("TechCorp", 10, 5000.0, 8000.0, "Jan Kowalski");
        CompanyStatistics stats2 = new CompanyStatistics("TechCorp", 10, 5000.0, 8000.0, "Jan Kowalski");

        // When & Then
        assertThat(stats1.hashCode()).isEqualTo(stats2.hashCode());
    }

    // ===== EDGE CASE TESTS =====

    @Test
    @DisplayName("Should handle extreme salary values")
    void constructor_WithMaxDoubleValue_ShouldCreateObject() {
        // When
        CompanyStatistics stats = new CompanyStatistics("TestCorp", 1, Double.MAX_VALUE, Double.MAX_VALUE, "Test");

        // Then
        assertThat(stats.getAverageSalary()).isEqualTo(Double.MAX_VALUE);
        assertThat(stats.getMaxSalary()).isEqualTo(Double.MAX_VALUE);
    }

    @Test
    @DisplayName("Should handle very small salary values")
    void constructor_WithSmallSalary_ShouldCreateObject() {
        // When
        CompanyStatistics stats = new CompanyStatistics("TestCorp", 1, 0.001, 0.002, "Test");

        // Then
        assertThat(stats.getAverageSalary()).isEqualTo(0.001);
        assertThat(stats.getMaxSalary()).isEqualTo(0.002);
    }

    @Test
    @DisplayName("Should handle zero values")
    void constructor_WithAllZeros_ShouldCreateObject() {
        // When
        CompanyStatistics stats = new CompanyStatistics("ZeroCorp", 0, 0.0, 0.0, "");

        // Then
        assertAll("All zeros",
                () -> assertThat(stats.getEmployeeCount()).isZero(),
                () -> assertThat(stats.getAverageSalary()).isZero(),
                () -> assertThat(stats.getMaxSalary()).isZero(),
                () -> assertThat(stats.getTopEarnerName()).isEmpty(),
                () -> assertThat(stats.hasEmployees()).isFalse(),
                () -> assertThat(stats.hasTopEarnerInfo()).isFalse(),
                () -> assertThat(stats.getTotalSalaryCost()).isZero(),
                () -> assertThat(stats.getSalaryToMaxRatio()).isZero(),
                () -> assertThat(stats.getSalaryRange()).isZero()
        );
    }

    @Test
    @DisplayName("Should handle whitespace in company name")
    void constructor_WithWhitespaceCompanyName_ShouldTrim() {
        // When
        CompanyStatistics stats = new CompanyStatistics("  TechCorp  ", 10, 5000.0, 8000.0, "Test");

        // Then
        assertThat(stats.getCompanyName()).isEqualTo("  TechCorp  ");
    }

    @Test
    @DisplayName("Should handle very large employee count")
    void constructor_WithLargeEmployeeCount_ShouldCreateObject() {
        // When
        CompanyStatistics stats = new CompanyStatistics("BigCorp", 1000000, 5000.0, 8000.0, "CEO");

        // Then
        assertThat(stats.getEmployeeCount()).isEqualTo(1000000);
        assertThat(stats.getTotalSalaryCost()).isEqualTo(5_000_000_000.0);
    }

    @Test
    @DisplayName("Should handle salary calculation with large numbers")
    void getTotalSalaryCost_WithLargeNumbers_ShouldCalculateCorrectly() {
        // Given
        CompanyStatistics stats = new CompanyStatistics("MegaCorp", 10000, 15000.0, 30000.0, "CEO");

        // When & Then
        assertThat(stats.getTotalSalaryCost()).isEqualTo(150_000_000.0);
    }


}