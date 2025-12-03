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

    // ✅ OPTYMALIZOWANA METODA Z PAGINACJĄ I PROJEKCJĄ
//    @GetMapping
//    public String listEmployees(
//            @RequestParam(defaultValue = "0") int page,
//            @RequestParam(defaultValue = "2") int size,
//            @RequestParam(defaultValue = "name") String sort,
//            Model model) {
//
//        Pageable pageable = PageRequest.of(page, size, Sort.by(sort));
//
//        // Użyj OPTYMALIZOWANEJ metody z projekcją
//        Page<EmployeeListView> employeesPage = employeeService.getAllEmployeesSummary(pageable);
//
//        model.addAttribute("employees", employeesPage.getContent());
//        model.addAttribute("currentPage", page);
//        model.addAttribute("totalPages", employeesPage.getTotalPages());
//        model.addAttribute("totalItems", employeesPage.getTotalElements());
//        model.addAttribute("pageSize", size);
//        model.addAttribute("sortField", sort);
//        model.addAttribute("pageTitle", "Lista Pracowników");
//
//        return "employees/list";
//    }


    // W EmployeeViewController.java - rozszerz metodę listEmployees:

//    @GetMapping
//    public String listEmployees(
//            @RequestParam(required = false) String name,
//            @RequestParam(required = false) String company,
//            @RequestParam(required = false) Position position,
//            @RequestParam(required = false) EmploymentStatus status,
//            @RequestParam(required = false) Double minSalary,
//            @RequestParam(required = false) Double maxSalary,
//            @RequestParam(defaultValue = "0") int page,
//            @RequestParam(defaultValue = "2") int size,
//            @RequestParam(defaultValue = "name") String sort,
//            Model model) {
//
//        Pageable pageable = PageRequest.of(page, size, Sort.by(sort));
//
//        // ✅ Użyj zaawansowanego wyszukiwania z serwisu
//        Page<EmployeeListView> employeesPage = employeeService.searchEmployeesAdvanced(
//                name, company, position, status, minSalary, maxSalary, null, pageable);
//
//        model.addAttribute("employees", employeesPage.getContent());
//        model.addAttribute("currentPage", page);
//        model.addAttribute("totalPages", employeesPage.getTotalPages());
//        model.addAttribute("totalItems", employeesPage.getTotalElements());
//        model.addAttribute("pageSize", size);
//        model.addAttribute("sortField", sort);
//        model.addAttribute("pageTitle", "Lista Pracowników");
//
//        // ✅ Dodaj dane dla formularza
//        EmployeeFormService.EmployeeFormData formData = employeeFormService.getFormData();
//        model.addAttribute("positions", formData.getPositions());
//        model.addAttribute("statuses", formData.getStatuses());
//
//        // ✅ Lista firm z serwisu (SQL)
//        List<String> companies = employeeService.getAllUniqueCompanies();
//        model.addAttribute("companies", companies);
//
//        // ✅ Zapisz wartości wyszukiwania dla formularza
//        model.addAttribute("searchName", name);
//        model.addAttribute("searchCompany", company);
//        model.addAttribute("searchPosition", position);
//        model.addAttribute("searchStatus", status);
//        model.addAttribute("searchMinSalary", minSalary);
//        model.addAttribute("searchMaxSalary", maxSalary);
//
//        return "employees/list";
//    }


