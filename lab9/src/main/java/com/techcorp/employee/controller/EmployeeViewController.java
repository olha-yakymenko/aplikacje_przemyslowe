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
            Model model,
            RedirectAttributes redirectAttributes) {

        try {
            Pageable pageable = PageRequest.of(page, size, Sort.by(sort));

            Page<EmployeeListView> employeesPage = employeeService.searchEmployeesAdvanced(
                    name, company, position, status, minSalary, maxSalary, departmentName, pageable);

            if (page >= employeesPage.getTotalPages() && employeesPage.getTotalPages() > 0) {
                String redirectUrl = buildPaginationRedirectUrl(
                        employeesPage.getTotalPages() - 1, name, company, position, status,
                        minSalary, maxSalary, departmentName, size, sort);
                return "redirect:" + redirectUrl;
            }

            if (page < 0) {
                String redirectUrl = buildPaginationRedirectUrl(0, name, company, position, status,
                        minSalary, maxSalary, departmentName, size, sort);
                return "redirect:" + redirectUrl;
            }

            populateModel(model, employeesPage, page, size, sort, name, company, position,
                    status, minSalary, maxSalary, departmentName);

            return "employees/list";

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error",
                    "Błąd podczas ładowania listy pracowników: " + e.getMessage());
            return "redirect:/employees";
        }
    }

    private String buildPaginationRedirectUrl(int targetPage,
                                              String name, String company, Position position,
                                              EmploymentStatus status, Double minSalary,
                                              Double maxSalary, String departmentName,
                                              int size, String sort) {

        StringBuilder url = new StringBuilder("/employees?page=").append(targetPage);

        if (size != 2) url.append("&size=").append(size);
        if (!"name".equals(sort)) url.append("&sort=").append(sort);

        if (name != null && !name.trim().isEmpty()) {
            url.append("&name=").append(URLEncoder.encode(name.trim(), StandardCharsets.UTF_8));
        }
        if (company != null && !company.trim().isEmpty()) {
            url.append("&company=").append(URLEncoder.encode(company.trim(), StandardCharsets.UTF_8));
        }
        if (position != null) {
            url.append("&position=").append(position);
        }
        if (status != null) {
            url.append("&status=").append(status);
        }
        if (minSalary != null) {
            url.append("&minSalary=").append(minSalary);
        }
        if (maxSalary != null) {
            url.append("&maxSalary=").append(maxSalary);
        }
        if (departmentName != null && !departmentName.trim().isEmpty() &&
                !"null".equalsIgnoreCase(departmentName)) {
            url.append("&departmentName=")
                    .append(URLEncoder.encode(departmentName.trim(), StandardCharsets.UTF_8));
        }

        return url.toString();
    }

    private void populateModel(Model model, Page<EmployeeListView> employeesPage,
                               int currentPage, int pageSize, String sortField,
                               String searchName, String searchCompany, Position searchPosition,
                               EmploymentStatus searchStatus, Double searchMinSalary,
                               Double searchMaxSalary, String searchDepartmentName) {

        model.addAttribute("employees", employeesPage.getContent());
        model.addAttribute("currentPage", currentPage);
        model.addAttribute("totalPages", employeesPage.getTotalPages());
        model.addAttribute("totalItems", employeesPage.getTotalElements());
        model.addAttribute("pageSize", pageSize);
        model.addAttribute("sortField", sortField);
        model.addAttribute("pageTitle", "Lista Pracowników");

        model.addAttribute("searchName", searchName);
        model.addAttribute("searchCompany", searchCompany);
        model.addAttribute("searchPosition", searchPosition);
        model.addAttribute("searchStatus", searchStatus);
        model.addAttribute("searchMinSalary", searchMinSalary);
        model.addAttribute("searchMaxSalary", searchMaxSalary);
        model.addAttribute("searchDepartmentName", searchDepartmentName);

        EmployeeFormService.EmployeeFormData formData = employeeFormService.getFormData();
        model.addAttribute("positions", formData.getPositions());
        model.addAttribute("statuses", formData.getStatuses());

        model.addAttribute("companies", employeeService.getAllUniqueCompanies());
        model.addAttribute("departments", departmentService.getAllDepartmentNames());
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

    @GetMapping("/list")
    public String listEmployeesQuick(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "name") String sort,
            Model model) {

        int validatedPage = Math.max(page, 0);
        int validatedSize = Math.min(Math.max(size, 1), 100);

        Pageable pageable = PageRequest.of(validatedPage, validatedSize, Sort.by(sort));

        Page<EmployeeListView> employeesPage = employeeService.getAllEmployeesProjection(pageable);

        int totalPages = employeesPage.getTotalPages();
        int finalPage = validatedPage;

        if (totalPages > 0 && validatedPage >= totalPages) {
            finalPage = totalPages - 1;
        }

        if (validatedPage != finalPage) {
            return "redirect:/employees/list?page=" + finalPage +
                    "&size=" + validatedSize + "&sort=" + sort;
        }

        model.addAttribute("employees", employeesPage.getContent());
        model.addAttribute("currentPage", finalPage);
        model.addAttribute("totalPages", totalPages);
        model.addAttribute("totalItems", employeesPage.getTotalElements());
        model.addAttribute("pageSize", validatedSize);
        model.addAttribute("sortField", sort);
        model.addAttribute("pageTitle", "Szybka lista pracowników");

        return "employees/list-quick";
    }

    @GetMapping("/search")
    public String searchEmployeesFull(
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

        int validatedPage = Math.max(page, 0);
        int validatedSize = Math.min(Math.max(size, 1), 100);

        Pageable pageable = PageRequest.of(validatedPage, validatedSize, Sort.by(sort));

        Page<Employee> employeesPage;

        boolean hasFilters = name != null || company != null || position != null ||
                status != null || minSalary != null || maxSalary != null ||
                (departmentName != null && !departmentName.trim().isEmpty());

        if (hasFilters) {
            employeesPage = employeeService.searchEmployeesWithSpecifications(
                    name, company, position, status, minSalary, maxSalary, departmentName, pageable);
        } else {
            employeesPage = employeeService.getAllEmployees(pageable);
        }

        int totalPages = employeesPage.getTotalPages();
        int finalPage = validatedPage;

        if (totalPages > 0 && validatedPage >= totalPages) {
            finalPage = totalPages - 1;
        }

        if (validatedPage != finalPage) {
            return buildRedirectUrlWithAllParams(finalPage, validatedSize, sort,
                    name, company, position, status, minSalary, maxSalary, departmentName);
        }

        model.addAttribute("employees", employeesPage.getContent());
        model.addAttribute("currentPage", finalPage);
        model.addAttribute("totalPages", totalPages);
        model.addAttribute("totalItems", employeesPage.getTotalElements());
        model.addAttribute("pageSize", validatedSize);
        model.addAttribute("sortField", sort);
        model.addAttribute("pageTitle", "Zaawansowane wyszukiwanie pracowników");

        model.addAttribute("searchName", name);
        model.addAttribute("searchCompany", company);
        model.addAttribute("searchPosition", position);
        model.addAttribute("searchStatus", status);
        model.addAttribute("searchMinSalary", minSalary);
        model.addAttribute("searchMaxSalary", maxSalary);
        model.addAttribute("searchDepartmentName", departmentName);

        EmployeeFormService.EmployeeFormData formData = employeeFormService.getFormData();
        model.addAttribute("positions", formData.getPositions());
        model.addAttribute("statuses", formData.getStatuses());
        model.addAttribute("companies", employeeService.getAllUniqueCompanies());
        model.addAttribute("departments", departmentService.getAllDepartmentNames());

        return "employees/search-full";
    }

    private String buildRedirectUrlWithAllParams(int targetPage, int size, String sort,
                                                 String name, String company, Position position,
                                                 EmploymentStatus status, Double minSalary,
                                                 Double maxSalary, String departmentName) {

        StringBuilder url = new StringBuilder();
        url.append("/employees/search?page=").append(targetPage)
                .append("&size=").append(size)
                .append("&sort=").append(sort);

        if (name != null && !name.trim().isEmpty()) {
            try {
                url.append("&name=").append(URLEncoder.encode(name.trim(), StandardCharsets.UTF_8));
            } catch (Exception e) {
                url.append("&name=").append(name.trim());
            }
        }

        if (company != null && !company.trim().isEmpty()) {
            try {
                url.append("&company=").append(URLEncoder.encode(company.trim(), StandardCharsets.UTF_8));
            } catch (Exception e) {
                url.append("&company=").append(company.trim());
            }
        }

        if (position != null) {
            url.append("&position=").append(position.name());
        }

        if (status != null) {
            url.append("&status=").append(status.name());
        }

        if (minSalary != null) {
            url.append("&minSalary=").append(minSalary);
        }

        if (maxSalary != null) {
            url.append("&maxSalary=").append(maxSalary);
        }

        if (departmentName != null && !departmentName.trim().isEmpty()) {
            try {
                url.append("&departmentName=").append(URLEncoder.encode(departmentName.trim(), StandardCharsets.UTF_8));
            } catch (Exception e) {
                url.append("&departmentName=").append(departmentName.trim());
            }
        }

        return "redirect:" + url.toString();
    }

}











