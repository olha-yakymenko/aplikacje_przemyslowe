package com.techcorp.employee.controller;

import com.techcorp.employee.dto.EmployeeDTO;
import com.techcorp.employee.exception.InvalidDataException;
import com.techcorp.employee.model.Employee;
import com.techcorp.employee.model.Position;
import com.techcorp.employee.model.EmploymentStatus;
import com.techcorp.employee.model.ImportSummary;
import com.techcorp.employee.service.EmployeeService;
import com.techcorp.employee.service.EmployeeFormService;
import com.techcorp.employee.service.ImportService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(EmployeeViewController.class)
public class EmployeeViewControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private EmployeeService employeeService;

    @MockBean
    private EmployeeFormService employeeFormService;

    @MockBean
    private ImportService importService;

    @Test
    public void testListEmployees() throws Exception {
        // Given
        Employee employee1 = new Employee("Jan Kowalski", "jan@example.com", "TechCorp",
                Position.PROGRAMMER, 5000.0, EmploymentStatus.ACTIVE);
        Employee employee2 = new Employee("Anna Nowak", "anna@example.com", "TechCorp",
                Position.MANAGER, 7000.0, EmploymentStatus.ACTIVE);
        List<Employee> employees = Arrays.asList(employee1, employee2);

        when(employeeService.getAllEmployees()).thenReturn(employees);

        // When & Then
        mockMvc.perform(get("/employees"))
                .andExpect(status().isOk())
                .andExpect(view().name("employees/list"))
                .andExpect(model().attributeExists("employees"))
                .andExpect(model().attributeExists("pageTitle"))
                .andExpect(model().attribute("pageTitle", "Lista Pracowników"));

        verify(employeeService, times(1)).getAllEmployees();
    }

    @Test
    public void testShowAddForm() throws Exception {
        // Given
        EmployeeFormService.EmployeeFormData formData = new EmployeeFormService.EmployeeFormData(
                Arrays.asList(Position.values()),
                Arrays.asList(EmploymentStatus.values())
        );
        when(employeeFormService.getFormData()).thenReturn(formData);

        // When & Then
        mockMvc.perform(get("/employees/add"))
                .andExpect(status().isOk())
                .andExpect(view().name("employees/add-form"))
                .andExpect(model().attributeExists("employee"))
                .andExpect(model().attributeExists("positions"))
                .andExpect(model().attributeExists("statuses"))
                .andExpect(model().attributeExists("pageTitle"))
                .andExpect(model().attribute("pageTitle", "Dodaj Pracownika"));

        verify(employeeFormService, times(1)).getFormData();
    }

    @Test
    public void testAddEmployee_Success() throws Exception {
        // Given
        EmployeeDTO employeeDTO = new EmployeeDTO();
        employeeDTO.setFirstName("Jan");
        employeeDTO.setLastName("Kowalski");
        employeeDTO.setEmail("jan@example.com");
        employeeDTO.setCompany("TechCorp");
        employeeDTO.setPosition(Position.PROGRAMMER);
        employeeDTO.setSalary(5000.0);
        employeeDTO.setStatus(EmploymentStatus.ACTIVE);

        Employee employee = new Employee("Jan Kowalski", "jan@example.com", "TechCorp",
                Position.PROGRAMMER, 5000.0, EmploymentStatus.ACTIVE);

        when(employeeFormService.convertToEntity(any(EmployeeDTO.class))).thenReturn(employee);
        when(employeeService.addEmployee(any(Employee.class))).thenReturn(true);

        // When & Then
        mockMvc.perform(post("/employees/add")
                        .param("firstName", "Jan")
                        .param("lastName", "Kowalski")
                        .param("email", "jan@example.com")
                        .param("company", "TechCorp")
                        .param("position", "PROGRAMMER")
                        .param("salary", "5000.0")
                        .param("status", "ACTIVE"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/employees"))
                .andExpect(flash().attributeExists("message"))
                .andExpect(flash().attribute("message", "Pracownik dodany pomyślnie!"));

        verify(employeeFormService, times(1)).convertToEntity(any(EmployeeDTO.class));
        verify(employeeService, times(1)).addEmployee(any(Employee.class));
    }

    @Test
    public void testAddEmployee_ValidationErrors() throws Exception {
        // Given - puste wymagane pola
        mockMvc.perform(post("/employees/add")
                        .param("firstName", "")
                        .param("lastName", "")
                        .param("email", "invalid-email")
                        .param("company", "")
                        .param("position", "")
                        .param("salary", "-1000.0")
                        .param("status", ""))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/employees/add"));

        verify(employeeService, never()).addEmployee(any(Employee.class));
    }

    @Test
    public void testAddEmployee_EmailAlreadyExists() throws Exception {
        // Given
        EmployeeDTO employeeDTO = new EmployeeDTO();
        employeeDTO.setFirstName("Jan");
        employeeDTO.setLastName("Kowalski");
        employeeDTO.setEmail("jan@example.com");
        employeeDTO.setCompany("TechCorp");
        employeeDTO.setPosition(Position.PROGRAMMER);
        employeeDTO.setSalary(5000.0);
        employeeDTO.setStatus(EmploymentStatus.ACTIVE);

        Employee employee = new Employee("Jan Kowalski", "jan@example.com", "TechCorp",
                Position.PROGRAMMER, 5000.0, EmploymentStatus.ACTIVE);

        when(employeeFormService.convertToEntity(any(EmployeeDTO.class))).thenReturn(employee);
        when(employeeService.addEmployee(any(Employee.class))).thenReturn(false);

        // When & Then
        mockMvc.perform(post("/employees/add")
                        .param("firstName", "Jan")
                        .param("lastName", "Kowalski")
                        .param("email", "jan@example.com")
                        .param("company", "TechCorp")
                        .param("position", "PROGRAMMER")
                        .param("salary", "5000.0")
                        .param("status", "ACTIVE"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/employees"))
                .andExpect(flash().attributeExists("error"))
                .andExpect(flash().attribute("error", "Nie udało się dodać pracownika. Email może już istnieć."));

        verify(employeeFormService, times(1)).convertToEntity(any(EmployeeDTO.class));
        verify(employeeService, times(1)).addEmployee(any(Employee.class));
    }

    @Test
    public void testShowEditForm_EmployeeExists() throws Exception {
        // Given
        Employee employee = new Employee("Jan Kowalski", "jan@example.com", "TechCorp",
                Position.PROGRAMMER, 5000.0, EmploymentStatus.ACTIVE);
        EmployeeDTO employeeDTO = new EmployeeDTO();
        employeeDTO.setFirstName("Jan");
        employeeDTO.setLastName("Kowalski");
        employeeDTO.setEmail("jan@example.com");
        employeeDTO.setCompany("TechCorp");
        employeeDTO.setPosition(Position.PROGRAMMER);
        employeeDTO.setSalary(5000.0);
        employeeDTO.setStatus(EmploymentStatus.ACTIVE);

        EmployeeFormService.EmployeeFormData formData = new EmployeeFormService.EmployeeFormData(
                Arrays.asList(Position.values()),
                Arrays.asList(EmploymentStatus.values())
        );

        when(employeeService.findEmployeeByEmail("jan@example.com")).thenReturn(Optional.of(employee));
        when(employeeFormService.convertToDTO(employee)).thenReturn(employeeDTO);
        when(employeeFormService.getFormData()).thenReturn(formData);

        // When & Then
        mockMvc.perform(get("/employees/edit/jan@example.com"))
                .andExpect(status().isOk())
                .andExpect(view().name("employees/edit-form"))
                .andExpect(model().attributeExists("employee"))
                .andExpect(model().attributeExists("positions"))
                .andExpect(model().attributeExists("statuses"))
                .andExpect(model().attributeExists("pageTitle"))
                .andExpect(model().attribute("pageTitle", "Edytuj Pracownika"));

        verify(employeeService, times(1)).findEmployeeByEmail("jan@example.com");
        verify(employeeFormService, times(1)).convertToDTO(employee);
        verify(employeeFormService, times(1)).getFormData();
    }

    @Test
    public void testShowEditForm_EmployeeNotFound() throws Exception {
        // Given
        when(employeeService.findEmployeeByEmail("nonexistent@example.com")).thenReturn(Optional.empty());

        // When & Then
        mockMvc.perform(get("/employees/edit/nonexistent@example.com"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/employees"))
                .andExpect(flash().attributeExists("error"))
                .andExpect(flash().attribute("error", "Pracownik o emailu nonexistent@example.com nie został znaleziony."));

        verify(employeeService, times(1)).findEmployeeByEmail("nonexistent@example.com");
        verify(employeeFormService, never()).convertToDTO(any(Employee.class));
    }

    @Test
    public void testUpdateEmployee_Success() throws Exception {
        // Given
        EmployeeDTO employeeDTO = new EmployeeDTO();
        employeeDTO.setFirstName("Jan");
        employeeDTO.setLastName("Kowalski");
        employeeDTO.setEmail("jan@example.com");
        employeeDTO.setCompany("TechCorp");
        employeeDTO.setPosition(Position.MANAGER);
        employeeDTO.setSalary(6000.0);
        employeeDTO.setStatus(EmploymentStatus.ACTIVE);

        Employee updatedEmployee = new Employee("Jan Kowalski", "jan@example.com", "TechCorp",
                Position.MANAGER, 6000.0, EmploymentStatus.ACTIVE);

        // Stwórz mock FormValidationResult
        EmployeeFormService.FormValidationResult validationResult = mock(EmployeeFormService.FormValidationResult.class);
        when(validationResult.isValid()).thenReturn(true);
        when(validationResult.getMessage()).thenReturn("");

        when(employeeFormService.validateEmployee(any(EmployeeDTO.class))).thenReturn(validationResult);
        when(employeeFormService.convertToEntity(any(EmployeeDTO.class))).thenReturn(updatedEmployee);

        // updateEmployee zwraca Employee, a nie jest void
        when(employeeService.updateEmployee(any(Employee.class))).thenReturn(updatedEmployee);

        // When & Then
        mockMvc.perform(post("/employees/edit")
                        .param("firstName", "Jan")
                        .param("lastName", "Kowalski")
                        .param("email", "jan@example.com")
                        .param("company", "TechCorp")
                        .param("position", "MANAGER")
                        .param("salary", "6000.0")
                        .param("status", "ACTIVE"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/employees"))
                .andExpect(flash().attributeExists("message"))
                .andExpect(flash().attribute("message", "Pracownik zaktualizowany pomyślnie!"));

        verify(employeeFormService, times(1)).validateEmployee(any(EmployeeDTO.class));
        verify(employeeFormService, times(1)).convertToEntity(any(EmployeeDTO.class));
        verify(employeeService, times(1)).updateEmployee(any(Employee.class));
    }







    @Test
    public void testUpdateEmployee_BusinessValidationFails() throws Exception {
        // Given
        EmployeeDTO employeeDTO = new EmployeeDTO();
        employeeDTO.setFirstName("Jan");
        employeeDTO.setLastName("Kowalski");
        employeeDTO.setEmail("jan@example.com");
        employeeDTO.setCompany("TechCorp");
        employeeDTO.setPosition(Position.MANAGER);
        employeeDTO.setSalary(6000.0);
        employeeDTO.setStatus(EmploymentStatus.ACTIVE);

        // Stwórz mock FormValidationResult z błędem
        EmployeeFormService.FormValidationResult validationResult = mock(EmployeeFormService.FormValidationResult.class);
        when(validationResult.isValid()).thenReturn(false);
        when(validationResult.getMessage()).thenReturn("Wynagrodzenie jest za niskie dla stanowiska MANAGER");

        when(employeeFormService.validateEmployee(any(EmployeeDTO.class))).thenReturn(validationResult);

        // When & Then
        mockMvc.perform(post("/employees/edit")
                        .param("firstName", "Jan")
                        .param("lastName", "Kowalski")
                        .param("email", "jan@example.com")
                        .param("company", "TechCorp")
                        .param("position", "MANAGER")
                        .param("salary", "6000.0")
                        .param("status", "ACTIVE"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/employees/edit/jan@example.com"))
                .andExpect(flash().attributeExists("error"))
                .andExpect(flash().attribute("error", "Wynagrodzenie jest za niskie dla stanowiska MANAGER"));

        verify(employeeFormService, times(1)).validateEmployee(any(EmployeeDTO.class));
        verify(employeeService, never()).updateEmployee(any(Employee.class));
    }



    @Test
    public void testDeleteEmployee_Success() throws Exception {
        // Given
        when(employeeService.removeEmployee("jan@example.com")).thenReturn(true);

        // When & Then
        mockMvc.perform(get("/employees/delete/jan@example.com"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/employees"))
                .andExpect(flash().attributeExists("message"))
                .andExpect(flash().attribute("message", "Pracownik usunięty pomyślnie!"));

        verify(employeeService, times(1)).removeEmployee("jan@example.com");
    }

    @Test
    public void testDeleteEmployee_NotFound() throws Exception {
        // Given
        when(employeeService.removeEmployee("nonexistent@example.com")).thenReturn(false);

        // When & Then
        mockMvc.perform(get("/employees/delete/nonexistent@example.com"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/employees"))
                .andExpect(flash().attributeExists("error"))
                .andExpect(flash().attribute("error", "Pracownik nie został znaleziony."));

        verify(employeeService, times(1)).removeEmployee("nonexistent@example.com");
    }

    @Test
    public void testShowSearchForm() throws Exception {
        // When & Then
        mockMvc.perform(get("/employees/search"))
                .andExpect(status().isOk())
                .andExpect(view().name("employees/search-form"))
                .andExpect(model().attributeExists("pageTitle"))
                .andExpect(model().attribute("pageTitle", "Wyszukaj Pracowników"));
    }

    @Test
    public void testSearchEmployees() throws Exception {
        // Given
        Employee employee1 = new Employee("Jan Kowalski", "jan@example.com", "TechCorp",
                Position.PROGRAMMER, 5000.0, EmploymentStatus.ACTIVE);
        List<Employee> employees = Arrays.asList(employee1);

        when(employeeService.getEmployeesByCompany("TechCorp")).thenReturn(employees);

        // When & Then
        mockMvc.perform(post("/employees/search")
                        .param("company", "TechCorp"))
                .andExpect(status().isOk())
                .andExpect(view().name("employees/search-results"))
                .andExpect(model().attributeExists("employees"))
                .andExpect(model().attributeExists("searchCompany"))
                .andExpect(model().attributeExists("pageTitle"))
                .andExpect(model().attribute("pageTitle", "Wyniki Wyszukiwania"));

        verify(employeeService, times(1)).getEmployeesByCompany("TechCorp");
    }

    @Test
    public void testSearchEmployees_NoResults() throws Exception {
        // Given
        when(employeeService.getEmployeesByCompany("NonexistentCorp")).thenReturn(Arrays.asList());

        // When & Then
        mockMvc.perform(post("/employees/search")
                        .param("company", "NonexistentCorp"))
                .andExpect(status().isOk())
                .andExpect(view().name("employees/search-results"))
                .andExpect(model().attributeExists("employees"))
                .andExpect(model().attributeExists("searchCompany"))
                .andExpect(model().attributeExists("message"))
                .andExpect(model().attribute("message", "Nie znaleziono pracowników dla firmy: NonexistentCorp"));

        verify(employeeService, times(1)).getEmployeesByCompany("NonexistentCorp");
    }

    @Test
    public void testShowImportForm() throws Exception {
        // When & Then
        mockMvc.perform(get("/employees/import"))
                .andExpect(status().isOk())
                .andExpect(view().name("employees/import-form"))
                .andExpect(model().attributeExists("pageTitle"))
                .andExpect(model().attribute("pageTitle", "Import Pracowników"));
    }

    @Test
    public void testImportEmployees_CSV_Success() throws Exception {
        // Given
        MockMultipartFile file = new MockMultipartFile("file", "employees.csv",
                "text/csv", "name,email,company,position,salary,status".getBytes());

        ImportSummary summary = new ImportSummary();
        summary.incrementImported(); // 1 pracownik
        summary.incrementImported(); // 2 pracowników

        when(importService.importCsvFile(any())).thenReturn(summary);

        // When & Then
        mockMvc.perform(multipart("/employees/import")
                        .file(file)
                        .param("fileType", "csv"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/employees"))
                .andExpect(flash().attributeExists("message"))
                .andExpect(flash().attribute("message", "Pomyślnie zaimportowano 2 pracowników"));

        verify(importService, times(1)).importCsvFile(any());
    }

    @Test
    public void testImportEmployees_XML_Success() throws Exception {
        // Given
        MockMultipartFile file = new MockMultipartFile("file", "employees.xml",
                "application/xml", "<employees></employees>".getBytes());

        ImportSummary summary = new ImportSummary();
        summary.incrementImported(); // 1 pracownik
        summary.incrementImported(); // 2 pracowników
        summary.incrementImported(); // 3 pracowników

        when(importService.importXmlFile(any())).thenReturn(summary);

        // When & Then
        mockMvc.perform(multipart("/employees/import")
                        .file(file)
                        .param("fileType", "xml"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/employees"))
                .andExpect(flash().attributeExists("message"))
                .andExpect(flash().attribute("message", "Pomyślnie zaimportowano 3 pracowników"));

        verify(importService, times(1)).importXmlFile(any());
    }

    @Test
    public void testImportEmployees_WithErrors() throws Exception {
        // Given
        MockMultipartFile file = new MockMultipartFile("file", "employees.csv",
                "text/csv", "name,email,company,position,salary,status".getBytes());

        ImportSummary summary = new ImportSummary();
        summary.incrementImported(); // 1 pracownik
        summary.incrementImported(); // 2 pracowników
        summary.addError("Błąd w wierszu 3");
        summary.addError("Błąd w wierszu 5");

        when(importService.importCsvFile(any())).thenReturn(summary);

        // When & Then
        mockMvc.perform(multipart("/employees/import")
                        .file(file)
                        .param("fileType", "csv"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/employees"))
                .andExpect(flash().attributeExists("error"))
                .andExpect(flash().attribute("error", "Import zakończony z błędami. Zaimportowano: 2, Błędy: 2"));

        verify(importService, times(1)).importCsvFile(any());
    }

    @Test
    public void testImportEmployees_EmptyFile() throws Exception {
        // Given
        MockMultipartFile file = new MockMultipartFile("file", "empty.csv",
                "text/csv", "".getBytes());

        // When & Then
        mockMvc.perform(multipart("/employees/import")
                        .file(file)
                        .param("fileType", "csv"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/employees/import"))
                .andExpect(flash().attributeExists("error"))
                .andExpect(flash().attribute("error", "Plik jest pusty"));

        verify(importService, never()).importCsvFile(any());
        verify(importService, never()).importXmlFile(any());
    }

    @Test
    public void testImportEmployees_UnsupportedFileType() throws Exception {
        // Given
        MockMultipartFile file = new MockMultipartFile("file", "employees.json",
                "application/json", "{}".getBytes());

        // When & Then
        mockMvc.perform(multipart("/employees/import")
                        .file(file)
                        .param("fileType", "json"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/employees/import"))
                .andExpect(flash().attributeExists("error"))
                .andExpect(flash().attribute("error", "Nieobsługiwany typ pliku. Obsługiwane typy: CSV, XML"));

        verify(importService, never()).importCsvFile(any());
        verify(importService, never()).importXmlFile(any());
    }

    @Test
    public void testImportEmployees_ImportServiceException() throws Exception {
        // Given
        MockMultipartFile file = new MockMultipartFile("file", "employees.csv",
                "text/csv", "name,email,company,position,salary,status".getBytes());

        when(importService.importCsvFile(any())).thenThrow(new RuntimeException("Błąd parsowania pliku"));

        // When & Then
        mockMvc.perform(multipart("/employees/import")
                        .file(file)
                        .param("fileType", "csv"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/employees"))
                .andExpect(flash().attributeExists("error"))
                .andExpect(flash().attribute("error", "Błąd podczas importu: Błąd parsowania pliku"));

        verify(importService, times(1)).importCsvFile(any());
    }
}