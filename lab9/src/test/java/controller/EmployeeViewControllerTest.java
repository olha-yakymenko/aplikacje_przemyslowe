package com.techcorp.employee.controller;

import com.techcorp.employee.dto.EmployeeDTO;
import com.techcorp.employee.dto.EmployeeListView;
import com.techcorp.employee.exception.InvalidDataException;
import com.techcorp.employee.model.*;
import com.techcorp.employee.service.DepartmentService;
import com.techcorp.employee.service.EmployeeService;
import com.techcorp.employee.service.EmployeeFormService;
import com.techcorp.employee.service.ImportService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.validation.BindingResult;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(EmployeeViewController.class)
class EmployeeViewControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private EmployeeService employeeService;

    @MockBean
    private EmployeeFormService employeeFormService;

    @MockBean
    private ImportService importService;

    @MockBean
    private DepartmentService departmentService;

    private Employee testEmployee;
    private EmployeeDTO testEmployeeDTO;

    @BeforeEach
    void setUp() {
        testEmployee = new Employee(
                "Jan Kowalski",
                "jan@example.com",
                "TechCorp",
                Position.PROGRAMMER,
                8000.0,
                EmploymentStatus.ACTIVE
        );
        testEmployee.setId(1L);

        testEmployeeDTO = new EmployeeDTO(
                "Jan",
                "Kowalski",
                "jan@example.com",
                "TechCorp",
                Position.PROGRAMMER,
                8000.0,
                EmploymentStatus.ACTIVE
        );
    }

//    @Test
//    void listEmployees_ShouldReturnEmployeesListView() throws Exception {
//        // Arrange
//        EmployeeListView employeeView = new EmployeeListView() {
//            @Override
//            public String getName() { return "Jan Kowalski"; }
//            @Override
//            public String getEmail() { return "jan@example.com"; }
//            @Override
//            public String getCompany() { return "TechCorp"; }
//            @Override
//            public String getPosition() { return "PROGRAMMER"; }
//            @Override
//            public Double getSalary() { return 8000.0; }
//            @Override
//            public EmploymentStatus getStatus() { return EmploymentStatus.ACTIVE; }
//            @Override
//            public String getDepartmentName() { return "IT"; }
//        };
//
//        Page<EmployeeListView> employeesPage = new PageImpl<>(
//                Collections.singletonList(employeeView),
//                PageRequest.of(0, 2),
//                1
//        );
//
//        when(employeeService.searchEmployeesAdvanced(
//                any(), any(), any(), any(), any(), any(), any(), any(Pageable.class)))
//                .thenReturn(employeesPage);
//
//        EmployeeFormService.EmployeeFormData formData = mock(EmployeeFormService.EmployeeFormData.class);
//        when(formData.getPositions()).thenReturn(Arrays.asList(Position.values()));
//        when(formData.getStatuses()).thenReturn(Arrays.asList(EmploymentStatus.values()));
//
//        when(employeeFormService.getFormData()).thenReturn(formData);
//        when(employeeService.getAllUniqueCompanies()).thenReturn(Arrays.asList("TechCorp", "OtherCorp"));
//        when(departmentService.getAllDepartmentNames()).thenReturn(Arrays.asList("IT", "HR"));
//
//        // Act & Assert
//        mockMvc.perform(get("/employees"))
//                .andExpect(status().isOk())
//                .andExpect(view().name("employees/list"))
//                .andExpect(model().attributeExists("employees"))
//                .andExpect(model().attributeExists("currentPage"))
//                .andExpect(model().attributeExists("totalPages"))
//                .andExpect(model().attributeExists("pageTitle"));
//    }

    @Test
    void listEmployees_WithSearchParameters_ShouldReturnFilteredResults() throws Exception {
        // Arrange
        Page<EmployeeListView> employeesPage = new PageImpl<>(Collections.emptyList());

        when(employeeService.searchEmployeesAdvanced(
                eq("Jan"), eq("TechCorp"), eq(Position.PROGRAMMER), eq(EmploymentStatus.ACTIVE),
                eq(5000.0), eq(10000.0), eq("IT"), any(Pageable.class)))
                .thenReturn(employeesPage);

        EmployeeFormService.EmployeeFormData formData = mock(EmployeeFormService.EmployeeFormData.class);
        when(formData.getPositions()).thenReturn(Arrays.asList(Position.values()));
        when(formData.getStatuses()).thenReturn(Arrays.asList(EmploymentStatus.values()));

        when(employeeFormService.getFormData()).thenReturn(formData);
        when(employeeService.getAllUniqueCompanies()).thenReturn(Collections.emptyList());
        when(departmentService.getAllDepartmentNames()).thenReturn(Collections.emptyList());

        // Act & Assert
        mockMvc.perform(get("/employees")
                        .param("name", "Jan")
                        .param("company", "TechCorp")
                        .param("position", "PROGRAMMER")
                        .param("status", "ACTIVE")
                        .param("minSalary", "5000.0")
                        .param("maxSalary", "10000.0")
                        .param("departmentName", "IT"))
                .andExpect(status().isOk())
                .andExpect(view().name("employees/list"));

        verify(employeeService, times(1)).searchEmployeesAdvanced(
                eq("Jan"), eq("TechCorp"), eq(Position.PROGRAMMER), eq(EmploymentStatus.ACTIVE),
                eq(5000.0), eq(10000.0), eq("IT"), any(Pageable.class));
    }

