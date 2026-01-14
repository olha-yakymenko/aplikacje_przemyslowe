package com.techcorp.employee.controller;

import com.techcorp.employee.dto.EmployeeDTO;
import com.techcorp.employee.dto.EmployeeListView;
import com.techcorp.employee.exception.EmployeeNotFoundException;
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
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
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
            Model model) {

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
    @PreAuthorize("hasRole('ADMIN')")
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
    @PreAuthorize("hasRole('ADMIN')")
    public String addEmployee(@Valid @ModelAttribute("employee") EmployeeDTO employeeDTO,
                              RedirectAttributes redirectAttributes) {
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
    @PreAuthorize("hasRole('ADMIN')")
    public String showEditForm(@PathVariable String email, Model model) {
        Optional<Employee> employeeOpt = employeeService.findEmployeeByEmail(email);

        if (employeeOpt.isPresent()) {
            EmployeeDTO employeeDTO = employeeFormService.convertToDTO(employeeOpt.get());
            EmployeeFormService.EmployeeFormData formData = employeeFormService.getFormData();

            model.addAttribute("employee", employeeDTO);
            model.addAttribute("positions", formData.getPositions());
            model.addAttribute("statuses", formData.getStatuses());
            model.addAttribute("pageTitle", "Edytuj Pracownika");
            return "employees/edit-form";
        }

        throw new EmployeeNotFoundException("Pracownik o emailu " + email + " nie został znaleziony.");
    }

    @PostMapping("/edit")
    @PreAuthorize("hasRole('ADMIN')")
    public String updateEmployee(@Valid @ModelAttribute("employee") EmployeeDTO employeeDTO,
                                 RedirectAttributes redirectAttributes) {
        Optional<Employee> existingEmployeeOpt = employeeService.findEmployeeByEmail(employeeDTO.getEmail());

        if (existingEmployeeOpt.isEmpty()) {
            throw new EmployeeNotFoundException("Pracownik nie został znaleziony.");
        }

        Employee existingEmployee = existingEmployeeOpt.get();
        Employee updatedEmployee = employeeFormService.convertToEntity(employeeDTO);
        updatedEmployee.setId(existingEmployee.getId());

        employeeService.updateEmployee(updatedEmployee);
        redirectAttributes.addFlashAttribute("message", "Pracownik zaktualizowany pomyślnie!");

        return "redirect:/employees";
    }

    @GetMapping("/delete/{email}")
    @PreAuthorize("hasRole('ADMIN')")
    public String deleteEmployee(@PathVariable String email, RedirectAttributes redirectAttributes) {
        boolean deleted = employeeService.removeEmployee(email);

        if (deleted) {
            redirectAttributes.addFlashAttribute("message", "Pracownik usunięty pomyślnie!");
        } else {
            throw new EmployeeNotFoundException("Pracownik nie został znaleziony.");
        }

        return "redirect:/employees";
    }

    @GetMapping("/import")
    @PreAuthorize("hasRole('ADMIN')")
    public String showImportForm(Model model) {
        model.addAttribute("pageTitle", "Import Pracowników");
        return "employees/import-form";
    }

    @PostMapping("/import")
    @PreAuthorize("hasRole('ADMIN')")
    public String importEmployees(@RequestParam("file") MultipartFile file,
                                  @RequestParam("fileType") String fileType,
                                  RedirectAttributes redirectAttributes) {

        if (file.isEmpty()) {
            throw new IllegalArgumentException("Plik jest pusty");
        }

        if (!"csv".equalsIgnoreCase(fileType) && !"xml".equalsIgnoreCase(fileType)) {
            throw new IllegalArgumentException("Nieobsługiwany typ pliku. Obsługiwane typy: CSV, XML");
        }

        ImportSummary summary;
        if ("csv".equalsIgnoreCase(fileType)) {
            summary = importService.importCsvFile(file);
        } else {
            summary = importService.importXmlFile(file);
        }

        int importedCount = summary.getImportedCount();
        int errorCount = summary.getErrors().size();

        System.out.println("BLEDY IMPOTUW" + summary.getErrors());

        if (errorCount > 0) {
            String errorMessage = "Import zakończony z błędami. Zaimportowano: " + importedCount +
                    ", Błędy: " + errorCount;
            redirectAttributes.addFlashAttribute("error", errorMessage);
        } else {
            redirectAttributes.addFlashAttribute("message",
                    "Pomyślnie zaimportowano " + importedCount + " pracowników");
        }

        return "redirect:/employees";
    }

    @GetMapping("/list")
    @PreAuthorize("isAuthenticated()")
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
    @PreAuthorize("isAuthenticated()")
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

        Page<Employee> employeesPage = employeeService.searchEmployeesWithSpecifications(
                name, company, position, status, minSalary, maxSalary, departmentName, pageable);

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

