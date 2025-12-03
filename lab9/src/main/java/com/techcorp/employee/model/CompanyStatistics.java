//package com.techcorp.employee.model;
//
//import java.util.Objects;
//import java.util.Optional;
//
//public class CompanyStatistics {
//    private final String companyName;
//    private final int employeeCount;
//    private final double averageSalary;
//    private final double maxSalary;
//    private Optional<String> highestPaidEmployee;
//
//    public CompanyStatistics(String companyName, int employeeCount, double averageSalary, double maxSalary) {
//        this.companyName = Objects.requireNonNull(companyName, "Company name cannot be null");
//        this.employeeCount = employeeCount;
//        this.averageSalary = averageSalary;
//        this.maxSalary = maxSalary;
//        this.highestPaidEmployee = Optional.empty();
//    }
//
//    // ===== GETTERY =====
//    public String getCompanyName() {
//        return companyName;
//    }
//
//    public int getEmployeeCount() {
//        return employeeCount;
//    }
//
//    public double getAverageSalary() {
//        return averageSalary;
//    }
//
//    public double getMaxSalary() {
//        return maxSalary;
//    }
//
//    public String getHighestPaidEmployee() {
//        return highestPaidEmployee.orElse("");
//    }
//
//
//    // ===== SETTER =====
//    public void setHighestPaidEmployee(String highestPaidEmployee) {
//        this.highestPaidEmployee = Optional.ofNullable(highestPaidEmployee); // 👈 NULL → Optional.empty()
//    }
//
//    // ===== EQUALS & HASHCODE =====
//    @Override
//    public boolean equals(Object o) {
//        if (this == o) return true;
//        if (o == null || getClass() != o.getClass()) return false;
//        CompanyStatistics that = (CompanyStatistics) o;
//        return employeeCount == that.employeeCount &&
//                Double.compare(that.averageSalary, averageSalary) == 0 &&
//                Double.compare(that.maxSalary, maxSalary) == 0 &&
//                Objects.equals(companyName, that.companyName) &&
//                Objects.equals(highestPaidEmployee, that.highestPaidEmployee);
//    }
//
//    @Override
//    public int hashCode() {
//        return Objects.hash(companyName, employeeCount, averageSalary, maxSalary, highestPaidEmployee);
//    }
//
//    @Override
//    public String toString() {
//        return String.format("Company: %s, Employees: %d, Avg Salary: %.2f, Max Salary: %.2f, Highest Paid: %s",
//                companyName, employeeCount, averageSalary, maxSalary, getHighestPaidEmployee());
//    }
//}




package com.techcorp.employee.model;

import com.techcorp.employee.dto.CompanyStatisticsDTO;

import java.util.Objects;

public class CompanyStatistics {
    private final String companyName;
    private final int employeeCount;
    private final double averageSalary;
    private final double maxSalary;
    private String topEarnerName;

    // ===== KONSTRUKTORY =====

    // Główny konstruktor z wszystkimi danymi
    public CompanyStatistics(String companyName, int employeeCount,
                             double averageSalary, double maxSalary, String topEarnerName) {
        this.companyName = Objects.requireNonNull(companyName, "Company name cannot be null");
        this.employeeCount = Math.max(0, employeeCount); // Nie może być ujemne
        this.averageSalary = Math.max(0, averageSalary); // Nie może być ujemne
        this.maxSalary = Math.max(0, maxSalary); // Nie może być ujemne
        this.topEarnerName = topEarnerName != null ? topEarnerName : "";

        validate();
    }

    // Konstruktor dla DTO (Long zamiast int, Double zamiast double)
    public CompanyStatistics(String companyName, Long employeeCount,
                             Double averageSalary, Double maxSalary, String topEarnerName) {
        this(
                companyName,
                employeeCount != null ? employeeCount.intValue() : 0,
                averageSalary != null ? averageSalary : 0.0,
                maxSalary != null ? maxSalary : 0.0,
                topEarnerName
        );
    }

    // Konstruktor bez topEarner (dla backward compatibility)
    public CompanyStatistics(String companyName, int employeeCount,
                             double averageSalary, double maxSalary) {
        this(companyName, employeeCount, averageSalary, maxSalary, "");
    }

    // ===== WALIDACJA =====

    private void validate() {
        if (employeeCount > 0 && maxSalary < averageSalary) {
            throw new IllegalArgumentException(
                    String.format("Max salary (%.2f) cannot be lower than average salary (%.2f) for company %s",
                            maxSalary, averageSalary, companyName)
            );
        }
    }

    // ===== GETTERY =====

    public String getCompanyName() {
        return companyName;
    }

    public int getEmployeeCount() {
        return employeeCount;
    }

    public double getAverageSalary() {
        return averageSalary;
    }

    public double getMaxSalary() {
        return maxSalary;
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

    // Obliczeniowe gettery
    public double getTotalSalaryCost() {
        return averageSalary * employeeCount;
    }

    public double getSalaryToMaxRatio() {
        return averageSalary > 0 ? maxSalary / averageSalary : 0;
    }

    public double getSalaryRange() {
        if (employeeCount == 0) return 0;
        return maxSalary - (averageSalary - (maxSalary - averageSalary));
    }

    // ===== METODY FABRYKUJĄCE =====

    public static CompanyStatistics empty(String companyName) {
        return new CompanyStatistics(companyName, 0, 0.0, 0.0, "");
    }

    public static CompanyStatistics fromDTO(CompanyStatisticsDTO dto) {
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
                employeeCount,
                averageSalary,
                maxSalary,
                topEarnerName
        );
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
                "%s: %d employees, avg: $%.2f, max: $%.2f%s",
                companyName,
                employeeCount,
                averageSalary,
                maxSalary,
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
                Double.compare(that.averageSalary, averageSalary) == 0 &&
                Double.compare(that.maxSalary, maxSalary) == 0 &&
                Objects.equals(companyName, that.companyName) &&
                Objects.equals(topEarnerName, that.topEarnerName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(companyName, employeeCount, averageSalary, maxSalary, topEarnerName);
    }

    // ===== TO STRING =====

    @Override
    public String toString() {
        return String.format(
                "CompanyStatistics{company='%s', employees=%d, avg=%.2f, max=%.2f, topEarner='%s'}",
                companyName, employeeCount, averageSalary, maxSalary, topEarnerName
        );
    }
}