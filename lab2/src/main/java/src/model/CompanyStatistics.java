package src.model;

import java.util.Objects;

public class CompanyStatistics {
    private int employeeCount;
    private double averageSalary;
    private String highestPaidEmployee;

    public CompanyStatistics(int employeeCount, double averageSalary, String highestPaidEmployee) {
        this.employeeCount = employeeCount;
        this.averageSalary = averageSalary;
        this.highestPaidEmployee = highestPaidEmployee;
    }

    // ===== GETTERY =====
    public int getEmployeeCount() {
        return employeeCount;
    }

    public double getAverageSalary() {
        return averageSalary;
    }

    public String getHighestPaidEmployee() {
        return highestPaidEmployee;
    }

    // ===== EQUALS & HASHCODE =====
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CompanyStatistics that = (CompanyStatistics) o;
        return employeeCount == that.employeeCount &&
                Double.compare(that.averageSalary, averageSalary) == 0 &&
                Objects.equals(highestPaidEmployee, that.highestPaidEmployee);
    }

    @Override
    public int hashCode() {
        return Objects.hash(employeeCount, averageSalary, highestPaidEmployee);
    }

    @Override
    public String toString() {
        return String.format("Employees: %d, Avg Salary: %.2f, Highest Paid: %s",
                employeeCount, averageSalary, highestPaidEmployee);
    }
}