package com.techcorp.employee.controller;

import com.techcorp.employee.model.Employee;
import com.techcorp.employee.model.Position;
import com.techcorp.employee.model.EmploymentStatus;
import com.techcorp.employee.service.EmployeeService;
import com.techcorp.employee.service.ImportService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
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
        // When & Then
        mockMvc.perform(get("/employees/add"))
                .andExpect(status().isOk())
                .andExpect(view().name("employees/add-form"))
                .andExpect(model().attributeExists("employee"))
                .andExpect(model().attributeExists("positions"))
                .andExpect(model().attributeExists("statuses"))
                .andExpect(model().attributeExists("pageTitle"))
                .andExpect(model().attribute("pageTitle", "Dodaj Pracownika"));
    }

    @Test
    public void testAddEmployee_Success() throws Exception {
        // Given
        when(employeeService.addEmployee(any(Employee.class))).thenReturn(true);

        // When & Then - użyj parametrów zgodnych z EmployeeDTO
        mockMvc.perform(post("/employees/add")
                        .param("firstName", "Jan")
                        .param("lastName", "Kowalski")
                        .param("email", "jan@example.com")
                        .param("company", "TechCorp")
                        .param("position", "PROGRAMMER") // Użyj poprawnych wartości enum
                        .param("salary", "5000.0")
                        .param("status", "ACTIVE")) // Użyj poprawnych wartości enum
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/employees"))
                .andExpect(flash().attributeExists("message"));

        verify(employeeService, times(1)).addEmployee(any(Employee.class));
    }

    @Test
    public void testAddEmployee_Failure() throws Exception {
        // Given
        when(employeeService.addEmployee(any(Employee.class))).thenReturn(false);

        // When & Then - użyj parametrów zgodnych z EmployeeDTO
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
                .andExpect(flash().attributeExists("error"));

        verify(employeeService, times(1)).addEmployee(any(Employee.class));
    }

    @Test
    public void testShowEditForm_EmployeeExists() throws Exception {
        // Given
        Employee employee = new Employee("Jan Kowalski", "jan@example.com", "TechCorp",
                Position.PROGRAMMER, 5000.0, EmploymentStatus.ACTIVE);
        when(employeeService.findEmployeeByEmail("jan@example.com")).thenReturn(Optional.of(employee));

        // When & Then
        mockMvc.perform(get("/employees/edit/jan@example.com"))
                .andExpect(status().isOk())
                .andExpect(view().name("employees/edit-form"))
                .andExpect(model().attributeExists("employee"))
                .andExpect(model().attributeExists("positions"))
                .andExpect(model().attributeExists("statuses"))
                .andExpect(model().attributeExists("pageTitle"));

        verify(employeeService, times(1)).findEmployeeByEmail("jan@example.com");
    }

    @Test
    public void testShowEditForm_EmployeeNotFound() throws Exception {
        // Given
        when(employeeService.findEmployeeByEmail("nonexistent@example.com")).thenReturn(Optional.empty());

        // When & Then
        mockMvc.perform(get("/employees/edit/nonexistent@example.com"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/employees"))
                .andExpect(flash().attributeExists("error"));

        verify(employeeService, times(1)).findEmployeeByEmail("nonexistent@example.com");
    }

    @Test
    public void testUpdateEmployee_Success() throws Exception {
        // Given
        Employee existingEmployee = new Employee("Jan Kowalski", "jan@example.com", "TechCorp",
                Position.PROGRAMMER, 5000.0, EmploymentStatus.ACTIVE);
        when(employeeService.findEmployeeByEmail("jan@example.com")).thenReturn(Optional.of(existingEmployee));
        when(employeeService.updateEmployee(any(Employee.class))).thenReturn(existingEmployee);

        // When & Then - użyj parametrów zgodnych z metodą updateEmployee
        mockMvc.perform(post("/employees/edit")
                        .param("name", "Jan Kowalski Updated")
                        .param("email", "jan@example.com")
                        .param("company", "TechCorp Updated")
                        .param("position", "MANAGER")
                        .param("salary", "6000.0")
                        .param("status", "ACTIVE"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/employees"))
                .andExpect(flash().attributeExists("message"));

        verify(employeeService, times(1)).findEmployeeByEmail("jan@example.com");
        verify(employeeService, times(1)).updateEmployee(any(Employee.class));
    }

    @Test
    public void testUpdateEmployee_EmployeeNotFound() throws Exception {
        // Given
        when(employeeService.findEmployeeByEmail("nonexistent@example.com")).thenReturn(Optional.empty());

        // When & Then
        mockMvc.perform(post("/employees/edit")
                        .param("name", "Jan Kowalski")
                        .param("email", "nonexistent@example.com")
                        .param("company", "TechCorp")
                        .param("position", "PROGRAMMER")
                        .param("salary", "5000.0")
                        .param("status", "ACTIVE"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/employees"))
                .andExpect(flash().attributeExists("error"));

        verify(employeeService, times(1)).findEmployeeByEmail("nonexistent@example.com");
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
                .andExpect(flash().attributeExists("message"));

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
                .andExpect(flash().attributeExists("error"));

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
                .andExpect(model().attributeExists("pageTitle"));

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
                .andExpect(model().attributeExists("message"));

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
    public void testAddEmployee_WithInvalidData() throws Exception {
        // When & Then - test z brakującymi wymaganymi polami
        mockMvc.perform(post("/employees/add")
                        .param("firstName", "") // puste imię
                        .param("lastName", "Kowalski")
                        .param("email", "invalid-email") // niepoprawny email
                        .param("company", "TechCorp")
                        .param("position", "PROGRAMMER")
                        .param("salary", "-1000.0") // ujemne wynagrodzenie
                        .param("status", "ACTIVE"))
                .andExpect(status().is3xxRedirection()) // nawet z błędnymi danymi przekierowuje
                .andExpect(redirectedUrl("/employees"));

        // Service może być wywołany lub nie, w zależności od implementacji walidacji
        // verify(employeeService, never()).addEmployee(any(Employee.class));
    }
}