//    @Test
//    void listEmployees_PageOutOfRange_ShouldWorkCorrectly() throws Exception {
//        // Arrange - kiedy strona jest pusta (0 elementów)
//        Page<EmployeeListView> employeesPage = new PageImpl<>(
//                Collections.emptyList(),
//                PageRequest.of(0, 2),
//                0
//        );
//
//        when(employeeService.searchEmployeesAdvanced(
//                any(), any(), any(), any(), any(), any(), any(), any(Pageable.class)))
//                .thenReturn(employeesPage);
//
//        EmployeeFormService.EmployeeFormData formData = mock(EmployeeFormService.EmployeeFormData.class);
//        when(formData.getPositions()).thenReturn(Arrays.asList(Position.values()));
//        when(formData.getStatuses()).thenReturn(Arrays.asList(EmploymentStatus.values()));
//
//        when(employeeFormService.getFormData()).thenReturn(formData);
//        when(employeeService.getAllUniqueCompanies()).thenReturn(Collections.emptyList());
//        when(departmentService.getAllDepartmentNames()).thenReturn(Collections.emptyList());
//
//        // Act & Assert - page 10 when totalPages is 0 - powinno obsłużyć poprawnie
//        mockMvc.perform(get("/employees")
//                        .param("page", "10")
//                        .param("size", "2"))
//                .andExpect(status().isOk()); // Powinno wyświetlić listę, a nie rzucać wyjątek
//    }

    @Test
    void showAddForm_ShouldReturnAddFormView() throws Exception {
        // Arrange
        EmployeeFormService.EmployeeFormData formData = mock(EmployeeFormService.EmployeeFormData.class);
        when(formData.getPositions()).thenReturn(Arrays.asList(Position.values()));
        when(formData.getStatuses()).thenReturn(Arrays.asList(EmploymentStatus.values()));
        when(employeeFormService.getFormData()).thenReturn(formData);

        // Act & Assert
        mockMvc.perform(get("/employees/add"))
                .andExpect(status().isOk())
                .andExpect(view().name("employees/add-form"))
                .andExpect(model().attributeExists("employee"))
                .andExpect(model().attributeExists("positions"))
                .andExpect(model().attributeExists("statuses"))
                .andExpect(model().attributeExists("pageTitle"));
    }

    @Test
    void addEmployee_ValidData_ShouldRedirectToList() throws Exception {
        // Arrange
        when(employeeFormService.convertToEntity(any(EmployeeDTO.class))).thenReturn(testEmployee);
        when(employeeService.addEmployee(any(Employee.class))).thenReturn(true);

        // Act & Assert
        mockMvc.perform(post("/employees/add")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("firstName", "Jan")
                        .param("lastName", "Kowalski")
                        .param("email", "jan@example.com")
                        .param("company", "TechCorp")
                        .param("position", "PROGRAMMER")
                        .param("salary", "8000.0")
                        .param("status", "ACTIVE"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/employees"))
                .andExpect(flash().attributeExists("message"));
    }

    @Test
    void addEmployee_InvalidData_ShouldRedirectToAddForm() throws Exception {
        // Act & Assert - missing required fields
        mockMvc.perform(post("/employees/add")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("firstName", "") // empty first name
                        .param("email", "invalid-email")) // invalid email
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/employees/add"));
    }

    @Test
    void showEditForm_EmployeeExists_ShouldReturnEditForm() throws Exception {
        // Arrange
        when(employeeService.findEmployeeByEmail("jan@example.com"))
                .thenReturn(Optional.of(testEmployee));
        when(employeeFormService.convertToDTO(any(Employee.class))).thenReturn(testEmployeeDTO);

        EmployeeFormService.EmployeeFormData formData = mock(EmployeeFormService.EmployeeFormData.class);
        when(formData.getPositions()).thenReturn(Arrays.asList(Position.values()));
        when(formData.getStatuses()).thenReturn(Arrays.asList(EmploymentStatus.values()));
        when(employeeFormService.getFormData()).thenReturn(formData);

        // Act & Assert
        mockMvc.perform(get("/employees/edit/jan@example.com"))
                .andExpect(status().isOk())
                .andExpect(view().name("employees/edit-form"))
                .andExpect(model().attributeExists("employee"))
                .andExpect(model().attributeExists("positions"))
                .andExpect(model().attributeExists("statuses"))
                .andExpect(model().attributeExists("pageTitle"));
    }

    @Test
    void showEditForm_EmployeeNotExists_ShouldRedirectWithError() throws Exception {
        // Arrange
        when(employeeService.findEmployeeByEmail("nonexistent@example.com"))
                .thenReturn(Optional.empty());

        // Act & Assert
        mockMvc.perform(get("/employees/edit/nonexistent@example.com"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/employees"))
                .andExpect(flash().attributeExists("error"));
    }


    @Test
    void updateEmployee_ValidData_ShouldRedirectToListWithSuccessMessage() throws Exception {
        // Arrange
        String email = "jan@example.com";

        // Mockowanie istniejącego pracownika
        Employee existingEmployee = new Employee(
                "Jan Kowalski",
                email,
                "TechCorp",
                Position.PROGRAMMER,
                7000.0,
                EmploymentStatus.ACTIVE
        );
        existingEmployee.setId(1L);

        // Mockowanie walidacji FormValidationResult
        EmployeeFormService.FormValidationResult validationResult =
                mock(EmployeeFormService.FormValidationResult.class);
        when(validationResult.isValid()).thenReturn(true);

        when(employeeService.findEmployeeByEmail(email))
                .thenReturn(Optional.of(existingEmployee));
        when(employeeFormService.validateEmployee(any(EmployeeDTO.class)))
                .thenReturn(validationResult);
        when(employeeFormService.convertToEntity(any(EmployeeDTO.class))).thenReturn(existingEmployee);
        when(employeeService.updateEmployee(any(Employee.class))).thenReturn(existingEmployee);

        // Act & Assert
        mockMvc.perform(post("/employees/edit")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("firstName", "Jan")
                        .param("lastName", "Kowalski")
                        .param("email", email)
                        .param("company", "TechCorp")
                        .param("position", "PROGRAMMER")
                        .param("salary", "8500.0") // zaktualizowana pensja
                        .param("status", "ACTIVE"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/employees"))
                .andExpect(flash().attribute("message", "Pracownik zaktualizowany pomyślnie!"));
    }


    @Test
    void updateEmployee_BindingResultHasErrors_ShouldRedirectBackToEditForm() throws Exception {
        // Arrange
        String email = "jan@example.com";

        // Act & Assert - brak wymaganych pól (Spring Validation)
        mockMvc.perform(post("/employees/edit")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("email", email))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/employees/edit/" + email))
                .andExpect(flash().attributeExists("org.springframework.validation.BindingResult.employee"))
                .andExpect(flash().attributeExists("employee"));

        // Verify - serwisy nie powinny być wywoływane
        verify(employeeService, never()).findEmployeeByEmail(anyString());
        verify(employeeFormService, never()).validateEmployee(any(EmployeeDTO.class));
    }

    @Test
    void updateEmployee_FormValidationFails_ShouldRedirectWithErrorMessage() throws Exception {
        // Arrange
        String email = "jan@example.com";

        // Mockowanie FormValidationResult z błędem
        EmployeeFormService.FormValidationResult validationResult =
                mock(EmployeeFormService.FormValidationResult.class);
        when(validationResult.isValid()).thenReturn(false);
        when(validationResult.getMessage()).thenReturn("Nieprawidłowy email");

        when(employeeFormService.validateEmployee(any(EmployeeDTO.class)))
                .thenReturn(validationResult);

        // Act & Assert
        mockMvc.perform(post("/employees/edit")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("firstName", "Jan")
                        .param("lastName", "Kowalski")
                        .param("email", email)
                        .param("company", "TechCorp")
                        .param("position", "PROGRAMMER")
                        .param("salary", "8500.0")
                        .param("status", "ACTIVE"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/employees/edit/" + email))
                .andExpect(flash().attribute("error", "Nieprawidłowy email"))
                .andExpect(flash().attributeExists("employee"));

        // Verify - nie powinno szukać pracownika
        verify(employeeService, never()).findEmployeeByEmail(anyString());
    }


    @Test
    void deleteEmployee_ExistingEmployee_ShouldRedirectWithSuccess() throws Exception {
        // Arrange
        when(employeeService.removeEmployee("jan@example.com")).thenReturn(true);

        // Act & Assert
        mockMvc.perform(get("/employees/delete/jan@example.com"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/employees"))
                .andExpect(flash().attributeExists("message"));
    }

    @Test
    void deleteEmployee_NonExistingEmployee_ShouldRedirectWithError() throws Exception {
        // Arrange
        when(employeeService.removeEmployee("nonexistent@example.com")).thenReturn(false);

        // Act & Assert
        mockMvc.perform(get("/employees/delete/nonexistent@example.com"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/employees"))
                .andExpect(flash().attributeExists("error"));
    }

    @Test
    void showImportForm_ShouldReturnImportFormView() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/employees/import"))
                .andExpect(status().isOk())
                .andExpect(view().name("employees/import-form"))
                .andExpect(model().attributeExists("pageTitle"));
    }

    @Test
    void importEmployees_CSVFile_ShouldImportSuccessfully() throws Exception {
        // Arrange
        MockMultipartFile file = new MockMultipartFile(
                "file", "test.csv", "text/csv", "test content".getBytes());

        ImportSummary summary = new ImportSummary();
        // Dodaj zaimportowane dane
        for (int i = 0; i < 10; i++) {
            summary.incrementImported();
        }

        when(importService.importCsvFile(any())).thenReturn(summary);

        // Act & Assert
        mockMvc.perform(multipart("/employees/import")
                        .file(file)
                        .param("fileType", "csv"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/employees"))
                .andExpect(flash().attributeExists("message"));
    }

    @Test
    void importEmployees_XMLFile_ShouldImportSuccessfully() throws Exception {
        // Arrange
        MockMultipartFile file = new MockMultipartFile(
                "file", "test.xml", "text/xml", "test content".getBytes());

        ImportSummary summary = new ImportSummary();
        // Dodaj zaimportowane dane
        for (int i = 0; i < 5; i++) {
            summary.incrementImported();
        }

        when(importService.importXmlFile(any())).thenReturn(summary);

        // Act & Assert
        mockMvc.perform(multipart("/employees/import")
                        .file(file)
                        .param("fileType", "xml"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/employees"))
                .andExpect(flash().attributeExists("message"));
    }

    @Test
    void importEmployees_InvalidFileType_ShouldRedirectWithError() throws Exception {
        // Arrange
        MockMultipartFile file = new MockMultipartFile(
                "file", "test.txt", "text/plain", "test content".getBytes());

        // Act & Assert
        mockMvc.perform(multipart("/employees/import")
                        .file(file)
                        .param("fileType", "txt"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/employees/import"))
                .andExpect(flash().attributeExists("error"));
    }

    @Test
    void importEmployees_EmptyFile_ShouldRedirectWithError() throws Exception {
        // Arrange
        MockMultipartFile file = new MockMultipartFile(
                "file", "empty.csv", "text/csv", new byte[0]);

        // Act & Assert
        mockMvc.perform(multipart("/employees/import")
                        .file(file)
                        .param("fileType", "csv"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/employees/import"))
                .andExpect(flash().attributeExists("error"));
    }

    @Test
    void importEmployees_WithImportErrors_ShouldShowErrorMessage() throws Exception {
        // Arrange
        MockMultipartFile file = new MockMultipartFile(
                "file", "test.csv", "text/csv", "test content".getBytes());

        ImportSummary summary = new ImportSummary();
        // Dodaj zaimportowane dane
        for (int i = 0; i < 5; i++) {
            summary.incrementImported();
        }
        // Dodaj błędy
        summary.addError("Error 1");
        summary.addError("Error 2");

        when(importService.importCsvFile(any())).thenReturn(summary);

        // Act & Assert
        mockMvc.perform(multipart("/employees/import")
                        .file(file)
                        .param("fileType", "csv"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/employees"))
                .andExpect(flash().attributeExists("error"));
    }

    @Test
    void importEmployees_ExceptionDuringImport_ShouldRedirectWithError() throws Exception {
        // Arrange
        MockMultipartFile file = new MockMultipartFile(
                "file", "test.csv", "text/csv", "test content".getBytes());

        when(importService.importCsvFile(any())).thenThrow(new RuntimeException("Import failed"));

        // Act & Assert
        mockMvc.perform(multipart("/employees/import")
                        .file(file)
                        .param("fileType", "csv"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/employees")) // Kontroler przekierowuje do /employees, nie /employees/import
                .andExpect(flash().attributeExists("error"));
    }
}