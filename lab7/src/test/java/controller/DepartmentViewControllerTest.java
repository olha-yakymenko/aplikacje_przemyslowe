package com.techcorp.employee.controller;

import com.techcorp.employee.model.Department;
import com.techcorp.employee.model.Employee;
import com.techcorp.employee.model.Position;
import com.techcorp.employee.model.EmploymentStatus;
import com.techcorp.employee.service.DepartmentService;
import com.techcorp.employee.service.EmployeeService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(DepartmentViewController.class)
public class DepartmentViewControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private DepartmentService departmentService;

    @MockBean
    private EmployeeService employeeService;

    @Test
    public void testListDepartments() throws Exception {
        // Given
        Department department1 = new Department(1L, "IT", "Dział technologii", "manager@example.com", 100000.0);
        Department department2 = new Department(2L, "HR", "Dział kadr", "hr@example.com", 50000.0);
        List<Department> departments = Arrays.asList(department1, department2);

        Employee manager = new Employee("Jan Manager", "manager@example.com", "TechCorp",
                Position.MANAGER, 8000.0, EmploymentStatus.ACTIVE);

        when(departmentService.getAllDepartments()).thenReturn(departments);
        when(employeeService.findEmployeeByEmail("manager@example.com")).thenReturn(Optional.of(manager));
        when(employeeService.findEmployeeByEmail("hr@example.com")).thenReturn(Optional.empty());

        // When & Then
        mockMvc.perform(get("/departments"))
                .andExpect(status().isOk())
                .andExpect(view().name("departments/list"))
                .andExpect(model().attributeExists("departments"))
                .andExpect(model().attributeExists("pageTitle"))
                .andExpect(model().attribute("pageTitle", "Lista Departamentów"))
                .andExpect(model().attribute("departments", departments));

        verify(departmentService, times(1)).getAllDepartments();
        verify(employeeService, times(2)).findEmployeeByEmail(anyString());
    }

    @Test
    public void testShowAddForm() throws Exception {
        // Given
        Employee manager = new Employee("Jan Manager", "manager@example.com", "TechCorp",
                Position.MANAGER, 8000.0, EmploymentStatus.ACTIVE);
        List<Employee> managers = Arrays.asList(manager);

        when(employeeService.getAllEmployees()).thenReturn(managers);

        // When & Then
        mockMvc.perform(get("/departments/add"))
                .andExpect(status().isOk())
                .andExpect(view().name("departments/form"))
                .andExpect(model().attributeExists("department"))
                .andExpect(model().attributeExists("managers"))
                .andExpect(model().attributeExists("pageTitle"))
                .andExpect(model().attribute("pageTitle", "Dodaj Departament"))
                .andExpect(model().attribute("managers", managers));

        verify(employeeService, times(1)).getAllEmployees();
    }

    @Test
    public void testShowAddForm_WhenExceptionOccurs() throws Exception {
        // Given
        when(employeeService.getAllEmployees()).thenThrow(new RuntimeException("Database error"));

        // When & Then
        mockMvc.perform(get("/departments/add"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/departments"));

        verify(employeeService, times(1)).getAllEmployees();
    }

    @Test
    public void testAddDepartment() throws Exception {
        // Given
        Department createdDepartment = new Department(1L, "IT", "Dział technologii", "manager@example.com", 100000.0);
        when(departmentService.createDepartment(any(Department.class))).thenReturn(createdDepartment);

        // When & Then
        mockMvc.perform(post("/departments/add")
                        .param("name", "IT")
                        .param("description", "Dział technologii")
                        .param("managerEmail", "manager@example.com")
                        .param("budget", "100000.0"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/departments"))
                .andExpect(flash().attributeExists("message"))
                .andExpect(flash().attribute("message", "Departament dodany pomyślnie!"));

        verify(departmentService, times(1)).createDepartment(any(Department.class));
    }

    @Test
    public void testShowEditForm_DepartmentExists() throws Exception {
        // Given
        Department department = new Department(1L, "IT", "Dział technologii", "manager@example.com", 100000.0);
        Employee manager = new Employee("Jan Manager", "manager@example.com", "TechCorp",
                Position.MANAGER, 8000.0, EmploymentStatus.ACTIVE);
        List<Employee> managers = Arrays.asList(manager);

        when(departmentService.getDepartmentById(1L)).thenReturn(Optional.of(department));
        when(employeeService.getAllEmployees()).thenReturn(managers);

        // When & Then
        mockMvc.perform(get("/departments/edit/1"))
                .andExpect(status().isOk())
                .andExpect(view().name("departments/form"))
                .andExpect(model().attributeExists("department"))
                .andExpect(model().attributeExists("managers"))
                .andExpect(model().attributeExists("pageTitle"))
                .andExpect(model().attribute("pageTitle", "Edytuj Departament"))
                .andExpect(model().attribute("department", department))
                .andExpect(model().attribute("managers", managers));

        verify(departmentService, times(1)).getDepartmentById(1L);
        verify(employeeService, times(1)).getAllEmployees();
    }

    @Test
    public void testShowEditForm_DepartmentNotFound() throws Exception {
        // Given
        when(departmentService.getDepartmentById(999L)).thenReturn(Optional.empty());

        // When & Then
        mockMvc.perform(get("/departments/edit/999"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/departments"));

        verify(departmentService, times(1)).getDepartmentById(999L);
        verify(employeeService, never()).getAllEmployees();
    }

    @Test
    public void testUpdateDepartment() throws Exception {
        // Given
        Department updatedDepartment = new Department(1L, "IT Updated", "Zaktualizowany dział", "newmanager@example.com", 120000.0);
        when(departmentService.updateDepartment(anyLong(), any(Department.class))).thenReturn(updatedDepartment);

        // When & Then
        mockMvc.perform(post("/departments/edit")
                        .param("id", "1")
                        .param("name", "IT Updated")
                        .param("description", "Zaktualizowany dział")
                        .param("managerEmail", "newmanager@example.com")
                        .param("budget", "120000.0"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/departments"))
                .andExpect(flash().attributeExists("message"))
                .andExpect(flash().attribute("message", "Departament zaktualizowany pomyślnie!"));

        verify(departmentService, times(1)).updateDepartment(eq(1L), any(Department.class));
    }

    @Test
    public void testDeleteDepartment() throws Exception {
        // Given
        when(departmentService.deleteDepartment(1L)).thenReturn(true);

        // When & Then
        mockMvc.perform(get("/departments/delete/1"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/departments"))
                .andExpect(flash().attributeExists("message"))
                .andExpect(flash().attribute("message", "Departament usunięty pomyślnie!"));

        verify(departmentService, times(1)).deleteDepartment(1L);
    }

    @Test
    public void testShowDepartmentDetails_DepartmentExists() throws Exception {
        // Given
        Department department = new Department(1L, "IT", "Dział technologii", "manager@example.com", 100000.0);
        Employee manager = new Employee("Jan Manager", "manager@example.com", "TechCorp",
                Position.MANAGER, 8000.0, EmploymentStatus.ACTIVE);
        Employee employee = new Employee("Jan Developer", "dev@example.com", "TechCorp",
                Position.PROGRAMMER, 5000.0, EmploymentStatus.ACTIVE);
        employee.setDepartmentId(1L);
        List<Employee> allEmployees = Arrays.asList(manager, employee);
        List<Employee> departmentEmployees = Arrays.asList(employee);

        when(departmentService.getDepartmentById(1L)).thenReturn(Optional.of(department));
        when(employeeService.getAllEmployees()).thenReturn(allEmployees);
        when(employeeService.findEmployeeByEmail("manager@example.com")).thenReturn(Optional.of(manager));

        // When & Then
        mockMvc.perform(get("/departments/details/1"))
                .andExpect(status().isOk())
                .andExpect(view().name("departments/details"))
                .andExpect(model().attributeExists("department"))
                .andExpect(model().attributeExists("employees"))
                .andExpect(model().attributeExists("manager"))
                .andExpect(model().attributeExists("pageTitle"))
                .andExpect(model().attribute("pageTitle", "Szczegóły Departamentu"))
                .andExpect(model().attribute("department", department))
                .andExpect(model().attribute("employees", departmentEmployees))
                .andExpect(model().attribute("manager", Optional.of(manager)));

        verify(departmentService, times(1)).getDepartmentById(1L);
        verify(employeeService, times(1)).getAllEmployees();
        verify(employeeService, times(1)).findEmployeeByEmail("manager@example.com");
    }

    @Test
    public void testShowDepartmentDetails_DepartmentNotFound() throws Exception {
        // Given
        when(departmentService.getDepartmentById(999L)).thenReturn(Optional.empty());

        // When & Then
        mockMvc.perform(get("/departments/details/999"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/departments"));

        verify(departmentService, times(1)).getDepartmentById(999L);
        verify(employeeService, never()).getAllEmployees();
        verify(employeeService, never()).findEmployeeByEmail(anyString());
    }

    @Test
    public void testShowDepartmentDetails_DepartmentExists_ManagerNotFound() throws Exception {
        // Given
        Department department = new Department(1L, "IT", "Dział technologii", "nonexistent@example.com", 100000.0);
        Employee employee = new Employee("Jan Developer", "dev@example.com", "TechCorp",
                Position.PROGRAMMER, 5000.0, EmploymentStatus.ACTIVE);
        employee.setDepartmentId(1L);
        List<Employee> allEmployees = Arrays.asList(employee);

        when(departmentService.getDepartmentById(1L)).thenReturn(Optional.of(department));
        when(employeeService.getAllEmployees()).thenReturn(allEmployees);
        when(employeeService.findEmployeeByEmail("nonexistent@example.com")).thenReturn(Optional.empty());

        // When & Then
        mockMvc.perform(get("/departments/details/1"))
                .andExpect(status().isOk())
                .andExpect(view().name("departments/details"))
                .andExpect(model().attributeExists("department"))
                .andExpect(model().attributeExists("employees"))
                .andExpect(model().attributeExists("manager"))
                .andExpect(model().attribute("manager", Optional.empty()));

        verify(departmentService, times(1)).getDepartmentById(1L);
        verify(employeeService, times(1)).getAllEmployees();
        verify(employeeService, times(1)).findEmployeeByEmail("nonexistent@example.com");
    }

}