package com.techcorp.employee.controller;

import com.techcorp.employee.model.Department;
import com.techcorp.employee.model.Employee;
import com.techcorp.employee.service.EmployeeService;
import com.techcorp.employee.service.DepartmentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@Controller
public class HomeController {

    @Autowired
    private EmployeeService employeeService;

    @Autowired
    private DepartmentService departmentService;

    @GetMapping("/")
    public String home(Model model) {
        try {
            // Pobierz dane z istniejących serwisów
            List<Employee> employees = employeeService.getAllEmployees();
            List<Department> departments = departmentService.getAllDepartments();

            // Oblicz statystyki
            long totalEmployees = employees.size();
            double avgSalary = employees.stream()
                    .mapToDouble(Employee::getSalary)
                    .average()
                    .orElse(0.0);
            long totalDepartments = departments.size();

            // Debugowanie
            System.out.println("=== HOME CONTROLLER ===");
            System.out.println("Total employees: " + totalEmployees);
            System.out.println("Avg salary: " + avgSalary);
            System.out.println("Total departments: " + totalDepartments);

            // Przekaż dane do modelu (tak jak w StatisticsViewController)
            model.addAttribute("totalEmployees", totalEmployees);
            model.addAttribute("avgSalary", avgSalary);
            model.addAttribute("totalDepartments", totalDepartments);

        } catch (Exception e) {
            System.out.println("Error in home controller: " + e.getMessage());
            // Ustaw wartości domyślne w przypadku błędu
            model.addAttribute("totalEmployees", 0);
            model.addAttribute("avgSalary", 0.0);
            model.addAttribute("totalDepartments", 0);
        }

        return "index";
    }
}