//
//    @GetMapping
//    public String listEmployees(
//            @RequestParam(defaultValue = "0") int page,
//            @RequestParam(defaultValue = "2") int size,
//            @RequestParam(defaultValue = "name") String sort,
//            Model model) {
//
//        Pageable pageable = PageRequest.of(page, size, Sort.by(sort));
//
//        // Użyj OPTYMALIZOWANEJ metody z projekcją
//        Page<EmployeeListView> employeesPage = employeeService.getAllEmployeesSummary(pageable);
//
//        model.addAttribute("employees", employeesPage.getContent());
//        model.addAttribute("currentPage", page);
//        model.addAttribute("totalPages", employeesPage.getTotalPages());
//        model.addAttribute("totalItems", employeesPage.getTotalElements());
//        model.addAttribute("pageSize", size);
//        model.addAttribute("sortField", sort);
//        model.addAttribute("pageTitle", "Lista Pracowników");
//
//        return "employees/list";
//    }


    // ✅ TYLKO JEDNA METODA listEmployees w EmployeeViewController.java:
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

        // ✅ SPRAWDŹ CZY STRONA ISTNIEJE
        if (page >= employeesPage.getTotalPages() && employeesPage.getTotalPages() > 0) {
            // Przekieruj na ostatnią istniejącą stronę
            return "redirect:/employees?page=" + (employeesPage.getTotalPages() - 1) +
                    "&size=" + size +
                    "&sort=" + sort +
                    (name != null ? "&name=" + URLEncoder.encode(name, StandardCharsets.UTF_8) : "") +
                    (company != null ? "&company=" + URLEncoder.encode(company, StandardCharsets.UTF_8) : "");
        }

        // ✅ SPRAWDŹ CZY STRONA JEST UJEMNA
        if (page < 0) {
            return "redirect:/employees?page=0" +
                    "&size=" + size +
                    "&sort=" + sort;
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

//    @PostMapping("/edit")
//    public String updateEmployee(@Valid @ModelAttribute("employee") EmployeeDTO employeeDTO,
//                                 BindingResult bindingResult,
//                                 RedirectAttributes redirectAttributes) throws InvalidDataException {
//
//        if (bindingResult.hasErrors()) {
//            redirectAttributes.addFlashAttribute("org.springframework.validation.BindingResult.employee", bindingResult);
//            redirectAttributes.addFlashAttribute("employee", employeeDTO);
//            return "redirect:/employees/edit/" + employeeDTO.getEmail();
//        }
//
//        EmployeeFormService.FormValidationResult validation = employeeFormService.validateEmployee(employeeDTO);
//        if (!validation.isValid()) {
//            redirectAttributes.addFlashAttribute("error", validation.getMessage());
//            redirectAttributes.addFlashAttribute("employee", employeeDTO);
//            return "redirect:/employees/edit/" + employeeDTO.getEmail();
//        }
//
//        Employee updatedEmployee = employeeFormService.convertToEntity(employeeDTO);
//        employeeService.updateEmployee(updatedEmployee);
//        redirectAttributes.addFlashAttribute("message", "Pracownik zaktualizowany pomyślnie!");
//
//        return "redirect:/employees";
//    }



    // W EmployeeViewController - poprawiona metoda updateEmployee:
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
            // 1. Znajdź istniejącego pracownika po emailu
            Optional<Employee> existingEmployeeOpt = employeeService.findEmployeeByEmail(employeeDTO.getEmail());

            if (existingEmployeeOpt.isEmpty()) {
                redirectAttributes.addFlashAttribute("error", "Pracownik nie został znaleziony.");
                return "redirect:/employees";
            }

            // 2. Pobierz istniejące ID
            Employee existingEmployee = existingEmployeeOpt.get();

            // 3. Przekonwertuj DTO na Entity, zachowując ID
            Employee updatedEmployee = employeeFormService.convertToEntity(employeeDTO);
            updatedEmployee.setId(existingEmployee.getId()); // KLUCZOWE: ustaw ID

            // 4. Zaktualizuj pracownika
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

//    @GetMapping("/search")
//    public String showSearchForm(Model model) {
//        model.addAttribute("pageTitle", "Wyszukaj Pracowników");
//        return "employees/search-form";
//    }


    // ✅ DODAJ TĘ METODĘ DO EmployeeViewController.java
    @GetMapping("/search")
    public String showSearchPage(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String company,
            @RequestParam(required = false) Position position,
            @RequestParam(required = false) EmploymentStatus status,
            @RequestParam(required = false) Double minSalary,
            @RequestParam(required = false) Double maxSalary,
            @RequestParam(required = false) String departmentName,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "name") String sort,
            Model model) {

        Pageable pageable = PageRequest.of(page, size, Sort.by(sort));

        // ✅ Użyj metody z zaawansowanym wyszukiwaniem
        Page<EmployeeListView> employeesPage = employeeService.searchEmployeesAdvanced(
                name, company, position, status, minSalary, maxSalary, departmentName, pageable);

        // ✅ Dodaj dane do modelu
        model.addAttribute("employees", employeesPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", employeesPage.getTotalPages());
        model.addAttribute("totalItems", employeesPage.getTotalElements());
        model.addAttribute("pageSize", size);
        model.addAttribute("sortField", sort);
        model.addAttribute("pageTitle", "Wyszukiwanie Pracowników");

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

        return "employees/search-full"; // Nowy widok
    }



    // ✅ OPTYMALIZOWANE WYSZUKIWANIE Z PAGINACJĄ
    // ✅ POPRAWIONE: Użyj metody z projekcją
    @PostMapping("/search")
    public String searchEmployees(
            @RequestParam("company") String company,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            Model model) {

        Pageable pageable = PageRequest.of(page, size, Sort.by("name"));

        // Użyj metody z PROJEKCJĄ
        Page<EmployeeListView> employeesPage = employeeService.getEmployeesByCompanyProjection(company, pageable);

        model.addAttribute("employees", employeesPage.getContent());
        model.addAttribute("searchCompany", company);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", employeesPage.getTotalPages());
        model.addAttribute("totalItems", employeesPage.getTotalElements());
        model.addAttribute("pageTitle", "Wyniki Wyszukiwania");

        if (employeesPage.isEmpty()) {
            model.addAttribute("message", "Nie znaleziono pracowników dla firmy: " + company);
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


    // W EmployeeViewController.java - ZASTĄP:
    @GetMapping("/search/advanced")
    public String showAdvancedSearchForm(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String company,
            @RequestParam(required = false) Position position,
            @RequestParam(required = false) EmploymentStatus status,
            @RequestParam(required = false) Double minSalary,
            @RequestParam(required = false) Double maxSalary,
            @RequestParam(required = false) String departmentName,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "2") int size,
            Model model) {

        model.addAttribute("pageTitle", "Zaawansowane wyszukiwanie");

        // ✅ TYLKO metody z serwisu (które używają SQL)
        EmployeeFormService.EmployeeFormData formData = employeeFormService.getFormData();
        model.addAttribute("positions", formData.getPositions()); // Z enuma
        model.addAttribute("statuses", formData.getStatuses());   // Z enuma

        // ✅ SQL przez serwis
        List<String> companies = employeeService.getAllUniqueCompanies();
        model.addAttribute("companies", companies);

        // ✅ SQL przez serwis (może być EmployeeService lub DepartmentService)
        List<String> departments = departmentService.getAllDepartmentNames();
        model.addAttribute("departments", departments);

        // ✅ Jeśli są kryteria, użyj metody z SQL projekcją
        if (name != null || company != null || position != null || status != null ||
                minSalary != null || maxSalary != null || departmentName != null) {

            Pageable pageable = PageRequest.of(page, size, Sort.by("name"));

            // ✅ Metoda z SQL w repozytorium (przez serwis)
            Page<EmployeeListView> employeesPage = employeeService.searchEmployeesAdvanced(
                    name, company, position, status, minSalary, maxSalary,
                    departmentName, pageable);

            model.addAttribute("employees", employeesPage.getContent());
            model.addAttribute("currentPage", page);
            model.addAttribute("totalPages", employeesPage.getTotalPages());
            model.addAttribute("totalItems", employeesPage.getTotalElements());
            model.addAttribute("hasSearchResults", true);
        }

        // Przekaż parametry wyszukiwania z powrotem do formularza
        model.addAttribute("searchName", name);
        model.addAttribute("searchCompany", company);
        model.addAttribute("searchPosition", position);
        model.addAttribute("searchStatus", status);
        model.addAttribute("searchMinSalary", minSalary);
        model.addAttribute("searchMaxSalary", maxSalary);
        model.addAttribute("searchDepartmentName", departmentName);

        return "employees/search-advanced";
    }
}