//package com.techcorp.employee.controller;
//
//import com.techcorp.employee.dto.EmployeeDTO;
//import com.techcorp.employee.dto.EmployeeListView;
//import com.techcorp.employee.exception.InvalidDataException;
//import com.techcorp.employee.model.*;
//import com.techcorp.employee.service.DepartmentService;
//import com.techcorp.employee.service.EmployeeService;
//import com.techcorp.employee.service.EmployeeFormService;
//import com.techcorp.employee.service.ImportService;
//import jakarta.validation.Valid;
//import org.springframework.data.domain.Page;
//import org.springframework.data.domain.PageRequest;
//import org.springframework.data.domain.Pageable;
//import org.springframework.data.domain.Sort;
//import org.springframework.stereotype.Controller;
//import org.springframework.ui.Model;
//import org.springframework.validation.BindingResult;
//import org.springframework.web.bind.annotation.*;
//import org.springframework.web.multipart.MultipartFile;
//import org.springframework.web.servlet.mvc.support.RedirectAttributes;
//
//import java.net.URLEncoder;
//import java.nio.charset.StandardCharsets;
//import java.util.List;
//import java.util.Optional;
//import java.util.stream.Collectors;
//
//@Controller
//@RequestMapping("/employees")
//public class EmployeeViewController {
//
//    private final EmployeeService employeeService;
//    private final EmployeeFormService employeeFormService;
//    private final ImportService importService;
//    private final DepartmentService departmentService;
//
//    public EmployeeViewController(EmployeeService employeeService,
//                                  EmployeeFormService employeeFormService,
//                                  ImportService importService, DepartmentService departmentService) {
//        this.employeeService = employeeService;
//        this.employeeFormService = employeeFormService;
//        this.importService = importService;
//        this.departmentService = departmentService;
//    }
//
//
////    @GetMapping
////    public String listEmployees(
////            @RequestParam(required = false) String name,
////            @RequestParam(required = false) String company,
////            @RequestParam(required = false) Position position,
////            @RequestParam(required = false) EmploymentStatus status,
////            @RequestParam(required = false) Double minSalary,
////            @RequestParam(required = false) Double maxSalary,
////            @RequestParam(required = false) String departmentName,
////            @RequestParam(defaultValue = "0") int page,
////            @RequestParam(defaultValue = "2") int size,
////            @RequestParam(defaultValue = "name") String sort,
////            Model model) {
////
////        Pageable pageable = PageRequest.of(page, size, Sort.by(sort));
////
////        System.out.println("=== DEBUG: listEmployees ===");
////        System.out.println("name: " + name);
////        System.out.println("company: " + company);
////        System.out.println("position: " + position);
////        System.out.println("status: " + status);
////
//////        Page<EmployeeListView> employeesPage = employeeService.searchEmployeesAdvanced(
//////                name, company, position, status, minSalary, maxSalary, departmentName, pageable);
////
////
////        Page<EmployeeListView> employeesPage;
////        if (name == null && company == null && position == null && status == null &&
////                minSalary == null && maxSalary == null && departmentName == null) {
////            // Kiedy brak filtrów, użyj prostej metody
////            employeesPage = employeeService.getAllEmployeesProjection(pageable);
////        } else {
////            // Kiedy są filtry, użyj zaawansowanej z projekcją
////            employeesPage = employeeService.searchEmployeesAdvanced(
////                    name, company, position, status, minSalary, maxSalary, departmentName, pageable);
////        }
////
////
////        String baseRedirectQuery = buildRedirectQueryString(
////                name, company, position, status, minSalary, maxSalary, departmentName, size, sort);
////
////        if (page >= employeesPage.getTotalPages() && employeesPage.getTotalPages() > 0) {
////            // Przekieruj na ostatnią istniejącą stronę
////            return "redirect:/employees?page=" + (employeesPage.getTotalPages() - 1) + baseRedirectQuery;
////        }
////
////        if (page < 0) {
////            return "redirect:/employees?page=0" + baseRedirectQuery;
////        }
////
////        model.addAttribute("employees", employeesPage.getContent());
////        model.addAttribute("currentPage", page);
////        model.addAttribute("totalPages", employeesPage.getTotalPages());
////        model.addAttribute("totalItems", employeesPage.getTotalElements());
////        model.addAttribute("pageSize", size);
////        model.addAttribute("sortField", sort);
////        model.addAttribute("pageTitle", "Lista Pracowników");
////
////        EmployeeFormService.EmployeeFormData formData = employeeFormService.getFormData();
////        model.addAttribute("positions", formData.getPositions());
////        model.addAttribute("statuses", formData.getStatuses());
////
////        List<String> companies = employeeService.getAllUniqueCompanies();
////        model.addAttribute("companies", companies);
////
////        List<String> departments = departmentService.getAllDepartmentNames();
////        model.addAttribute("departments", departments);
////
////        model.addAttribute("searchName", name);
////        model.addAttribute("searchCompany", company);
////        model.addAttribute("searchPosition", position);
////        model.addAttribute("searchStatus", status);
////        model.addAttribute("searchMinSalary", minSalary);
////        model.addAttribute("searchMaxSalary", maxSalary);
////        model.addAttribute("searchDepartmentName", departmentName);
////
////        System.out.println("=== DEBUG END ===");
////        return "employees/list";
////    }
//
//
//
//@GetMapping
//public String listEmployees(
//        @RequestParam(required = false) String name,
//        @RequestParam(required = false) String company,
//        @RequestParam(required = false) Position position,
//        @RequestParam(required = false) EmploymentStatus status,
//        @RequestParam(required = false) Double minSalary,
//        @RequestParam(required = false) Double maxSalary,
//        @RequestParam(required = false) String departmentName,
//        @RequestParam(defaultValue = "0") int page,
//        @RequestParam(defaultValue = "2") int size,
//        @RequestParam(defaultValue = "name") String sort,
//        Model model,
//        RedirectAttributes redirectAttributes) {  // ✅ Dodaj RedirectAttributes
//
//    try {
//        Pageable pageable = PageRequest.of(page, size, Sort.by(sort));
//
//        Page<EmployeeListView> employeesPage = employeeService.searchEmployeesAdvanced(
//                name, company, position, status, minSalary, maxSalary, departmentName, pageable);
//
//        if (page >= employeesPage.getTotalPages() && employeesPage.getTotalPages() > 0) {
//            String redirectUrl = buildPaginationRedirectUrl(
//                    employeesPage.getTotalPages() - 1, name, company, position, status,
//                    minSalary, maxSalary, departmentName, size, sort);
//            return "redirect:" + redirectUrl;
//        }
//
//        if (page < 0) {
//            String redirectUrl = buildPaginationRedirectUrl(0, name, company, position, status,
//                    minSalary, maxSalary, departmentName, size, sort);
//            return "redirect:" + redirectUrl;
//        }
//
//        populateModel(model, employeesPage, page, size, sort, name, company, position,
//                status, minSalary, maxSalary, departmentName);
//
//        return "employees/list";
//
//    } catch (Exception e) {
//        redirectAttributes.addFlashAttribute("error",
//                "Błąd podczas ładowania listy pracowników: " + e.getMessage());
//        return "redirect:/employees";
//    }
//}
//
//
//    //  1. Metoda do budowania URL dla przekierowań paginacji
//    private String buildPaginationRedirectUrl(int targetPage,
//                                              String name, String company, Position position,
//                                              EmploymentStatus status, Double minSalary,
//                                              Double maxSalary, String departmentName,
//                                              int size, String sort) {
//
//        StringBuilder url = new StringBuilder("/employees?page=").append(targetPage);
//
//        if (size != 2) url.append("&size=").append(size);
//        if (!"name".equals(sort)) url.append("&sort=").append(sort);
//
//        if (name != null && !name.trim().isEmpty()) {
//            url.append("&name=").append(URLEncoder.encode(name.trim(), StandardCharsets.UTF_8));
//        }
//        if (company != null && !company.trim().isEmpty()) {
//            url.append("&company=").append(URLEncoder.encode(company.trim(), StandardCharsets.UTF_8));
//        }
//        if (position != null) {
//            url.append("&position=").append(position);
//        }
//        if (status != null) {
//            url.append("&status=").append(status);
//        }
//        if (minSalary != null) {
//            url.append("&minSalary=").append(minSalary);
//        }
//        if (maxSalary != null) {
//            url.append("&maxSalary=").append(maxSalary);
//        }
//        if (departmentName != null && !departmentName.trim().isEmpty() &&
//                !"null".equalsIgnoreCase(departmentName)) {
//            url.append("&departmentName=")
//                    .append(URLEncoder.encode(departmentName.trim(), StandardCharsets.UTF_8));
//        }
//
//        return url.toString();
//    }
//
//    //  2. Metoda do wypełniania modelu (separacja odpowiedzialności)
//    private void populateModel(Model model, Page<EmployeeListView> employeesPage,
//                               int currentPage, int pageSize, String sortField,
//                               String searchName, String searchCompany, Position searchPosition,
//                               EmploymentStatus searchStatus, Double searchMinSalary,
//                               Double searchMaxSalary, String searchDepartmentName) {
//
//        // Podstawowe atrybuty paginacji
//        model.addAttribute("employees", employeesPage.getContent());
//        model.addAttribute("currentPage", currentPage);
//        model.addAttribute("totalPages", employeesPage.getTotalPages());
//        model.addAttribute("totalItems", employeesPage.getTotalElements());
//        model.addAttribute("pageSize", pageSize);
//        model.addAttribute("sortField", sortField);
//        model.addAttribute("pageTitle", "Lista Pracowników");
//
//        // Wartości wyszukiwania (dla formularza)
//        model.addAttribute("searchName", searchName);
//        model.addAttribute("searchCompany", searchCompany);
//        model.addAttribute("searchPosition", searchPosition);
//        model.addAttribute("searchStatus", searchStatus);
//        model.addAttribute("searchMinSalary", searchMinSalary);
//        model.addAttribute("searchMaxSalary", searchMaxSalary);
//        model.addAttribute("searchDepartmentName", searchDepartmentName);
//
//        // Dane dla selectów w formularzu
//        EmployeeFormService.EmployeeFormData formData = employeeFormService.getFormData();
//        model.addAttribute("positions", formData.getPositions());
//        model.addAttribute("statuses", formData.getStatuses());
//
//        // Listy firm i departamentów
//        model.addAttribute("companies", employeeService.getAllUniqueCompanies());
//        model.addAttribute("departments", departmentService.getAllDepartmentNames());
//    }
//
//
////    @GetMapping
////    public String listEmployees(
////            @RequestParam(required = false) String name,
////            @RequestParam(required = false) String company,
////            @RequestParam(required = false) Position position,
////            @RequestParam(required = false) EmploymentStatus status,
////            @RequestParam(required = false) Double minSalary,
////            @RequestParam(required = false) Double maxSalary,
////            @RequestParam(required = false) String departmentName,
////            @RequestParam(defaultValue = "0") int page,
////            @RequestParam(defaultValue = "2") int size,
////            @RequestParam(defaultValue = "name") String sort,
////            Model model) {
////
////        Pageable pageable = PageRequest.of(page, size, Sort.by(sort));
////
////        // DEBUG: Wyświetl parametry
////        System.out.println("=== DEBUG: listEmployees ===");
////        System.out.println("name: " + name);
////        System.out.println("company: " + company);
////        System.out.println("position: " + position);
////        System.out.println("status: " + status);
////
////        // ✅ Użyj metody z zaawansowanym wyszukiwaniem
////        Page<EmployeeListView> employeesPage = employeeService.searchEmployeesAdvanced(
////                name, company, position, status, minSalary, maxSalary, departmentName, pageable);
////
////        // ✅ SPRAWDŹ CZY STRONA ISTNIEJE
////        if (page >= employeesPage.getTotalPages() && employeesPage.getTotalPages() > 0) {
////            // Przekieruj na ostatnią istniejącą stronę
////            return "redirect:/employees?page=" + (employeesPage.getTotalPages() - 1) +
////                    "&size=" + size +
////                    "&sort=" + sort +
////                    (name != null ? "&name=" + URLEncoder.encode(name, StandardCharsets.UTF_8) : "") +
////                    (company != null ? "&company=" + URLEncoder.encode(company, StandardCharsets.UTF_8) : "");
////        }
////
////        // ✅ SPRAWDŹ CZY STRONA JEST UJEMNA
////        if (page < 0) {
////            return "redirect:/employees?page=0" +
////                    "&size=" + size +
////                    "&sort=" + sort;
////        }
////
////        model.addAttribute("employees", employeesPage.getContent());
////        model.addAttribute("currentPage", page);
////        model.addAttribute("totalPages", employeesPage.getTotalPages());
////        model.addAttribute("totalItems", employeesPage.getTotalElements());
////        model.addAttribute("pageSize", size);
////        model.addAttribute("sortField", sort);
////        model.addAttribute("pageTitle", "Lista Pracowników");
////
////        // ✅ Dodaj dane dla formularza wyszukiwania
////        EmployeeFormService.EmployeeFormData formData = employeeFormService.getFormData();
////        model.addAttribute("positions", formData.getPositions());
////        model.addAttribute("statuses", formData.getStatuses());
////
////        // ✅ Lista firm z serwisu (SQL)
////        List<String> companies = employeeService.getAllUniqueCompanies();
////        model.addAttribute("companies", companies);
////
////        // ✅ Lista departamentów
////        List<String> departments = departmentService.getAllDepartmentNames();
////        model.addAttribute("departments", departments);
////
////        // ✅ Zapisz wartości wyszukiwania dla formularza
////        model.addAttribute("searchName", name);
////        model.addAttribute("searchCompany", company);
////        model.addAttribute("searchPosition", position);
////        model.addAttribute("searchStatus", status);
////        model.addAttribute("searchMinSalary", minSalary);
////        model.addAttribute("searchMaxSalary", maxSalary);
////        model.addAttribute("searchDepartmentName", departmentName);
////
////        System.out.println("=== DEBUG END ===");
////        return "employees/list";
////    }
//
//
//
//
//    @GetMapping("/add")
//    public String showAddForm(Model model) {
//        if (!model.containsAttribute("employee")) {
//            model.addAttribute("employee", new EmployeeDTO());
//        }
//
//        EmployeeFormService.EmployeeFormData formData = employeeFormService.getFormData();
//        model.addAttribute("positions", formData.getPositions());
//        model.addAttribute("statuses", formData.getStatuses());
//        model.addAttribute("pageTitle", "Dodaj Pracownika");
//
//        return "employees/add-form";
//    }
//
//    @PostMapping("/add")
//    public String addEmployee(@Valid @ModelAttribute("employee") EmployeeDTO employeeDTO,
//                              BindingResult bindingResult,
//                              RedirectAttributes redirectAttributes) throws InvalidDataException {
//
//        if (bindingResult.hasErrors()) {
//            redirectAttributes.addFlashAttribute("org.springframework.validation.BindingResult.employee", bindingResult);
//            redirectAttributes.addFlashAttribute("employee", employeeDTO);
//            return "redirect:/employees/add";
//        }
//
//        Employee employee = employeeFormService.convertToEntity(employeeDTO);
//        boolean added = employeeService.addEmployee(employee);
//
//        if (added) {
//            redirectAttributes.addFlashAttribute("message", "Pracownik dodany pomyślnie!");
//        } else {
//            redirectAttributes.addFlashAttribute("error", "Nie udało się dodać pracownika. Email może już istnieć.");
//        }
//
//        return "redirect:/employees";
//    }
//
//    @GetMapping("/edit/{email}")
//    public String showEditForm(@PathVariable String email, Model model, RedirectAttributes redirectAttributes) {
//        Optional<Employee> employeeOpt = employeeService.findEmployeeByEmail(email);
//
//        if (employeeOpt.isPresent()) {
//            EmployeeDTO employeeDTO = employeeFormService.convertToDTO(employeeOpt.get());
//            EmployeeFormService.EmployeeFormData formData = employeeFormService.getFormData();
//
//            model.addAttribute("employee", employeeDTO);
//            model.addAttribute("positions", formData.getPositions());
//            model.addAttribute("statuses", formData.getStatuses());
//            model.addAttribute("pageTitle", "Edytuj Pracownika");
//            return "employees/edit-form";
//        } else {
//            redirectAttributes.addFlashAttribute("error", "Pracownik o emailu " + email + " nie został znaleziony.");
//            return "redirect:/employees";
//        }
//    }
//
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
//        try {
//            Optional<Employee> existingEmployeeOpt = employeeService.findEmployeeByEmail(employeeDTO.getEmail());
//
//            if (existingEmployeeOpt.isEmpty()) {
//                redirectAttributes.addFlashAttribute("error", "Pracownik nie został znaleziony.");
//                return "redirect:/employees";
//            }
//
//            Employee existingEmployee = existingEmployeeOpt.get();
//
//            Employee updatedEmployee = employeeFormService.convertToEntity(employeeDTO);
//            updatedEmployee.setId(existingEmployee.getId());
//
//            employeeService.updateEmployee(updatedEmployee);
//
//            redirectAttributes.addFlashAttribute("message", "Pracownik zaktualizowany pomyślnie!");
//        } catch (Exception e) {
//            redirectAttributes.addFlashAttribute("error", "Błąd podczas aktualizacji: " + e.getMessage());
//            return "redirect:/employees/edit/" + employeeDTO.getEmail();
//        }
//
//        return "redirect:/employees";
//    }
//
//    @GetMapping("/delete/{email}")
//    public String deleteEmployee(@PathVariable String email, RedirectAttributes redirectAttributes) {
//        boolean deleted = employeeService.removeEmployee(email);
//
//        if (deleted) {
//            redirectAttributes.addFlashAttribute("message", "Pracownik usunięty pomyślnie!");
//        } else {
//            redirectAttributes.addFlashAttribute("error", "Pracownik nie został znaleziony.");
//        }
//
//        return "redirect:/employees";
//    }
//
//
//    @GetMapping("/import")
//    public String showImportForm(Model model) {
//        model.addAttribute("pageTitle", "Import Pracowników");
//        return "employees/import-form";
//    }
//
//    @PostMapping("/import")
//    public String importEmployees(@RequestParam("file") MultipartFile file,
//                                  @RequestParam("fileType") String fileType,
//                                  RedirectAttributes redirectAttributes) {
//
//        if (file.isEmpty()) {
//            redirectAttributes.addFlashAttribute("error", "Plik jest pusty");
//            return "redirect:/employees/import";
//        }
//
//        ImportSummary summary;
//        try {
//            if ("csv".equalsIgnoreCase(fileType)) {
//                summary = importService.importCsvFile(file);
//            } else if ("xml".equalsIgnoreCase(fileType)) {
//                summary = importService.importXmlFile(file);
//            } else {
//                redirectAttributes.addFlashAttribute("error", "Nieobsługiwany typ pliku. Obsługiwane typy: CSV, XML");
//                return "redirect:/employees/import";
//            }
//
//            int importedCount = summary.getImportedCount();
//            int errorCount = summary.getErrors().size();
//
//            if (errorCount > 0) {
//                String errorMessage = "Import zakończony z błędami. Zaimportowano: " + importedCount +
//                        ", Błędy: " + errorCount;
//                redirectAttributes.addFlashAttribute("error", errorMessage);
//            } else {
//                redirectAttributes.addFlashAttribute("message",
//                        "Pomyślnie zaimportowano " + importedCount + " pracowników");
//            }
//
//        } catch (Exception e) {
//            redirectAttributes.addFlashAttribute("error", "Błąd podczas importu: " + e.getMessage());
//        }
//
//        return "redirect:/employees";
//    }

