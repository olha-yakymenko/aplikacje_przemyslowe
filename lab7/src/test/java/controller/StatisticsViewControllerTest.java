package com.techcorp.employee.controller;

import com.techcorp.employee.model.CompanyStatistics;
import com.techcorp.employee.model.Employee;
import com.techcorp.employee.model.Department;
import com.techcorp.employee.model.Position;
import com.techcorp.employee.model.EmploymentStatus;
import com.techcorp.employee.service.EmployeeService;
import com.techcorp.employee.service.DepartmentService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.List;
import java.util.Collections;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(StatisticsViewController.class)
public class StatisticsViewControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private EmployeeService employeeService;

    @MockBean
    private DepartmentService departmentService;

    @Test
    public void testShowStatistics() throws Exception {
        // Given
        Employee employee1 = new Employee("Jan Kowalski", "jan@example.com", "TechCorp",
                Position.PROGRAMMER, 5000.0, EmploymentStatus.ACTIVE);
        Employee employee2 = new Employee("Anna Nowak", "anna@example.com", "TechCorp",
                Position.MANAGER, 7000.0, EmploymentStatus.ACTIVE);
        Employee employee3 = new Employee("Piotr Test", "piotr@example.com", "OtherCorp",
                Position.PROGRAMMER, 4500.0, EmploymentStatus.ACTIVE);
        List<Employee> employees = Arrays.asList(employee1, employee2, employee3);

        Department department1 = new Department(1L, "IT", "Dział IT", "manager@example.com", 100000.0);
        Department department2 = new Department(2L, "HR", "Dział HR", "hr@example.com", 50000.0);
        List<Department> departments = Arrays.asList(department1, department2);

        CompanyStatistics techCorpStats = new CompanyStatistics(2, 6000.0, "Anna Nowak");
        CompanyStatistics otherCorpStats = new CompanyStatistics(1, 4500.0, "Piotr Test");

        when(employeeService.getAllEmployees()).thenReturn(employees);
        when(departmentService.getAllDepartments()).thenReturn(departments);
        when(employeeService.getCompanyStatistics("TechCorp")).thenReturn(techCorpStats);
        when(employeeService.getCompanyStatistics("OtherCorp")).thenReturn(otherCorpStats);

        // When & Then - TYLKO sprawdzamy czy kontroler się wykonuje bez błędów
        mockMvc.perform(get("/statistics"))
                .andExpect(status().isOk()); // Nie sprawdzamy modelu, tylko status

        verify(employeeService, times(1)).getAllEmployees();
        verify(departmentService, times(1)).getAllDepartments();
    }


    @Test
    public void testShowStatistics_EmptyData() throws Exception {
        // Given - puste dane (NIE null!)
        when(employeeService.getAllEmployees()).thenReturn(Collections.emptyList());
        when(departmentService.getAllDepartments()).thenReturn(Collections.emptyList());

        // When & Then
        mockMvc.perform(get("/statistics"))
                .andExpect(status().isOk());

        verify(employeeService, times(1)).getAllEmployees();
        verify(departmentService, times(1)).getAllDepartments();
    }



    @Test
    public void testShowStatistics_SingleEmployee() throws Exception {
        // Given - tylko jeden pracownik
        Employee employee = new Employee("Jan Kowalski", "jan@example.com", "TechCorp",
                Position.PROGRAMMER, 5000.0, EmploymentStatus.ACTIVE);
        List<Employee> employees = Arrays.asList(employee);

        Department department = new Department(1L, "IT", "Dział IT", "manager@example.com", 100000.0);
        List<Department> departments = Arrays.asList(department);

        CompanyStatistics companyStats = new CompanyStatistics(1, 5000.0, "Jan Kowalski");

        when(employeeService.getAllEmployees()).thenReturn(employees);
        when(departmentService.getAllDepartments()).thenReturn(departments);
        when(employeeService.getCompanyStatistics("TechCorp")).thenReturn(companyStats);

        // When & Then
        mockMvc.perform(get("/statistics"))
                .andExpect(status().isOk());

        verify(employeeService, times(1)).getAllEmployees();
        verify(departmentService, times(1)).getAllDepartments();
        verify(employeeService, times(1)).getCompanyStatistics("TechCorp");
    }
}