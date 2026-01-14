package com.techcorp.employee.model;

import com.techcorp.employee.dto.CompanyStatisticsDTO;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.math.BigDecimal;
import java.math.RoundingMode;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;

class CompanyStatisticsTest {

    // Helper method do porównywania BigDecimal z ignorowaniem skali
    private void assertBigDecimalEquals(BigDecimal expected, BigDecimal actual, String message) {
        // Strip trailing zeros dla porównania wartości
        BigDecimal expectedNormalized = expected.stripTrailingZeros();
        BigDecimal actualNormalized = actual.stripTrailingZeros();

        assertThat(actualNormalized.compareTo(expectedNormalized))
                .withFailMessage(() -> String.format("%s. Expected: %s, Actual: %s",
                        message, expected, actual))
                .isZero();
    }

    @Test
    @DisplayName("Should create CompanyStatistics with BigDecimal constructor")
    void shouldCreateStatisticsWithBigDecimalConstructor() {
        // When
        CompanyStatistics stats = new CompanyStatistics(
                "TechCorp",
                10,
                BigDecimal.valueOf(5000.0),
                BigDecimal.valueOf(8000.0),
                "Jan Kowalski"
        );

        // Then - użyj isEqualByComparingTo() zamiast equals()
        assertAll("CompanyStatistics with BigDecimal",
                () -> assertThat(stats.getCompanyName()).isEqualTo("TechCorp"),
                () -> assertThat(stats.getEmployeeCount()).isEqualTo(10),
                () -> assertThat(stats.getAverageSalary())
                        .isEqualByComparingTo("5000.0"), // Użyj String konstruktora
                () -> assertThat(stats.getMaxSalary())
                        .isEqualByComparingTo("8000.0"),
                () -> assertThat(stats.getTopEarnerName()).isEqualTo("Jan Kowalski"),
                () -> assertThat(stats.getAverageSalaryDouble()).isEqualTo(5000.0),
                () -> assertThat(stats.getMaxSalaryDouble()).isEqualTo(8000.0),
                // Dodatkowo sprawdź skalę
                () -> assertThat(stats.getAverageSalary().scale()).isEqualTo(2),
                () -> assertThat(stats.getMaxSalary().scale()).isEqualTo(2)
        );
    }

    @Test
    @DisplayName("Should create CompanyStatistics with Long/Double constructor")
    void shouldCreateStatisticsWithLongDoubleConstructor() {
        // When
        CompanyStatistics stats = new CompanyStatistics(
                "TechCorp",
                5L,
                BigDecimal.valueOf(4000.0),
                BigDecimal.valueOf(5000.0),
                "Anna Nowak"
        );

        // Then
        assertAll("CompanyStatistics with Long/Double",
                () -> assertThat(stats.getEmployeeCount()).isEqualTo(5),
                () -> assertThat(stats.getAverageSalary())
                        .isEqualByComparingTo("4000.0"),
                () -> assertThat(stats.getTopEarnerName()).isEqualTo("Anna Nowak"),
                () -> assertThat(stats.getCompanyName()).isEqualTo("TechCorp"),
                () -> assertThat(stats.getMaxSalary())
                        .isEqualByComparingTo("5000.0"),
                () -> assertThat(stats.getAverageSalary().scale()).isEqualTo(2)
        );
    }

//    @Test
//    @DisplayName("Should handle null values in constructor")
//    void shouldHandleNullValuesInConstructor() {
//        // When
//        CompanyStatistics stats = new CompanyStatistics(
//                "TestCorp",
//                (Long) null,
//                null,
//                null,
//                null
//        );
//
//        // Then
//        assertAll("Null values handling",
//                () -> assertThat(stats.getEmployeeCount()).isZero(),
//                () -> assertThat(stats.getAverageSalary())
//                        .isEqualByComparingTo(BigDecimal.ZERO),
//                () -> assertThat(stats.getMaxSalary())
//                        .isEqualByComparingTo(BigDecimal.ZERO),
//                () -> assertThat(stats.getTopEarnerName()).isEmpty(),
//                () -> assertThat(stats.getAverageSalary().scale()).isEqualTo(2)
//        );
//    }