//
//    @GetMapping("/list")
//    public String listEmployeesQuick(
//            @RequestParam(defaultValue = "0") int page,
//            @RequestParam(defaultValue = "10") int size,
//            @RequestParam(defaultValue = "name") String sort,
//            Model model) {
//
//        Pageable pageable = PageRequest.of(page, size, Sort.by(sort));
//
//        Page<EmployeeListView> employeesPage = employeeService.getAllEmployeesProjection(pageable);
//
//        if (page >= employeesPage.getTotalPages() && employeesPage.getTotalPages() > 0) {
//            return "redirect:/employees/list?page=" + (employeesPage.getTotalPages() - 1) +
//                    "&size=" + size + "&sort=" + sort;
//        }
//        if (page < 0) {
//            return "redirect:/employees/list?page=0&size=" + size + "&sort=" + sort;
//        }
//
//        model.addAttribute("employees", employeesPage.getContent());
//        model.addAttribute("currentPage", page);
//        model.addAttribute("totalPages", employeesPage.getTotalPages());
//        model.addAttribute("totalItems", employeesPage.getTotalElements());
//        model.addAttribute("pageSize", size);
//        model.addAttribute("sortField", sort);
//        model.addAttribute("pageTitle", "Szybka lista pracowników");
//
//        return "employees/list-quick";
//    }
//
//    @GetMapping("/search")
//    public String searchEmployeesFull(
//            @RequestParam(required = false) String name,
//            @RequestParam(required = false) String company,
//            @RequestParam(required = false) Position position,
//            @RequestParam(required = false) EmploymentStatus status,
//            @RequestParam(required = false) Double minSalary,
//            @RequestParam(required = false) Double maxSalary,
//            @RequestParam(required = false) String departmentName,
//            @RequestParam(defaultValue = "0") int page,
//            @RequestParam(defaultValue = "10") int size,
//            @RequestParam(defaultValue = "name") String sort,
//            Model model) {
//
//        Pageable pageable = PageRequest.of(page, size, Sort.by(sort));
//
//        Page<Employee> employeesPage;
//
//        boolean hasFilters = name != null || company != null || position != null ||
//                status != null || minSalary != null || maxSalary != null ||
//                (departmentName != null && !departmentName.trim().isEmpty());
//
//        if (hasFilters) {
//            employeesPage = employeeService.searchEmployeesWithSpecifications(
//                    name, company, position, status, minSalary, maxSalary, departmentName, pageable);
//        } else {
//            employeesPage = employeeService.getAllEmployees(pageable);
//        }
//
//        if (page >= employeesPage.getTotalPages() && employeesPage.getTotalPages() > 0) {
//            return buildRedirectUrlWithAllParams((employeesPage.getTotalPages() - 1),
//                    size, sort, name, company, position, status, minSalary, maxSalary, departmentName);
//        }
//        if (page < 0) {
//            return buildRedirectUrlWithAllParams(0, size, sort,
//                    name, company, position, status, minSalary, maxSalary, departmentName);
//        }
//
//        // ✅ WYPEŁNIENIE MODELU
//        model.addAttribute("employees", employeesPage.getContent());
//        model.addAttribute("currentPage", page);
//        model.addAttribute("totalPages", employeesPage.getTotalPages());
//        model.addAttribute("totalItems", employeesPage.getTotalElements());
//        model.addAttribute("pageSize", size);
//        model.addAttribute("sortField", sort);
//        model.addAttribute("pageTitle", "Zaawansowane wyszukiwanie pracowników");
//
//        // ✅ DANE DLA FORMULARZA FILTROWANIA
//        model.addAttribute("searchName", name);
//        model.addAttribute("searchCompany", company);
//        model.addAttribute("searchPosition", position);
//        model.addAttribute("searchStatus", status);
//        model.addAttribute("searchMinSalary", minSalary);
//        model.addAttribute("searchMaxSalary", maxSalary);
//        model.addAttribute("searchDepartmentName", departmentName);
//
//        // ✅ LISTY DLA SELECTÓW
//        EmployeeFormService.EmployeeFormData formData = employeeFormService.getFormData();
//        model.addAttribute("positions", formData.getPositions());
//        model.addAttribute("statuses", formData.getStatuses());
//        model.addAttribute("companies", employeeService.getAllUniqueCompanies());
//        model.addAttribute("departments", departmentService.getAllDepartmentNames());
//
//        return "employees/search-full";
//    }
//
//
//
//
//    private String buildRedirectUrlWithAllParams(int targetPage, int size, String sort,
//                                                 String name, String company, Position position,
//                                                 EmploymentStatus status, Double minSalary,
//                                                 Double maxSalary, String departmentName) {
//
//        StringBuilder url = new StringBuilder();
//        url.append("/employees/search?page=").append(targetPage)
//                .append("&size=").append(size)
//                .append("&sort=").append(sort);
//
//        if (name != null && !name.trim().isEmpty()) {
//            try {
//                url.append("&name=").append(URLEncoder.encode(name.trim(), StandardCharsets.UTF_8));
//            } catch (Exception e) {
//                url.append("&name=").append(name.trim());
//            }
//        }
//
//        if (company != null && !company.trim().isEmpty()) {
//            try {
//                url.append("&company=").append(URLEncoder.encode(company.trim(), StandardCharsets.UTF_8));
//            } catch (Exception e) {
//                url.append("&company=").append(company.trim());
//            }
//        }
//
//        if (position != null) {
//            url.append("&position=").append(position.name());
//        }
//
//        if (status != null) {
//            url.append("&status=").append(status.name());
//        }
//
//        if (minSalary != null) {
//            url.append("&minSalary=").append(minSalary);
//        }
//
//        if (maxSalary != null) {
//            url.append("&maxSalary=").append(maxSalary);
//        }
//
//        if (departmentName != null && !departmentName.trim().isEmpty()) {
//            try {
//                url.append("&departmentName=").append(URLEncoder.encode(departmentName.trim(), StandardCharsets.UTF_8));
//            } catch (Exception e) {
//                url.append("&departmentName=").append(departmentName.trim());
//            }
//        }
//
//        return "redirect:" + url.toString();
//    }
//
//
//}