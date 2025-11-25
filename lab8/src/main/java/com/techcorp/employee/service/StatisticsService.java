
package com.techcorp.employee.service;

import com.techcorp.employee.dto.CompanyStatisticsDTO;
import com.techcorp.employee.model.CompanyStatistics;
import com.techcorp.employee.model.Employee;
import com.techcorp.employee.model.Department;
import com.techcorp.employee.model.EmploymentStatus;
import com.techcorp.employee.model.Position;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class StatisticsService {

    @Autowired
    private EmployeeService employeeService;

    @Autowired
    private DepartmentService departmentService;

    // ===== METODY DLA STATISTICSVIEWCONTROLLER =====

    public Map<String, Object> getAllStatistics() {
        List<Employee> employees = employeeService.getAllEmployees();
        List<Department> departments = departmentService.getAllDepartments();

        Map<String, Object> statistics = new HashMap<>();

        // Ogólne statystyki
        statistics.put("totalEmployees", employees.size());
        statistics.put("avgSalary", calculateAverageSalary(employees));
        statistics.put("totalDepartments", departments.size());
        statistics.put("totalBudget", calculateTotalBudget(departments));

        // Statystyki per firma
        statistics.put("companyStats", getCompanyStatisticsMap(employees));

        // Rozkład stanowisk
        statistics.put("positionDistribution", getPositionDistribution(employees));

        return statistics;
    }

    public CompanyStatistics getCompanyStatistics(String companyName) {
        return employeeService.getCompanyStatistics(companyName);
    }

    public List<Employee> getEmployeesByCompany(String companyName) {
        return employeeService.getEmployeesByCompany(companyName);
    }

    // ===== METODY DLA STATISTICSCONTROLLER (REST API) =====

    public Map<String, Double> getAverageSalary(String company) {
        Map<String, Double> response = new HashMap<>();

        if (company != null && !company.trim().isEmpty()) {
            double avgSalary = employeeService.calculateAverageSalaryByCompany(company)
                    .orElse(0.0);
            response.put("averageSalary", avgSalary);
        } else {
            double avgSalary = employeeService.calculateAverageSalary()
                    .orElse(0.0);
            response.put("averageSalary", avgSalary);
        }

        return response;
    }

    public CompanyStatisticsDTO getCompanyStatisticsDTO(String companyName) {
        CompanyStatistics stats = employeeService.getCompanyStatistics(companyName);
        double highestSalary = employeeService.findHighestSalaryByCompany(companyName);

        return new CompanyStatisticsDTO(
                companyName,
                stats.getEmployeeCount(),
                stats.getAverageSalary(),
                highestSalary,
                stats.getHighestPaidEmployee()
        );
    }

    public Map<String, Integer> getPositionStatistics() {
        Map<Position, Long> positionCounts = employeeService.countEmployeesByPosition();

        return positionCounts.entrySet().stream()
                .collect(Collectors.toMap(
                        entry -> entry.getKey().name(),
                        entry -> entry.getValue().intValue()
                ));
    }

    public Map<String, Integer> getEmploymentStatusStatistics() {
        Map<EmploymentStatus, Long> statusDistribution = employeeService.getEmploymentStatusDistribution();

        return statusDistribution.entrySet().stream()
                .collect(Collectors.toMap(
                        entry -> entry.getKey().name(),
                        entry -> entry.getValue().intValue()
                ));
    }

    // ===== METODY POMOCNICZE =====

    private double calculateAverageSalary(List<Employee> employees) {
        return employees.stream()
                .mapToDouble(Employee::getSalary)
                .average()
                .orElse(0.0);
    }

    private double calculateTotalBudget(List<Department> departments) {
        return departments.stream()
                .mapToDouble(Department::getBudget)
                .sum();
    }

    private Map<String, CompanyStatistics> getCompanyStatisticsMap(List<Employee> employees) {
        Map<String, CompanyStatistics> companyStats = new HashMap<>();
        for (Employee emp : employees) {
            companyStats.computeIfAbsent(emp.getCompany(),
                    k -> employeeService.getCompanyStatistics(k));
        }
        return companyStats;
    }

    private Map<String, Long> getPositionDistribution(List<Employee> employees) {
        return employees.stream()
                .collect(Collectors.groupingBy(
                        emp -> emp.getPosition().name(),
                        Collectors.counting()
                ));
    }
}
