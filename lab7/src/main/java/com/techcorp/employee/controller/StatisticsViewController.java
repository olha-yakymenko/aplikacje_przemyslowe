package com.techcorp.employee.controller;

import com.techcorp.employee.model.CompanyStatistics;
import com.techcorp.employee.model.Employee;
import com.techcorp.employee.model.Department;
import com.techcorp.employee.service.EmployeeService;
import com.techcorp.employee.service.DepartmentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.*;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/statistics")
public class StatisticsViewController {

    @Autowired
    private EmployeeService employeeService;

    @Autowired
    private DepartmentService departmentService;

    @GetMapping
    public String showStatistics(Model model) {
        List<Employee> employees = employeeService.getAllEmployees();
        List<Department> departments = departmentService.getAllDepartments();

        // Ogólne statystyki
        long totalEmployees = employees.size();
        double avgSalary = employees.stream()
                .mapToDouble(Employee::getSalary)
                .average()
                .orElse(0.0);
        long totalDepartments = departments.size();
        double totalBudget = departments.stream()
                .mapToDouble(Department::getBudget)
                .sum();

        // Statystyki per firma
        Map<String, CompanyStatistics> companyStats = new HashMap<>();
        for (Employee emp : employees) {
            companyStats.computeIfAbsent(emp.getCompany(),
                    k -> employeeService.getCompanyStatistics(k));
        }

        // Rozkład stanowisk
        Map<String, Long> positionDistribution = employees.stream()
                .collect(Collectors.groupingBy(
                        emp -> emp.getPosition().name(),
                        Collectors.counting()
                ));

        model.addAttribute("totalEmployees", totalEmployees);
        model.addAttribute("avgSalary", avgSalary);
        model.addAttribute("totalDepartments", totalDepartments);
        model.addAttribute("totalBudget", totalBudget);
        model.addAttribute("companyStats", companyStats);
        model.addAttribute("positionDistribution", positionDistribution);
        model.addAttribute("pageTitle", "Statystyki");

        System.out.println(totalEmployees);
        return "statistics/index";
    }

    @GetMapping("/company/{companyName}")
    public String showCompanyStatistics(@PathVariable String companyName, Model model) {
        CompanyStatistics stats = employeeService.getCompanyStatistics(companyName);
        List<Employee> companyEmployees = employeeService.getEmployeesByCompany(companyName);

        model.addAttribute("stats", stats);
        model.addAttribute("companyName", companyName);
        model.addAttribute("employees", companyEmployees);
        model.addAttribute("pageTitle", "Statystyki - " + companyName);

        return "statistics/company-details";
    }
}