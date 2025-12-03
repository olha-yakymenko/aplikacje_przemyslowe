package com.techcorp.employee.controller;

import com.techcorp.employee.dto.EmployeeDTO;
import com.techcorp.employee.dto.EmployeeListView;
import com.techcorp.employee.exception.InvalidDataException;
import com.techcorp.employee.model.*;
import com.techcorp.employee.service.DepartmentService;
import com.techcorp.employee.service.EmployeeService;
import com.techcorp.employee.service.EmployeeFormService;
import com.techcorp.employee.service.ImportService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/employees")
public class EmployeeViewController {

    private final EmployeeService employeeService;
    private final EmployeeFormService employeeFormService;
    private final ImportService importService;
    private final DepartmentService departmentService;

    public EmployeeViewController(EmployeeService employeeService,
                                  EmployeeFormService employeeFormService,
                                  ImportService importService, DepartmentService departmentService) {
        this.employeeService = employeeService;
        this.employeeFormService = employeeFormService;
        this.importService = importService;
        this.departmentService = departmentService;
    }


    @GetMapping
    public String listEmployees(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String company,
            @RequestParam(required = false) Position position,
            @RequestParam(required = false) EmploymentStatus status,
            @RequestParam(required = false) Double minSalary,
            @RequestParam(required = false) Double maxSalary,
            @RequestParam(required = false) String departmentName,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "2") int size,
            @RequestParam(defaultValue = "name") String sort,
            Model model) {

        Pageable pageable = PageRequest.of(page, size, Sort.by(sort));

        // DEBUG: Wyświetl parametry
        System.out.println("=== DEBUG: listEmployees ===");
        System.out.println("name: " + name);
        System.out.println("company: " + company);
        System.out.println("position: " + position);
        System.out.println("status: " + status);

        // ✅ Użyj metody z zaawansowanym wyszukiwaniem
        Page<EmployeeListView> employeesPage = employeeService.searchEmployeesAdvanced(
                name, company, position, status, minSalary, maxSalary, departmentName, pageable);

        // ✅ UTWÓRZ BAZOWY QUERY STRING DLA REDIRECTÓW
        String baseRedirectQuery = buildRedirectQueryString(
                name, company, position, status, minSalary, maxSalary, departmentName, size, sort);

        // ✅ SPRAWDŹ CZY STRONA ISTNIEJE
        if (page >= employeesPage.getTotalPages() && employeesPage.getTotalPages() > 0) {
            // Przekieruj na ostatnią istniejącą stronę
            return "redirect:/employees?page=" + (employeesPage.getTotalPages() - 1) + baseRedirectQuery;
        }

        // ✅ SPRAWDŹ CZY STRONA JEST UJEMNA
        if (page < 0) {
            return "redirect:/employees?page=0" + baseRedirectQuery;
        }

        model.addAttribute("employees", employeesPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", employeesPage.getTotalPages());
        model.addAttribute("totalItems", employeesPage.getTotalElements());
        model.addAttribute("pageSize", size);
        model.addAttribute("sortField", sort);
        model.addAttribute("pageTitle", "Lista Pracowników");

        // ✅ Dodaj dane dla formularza wyszukiwania
        EmployeeFormService.EmployeeFormData formData = employeeFormService.getFormData();
        model.addAttribute("positions", formData.getPositions());
        model.addAttribute("statuses", formData.getStatuses());

        // ✅ Lista firm z serwisu (SQL)
        List<String> companies = employeeService.getAllUniqueCompanies();
        model.addAttribute("companies", companies);

        // ✅ Lista departamentów
        List<String> departments = departmentService.getAllDepartmentNames();
        model.addAttribute("departments", departments);

        // ✅ Zapisz wartości wyszukiwania dla formularza
        model.addAttribute("searchName", name);
        model.addAttribute("searchCompany", company);
        model.addAttribute("searchPosition", position);
        model.addAttribute("searchStatus", status);
        model.addAttribute("searchMinSalary", minSalary);
        model.addAttribute("searchMaxSalary", maxSalary);
        model.addAttribute("searchDepartmentName", departmentName);

        System.out.println("=== DEBUG END ===");
        return "employees/list";
    }



    // ✅ METODA POMOCNICZA do budowania query string dla redirectów
    private String buildRedirectQueryString(String name, String company, Position position,
                                            EmploymentStatus status, Double minSalary, Double maxSalary,
                                            String departmentName, int size, String sort) {
        try {
            StringBuilder query = new StringBuilder();
            query.append("&size=").append(size);
            query.append("&sort=").append(sort);

            if (name != null && !name.isEmpty()) {
                query.append("&name=").append(URLEncoder.encode(name, StandardCharsets.UTF_8));
            }
            if (company != null && !company.isEmpty()) {
                query.append("&company=").append(URLEncoder.encode(company, StandardCharsets.UTF_8));
            }
            if (position != null) {
                query.append("&position=").append(position);
            }
            if (status != null) {
                query.append("&status=").append(status);
            }
            if (minSalary != null) {
                query.append("&minSalary=").append(minSalary);
            }
            if (maxSalary != null) {
                query.append("&maxSalary=").append(maxSalary);
            }
            if (departmentName != null && !departmentName.isEmpty()) {
                query.append("&departmentName=").append(URLEncoder.encode(departmentName, StandardCharsets.UTF_8));
            }

            return query.toString();
        } catch (Exception e) {
            // Fallback - podstawowe parametry
            return "&size=" + size + "&sort=" + sort;
        }
    }

//    @GetMapping
//    public String listEmployees(
//            @RequestParam(required = false) String name,
//            @RequestParam(required = false) String company,
//            @RequestParam(required = false) Position position,
//            @RequestParam(required = false) EmploymentStatus status,
//            @RequestParam(required = false) Double minSalary,
//            @RequestParam(required = false) Double maxSalary,
//            @RequestParam(required = false) String departmentName,
//            @RequestParam(defaultValue = "0") int page,
//            @RequestParam(defaultValue = "2") int size,
//            @RequestParam(defaultValue = "name") String sort,
//            Model model) {
//
//        Pageable pageable = PageRequest.of(page, size, Sort.by(sort));
//
//        // DEBUG: Wyświetl parametry
//        System.out.println("=== DEBUG: listEmployees ===");
//        System.out.println("name: " + name);
//        System.out.println("company: " + company);
//        System.out.println("position: " + position);
//        System.out.println("status: " + status);
//
//        // ✅ Użyj metody z zaawansowanym wyszukiwaniem
//        Page<EmployeeListView> employeesPage = employeeService.searchEmployeesAdvanced(
//                name, company, position, status, minSalary, maxSalary, departmentName, pageable);
//
//        // ✅ SPRAWDŹ CZY STRONA ISTNIEJE
//        if (page >= employeesPage.getTotalPages() && employeesPage.getTotalPages() > 0) {
//            // Przekieruj na ostatnią istniejącą stronę
//            return "redirect:/employees?page=" + (employeesPage.getTotalPages() - 1) +
//                    "&size=" + size +
//                    "&sort=" + sort +
//                    (name != null ? "&name=" + URLEncoder.encode(name, StandardCharsets.UTF_8) : "") +
//                    (company != null ? "&company=" + URLEncoder.encode(company, StandardCharsets.UTF_8) : "");
//        }
//
//        // ✅ SPRAWDŹ CZY STRONA JEST UJEMNA
//        if (page < 0) {
//            return "redirect:/employees?page=0" +
//                    "&size=" + size +
//                    "&sort=" + sort;
//        }
//
//        model.addAttribute("employees", employeesPage.getContent());
//        model.addAttribute("currentPage", page);
//        model.addAttribute("totalPages", employeesPage.getTotalPages());
//        model.addAttribute("totalItems", employeesPage.getTotalElements());
//        model.addAttribute("pageSize", size);
//        model.addAttribute("sortField", sort);
//        model.addAttribute("pageTitle", "Lista Pracowników");
//
//        // ✅ Dodaj dane dla formularza wyszukiwania
//        EmployeeFormService.EmployeeFormData formData = employeeFormService.getFormData();
//        model.addAttribute("positions", formData.getPositions());
//        model.addAttribute("statuses", formData.getStatuses());
//
//        // ✅ Lista firm z serwisu (SQL)
//        List<String> companies = employeeService.getAllUniqueCompanies();
//        model.addAttribute("companies", companies);
//
//        // ✅ Lista departamentów
//        List<String> departments = departmentService.getAllDepartmentNames();
//        model.addAttribute("departments", departments);
//
//        // ✅ Zapisz wartości wyszukiwania dla formularza
//        model.addAttribute("searchName", name);
//        model.addAttribute("searchCompany", company);
//        model.addAttribute("searchPosition", position);
//        model.addAttribute("searchStatus", status);
//        model.addAttribute("searchMinSalary", minSalary);
//        model.addAttribute("searchMaxSalary", maxSalary);
//        model.addAttribute("searchDepartmentName", departmentName);
//
//        System.out.println("=== DEBUG END ===");
//        return "employees/list";
//    }




    @GetMapping("/add")
    public String showAddForm(Model model) {
        if (!model.containsAttribute("employee")) {
            model.addAttribute("employee", new EmployeeDTO());
        }

        EmployeeFormService.EmployeeFormData formData = employeeFormService.getFormData();
        model.addAttribute("positions", formData.getPositions());
        model.addAttribute("statuses", formData.getStatuses());
        model.addAttribute("pageTitle", "Dodaj Pracownika");

        return "employees/add-form";
    }

    @PostMapping("/add")
    public String addEmployee(@Valid @ModelAttribute("employee") EmployeeDTO employeeDTO,
                              BindingResult bindingResult,
                              RedirectAttributes redirectAttributes) throws InvalidDataException {

        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("org.springframework.validation.BindingResult.employee", bindingResult);
            redirectAttributes.addFlashAttribute("employee", employeeDTO);
            return "redirect:/employees/add";
        }

        Employee employee = employeeFormService.convertToEntity(employeeDTO);
        boolean added = employeeService.addEmployee(employee);

        if (added) {
            redirectAttributes.addFlashAttribute("message", "Pracownik dodany pomyślnie!");
        } else {
            redirectAttributes.addFlashAttribute("error", "Nie udało się dodać pracownika. Email może już istnieć.");
        }

        return "redirect:/employees";
    }

    @GetMapping("/edit/{email}")
    public String showEditForm(@PathVariable String email, Model model, RedirectAttributes redirectAttributes) {
        Optional<Employee> employeeOpt = employeeService.findEmployeeByEmail(email);

        if (employeeOpt.isPresent()) {
            EmployeeDTO employeeDTO = employeeFormService.convertToDTO(employeeOpt.get());
            EmployeeFormService.EmployeeFormData formData = employeeFormService.getFormData();

            model.addAttribute("employee", employeeDTO);
            model.addAttribute("positions", formData.getPositions());
            model.addAttribute("statuses", formData.getStatuses());
            model.addAttribute("pageTitle", "Edytuj Pracownika");
            return "employees/edit-form";
        } else {
            redirectAttributes.addFlashAttribute("error", "Pracownik o emailu " + email + " nie został znaleziony.");
            return "redirect:/employees";
        }
    }

    @PostMapping("/edit")
    public String updateEmployee(@Valid @ModelAttribute("employee") EmployeeDTO employeeDTO,
                                 BindingResult bindingResult,
                                 RedirectAttributes redirectAttributes) throws InvalidDataException {

        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("org.springframework.validation.BindingResult.employee", bindingResult);
            redirectAttributes.addFlashAttribute("employee", employeeDTO);
            return "redirect:/employees/edit/" + employeeDTO.getEmail();
        }

        EmployeeFormService.FormValidationResult validation = employeeFormService.validateEmployee(employeeDTO);
        if (!validation.isValid()) {
            redirectAttributes.addFlashAttribute("error", validation.getMessage());
            redirectAttributes.addFlashAttribute("employee", employeeDTO);
            return "redirect:/employees/edit/" + employeeDTO.getEmail();
        }

        try {
            Optional<Employee> existingEmployeeOpt = employeeService.findEmployeeByEmail(employeeDTO.getEmail());

            if (existingEmployeeOpt.isEmpty()) {
                redirectAttributes.addFlashAttribute("error", "Pracownik nie został znaleziony.");
                return "redirect:/employees";
            }

            Employee existingEmployee = existingEmployeeOpt.get();

            Employee updatedEmployee = employeeFormService.convertToEntity(employeeDTO);
            updatedEmployee.setId(existingEmployee.getId());

            employeeService.updateEmployee(updatedEmployee);

            redirectAttributes.addFlashAttribute("message", "Pracownik zaktualizowany pomyślnie!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Błąd podczas aktualizacji: " + e.getMessage());
            return "redirect:/employees/edit/" + employeeDTO.getEmail();
        }

        return "redirect:/employees";
    }

    @GetMapping("/delete/{email}")
    public String deleteEmployee(@PathVariable String email, RedirectAttributes redirectAttributes) {
        boolean deleted = employeeService.removeEmployee(email);

        if (deleted) {
            redirectAttributes.addFlashAttribute("message", "Pracownik usunięty pomyślnie!");
        } else {
            redirectAttributes.addFlashAttribute("error", "Pracownik nie został znaleziony.");
        }

        return "redirect:/employees";
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

        if (file.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Plik jest pusty");
            return "redirect:/employees/import";
        }

        ImportSummary summary;
        try {
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
                redirectAttributes.addFlashAttribute("error", errorMessage);
            } else {
                redirectAttributes.addFlashAttribute("message",
                        "Pomyślnie zaimportowano " + importedCount + " pracowników");
            }

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Błąd podczas importu: " + e.getMessage());
        }

        return "redirect:/employees";
    }

}