package com.techcorp.employee.controller;

import com.techcorp.employee.model.Department;
import com.techcorp.employee.model.Employee;
import com.techcorp.employee.service.DepartmentService;
import com.techcorp.employee.service.EmployeeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/departments")
public class DepartmentViewController {

    @Autowired
    private DepartmentService departmentService;

    @Autowired
    private EmployeeService employeeService;

    @GetMapping
    public String listDepartments(Model model) {
        List<Department> departments = departmentService.getAllDepartments();

        // Dodaj informacje o managerze dla każdego departamentu
        for (Department dept : departments) {
            if (dept.getManagerEmail() != null) {
                Optional<Employee> manager = employeeService.findEmployeeByEmail(dept.getManagerEmail());
                if (manager.isPresent()) {
                    dept.setName(dept.getName());
                }
            }
        }

        model.addAttribute("departments", departments);
        model.addAttribute("pageTitle", "Lista Departamentów");
        return "departments/list";
    }

//    @GetMapping("/add")
//    public String showAddForm(Model model) {
//        model.addAttribute("department", new Department());
//        model.addAttribute("managers", employeeService.getAllEmployees());
//        model.addAttribute("pageTitle", "Dodaj Departament");
//        return "departments/form";
//    }


    @GetMapping("/add")
    public String showAddForm(Model model) {
        System.out.println("=== DEBUG: showAddForm START ===");

        try {
            // Sprawdź czy EmployeeService działa
            List<Employee> managers = employeeService.getAllEmployees();
            System.out.println("Number of managers: " + managers.size());

            // Utwórz nowy department
            Department department = new Department();
            System.out.println("Department object created: " + (department != null));

            model.addAttribute("department", department);
            model.addAttribute("managers", managers);
            model.addAttribute("pageTitle", "Dodaj Departament");

            System.out.println("=== DEBUG: showAddForm SUCCESS ===");
            return "departments/form";

        } catch (Exception e) {
            System.out.println("=== DEBUG: showAddForm ERROR ===");
            System.out.println("Error: " + e.getMessage());
            e.printStackTrace();
            return "redirect:/departments";
        }
    }

    @PostMapping("/add")
    public String addDepartment(@ModelAttribute Department department, RedirectAttributes redirectAttributes) {
        departmentService.createDepartment(department);
        redirectAttributes.addFlashAttribute("message", "Departament dodany pomyślnie!");
        return "redirect:/departments";
    }

    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable Long id, Model model) {
        Optional<Department> department = departmentService.getDepartmentById(id);
        if (department.isPresent()) {
            model.addAttribute("department", department.get());
            model.addAttribute("managers", employeeService.getAllEmployees());
            model.addAttribute("pageTitle", "Edytuj Departament");
            return "departments/form";
        }
        return "redirect:/departments";
    }

    @PostMapping("/edit")
    public String updateDepartment(@ModelAttribute Department department, RedirectAttributes redirectAttributes) {
        departmentService.updateDepartment(department.getId(), department);
        redirectAttributes.addFlashAttribute("message", "Departament zaktualizowany pomyślnie!");
        return "redirect:/departments";
    }

    @GetMapping("/delete/{id}")
    public String deleteDepartment(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        departmentService.deleteDepartment(id);
        redirectAttributes.addFlashAttribute("message", "Departament usunięty pomyślnie!");
        return "redirect:/departments";
    }

    @GetMapping("/details/{id}")
    public String showDepartmentDetails(@PathVariable Long id, Model model) {
        Optional<Department> department = departmentService.getDepartmentById(id);
        if (department.isPresent()) {
            // Pobierz pracowników w tym departamencie
            List<Employee> departmentEmployees = employeeService.getAllEmployees().stream()
                    .filter(emp -> id.equals(emp.getDepartmentId()))
                    .toList();

            // Pobierz managera
            Optional<Employee> manager = null;
            if (department.get().getManagerEmail() != null) {
                manager = employeeService.findEmployeeByEmail(department.get().getManagerEmail());
            }

            model.addAttribute("department", department.get());
            model.addAttribute("employees", departmentEmployees);
            model.addAttribute("manager", manager);
            model.addAttribute("pageTitle", "Szczegóły Departamentu");
            return "departments/details";
        }
        return "redirect:/departments";
    }
}