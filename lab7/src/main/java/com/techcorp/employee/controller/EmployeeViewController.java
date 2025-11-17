package com.techcorp.employee.controller;

import com.techcorp.employee.dto.EmployeeDTO;
import com.techcorp.employee.exception.InvalidDataException;
import com.techcorp.employee.model.Employee;
import com.techcorp.employee.model.Position;
import com.techcorp.employee.model.EmploymentStatus;
import com.techcorp.employee.service.EmployeeService;
import com.techcorp.employee.service.ImportService;
import com.techcorp.employee.model.ImportSummary;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.multipart.MultipartFile;

import org.springframework.web.multipart.MultipartFile;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/employees")
public class EmployeeViewController {

    @Autowired
    private EmployeeService employeeService;

    @Autowired
    private ImportService importService;

    @GetMapping
    public String listEmployees(Model model) {
        List<Employee> employees = employeeService.getAllEmployees();

        System.out.println("=== LIST EMPLOYEES ===");
        System.out.println("Number of employees: " + employees.size());
        employees.forEach(emp ->
                System.out.println(" - " + emp.getEmail() + ": " + emp.getPosition())
        );

        model.addAttribute("employees", employees);
        model.addAttribute("pageTitle", "Lista Pracowników");
        return "employees/list";
    }

    @GetMapping("/add")
    public String showAddForm(Model model) {
        // Użyj EmployeeDTO zamiast Employee
        model.addAttribute("employee", new EmployeeDTO());
        model.addAttribute("positions", Arrays.asList(Position.values()));
        model.addAttribute("statuses", Arrays.asList(EmploymentStatus.values()));
        model.addAttribute("pageTitle", "Dodaj Pracownika");
        return "employees/add-form";
    }

    @PostMapping("/add")
    public String addEmployee(@ModelAttribute("employee") EmployeeDTO employeeDTO,
                              RedirectAttributes redirectAttributes) {
        try {
            // Konwersja EmployeeDTO na Employee
            String fullName = employeeDTO.getFirstName() + " " + employeeDTO.getLastName();
            Employee employee = new Employee(
                    fullName,
                    employeeDTO.getEmail(),
                    employeeDTO.getCompany(),
                    employeeDTO.getPosition(),
                    employeeDTO.getSalary(),
                    employeeDTO.getStatus()
            );

            boolean added = employeeService.addEmployee(employee);
            if (added) {
                redirectAttributes.addFlashAttribute("message", "Pracownik dodany pomyślnie!");
            } else {
                redirectAttributes.addFlashAttribute("error", "Nie udało się dodać pracownika. Email może już istnieć.");
            }
        } catch (InvalidDataException e) {
            redirectAttributes.addFlashAttribute("error", "Błąd danych: " + e.getMessage());
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Wystąpił nieoczekiwany błąd: " + e.getMessage());
        }
        return "redirect:/employees";
    }

    @GetMapping("/edit/{email}")
    public String showEditForm(@PathVariable String email, Model model, RedirectAttributes redirectAttributes) {
        Optional<Employee> employee = employeeService.findEmployeeByEmail(email);
        if (employee.isPresent()) {
            model.addAttribute("employee", employee.get());
            model.addAttribute("positions", Arrays.asList(Position.values()));
            model.addAttribute("statuses", Arrays.asList(EmploymentStatus.values()));
            model.addAttribute("pageTitle", "Edytuj Pracownika");
            return "employees/edit-form";
        } else {
            redirectAttributes.addFlashAttribute("error", "Pracownik o emailu " + email + " nie został znaleziony.");
            return "redirect:/employees";
        }
    }