    @Test
    @DisplayName("Should implement equals and hashCode with BigDecimal")
    void shouldImplementEqualsAndHashCodeWithBigDecimal() {
        // Given
        CompanyStatistics stats1 = new CompanyStatistics(
                "TechCorp",
                10,
                new BigDecimal("5000.0"),
                new BigDecimal("8000.0"),
                "Jan Kowalski"
        );

        CompanyStatistics stats2 = new CompanyStatistics(
                "TechCorp",
                10,
                new BigDecimal("5000.00"), // Inna skala
                new BigDecimal("8000.00"), // Inna skala
                "Jan Kowalski"
        );

        CompanyStatistics stats3 = new CompanyStatistics(
                "OtherCorp",
                5,
                new BigDecimal("4000.0"),
                new BigDecimal("6000.0"),
                "Anna Nowak"
        );

        // Then - equals() w CompanyStatistics używa compareTo(), więc różne skale są równe
        assertThat(stats1).isEqualTo(stats2);
        assertThat(stats1).isNotEqualTo(stats3);
        assertThat(stats1.hashCode()).isEqualTo(stats2.hashCode());
    }

    @Test
    @DisplayName("Should return correct toString with BigDecimal")
    void shouldReturnCorrectToStringWithBigDecimal() {
        // Given
        CompanyStatistics stats = new CompanyStatistics(
                "TechCorp",
                10,
                new BigDecimal("5000.50"),
                new BigDecimal("8000.75"),
                "Jan Kowalski"
        );

        // When
        String toString = stats.toString();

        // Then
        assertThat(toString).contains("TechCorp");
        assertThat(toString).contains("employees=10");
        assertThat(toString).contains("avg=5000.50");
        assertThat(toString).contains("max=8000.75");
        assertThat(toString).contains("topEarner='Jan Kowalski'");
    }

    @Test
    @DisplayName("Should validate successfully for zero employees with BigDecimal")
    void constructor_WithZeroEmployees_ShouldSkipValidation() {
        // When & Then - should not throw for zero employees
        assertThat(new CompanyStatistics(
                "TestCorp",
                0,
                new BigDecimal("8000.0"),
                new BigDecimal("5000.0"),
                "Test"
        )).isNotNull();
    }

    // ===== TESTS FOR COMPUTED GETTERS WITH BIGDECIMAL =====

    @Test
    @DisplayName("Should calculate total salary cost correctly with BigDecimal")
    void getTotalSalaryCost_ShouldCalculateCorrectlyWithBigDecimal() {
        // Given
        CompanyStatistics stats = new CompanyStatistics(
                "TechCorp",
                10,
                new BigDecimal("5000.0"),
                new BigDecimal("8000.0"),
                "Test"
        );

        // When & Then
        assertThat(stats.getTotalSalaryCost())
                .isEqualByComparingTo("50000.00");
    }

    @Test
    @DisplayName("Total salary cost should be zero for zero employees with BigDecimal")
    void getTotalSalaryCost_WithZeroEmployees_ShouldBeZero() {
        // Given
        CompanyStatistics stats = new CompanyStatistics(
                "TechCorp",
                0,
                new BigDecimal("5000.0"),
                new BigDecimal("8000.0"),
                "Test"
        );

        // When & Then
        assertThat(stats.getTotalSalaryCost())
                .isEqualByComparingTo("0.00");
    }

    @Test
    @DisplayName("Should calculate salary to max ratio correctly with BigDecimal")
    void getSalaryToMaxRatio_ShouldCalculateCorrectlyWithBigDecimal() {
        // Given
        CompanyStatistics stats = new CompanyStatistics(
                "TechCorp",
                10,
                new BigDecimal("5000.0"),
                new BigDecimal("8000.0"),
                "Test"
        );

        // When & Then
        assertThat(stats.getSalaryToMaxRatio())
                .isEqualByComparingTo("1.6000"); // 8000 / 5000 = 1.6
    }

