package com.techcorp.employee.dto;

public class DashboardStatisticsDTO {
    private final long totalEmployees;
    private final double averageSalary;
    private final long totalDepartments;

    public DashboardStatisticsDTO(long totalEmployees, double averageSalary, long totalDepartments) {
        this.totalEmployees = totalEmployees;
        this.averageSalary = averageSalary;
        this.totalDepartments = totalDepartments;
    }

    // Gettery
    public long getTotalEmployees() { return totalEmployees; }
    public double getAverageSalary() { return averageSalary; }
    public long getTotalDepartments() { return totalDepartments; }
}