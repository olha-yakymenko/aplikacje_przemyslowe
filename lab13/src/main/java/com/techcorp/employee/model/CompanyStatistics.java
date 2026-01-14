package com.techcorp.employee.model;

import com.techcorp.employee.dto.CompanyStatisticsDTO;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

public class CompanyStatistics {
    private final String companyName;
    private final int employeeCount;
    private final BigDecimal averageSalary;
    private final BigDecimal maxSalary;
    private String topEarnerName;

    // ===== KONSTRUKTORY =====

    // Główny konstruktor z BigDecimal
    public CompanyStatistics(String companyName, int employeeCount,
                             BigDecimal averageSalary, BigDecimal maxSalary,
                             String topEarnerName) {
        this.companyName = Objects.requireNonNull(companyName, "Company name cannot be null");
        this.employeeCount = Math.max(0, employeeCount);

        // Ustaw domyślne wartości BigDecimal jeśli null
        this.averageSalary = averageSalary != null ?
                averageSalary.setScale(2, RoundingMode.HALF_UP) :
                BigDecimal.ZERO.setScale(2);

        this.maxSalary = maxSalary != null ?
                maxSalary.setScale(2, RoundingMode.HALF_UP) :
                BigDecimal.ZERO.setScale(2);

        this.topEarnerName = topEarnerName != null ? topEarnerName : "";

        validate();
    }

    // Konstruktor dla Long/Double (dla kompatybilności z zapytaniami JPQL)
    public CompanyStatistics(String companyName, Long employeeCount,
                             Double averageSalary, Double maxSalary, String topEarnerName) {
        this(
                companyName,
                employeeCount != null ? employeeCount.intValue() : 0,
                averageSalary != null ? BigDecimal.valueOf(averageSalary) : BigDecimal.ZERO,
                maxSalary != null ? BigDecimal.valueOf(maxSalary) : BigDecimal.ZERO,
                topEarnerName
        );
    }

    // Konstruktor dla BigDecimal z zapytania JPQL
    public CompanyStatistics(String companyName, Long employeeCount,
                             BigDecimal averageSalary, BigDecimal maxSalary,
                             String topEarnerName) {
        this(
                companyName,
                employeeCount != null ? employeeCount.intValue() : 0,
                averageSalary,
                maxSalary,
                topEarnerName
        );
    }

    // Konstruktor bez topEarner (dla backward compatibility)
    public CompanyStatistics(String companyName, int employeeCount,
                             BigDecimal averageSalary, BigDecimal maxSalary) {
        this(companyName, employeeCount, averageSalary, maxSalary, "");
    }

    // ===== WALIDACJA =====

    private void validate() {
        if (employeeCount > 0 && averageSalary.compareTo(BigDecimal.ZERO) > 0) {
            // Sprawdź czy maxSalary >= averageSalary
            if (maxSalary.compareTo(averageSalary) < 0) {
                throw new IllegalArgumentException(
                        String.format("Max salary (%s) cannot be lower than average salary (%s) for company %s",
                                maxSalary.toPlainString(), averageSalary.toPlainString(), companyName)
                );
            }
        }
    }

    // ===== GETTERY =====

    public String getCompanyName() {
        return companyName;
    }

    public int getEmployeeCount() {
        return employeeCount;
    }

    // Zwróć BigDecimal (dla dokładnych obliczeń)
    public BigDecimal getAverageSalary() {
        return averageSalary;
    }

    // Zwróć BigDecimal (dla dokładnych obliczeń)
    public BigDecimal getMaxSalary() {
        return maxSalary;
    }

    // Zwróć double (dla kompatybilności)
    public double getAverageSalaryDouble() {
        return averageSalary.doubleValue();
    }

    // Zwróć double (dla kompatybilności)
    public double getMaxSalaryDouble() {
        return maxSalary.doubleValue();
    }

    public String getTopEarnerName() {
        return topEarnerName;
    }

    public String getHighestPaidEmployee() {
        return topEarnerName;
    }

    public void setHighestPaidEmployee(String highestPaidEmployee) {
        this.topEarnerName = highestPaidEmployee;
    }

    // ===== OBLICZENIOWE GETTERY =====

    public BigDecimal getTotalSalaryCost() {
        return averageSalary.multiply(BigDecimal.valueOf(employeeCount))
                .setScale(2, RoundingMode.HALF_UP);
    }

