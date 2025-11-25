//
//
//package com.techcorp.employee.model;
//
//import java.util.Objects;
//
//public class CompanyStatistics {
//    private int employeeCount;
//    private double averageSalary;
//    private String highestPaidEmployee;
//
//    public CompanyStatistics(int employeeCount, double averageSalary, String highestPaidEmployee) {
//        this.employeeCount = employeeCount;
//        this.averageSalary = averageSalary;
//        this.highestPaidEmployee = highestPaidEmployee;
//    }
//
//    // ===== GETTERY =====
//    public int getEmployeeCount() {
//        return employeeCount;
//    }
//
//    public double getAverageSalary() {
//        return averageSalary;
//    }
//
//    public String getHighestPaidEmployee() {
//        return highestPaidEmployee;
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
//                Objects.equals(highestPaidEmployee, that.highestPaidEmployee);
//    }
//
//    @Override
//    public int hashCode() {
//        return Objects.hash(employeeCount, averageSalary, highestPaidEmployee);
//    }
//
//    @Override
//    public String toString() {
//        return String.format("Employees: %d, Avg Salary: %.2f, Highest Paid: %s",
//                employeeCount, averageSalary, highestPaidEmployee);
//    }
//}








package com.techcorp.employee.model;

import java.util.Objects;
import java.util.Optional;

public class CompanyStatistics {
    private final String companyName;
    private final int employeeCount;
    private final double averageSalary;
    private final double maxSalary;
    private Optional<String> highestPaidEmployee;

    // JEDYNY KONSTRUKTOR - dla danych z bazy
    public CompanyStatistics(String companyName, int employeeCount, double averageSalary, double maxSalary) {
        this.companyName = Objects.requireNonNull(companyName, "Company name cannot be null");
        this.employeeCount = employeeCount;
        this.averageSalary = averageSalary;
        this.maxSalary = maxSalary;
        this.highestPaidEmployee = Optional.empty(); // 👈 POCZĄTKOWO PUSTE
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

    // 👇 DWA GETTERY - dla wygody i zaawansowanych przypadków
    public String getHighestPaidEmployee() {
        return highestPaidEmployee.orElse("None"); // 👈 DLA PROSTYCH PRZYPADKÓW
    }

    public Optional<String> getHighestPaidEmployeeOpt() {
        return highestPaidEmployee; // 👈 DLA ZAAWANSOWANEJ LOGIKI
    }

    // ===== SETTER =====
    public void setHighestPaidEmployee(String highestPaidEmployee) {
        this.highestPaidEmployee = Optional.ofNullable(highestPaidEmployee); // 👈 NULL → Optional.empty()
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
                Objects.equals(highestPaidEmployee, that.highestPaidEmployee);
    }

    @Override
    public int hashCode() {
        return Objects.hash(companyName, employeeCount, averageSalary, maxSalary, highestPaidEmployee);
    }

    @Override
    public String toString() {
        return String.format("Company: %s, Employees: %d, Avg Salary: %.2f, Max Salary: %.2f, Highest Paid: %s",
                companyName, employeeCount, averageSalary, maxSalary, getHighestPaidEmployee());
    }
}