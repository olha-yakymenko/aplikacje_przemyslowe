package com.techcorp.employee.controller;

import com.techcorp.employee.dto.CompanyStatisticsDTO;
import com.techcorp.employee.model.CompanyStatistics;
import com.techcorp.employee.model.Employee;
import com.techcorp.employee.model.Position;
import com.techcorp.employee.model.EmploymentStatus;
import com.techcorp.employee.service.StatisticsService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(StatisticsViewController.class)
public class StatisticsViewControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private StatisticsService statisticsService;

    @Test
    public void testShowStatistics() throws Exception {
        // Given
        Map<String, Object> statistics = new HashMap<>();
        statistics.put("totalEmployees", 100L);
        statistics.put("avgSalary", 7500.0);
        statistics.put("totalDepartments", 5L);
        statistics.put("totalBudget", 500000.0);

        // 👇 POPRAWIONE - użyj nowego konstruktora
        Map<String, CompanyStatistics> companyStats = new HashMap<>();
        CompanyStatistics techCorpStats = new CompanyStatistics("TechCorp", 50, 8000.0, 10000.0);
        techCorpStats.setHighestPaidEmployee("Jan Kowalski"); // 👈 DODAJ SETTER
        companyStats.put("TechCorp", techCorpStats);

        statistics.put("companyStats", companyStats);

        Map<String, Long> positionDistribution = new HashMap<>();
        positionDistribution.put("PROGRAMMER", 60L);
        positionDistribution.put("MANAGER", 20L);
        statistics.put("positionDistribution", positionDistribution);

        when(statisticsService.getAllStatistics()).thenReturn(statistics);

        // When & Then
        mockMvc.perform(get("/statistics"))
                .andExpect(status().isOk())
                .andExpect(view().name("statistics/index"))
                .andExpect(model().attributeExists("totalEmployees"))
                .andExpect(model().attributeExists("avgSalary"))
                .andExpect(model().attributeExists("totalDepartments"))
                .andExpect(model().attributeExists("totalBudget"))
                .andExpect(model().attributeExists("companyStats"))
                .andExpect(model().attributeExists("positionDistribution"))
                .andExpect(model().attributeExists("pageTitle"))
                .andExpect(model().attribute("pageTitle", "Statystyki"))
                .andExpect(model().attribute("totalEmployees", 100L))
                .andExpect(model().attribute("avgSalary", 7500.0));

        verify(statisticsService, times(1)).getAllStatistics();
    }

    @Test
    public void testShowStatistics_EmptyData() throws Exception {
        // Given
        Map<String, Object> statistics = new HashMap<>();
        statistics.put("totalEmployees", 0L);
        statistics.put("avgSalary", 0.0);
        statistics.put("totalDepartments", 0L);
        statistics.put("totalBudget", 0.0);
        statistics.put("companyStats", new HashMap<>());
        statistics.put("positionDistribution", new HashMap<>());

        when(statisticsService.getAllStatistics()).thenReturn(statistics);

        // When & Then
        mockMvc.perform(get("/statistics"))
                .andExpect(status().isOk())
                .andExpect(view().name("statistics/index"))
                .andExpect(model().attribute("totalEmployees", 0L))
                .andExpect(model().attribute("avgSalary", 0.0))
                .andExpect(model().attribute("totalDepartments", 0L))
                .andExpect(model().attribute("totalBudget", 0.0));

        verify(statisticsService, times(1)).getAllStatistics();
    }

    @Test
    public void testShowCompanyStatistics() throws Exception {
        // Given
        String companyName = "TechCorp";
        CompanyStatisticsDTO statsDTO = new CompanyStatisticsDTO(
                companyName, 25, 8500.0, 15000.0, "Anna Nowak"
        );

        Employee employee1 = new Employee("Anna Nowak", "anna@techcorp.com", "TechCorp",
                Position.MANAGER, 10000.0, EmploymentStatus.ACTIVE);
        Employee employee2 = new Employee("Jan Kowalski", "jan@techcorp.com", "TechCorp",
                Position.PROGRAMMER, 5000.0, EmploymentStatus.ACTIVE);
        List<Employee> companyEmployees = Arrays.asList(employee1, employee2);

        when(statisticsService.getCompanyStatisticsDTO(companyName)).thenReturn(statsDTO);
        when(statisticsService.getEmployeesByCompany(companyName)).thenReturn(companyEmployees);

        // When & Then
        mockMvc.perform(get("/statistics/company/{companyName}", companyName))
                .andExpect(status().isOk())
                .andExpect(view().name("statistics/company-details"))
                .andExpect(model().attributeExists("stats"))
                .andExpect(model().attributeExists("companyName"))
                .andExpect(model().attributeExists("employees"))
                .andExpect(model().attributeExists("pageTitle"))
                .andExpect(model().attribute("companyName", companyName))
                .andExpect(model().attribute("pageTitle", "Statystyki - " + companyName))
                .andExpect(model().attribute("stats", statsDTO))
                .andExpect(model().attribute("employees", companyEmployees));

        verify(statisticsService, times(1)).getCompanyStatisticsDTO(companyName);
        verify(statisticsService, times(1)).getEmployeesByCompany(companyName);
    }

    @Test
    public void testShowCompanyStatistics_NoEmployees() throws Exception {
        // Given
        String companyName = "EmptyCorp";
        CompanyStatisticsDTO statsDTO = new CompanyStatisticsDTO(
                companyName, 0, 0.0, 0.0, null
        );
        List<Employee> companyEmployees = Arrays.asList();

        when(statisticsService.getCompanyStatisticsDTO(companyName)).thenReturn(statsDTO);
        when(statisticsService.getEmployeesByCompany(companyName)).thenReturn(companyEmployees);

        // When & Then
        mockMvc.perform(get("/statistics/company/{companyName}", companyName))
                .andExpect(status().isOk())
                .andExpect(view().name("statistics/company-details"))
                .andExpect(model().attribute("companyName", companyName))
                .andExpect(model().attribute("stats", statsDTO))
                .andExpect(model().attribute("employees", companyEmployees));

        verify(statisticsService, times(1)).getCompanyStatisticsDTO(companyName);
        verify(statisticsService, times(1)).getEmployeesByCompany(companyName);
    }

    @Test
    public void testShowCompanyStatistics_CompanyNotFound() throws Exception {
        // Given
        String companyName = "NonExistentCorp";
        CompanyStatisticsDTO statsDTO = new CompanyStatisticsDTO(
                companyName, 0, 0.0, 0.0, null
        );
        List<Employee> companyEmployees = Arrays.asList();

        when(statisticsService.getCompanyStatisticsDTO(companyName)).thenReturn(statsDTO);
        when(statisticsService.getEmployeesByCompany(companyName)).thenReturn(companyEmployees);

        // When & Then
        mockMvc.perform(get("/statistics/company/{companyName}", companyName))
                .andExpect(status().isOk())
                .andExpect(view().name("statistics/company-details"))
                .andExpect(model().attribute("companyName", companyName))
                .andExpect(model().attribute("stats", statsDTO))
                .andExpect(model().attribute("employees", companyEmployees));

        verify(statisticsService, times(1)).getCompanyStatisticsDTO(companyName);
        verify(statisticsService, times(1)).getEmployeesByCompany(companyName);
    }

    @Test
    public void testShowStatistics_ServiceException() throws Exception {
        // Given
        when(statisticsService.getAllStatistics()).thenThrow(new RuntimeException("Database error"));

        // When & Then - z globalną obsługą błędów, oczekujemy statusu 500
        mockMvc.perform(get("/statistics"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.message").value("An unexpected error occurred: Database error"))
                .andExpect(jsonPath("$.status").value(500));

        verify(statisticsService, times(1)).getAllStatistics();
    }

    @Test
    public void testShowCompanyStatistics_ServiceException() throws Exception {
        // Given
        String companyName = "TechCorp";
        when(statisticsService.getCompanyStatisticsDTO(companyName)).thenThrow(new RuntimeException("Service error"));

        // When & Then - z globalną obsługą błędów, oczekujemy statusu 500
        mockMvc.perform(get("/statistics/company/{companyName}", companyName))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.message").value("An unexpected error occurred: Service error"))
                .andExpect(jsonPath("$.status").value(500));

        verify(statisticsService, times(1)).getCompanyStatisticsDTO(companyName);
        verify(statisticsService, never()).getEmployeesByCompany(anyString());
    }

    @Test
    public void testShowCompanyStatistics_EmployeesServiceException() throws Exception {
        // Given
        String companyName = "TechCorp";
        CompanyStatisticsDTO statsDTO = new CompanyStatisticsDTO(
                companyName, 25, 8500.0, 15000.0, "Anna Nowak"
        );

        when(statisticsService.getCompanyStatisticsDTO(companyName)).thenReturn(statsDTO);
        when(statisticsService.getEmployeesByCompany(companyName)).thenThrow(new RuntimeException("Employees service error"));

        // When & Then - z globalną obsługą błędów, oczekujemy statusu 500
        mockMvc.perform(get("/statistics/company/{companyName}", companyName))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.message").value("An unexpected error occurred: Employees service error"))
                .andExpect(jsonPath("$.status").value(500));

        verify(statisticsService, times(1)).getCompanyStatisticsDTO(companyName);
        verify(statisticsService, times(1)).getEmployeesByCompany(companyName);
    }
}