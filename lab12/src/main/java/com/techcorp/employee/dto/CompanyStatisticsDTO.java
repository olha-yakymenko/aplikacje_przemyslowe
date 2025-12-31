package com.techcorp.employee.dto;

import java.math.BigDecimal;

public class CompanyStatisticsDTO {
    private String companyName;
    private Long employeeCount;
    private BigDecimal averageSalary;  // Zmień na BigDecimal
    private BigDecimal highestSalary;  // Zmień na BigDecimal
    private String topEarnerName;

    // Konstruktor dla JPQL (BigDecimal z zapytania)
    public CompanyStatisticsDTO(String companyName, Long employeeCount,
                                BigDecimal averageSalary, BigDecimal maxSalary,
                                String topEarnerName) {
        this.companyName = companyName;
        this.employeeCount = employeeCount != null ? employeeCount : 0L;
        this.averageSalary = averageSalary != null ? averageSalary : BigDecimal.ZERO;
        this.highestSalary = maxSalary != null ? maxSalary : BigDecimal.ZERO;
        this.topEarnerName = topEarnerName != null ? topEarnerName : "";
    }

    // Konstruktor dla kompatybilności (Double)
    public CompanyStatisticsDTO(String companyName, Long employeeCount,
                                Double averageSalary, Double maxSalary,
                                String topEarnerName) {
        this(companyName, employeeCount,
                averageSalary != null ? BigDecimal.valueOf(averageSalary) : BigDecimal.ZERO,
                maxSalary != null ? BigDecimal.valueOf(maxSalary) : BigDecimal.ZERO,
                topEarnerName);
    }

    // Gettery
    public String getCompanyName() { return companyName; }
    public Long getEmployeeCount() { return employeeCount; }

    public BigDecimal getAverageSalary() { return averageSalary; }
    public Double getAverageSalaryDouble() {
        return averageSalary != null ? averageSalary.doubleValue() : 0.0;
    }

    public BigDecimal getHighestSalary() { return highestSalary; }
    public Double getHighestSalaryDouble() {
        return highestSalary != null ? highestSalary.doubleValue() : 0.0;
    }

    public String getTopEarnerName() { return topEarnerName; }

    // Settery jeśli potrzebne
    public void setCompanyName(String companyName) { this.companyName = companyName; }
    public void setEmployeeCount(Long employeeCount) { this.employeeCount = employeeCount; }
    public void setAverageSalary(BigDecimal averageSalary) { this.averageSalary = averageSalary; }
    public void setHighestSalary(BigDecimal highestSalary) { this.highestSalary = highestSalary; }
    public void setTopEarnerName(String topEarnerName) { this.topEarnerName = topEarnerName; }
}