    @PostMapping("/edit")
    public String updateEmployee(@RequestParam String name,
                                 @RequestParam String email,
                                 @RequestParam String company,
                                 @RequestParam Position position,
                                 @RequestParam double salary,
                                 @RequestParam EmploymentStatus status,
                                 @RequestParam(required = false) Long departmentId,
                                 RedirectAttributes redirectAttributes) {
        try {
            System.out.println("=== DEBUG UPDATE ===");
            System.out.println("Name: " + name);
            System.out.println("Email: " + email);
            System.out.println("Company: " + company);
            System.out.println("Position: " + position);
            System.out.println("Salary: " + salary);
            System.out.println("Status: " + status);
            System.out.println("DepartmentId: " + departmentId);

            // Sprawdź czy pracownik istnieje
            Optional<Employee> existingEmployee = employeeService.findEmployeeByEmail(email);
            if (existingEmployee.isPresent()) {
                System.out.println("Found existing employee: " + existingEmployee.get());

                // Tworzymy nowego pracownika z zaktualizowanymi danymi
                Employee updatedEmployee = new Employee(name, email, company, position, salary, status);
                if (departmentId != null) {
                    updatedEmployee.setDepartmentId(departmentId);
                }

                System.out.println("Updated employee: " + updatedEmployee);

                // Użyj metody updateEmployee z serwisu
                Employee result = employeeService.updateEmployee(updatedEmployee);
                System.out.println("Result from service: " + result);

                redirectAttributes.addFlashAttribute("message", "Pracownik zaktualizowany pomyślnie!");
            } else {
                System.out.println("Employee not found!");
                redirectAttributes.addFlashAttribute("error", "Pracownik nie istnieje.");
            }
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("error", "Błąd podczas aktualizacji: " + e.getMessage());
        }
        return "redirect:/employees";
    }

    @GetMapping("/delete/{email}")
    public String deleteEmployee(@PathVariable String email, RedirectAttributes redirectAttributes) {
        try {
            boolean deleted = employeeService.removeEmployee(email);
            if (deleted) {
                redirectAttributes.addFlashAttribute("message", "Pracownik usunięty pomyślnie!");
            } else {
                redirectAttributes.addFlashAttribute("error", "Pracownik nie został znaleziony.");
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Błąd podczas usuwania: " + e.getMessage());
        }
        return "redirect:/employees";
    }

    @GetMapping("/search")
    public String showSearchForm(Model model) {
        model.addAttribute("pageTitle", "Wyszukaj Pracowników");
        return "employees/search-form";
    }

    @PostMapping("/search")
    public String searchEmployees(@RequestParam("company") String company, Model model) {
        try {
            List<Employee> employees = employeeService.getEmployeesByCompany(company);
            model.addAttribute("employees", employees);
            model.addAttribute("searchCompany", company);
            model.addAttribute("pageTitle", "Wyniki Wyszukiwania");

            if (employees.isEmpty()) {
                model.addAttribute("message", "Nie znaleziono pracowników dla firmy: " + company);
            }
        } catch (Exception e) {
            model.addAttribute("error", "Błąd podczas wyszukiwania: " + e.getMessage());
        }
        return "employees/search-results";
    }

    @GetMapping("/import")
    public String showImportForm(Model model) {
        model.addAttribute("pageTitle", "Import Pracowników");
        return "employees/import-form";
    }

    @PostMapping("/import")
    public String importEmployees(@RequestParam("file") MultipartFile file,
                                  @RequestParam("fileType") String fileType,
                                  RedirectAttributes redirectAttributes) {
        try {
            // Walidacja podstawowa pliku
            if (file.isEmpty()) {
                redirectAttributes.addFlashAttribute("error", "Plik jest pusty");
                return "redirect:/employees/import";
            }

            ImportSummary summary;
            if ("csv".equalsIgnoreCase(fileType)) {
                summary = importService.importCsvFile(file);
            } else if ("xml".equalsIgnoreCase(fileType)) {
                summary = importService.importXmlFile(file);
            } else {
                redirectAttributes.addFlashAttribute("error", "Nieobsługiwany typ pliku. Obsługiwane typy: CSV, XML");
                return "redirect:/employees/import";
            }

            int importedCount = summary.getImportedCount();
            int errorCount = summary.getErrors().size();

            if (errorCount > 0) {
                String errorMessage = "Import zakończony z błędami. Zaimportowano: " + importedCount +
                        ", Błędy: " + errorCount;

                // Jeśli są jakieś błędy, pokaż pierwsze 3 jako przykład
                if (!summary.getErrors().isEmpty()) {
                    List<String> sampleErrors = summary.getErrors().subList(0, Math.min(3, summary.getErrors().size()));
                    errorMessage += "<br>Przykładowe błędy: " + String.join("; ", sampleErrors);
                }

                redirectAttributes.addFlashAttribute("error", errorMessage);
            } else {
                redirectAttributes.addFlashAttribute("message",
                        "Pomyślnie zaimportowano " + importedCount + " pracowników");
            }

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error",
                    "Błąd podczas importu: " + e.getMessage());
        }
        return "redirect:/employees";
    }
}