    @Test
    @DisplayName("Salary to max ratio should be zero for zero average with BigDecimal")
    void getSalaryToMaxRatio_WithZeroAverage_ShouldBeZero() {
        // Given
        CompanyStatistics stats = new CompanyStatistics(
                "TechCorp",
                10,
                BigDecimal.ZERO,
                new BigDecimal("8000.0"),
                "Test"
        );

        // When & Then
        assertThat(stats.getSalaryToMaxRatio())
                .isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    @DisplayName("Should calculate salary range with BigDecimal")
    void getSalaryRange_ShouldCalculateCorrectlyWithBigDecimal() {
        // Given
        CompanyStatistics stats = new CompanyStatistics(
                "TechCorp",
                10,
                new BigDecimal("5000.0"),
                new BigDecimal("8000.0"),
                "Test"
        );

        // When
        BigDecimal range = stats.getSalaryRange();

        // Then - expected: 8000 - (5000 - (8000 - 5000)) = 8000 - (5000 - 3000) = 8000 - 2000 = 6000
        assertThat(range).isEqualByComparingTo("6000.00");
    }

    @ParameterizedTest
    @CsvSource({
            "TechCorp, 10, 5000.0, 8000.0, 'Jan Kowalski'",
            "SoftInc, 5, 4000.0, 6000.0, 'Anna Nowak'",
            "DataCorp, 0, 0.0, 0.0, ''",
            "BigCorp, 100, 10000.0, 20000.0, 'CEO'"
    })
    @DisplayName("Should create CompanyStatistics with various parameters")
    void constructor_WithVariousParameters_ShouldCreateObject(
            String companyName, int employeeCount, double avgSalary,
            double maxSalary, String topEarner) {

        // When
        CompanyStatistics stats = new CompanyStatistics(
                companyName,
                employeeCount,
                BigDecimal.valueOf(avgSalary),
                BigDecimal.valueOf(maxSalary),
                topEarner
        );

        // Assert - używaj isEqualByComparingTo() dla BigDecimal
        assertAll("Parameterized statistics validation",
                () -> assertThat(stats.getCompanyName()).isEqualTo(companyName),
                () -> assertThat(stats.getEmployeeCount()).isEqualTo(employeeCount),
                () -> assertThat(stats.getAverageSalary())
                        .isEqualByComparingTo(BigDecimal.valueOf(avgSalary)),
                () -> assertThat(stats.getMaxSalary())
                        .isEqualByComparingTo(BigDecimal.valueOf(maxSalary)),
                () -> assertThat(stats.getTopEarnerName()).isEqualTo(topEarner),
                () -> assertThat(stats.getAverageSalaryDouble()).isEqualTo(avgSalary),
                () -> assertThat(stats.getMaxSalaryDouble()).isEqualTo(maxSalary)
        );
    }

    @Test
    @DisplayName("Should handle rounding of BigDecimal values")
    void shouldHandleBigDecimalRounding() {
        // Given - wartości z wieloma miejscami po przecinku
        BigDecimal preciseAvg = new BigDecimal("5123.456789");
        BigDecimal preciseMax = new BigDecimal("7890.123456");

        CompanyStatistics stats = new CompanyStatistics(
                "PreciseCorp",
                7,
                preciseAvg,
                preciseMax,
                "Precision Tester"
        );

        // Then - wartości powinny być zaokrąglone do 2 miejsc
        assertThat(stats.getAverageSalary().scale()).isEqualTo(2);
        assertThat(stats.getMaxSalary().scale()).isEqualTo(2);

        // Sprawdź zaokrąglenie HALF_UP
        assertThat(stats.getAverageSalary()).isEqualByComparingTo("5123.46");
        assertThat(stats.getMaxSalary()).isEqualByComparingTo("7890.12");
    }

    @Test
    @DisplayName("Should create empty statistics with BigDecimal")
    void empty_ShouldCreateZeroStatisticsWithBigDecimal() {
        // When
        CompanyStatistics empty = CompanyStatistics.empty("EmptyCorp");

        // Then
        assertAll("Empty statistics with BigDecimal",
                () -> assertThat(empty.getCompanyName()).isEqualTo("EmptyCorp"),
                () -> assertThat(empty.getEmployeeCount()).isZero(),
                () -> assertThat(empty.getAverageSalary())
                        .isEqualByComparingTo(BigDecimal.ZERO),
                () -> assertThat(empty.getMaxSalary())
                        .isEqualByComparingTo(BigDecimal.ZERO),
                () -> assertThat(empty.getTopEarnerName()).isEmpty(),
                () -> assertThat(empty.hasEmployees()).isFalse(),
                () -> assertThat(empty.getAverageSalary().scale()).isEqualTo(2)
        );
    }

    @Test
    @DisplayName("Should convert to and from DTO")
    void shouldConvertToAndFromDTO() {
        // Given
        CompanyStatistics original = new CompanyStatistics(
                "TechCorp",
                25,
                new BigDecimal("8500.75"),
                new BigDecimal("12000.50"),
                "Jan Kowalski"
        );

        // When
        CompanyStatisticsDTO dto = original.toDTO();
        CompanyStatistics fromDto = CompanyStatistics.fromDTO(dto);

        // Then - użyj isEqualByComparingTo() dla BigDecimal
        assertAll("DTO conversion",
                () -> assertThat(fromDto.getCompanyName()).isEqualTo(original.getCompanyName()),
                () -> assertThat(fromDto.getEmployeeCount()).isEqualTo(original.getEmployeeCount()),
                () -> assertThat(fromDto.getAverageSalary())
                        .isEqualByComparingTo(original.getAverageSalary()),
                () -> assertThat(fromDto.getMaxSalary())
                        .isEqualByComparingTo(original.getMaxSalary()),
                () -> assertThat(fromDto.getTopEarnerName()).isEqualTo(original.getTopEarnerName())
        );
    }

    @Test
    @DisplayName("Should create from JPQL result with BigDecimal")
    void shouldCreateFromJPQLResult() {
        // Given
        String companyName = "TechCorp";
        Long employeeCount = 15L;
        BigDecimal avgSalary = new BigDecimal("7500.50");
        BigDecimal maxSalary = new BigDecimal("9500.75");
        String topEarner = "Anna Nowak";

        // When
        CompanyStatistics stats = CompanyStatistics.fromJPQLResult(
                companyName, employeeCount, avgSalary, maxSalary, topEarner
        );

        // Then
        assertAll("JPQL result conversion",
                () -> assertThat(stats.getCompanyName()).isEqualTo(companyName),
                () -> assertThat(stats.getEmployeeCount()).isEqualTo(employeeCount.intValue()),
                () -> assertThat(stats.getAverageSalary())
                        .isEqualByComparingTo("7500.50"),
                () -> assertThat(stats.getMaxSalary())
                        .isEqualByComparingTo("9500.75"),
                () -> assertThat(stats.getTopEarnerName()).isEqualTo(topEarner),
                () -> assertThat(stats.getAverageSalary().scale()).isEqualTo(2)
        );
    }

    @Test
    @DisplayName("Should handle comparison methods with BigDecimal")
    void shouldHandleComparisonMethods() {
        // Given
        CompanyStatistics stats = new CompanyStatistics(
                "TechCorp",
                10,
                new BigDecimal("7500.0"),
                new BigDecimal("10000.0"),
                "Test"
        );

        // When & Then
        assertAll("Comparison methods",
                () -> assertThat(stats.isSalaryAboveAverage(new BigDecimal("7000.0"))).isTrue(),
                () -> assertThat(stats.isSalaryAboveAverage(new BigDecimal("8000.0"))).isFalse(),
                () -> assertThat(stats.isMaxSalaryAbove(new BigDecimal("9000.0"))).isTrue(),
                () -> assertThat(stats.isMaxSalaryAbove(new BigDecimal("11000.0"))).isFalse()
        );
    }

    @Test
    @DisplayName("Should calculate salary variance with BigDecimal")
    void shouldCalculateSalaryVariance() {
        // Given
        CompanyStatistics stats = new CompanyStatistics(
                "TechCorp",
                10,
                new BigDecimal("5000.0"),
                new BigDecimal("8000.0"),
                "Test"
        );

        // When
        BigDecimal variance = stats.getSalaryVariance();

        // Then - wariancja ≈ (max - avg) * 0.5 = (8000 - 5000) * 0.5 = 3000 * 0.5 = 1500
        assertThat(variance).isEqualByComparingTo("1500.00");
    }

    @ParameterizedTest
    @ValueSource(strings = {"0.0", "1000000.0"})
    @DisplayName("Should handle various salary values with BigDecimal")
    void constructor_WithVariousSalaryValues_ShouldCreateObject(String salaryStr) {
        // Convert to BigDecimal
        BigDecimal salary = new BigDecimal(salaryStr);

        // When
        CompanyStatistics stats = new CompanyStatistics(
                "TestCorp",
                1,
                salary,
                salary.multiply(new BigDecimal("1.2")),
                "Test Employee"
        );

        // Then
        assertThat(stats.getAverageSalary())
                .isEqualByComparingTo(salary);
    }

    @Test
    @DisplayName("Should handle large employee count with BigDecimal")
    void constructor_WithLargeEmployeeCount_ShouldCreateObject() {
        // When
        CompanyStatistics stats = new CompanyStatistics(
                "BigCorp",
                1000000,
                new BigDecimal("5000.0"),
                new BigDecimal("8000.0"),
                "CEO"
        );

        // Then
        assertThat(stats.getEmployeeCount()).isEqualTo(1000000);
        assertThat(stats.getTotalSalaryCost())
                .isEqualByComparingTo("5000000000.00"); // 5,000,000,000.00
    }

    @Test
    @DisplayName("Should handle decimal precision in calculations")
    void shouldHandleDecimalPrecisionInCalculations() {
        // Given - wartości z wieloma miejscami po przecinku
        BigDecimal preciseAvg = new BigDecimal("12345.6789");
        BigDecimal preciseMax = new BigDecimal("23456.7891");

        CompanyStatistics stats = new CompanyStatistics(
                "PrecisionCorp",
                7,
                preciseAvg,
                preciseMax,
                "Precision Expert"
        );

        // When
        BigDecimal totalCost = stats.getTotalSalaryCost();

        assertThat(totalCost).isEqualByComparingTo("86419.76");
    }

    @Test
    @DisplayName("Should handle edge cases with BigDecimal")
    void shouldHandleEdgeCasesWithBigDecimal() {
        // Test 1: Very small values
        CompanyStatistics smallStats = new CompanyStatistics(
                "SmallCorp",
                1,
                new BigDecimal("0.0001"),
                new BigDecimal("0.0002"),
                "Tiny"
        );
        assertThat(smallStats.getAverageSalary())
                .isEqualByComparingTo("0.00"); // Rounded to 2 decimal places

        // Test 2: Negative employee count (should be normalized to 0)
        CompanyStatistics negativeStats = new CompanyStatistics(
                "NegativeCorp",
                -5,
                new BigDecimal("5000.0"),
                new BigDecimal("6000.0"),
                "Test"
        );
        assertThat(negativeStats.getEmployeeCount()).isEqualTo(0);

        // Test 3: Exactly equal salaries
        CompanyStatistics equalStats = new CompanyStatistics(
                "EqualCorp",
                5,
                new BigDecimal("5000.0"),
                new BigDecimal("5000.0"),
                "Test"
        );
        assertThat(equalStats.getAverageSalary())
                .isEqualByComparingTo(equalStats.getMaxSalary());

        // Test 4: Very large values
        CompanyStatistics largeStats = new CompanyStatistics(
                "LargeCorp",
                10000,
                new BigDecimal("9999999.99"),
                new BigDecimal("99999999.99"),
                "Rich"
        );
        assertThat(largeStats.getTotalSalaryCost().scale()).isEqualTo(2);
    }
}