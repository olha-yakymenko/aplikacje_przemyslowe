package com.techcorp.employee.controller;

import com.techcorp.employee.dto.DepartmentDTO;
import com.techcorp.employee.model.Department;
import com.techcorp.employee.model.Employee;
import com.techcorp.employee.service.DepartmentService;
import com.techcorp.employee.service.EmployeeService;
import com.techcorp.employee.service.FileStorageService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
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

    @Autowired
    private FileStorageService fileStorageService;

    @GetMapping
    public String listDepartments(Model model) {
        model.addAttribute("departments", departmentService.getAllDepartments());
        model.addAttribute("pageTitle", "Lista Departamentów");
        return "departments/list";
    }

    @GetMapping("/add")
    public String showAddForm(Model model) {
        if (!model.containsAttribute("department")) {
            model.addAttribute("department", new Department());
        }
        model.addAttribute("managers", employeeService.getAvailableManagers());
        model.addAttribute("pageTitle", "Dodaj Departament");
        return "departments/form";
    }

    @PostMapping("/add")
    public String addDepartment(@Valid @ModelAttribute Department department,
                                RedirectAttributes redirectAttributes) {
        departmentService.createDepartment(department);
        redirectAttributes.addFlashAttribute("message", "Departament dodany pomyślnie!");
        return "redirect:/departments";
    }

    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable @Min(1) Long id, Model model) {
        Optional<Department> department = departmentService.getDepartmentById(id);
        if (department.isPresent()) {
            model.addAttribute("department", department.get());
            model.addAttribute("managers", employeeService.getAvailableManagers());
            model.addAttribute("pageTitle", "Edytuj Departament");
            return "departments/form";
        } else {
            throw new IllegalArgumentException("Departament o ID " + id + " nie został znaleziony.");
        }
    }

    @GetMapping("/details/{id}/assign-employee")
    public String showAssignEmployeeForm(@PathVariable @Min(1) Long id, Model model) {
        Optional<Department> department = departmentService.getDepartmentById(id);
        if (department.isPresent()) {
            List<Employee> availableEmployees = employeeService.getEmployeesWithoutDepartment();

            model.addAttribute("department", department.get());
            model.addAttribute("availableEmployees", availableEmployees);
            model.addAttribute("pageTitle", "Przypisz Pracownika do Departamentu");
            return "departments/assign-employee-form";
        }
        throw new IllegalArgumentException("Departament o ID " + id + " nie został znaleziony.");
    }

    @PostMapping("/details/{id}/assign-employee")
    public String assignEmployeeToDepartment(
            @PathVariable @Min(1) Long id,
            @RequestParam @NotBlank(message = "Employee email cannot be blank") String employeeEmail,
            RedirectAttributes redirectAttributes) {
        departmentService.assignEmployeeToDepartment(employeeEmail, id);
        redirectAttributes.addFlashAttribute("message", "Pracownik został przypisany do departamentu!");
        return "redirect:/departments/details/" + id;
    }

    @GetMapping("/details/{departmentId}/remove-employee/{employeeEmail}")
    public String removeEmployeeFromDepartment(
            @PathVariable @Min(1) Long departmentId,
            @PathVariable @NotBlank(message = "Employee email cannot be blank") String employeeEmail,
            RedirectAttributes redirectAttributes) {
        departmentService.removeEmployeeFromDepartment(employeeEmail);
        redirectAttributes.addFlashAttribute("message", "Pracownik został usunięty z departamentu!");
        return "redirect:/departments/details/" + departmentId;
    }

    @PostMapping("/edit")
    public String updateDepartment(@Valid @ModelAttribute Department department,
                                   RedirectAttributes redirectAttributes) {
        departmentService.updateDepartment(department.getId(), department);
        redirectAttributes.addFlashAttribute("message", "Departament zaktualizowany pomyślnie!");
        return "redirect:/departments";
    }

    @GetMapping("/delete/{id}")
    public String deleteDepartment(@PathVariable @Min(1) Long id, RedirectAttributes redirectAttributes) {
        departmentService.deleteDepartment(id);
        redirectAttributes.addFlashAttribute("message", "Departament usunięty pomyślnie!");
        return "redirect:/departments";
    }

    @GetMapping("/details/{id}")
    public String showDepartmentDetails(@PathVariable @Min(1) Long id, Model model) {
        DepartmentDTO departmentDetails = departmentService.getDepartmentDetails(id);

        if (departmentDetails != null) {
            model.addAttribute("department", departmentDetails.getDepartment());
            model.addAttribute("employees", departmentDetails.getEmployees());
            model.addAttribute("manager", departmentDetails.getManager());
            model.addAttribute("pageTitle", "Szczegóły Departamentu");
            return "departments/details";
        }

        throw new IllegalArgumentException("Departament o ID " + id + " nie został znaleziony.");
    }

    @GetMapping("/documents/{id}")
    public String showDepartmentDocuments(@PathVariable @Min(1) Long id, Model model) {
        Optional<Department> department = departmentService.getDepartmentById(id);
        if (department.isPresent()) {
            List<String> documents = getDepartmentDocumentsFromApi(id);

            model.addAttribute("department", department.get());
            model.addAttribute("documents", documents);
            model.addAttribute("pageTitle", "Dokumenty Departamentu");
            return "departments/documents";
        } else {
            throw new IllegalArgumentException("Departament o ID " + id + " nie został znaleziony.");
        }
    }

    @PostMapping("/documents/{id}/upload")
    public String uploadDepartmentDocument(
            @PathVariable @Min(1) Long id,
            @RequestParam("file") MultipartFile file,
            RedirectAttributes redirectAttributes) {
        uploadDocumentToApi(id, file);
        redirectAttributes.addFlashAttribute("message", "Dokument dodany pomyślnie!");
        return "redirect:/departments/documents/" + id;
    }

    @GetMapping("/documents/{departmentId}/download/{fileName}")
    public ResponseEntity<Resource> downloadDepartmentDocument(
            @PathVariable @Min(1) Long departmentId,
            @PathVariable @NotBlank(message = "File name cannot be blank") String fileName) {
        return downloadDocumentFromApi(departmentId, fileName);
    }

    @GetMapping("/documents/{departmentId}/delete/{fileName}")
    public String deleteDepartmentDocument(
            @PathVariable @Min(1) Long departmentId,
            @PathVariable @NotBlank(message = "File name cannot be blank") String fileName,
            RedirectAttributes redirectAttributes) {
        deleteDocumentFromApi(departmentId, fileName);
        redirectAttributes.addFlashAttribute("message", "Dokument usunięty pomyślnie!");
        return "redirect:/departments/documents/" + departmentId;
    }

    private List<String> getDepartmentDocumentsFromApi(Long departmentId) {
        try {
            return fileStorageService.getDepartmentDocumentNames(departmentId);
        } catch (Exception e) {
            return List.of();
        }
    }

    private void uploadDocumentToApi(Long departmentId, MultipartFile file) {
        fileStorageService.storeDepartmentDocument(file, departmentId);
    }

    private ResponseEntity<Resource> downloadDocumentFromApi(Long departmentId, String fileName) {
        Resource resource = fileStorageService.loadDepartmentDocument(fileName, departmentId);

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + fileName + "\"")
                .body(resource);
    }

    private void deleteDocumentFromApi(Long departmentId, String fileName) {
        fileStorageService.deleteDepartmentDocument(fileName, departmentId);
    }
}



