package com.techcorp.employee.controller;

import com.techcorp.employee.dto.EmployeeDTO;
import com.techcorp.employee.dto.EmployeeListView;
import com.techcorp.employee.exception.EmployeeNotFoundException;
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
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
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
                "jan.kowalski@techcorp.com",
                "TechCorp",
                Position.PROGRAMMER,
                new BigDecimal("8000.00"),
                EmploymentStatus.ACTIVE
        );
        testEmployee.setId(1L);

        testEmployeeDTO = new EmployeeDTO(
                "Jan",
                "Kowalski",
                "jan.kowalski@techcorp.com",
                "TechCorp",
                Position.PROGRAMMER,
                new BigDecimal("8000.00"),
                EmploymentStatus.ACTIVE
        );

        // Setup common mock form data
        EmployeeFormService.EmployeeFormData mockFormData = mock(EmployeeFormService.EmployeeFormData.class);
        when(mockFormData.getPositions()).thenReturn(Arrays.asList(Position.values()));
        when(mockFormData.getStatuses()).thenReturn(Arrays.asList(EmploymentStatus.values()));
        when(employeeFormService.getFormData()).thenReturn(mockFormData);

        // Setup common mocks
        when(employeeService.getAllUniqueCompanies()).thenReturn(Arrays.asList("TechCorp", "OtherCorp"));
        when(departmentService.getAllDepartmentNames()).thenReturn(Arrays.asList("IT", "HR"));
    }

    // ==================== LIST EMPLOYEES TESTS ====================

    @Test
    @WithMockUser
    void listEmployees_ShouldReturnListView() throws Exception {
        // Given
        Page<EmployeeListView> employeesPage = createTestEmployeeListViewPage();
        when(employeeService.searchEmployeesAdvanced(
                any(), any(), any(), any(), any(), any(), any(), any(Pageable.class)))
                .thenReturn(employeesPage);

        // When & Then
        mockMvc.perform(get("/employees"))
                .andExpect(status().isOk())
                .andExpect(view().name("employees/list"))
                .andExpect(model().attributeExists("employees"))
                .andExpect(model().attributeExists("pageTitle"))
                .andExpect(model().attribute("pageTitle", "Lista Pracowników"));
    }

    @Test
    @WithMockUser
    void listEmployees_WithSearchParameters_ShouldReturnFilteredResults() throws Exception {
        // Given
        Page<EmployeeListView> employeesPage = new PageImpl<>(Collections.emptyList());
        when(employeeService.searchEmployeesAdvanced(
                eq("Jan"), eq("TechCorp"), eq(Position.PROGRAMMER), eq(EmploymentStatus.ACTIVE),
                eq(5000.0), eq(10000.0), eq("IT"), any(Pageable.class)))
                .thenReturn(employeesPage);

        // When & Then
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

    @Test
    @WithMockUser
    void listEmployees_WhenPageOutOfRange_ShouldRedirectToLastPage() throws Exception {
        // Given - tylko 2 strony, ale próbujemy strona 5
        Page<EmployeeListView> employeesPage = new PageImpl<>(
                Collections.emptyList(),
                PageRequest.of(0, 10),
                15 // total elements = 2 strony (0, 1)
        );

        when(employeeService.searchEmployeesAdvanced(
                any(), any(), any(), any(), any(), any(), any(), any(Pageable.class)))
                .thenReturn(employeesPage);

        // When & Then
        mockMvc.perform(get("/employees")
                        .param("page", "5")
                        .param("size", "10"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("/employees?page=1*"));
    }

    // ==================== ADD EMPLOYEE TESTS ====================

    @Test
    @WithMockUser
    void showAddForm_ShouldReturnAddFormView() throws Exception {
        // When & Then
        mockMvc.perform(get("/employees/add"))
                .andExpect(status().isOk())
                .andExpect(view().name("employees/add-form"))
                .andExpect(model().attributeExists("employee"))
                .andExpect(model().attributeExists("positions"))
                .andExpect(model().attributeExists("statuses"))
                .andExpect(model().attribute("pageTitle", "Dodaj Pracownika"));
    }

    @Test
    @WithMockUser
    void addEmployee_ValidData_ShouldRedirectToListWithSuccessMessage() throws Exception {
        // Given
        when(employeeFormService.convertToEntity(any(EmployeeDTO.class))).thenReturn(testEmployee);
        when(employeeService.addEmployee(any(Employee.class))).thenReturn(true);

        // When & Then
        mockMvc.perform(post("/employees/add")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("firstName", "Jan")
                        .param("lastName", "Kowalski")
                        .param("email", "jan.kowalski@techcorp.com")
                        .param("company", "TechCorp")
                        .param("position", "PROGRAMMER")
                        .param("salary", "8000.00")
                        .param("status", "ACTIVE")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/employees"))
                .andExpect(flash().attribute("message", "Pracownik dodany pomyślnie!"));
    }

    @Test
    @WithMockUser
    void addEmployee_DuplicateEmail_ShouldShowErrorMessage() throws Exception {
        // Given
        when(employeeFormService.convertToEntity(any(EmployeeDTO.class))).thenReturn(testEmployee);
        when(employeeService.addEmployee(any(Employee.class))).thenReturn(false);

        // When & Then
        mockMvc.perform(post("/employees/add")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("firstName", "Jan")
                        .param("lastName", "Kowalski")
                        .param("email", "jan.kowalski@techcorp.com")
                        .param("company", "TechCorp")
                        .param("position", "PROGRAMMER")
                        .param("salary", "8000.00")
                        .param("status", "ACTIVE")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/employees"))
                .andExpect(flash().attribute("error", "Nie udało się dodać pracownika. Email może już istnieć."));
    }

    // ==================== EDIT EMPLOYEE TESTS ====================

    @Test
    @WithMockUser
    void showEditForm_EmployeeExists_ShouldReturnEditForm() throws Exception {
        // Given
        when(employeeService.findEmployeeByEmail("jan.kowalski@techcorp.com"))
                .thenReturn(Optional.of(testEmployee));
        when(employeeFormService.convertToDTO(any(Employee.class))).thenReturn(testEmployeeDTO);

        // When & Then
        mockMvc.perform(get("/employees/edit/jan.kowalski@techcorp.com"))
                .andExpect(status().isOk())
                .andExpect(view().name("employees/edit-form"))
                .andExpect(model().attributeExists("employee"))
                .andExpect(model().attributeExists("positions"))
                .andExpect(model().attributeExists("statuses"))
                .andExpect(model().attribute("pageTitle", "Edytuj Pracownika"));
    }

    @Test
    @WithMockUser
    void showEditForm_EmployeeNotFound_ShouldThrowException() throws Exception {
        // Given
        when(employeeService.findEmployeeByEmail("notfound@techcorp.com"))
                .thenReturn(Optional.empty());

        // When & Then
        mockMvc.perform(get("/employees/edit/notfound@techcorp.com"))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser
    void updateEmployee_ValidData_ShouldRedirectWithSuccessMessage() throws Exception {
        // Given
        when(employeeService.findEmployeeByEmail("jan.kowalski@techcorp.com"))
                .thenReturn(Optional.of(testEmployee));
        when(employeeFormService.convertToEntity(any(EmployeeDTO.class))).thenReturn(testEmployee);

        // When & Then
        mockMvc.perform(post("/employees/edit")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("firstName", "Jan")
                        .param("lastName", "Kowalski")
                        .param("email", "jan.kowalski@techcorp.com")
                        .param("company", "TechCorp")
                        .param("position", "PROGRAMMER")
                        .param("salary", "8500.00")
                        .param("status", "ACTIVE")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/employees"))
                .andExpect(flash().attribute("message", "Pracownik zaktualizowany pomyślnie!"));
    }

    @Test
    @WithMockUser
    void updateEmployee_EmployeeNotFound_ShouldThrowException() throws Exception {
        // Given
        when(employeeService.findEmployeeByEmail("notfound@techcorp.com"))
                .thenReturn(Optional.empty());

        // When & Then
        mockMvc.perform(post("/employees/edit")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("firstName", "Jan")
                        .param("lastName", "Kowalski")
                        .param("email", "notfound@techcorp.com")
                        .param("company", "TechCorp")
                        .param("position", "PROGRAMMER")
                        .param("salary", "8000.00")
                        .param("status", "ACTIVE")
                        .with(csrf()))
                .andExpect(status().isNotFound());
    }

    // ==================== DELETE EMPLOYEE TESTS ====================

    @Test
    @WithMockUser
    void deleteEmployee_ExistingEmployee_ShouldRedirectWithSuccess() throws Exception {
        // Given
        when(employeeService.removeEmployee("jan.kowalski@techcorp.com")).thenReturn(true);

        // When & Then
        mockMvc.perform(get("/employees/delete/jan.kowalski@techcorp.com"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/employees"))
                .andExpect(flash().attribute("message", "Pracownik usunięty pomyślnie!"));
    }

    @Test
    @WithMockUser
    void deleteEmployee_NonExistingEmployee_ShouldThrowException() throws Exception {
        // Given
        when(employeeService.removeEmployee("notfound@techcorp.com"))
                .thenThrow(new EmployeeNotFoundException("Pracownik nie został znaleziony."));

        // When & Then
        mockMvc.perform(get("/employees/delete/notfound@techcorp.com"))
                .andExpect(status().isNotFound());
    }

    // ==================== IMPORT EMPLOYEES TESTS ====================

    @Test
    @WithMockUser
    void showImportForm_ShouldReturnImportFormView() throws Exception {
        // When & Then
        mockMvc.perform(get("/employees/import"))
                .andExpect(status().isOk())
                .andExpect(view().name("employees/import-form"))
                .andExpect(model().attribute("pageTitle", "Import Pracowników"));
    }

    @Test
    @WithMockUser
    void importEmployees_CSVFile_ShouldImportSuccessfully() throws Exception {
        // Given
        MockMultipartFile file = new MockMultipartFile(
                "file", "test.csv", "text/csv", "test content".getBytes());

        ImportSummary summary = new ImportSummary();
        summary.incrementImported();
        summary.incrementImported();

        when(importService.importCsvFile(any())).thenReturn(summary);

        // When & Then
        mockMvc.perform(multipart("/employees/import")
                        .file(file)
                        .param("fileType", "csv")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/employees"))
                .andExpect(flash().attribute("message", "Pomyślnie zaimportowano 2 pracowników"));
    }

    @Test
    @WithMockUser
    void importEmployees_XMLFile_ShouldImportSuccessfully() throws Exception {
        // Given
        MockMultipartFile file = new MockMultipartFile(
                "file", "test.xml", "text/xml", "test content".getBytes());

        ImportSummary summary = new ImportSummary();
        summary.incrementImported();

        when(importService.importXmlFile(any())).thenReturn(summary);

        // When & Then
        mockMvc.perform(multipart("/employees/import")
                        .file(file)
                        .param("fileType", "xml")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/employees"))
                .andExpect(flash().attribute("message", "Pomyślnie zaimportowano 1 pracowników"));
    }

    @Test
    @WithMockUser
    void importEmployees_WithErrors_ShouldShowErrorMessage() throws Exception {
        // Given
        MockMultipartFile file = new MockMultipartFile(
                "file", "test.csv", "text/csv", "test content".getBytes());

        ImportSummary summary = new ImportSummary();
        summary.incrementImported();
        summary.addError("Błąd 1");
        summary.addError("Błąd 2");

        when(importService.importCsvFile(any())).thenReturn(summary);

        // When & Then
        mockMvc.perform(multipart("/employees/import")
                        .file(file)
                        .param("fileType", "csv")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/employees"))
                .andExpect(flash().attribute("error", "Import zakończony z błędami. Zaimportowano: 1, Błędy: 2"));
    }

    @Test
    @WithMockUser
    void importEmployees_EmptyFile_ShouldThrowException() throws Exception {
        // Given
        MockMultipartFile emptyFile = new MockMultipartFile(
                "file", "empty.csv", "text/csv", new byte[0]);

        // When & Then
        mockMvc.perform(multipart("/employees/import")
                        .file(emptyFile)
                        .param("fileType", "csv")
                        .with(csrf()))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser
    void importEmployees_InvalidFileType_ShouldThrowException() throws Exception {
        // Given
        MockMultipartFile file = new MockMultipartFile(
                "file", "test.pdf", "application/pdf", "test content".getBytes());

        // When & Then
        mockMvc.perform(multipart("/employees/import")
                        .file(file)
                        .param("fileType", "pdf")
                        .with(csrf()))
                .andExpect(status().isBadRequest());
    }

    // ==================== QUICK LIST AND SEARCH TESTS ====================

    @Test
    @WithMockUser
    void listEmployeesQuick_ShouldReturnQuickListView() throws Exception {
        // Given
        Page<EmployeeListView> employeesPage = createTestEmployeeListViewPage();
        when(employeeService.getAllEmployeesProjection(any(Pageable.class))).thenReturn(employeesPage);

        // When & Then
        mockMvc.perform(get("/employees/list"))
                .andExpect(status().isOk())
                .andExpect(view().name("employees/list-quick"))
                .andExpect(model().attribute("pageTitle", "Szybka lista pracowników"));
    }

    @Test
    @WithMockUser
    void searchEmployeesFull_ShouldReturnSearchView() throws Exception {
        // Given
        Page<Employee> employeesPage = new PageImpl<>(
                Collections.singletonList(testEmployee),
                PageRequest.of(0, 10),
                1
        );

        when(employeeService.searchEmployeesWithSpecifications(
                any(), any(), any(), any(), any(), any(), any(), any(Pageable.class)))
                .thenReturn(employeesPage);

        // When & Then
        mockMvc.perform(get("/employees/search")
                        .param("name", "Jan")
                        .param("company", "TechCorp"))
                .andExpect(status().isOk())
                .andExpect(view().name("employees/search-full"))
                .andExpect(model().attribute("pageTitle", "Zaawansowane wyszukiwanie pracowników"));
    }

    // ==================== HELPER METHODS ====================

    private Page<EmployeeListView> createTestEmployeeListViewPage() {
        EmployeeListView employeeView = new EmployeeListView() {
            @Override
            public String getName() { return "Jan Kowalski"; }
            @Override
            public String getEmail() { return "jan.kowalski@techcorp.com"; }
            @Override
            public String getCompany() { return "TechCorp"; }
            @Override
            public String getPosition() { return "PROGRAMMER"; }
            @Override
            public String getDepartmentName() { return "IT"; }
        };

        return new PageImpl<>(
                Collections.singletonList(employeeView),
                PageRequest.of(0, 10),
                1
        );
    }
}