    public BigDecimal getSalaryToMaxRatio() {
        if (averageSalary.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }
        return maxSalary.divide(averageSalary, 4, RoundingMode.HALF_UP);
    }

    public BigDecimal getSalaryRange() {
        if (employeeCount == 0) return BigDecimal.ZERO;

        // Zakładając, że minSalary ≈ 2*averageSalary - maxSalary
        BigDecimal minSalary = averageSalary.multiply(BigDecimal.valueOf(2))
                .subtract(maxSalary);
        return maxSalary.subtract(minSalary).max(BigDecimal.ZERO);
    }

    // ===== METODY FABRYKUJĄCE =====

    public static CompanyStatistics empty(String companyName) {
        return new CompanyStatistics(companyName, 0,
                BigDecimal.ZERO, BigDecimal.ZERO, "");
    }

    public static CompanyStatistics fromDTO(CompanyStatisticsDTO dto) {
        if (dto == null) {
            return empty("");
        }

        // Zakładając, że DTO używa double - konwertuj na BigDecimal
        return new CompanyStatistics(
                dto.getCompanyName(),
                dto.getEmployeeCount(),
                dto.getAverageSalary(),
                dto.getHighestSalary(),
                dto.getTopEarnerName()
        );
    }

    public CompanyStatisticsDTO toDTO() {
        return new CompanyStatisticsDTO(
                companyName,
                (long) employeeCount,
                averageSalary.doubleValue(),
                maxSalary.doubleValue(),
                topEarnerName
        );
    }

    // Nowa metoda fabrykująca dla zapytań JPQL zwracających BigDecimal
    public static CompanyStatistics fromJPQLResult(String companyName, Long employeeCount,
                                                   BigDecimal averageSalary, BigDecimal maxSalary,
                                                   String topEarnerName) {
        return new CompanyStatistics(companyName, employeeCount,
                averageSalary, maxSalary, topEarnerName);
    }

    // ===== METODY POMOCNICZE =====

    public boolean hasEmployees() {
        return employeeCount > 0;
    }

    public boolean hasTopEarnerInfo() {
        return topEarnerName != null && !topEarnerName.trim().isEmpty();
    }

    public String getSummary() {
        if (employeeCount == 0) {
            return String.format("Company: %s (no employees)", companyName);
        }

        return String.format(
                "%s: %d employees, avg: $%s, max: $%s%s",
                companyName,
                employeeCount,
                averageSalary.toPlainString(),
                maxSalary.toPlainString(),
                hasTopEarnerInfo() ? String.format(" (top earner: %s)", topEarnerName) : ""
        );
    }

    // ===== EQUALS & HASHCODE =====

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CompanyStatistics that = (CompanyStatistics) o;
        return employeeCount == that.employeeCount &&
                Objects.equals(companyName, that.companyName) &&
                averageSalary.compareTo(that.averageSalary) == 0 &&
                maxSalary.compareTo(that.maxSalary) == 0 &&
                Objects.equals(topEarnerName, that.topEarnerName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(companyName, employeeCount,
                averageSalary.stripTrailingZeros(),
                maxSalary.stripTrailingZeros(),
                topEarnerName);
    }

    // ===== TO STRING =====

    @Override
    public String toString() {
        return String.format(
                "CompanyStatistics{company='%s', employees=%d, avg=%s, max=%s, topEarner='%s'}",
                companyName, employeeCount,
                averageSalary.toPlainString(),
                maxSalary.toPlainString(),
                topEarnerName
        );
    }

    // ===== METODY PORÓWNYWANIA =====

    public boolean isSalaryAboveAverage(BigDecimal threshold) {
        return averageSalary.compareTo(threshold) > 0;
    }

    public boolean isMaxSalaryAbove(BigDecimal threshold) {
        return maxSalary.compareTo(threshold) > 0;
    }

    public BigDecimal getSalaryVariance() {
        // Przykładowe obliczenie wariancji (dla uproszczenia)
        if (employeeCount <= 1) return BigDecimal.ZERO;

        // Załóżmy, że wariancja ≈ (max - avg) * 0.5
        BigDecimal diff = maxSalary.subtract(averageSalary);
        return diff.multiply(new BigDecimal("0.5"))
                .setScale(2, RoundingMode.HALF_UP)
                .max(BigDecimal.ZERO);
    }
}