//
//
//
//
//package com.techcorp.employee.controller;
//
//import com.techcorp.employee.dto.DepartmentDTO;
//import com.techcorp.employee.model.Department;
//import com.techcorp.employee.model.Employee;
//import com.techcorp.employee.service.DepartmentService;
//import com.techcorp.employee.service.EmployeeService;
//import com.techcorp.employee.service.FileStorageService;
//import jakarta.validation.Valid;
//import jakarta.validation.constraints.Min;
//import jakarta.validation.constraints.NotBlank;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.core.io.Resource;
//import org.springframework.http.HttpHeaders;
//import org.springframework.http.MediaType;
//import org.springframework.http.ResponseEntity;
//import org.springframework.stereotype.Controller;
//import org.springframework.ui.Model;
//import org.springframework.validation.BindingResult;
//import org.springframework.web.bind.annotation.*;
//import org.springframework.web.multipart.MultipartFile;
//import org.springframework.web.servlet.mvc.support.RedirectAttributes;
//
//
//import java.util.List;
//import java.util.Optional;
//
//@Controller
//@RequestMapping("/departments")
//public class DepartmentViewController {
//
//    @Autowired
//    private DepartmentService departmentService;
//
//    @Autowired
//    private EmployeeService employeeService;
//
//    @Autowired
//    private FileStorageService fileStorageService;
//
//    @GetMapping
//    public String listDepartments(Model model) {
//        model.addAttribute("departments", departmentService.getAllDepartments());
//        model.addAttribute("pageTitle", "Lista Departamentów");
//        return "departments/list";
//    }
//
//    @GetMapping("/add")
//    public String showAddForm(Model model) {
//        if (!model.containsAttribute("department")) {
//            model.addAttribute("department", new Department());
//        }
//        model.addAttribute("managers", employeeService.getAvailableManagers());
//        model.addAttribute("pageTitle", "Dodaj Departament");
//        return "departments/form";
//    }
//
//    @PostMapping("/add")
//    public String addDepartment(@Valid @ModelAttribute Department department,
//                                BindingResult bindingResult,
//                                RedirectAttributes redirectAttributes) {
//
//        if (bindingResult.hasErrors()) {
//            redirectAttributes.addFlashAttribute("org.springframework.validation.BindingResult.department", bindingResult);
//            redirectAttributes.addFlashAttribute("department", department);
//            return "redirect:/departments/add";
//        }
//
//        try {
//            departmentService.createDepartment(department);
//            redirectAttributes.addFlashAttribute("message", "Departament dodany pomyślnie!");
//        } catch (Exception e) {
//            redirectAttributes.addFlashAttribute("error", "Błąd podczas dodawania departamentu: " + e.getMessage());
//        }
//        return "redirect:/departments";
//    }
//
//    @GetMapping("/edit/{id}")
//    public String showEditForm(@PathVariable @Min(1) Long id, Model model) {
//        Optional<Department> department = departmentService.getDepartmentById(id);
//        if (department.isPresent()) {
//            model.addAttribute("department", department.get());
//            model.addAttribute("managers", employeeService.getAvailableManagers());
//            model.addAttribute("pageTitle", "Edytuj Departament");
//            return "departments/form";
//        } else {
//            return "redirect:/departments";
//        }
//    }
//
//    @GetMapping("/details/{id}/assign-employee")
//    public String showAssignEmployeeForm(@PathVariable @Min(1) Long id, Model model) {
//        Optional<Department> department = departmentService.getDepartmentById(id);
//        if (department.isPresent()) {
//            List<Employee> availableEmployees = employeeService.getEmployeesWithoutDepartment();
//
//            model.addAttribute("department", department.get());
//            model.addAttribute("availableEmployees", availableEmployees);
//            model.addAttribute("pageTitle", "Przypisz Pracownika do Departamentu");
//            return "departments/assign-employee-form";
//        }
//        return "redirect:/departments";
//    }
//
//    @PostMapping("/details/{id}/assign-employee")
//    public String assignEmployeeToDepartment(
//            @PathVariable @Min(1) Long id,
//            @RequestParam @NotBlank(message = "Employee email cannot be blank") String employeeEmail,
//            RedirectAttributes redirectAttributes) {
//
//        try {
//            departmentService.assignEmployeeToDepartment(employeeEmail, id);
//            redirectAttributes.addFlashAttribute("message", "Pracownik został przypisany do departamentu!");
//        } catch (Exception e) {
//            redirectAttributes.addFlashAttribute("error", "Błąd podczas przypisywania pracownika: " + e.getMessage());
//        }
//        return "redirect:/departments/details/" + id;
//    }
//
//    @GetMapping("/details/{departmentId}/remove-employee/{employeeEmail}")
//    public String removeEmployeeFromDepartment(
//            @PathVariable @Min(1) Long departmentId,
//            @PathVariable @NotBlank(message = "Employee email cannot be blank") String employeeEmail,
//            RedirectAttributes redirectAttributes) {
//
//        try {
//            departmentService.removeEmployeeFromDepartment(employeeEmail);
//            redirectAttributes.addFlashAttribute("message", "Pracownik został usunięty z departamentu!");
//        } catch (Exception e) {
//            redirectAttributes.addFlashAttribute("error", "Błąd podczas usuwania pracownika: " + e.getMessage());
//        }
//        return "redirect:/departments/details/" + departmentId;
//    }
//
//    @PostMapping("/edit")
//    public String updateDepartment(@Valid @ModelAttribute Department department,
//                                   BindingResult bindingResult,
//                                   RedirectAttributes redirectAttributes) {
//
//        if (bindingResult.hasErrors()) {
//            redirectAttributes.addFlashAttribute("org.springframework.validation.BindingResult.department", bindingResult);
//            redirectAttributes.addFlashAttribute("department", department);
//            return "redirect:/departments/edit/" + department.getId();
//        }
//
//        try {
//            departmentService.updateDepartment(department.getId(), department);
//            redirectAttributes.addFlashAttribute("message", "Departament zaktualizowany pomyślnie!");
//        } catch (Exception e) {
//            redirectAttributes.addFlashAttribute("error", "Błąd podczas aktualizacji departamentu: " + e.getMessage());
//        }
//        return "redirect:/departments";
//    }
//
//    @GetMapping("/delete/{id}")
//    public String deleteDepartment(@PathVariable @Min(1) Long id, RedirectAttributes redirectAttributes) {
//        try {
//            departmentService.deleteDepartment(id);
//            redirectAttributes.addFlashAttribute("message", "Departament usunięty pomyślnie!");
//        } catch (Exception e) {
//            redirectAttributes.addFlashAttribute("error", "Błąd podczas usuwania departamentu: " + e.getMessage());
//        }
//        return "redirect:/departments";
//    }
//
//    @GetMapping("/details/{id}")
//    public String showDepartmentDetails(@PathVariable @Min(1) Long id, Model model) {
//        DepartmentDTO departmentDetails = departmentService.getDepartmentDetails(id);
//
//        if (departmentDetails != null) {
//            model.addAttribute("department", departmentDetails.getDepartment());
//            model.addAttribute("employees", departmentDetails.getEmployees());
//            model.addAttribute("manager", departmentDetails.getManager());
//            model.addAttribute("pageTitle", "Szczegóły Departamentu");
//            return "departments/details";
//        }
//
//        return "redirect:/departments";
//    }
//
//    @GetMapping("/documents/{id}")
//    public String showDepartmentDocuments(@PathVariable @Min(1) Long id, Model model) {
//        Optional<Department> department = departmentService.getDepartmentById(id);
//        if (department.isPresent()) {
//            List<String> documents = getDepartmentDocumentsFromApi(id);
//
//            model.addAttribute("department", department.get());
//            model.addAttribute("documents", documents);
//            model.addAttribute("pageTitle", "Dokumenty Departamentu");
//            return "departments/documents";
//        } else {
//            return "redirect:/departments";
//        }
//    }
//
//    @PostMapping("/documents/{id}/upload")
//    public String uploadDepartmentDocument(
//            @PathVariable @Min(1) Long id,
//            @RequestParam("file") MultipartFile file,
//            RedirectAttributes redirectAttributes) {
//
//        try {
//            uploadDocumentToApi(id, file);
//            redirectAttributes.addFlashAttribute("message", "Dokument dodany pomyślnie!");
//        } catch (Exception e) {
//            redirectAttributes.addFlashAttribute("error", "Błąd podczas dodawania dokumentu: " + e.getMessage());
//        }
//        return "redirect:/departments/documents/" + id;
//    }
//
//    @GetMapping("/documents/{departmentId}/download/{fileName}")
//    public ResponseEntity<Resource> downloadDepartmentDocument(
//            @PathVariable @Min(1) Long departmentId,
//            @PathVariable @NotBlank(message = "File name cannot be blank") String fileName) {
//
//        return downloadDocumentFromApi(departmentId, fileName);
//    }
//
//    @GetMapping("/documents/{departmentId}/delete/{fileName}")
//    public String deleteDepartmentDocument(
//            @PathVariable @Min(1) Long departmentId,
//            @PathVariable @NotBlank(message = "File name cannot be blank") String fileName,
//            RedirectAttributes redirectAttributes) {
//
//        try {
//            deleteDocumentFromApi(departmentId, fileName);
//            redirectAttributes.addFlashAttribute("message", "Dokument usunięty pomyślnie!");
//        } catch (Exception e) {
//            redirectAttributes.addFlashAttribute("error", "Błąd podczas usuwania dokumentu: " + e.getMessage());
//        }
//        return "redirect:/departments/documents/" + departmentId;
//    }
//
//    // Metody pomocnicze do komunikacji z REST API
//    private List<String> getDepartmentDocumentsFromApi(Long departmentId) {
//        try {
//            return fileStorageService.getDepartmentDocumentNames(departmentId);
//        } catch (Exception e) {
//            return List.of();
//        }
//    }
//
//    private void uploadDocumentToApi(Long departmentId, MultipartFile file) {
//        fileStorageService.storeDepartmentDocument(file, departmentId);
//    }
//
//    private ResponseEntity<Resource> downloadDocumentFromApi(Long departmentId, String fileName) {
//        Resource resource = fileStorageService.loadDepartmentDocument(fileName, departmentId);
//
//        return ResponseEntity.ok()
//                .contentType(MediaType.APPLICATION_OCTET_STREAM)
//                .header(HttpHeaders.CONTENT_DISPOSITION,
//                        "attachment; filename=\"" + fileName + "\"")
//                .body(resource);
//    }
//
//    private void deleteDocumentFromApi(Long departmentId, String fileName) {
//        fileStorageService.deleteDepartmentDocument(fileName, departmentId);
//    }
//}