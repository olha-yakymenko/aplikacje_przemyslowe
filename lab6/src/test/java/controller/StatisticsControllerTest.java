package com.techcorp.employee.controller;

import com.techcorp.employee.dto.CompanyStatisticsDTO;
import com.techcorp.employee.model.CompanyStatistics;
import com.techcorp.employee.model.Employee;
import com.techcorp.employee.model.EmploymentStatus;
import com.techcorp.employee.model.Position;
import com.techcorp.employee.service.EmployeeService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.OptionalDouble;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(StatisticsController.class)
class StatisticsControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private EmployeeService employeeService;

    @Test
    void getAverageSalary_ShouldReturnAverage() throws Exception {
        // Given
        when(employeeService.calculateAverageSalary()).thenReturn(OptionalDouble.of(7500.0));

        // When & Then
        mockMvc.perform(get("/api/statistics/salary/average"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.averageSalary").value(7500.0));

        verify(employeeService, times(1)).calculateAverageSalary();
    }

    @Test
    void getAverageSalary_WithCompany_ShouldReturnCompanyAverage() throws Exception {
        // Given
        when(employeeService.calculateAverageSalaryByCompany("TechCorp"))
                .thenReturn(OptionalDouble.of(8000.0));

        // When & Then
        mockMvc.perform(get("/api/statistics/salary/average")
                        .param("company", "TechCorp"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.averageSalary").value(8000.0));

        verify(employeeService, times(1)).calculateAverageSalaryByCompany("TechCorp");
    }

    @Test
    void getAverageSalary_WithNonExistingCompany_ShouldReturnZero() throws Exception {
        // Given
        when(employeeService.calculateAverageSalaryByCompany("NonExisting"))
                .thenReturn(OptionalDouble.empty());

        // When & Then
        mockMvc.perform(get("/api/statistics/salary/average")
                        .param("company", "NonExisting"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.averageSalary").value(0.0));

        verify(employeeService, times(1)).calculateAverageSalaryByCompany("NonExisting");
    }

    @Test
    void getAverageSalary_EmptyDatabase_ShouldReturnZero() throws Exception {
        // Given
        when(employeeService.calculateAverageSalary()).thenReturn(OptionalDouble.empty());

        // When & Then
        mockMvc.perform(get("/api/statistics/salary/average"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.averageSalary").value(0.0));

        verify(employeeService, times(1)).calculateAverageSalary();
    }

    @Test
    void getCompanyStatistics_ShouldReturnStatistics() throws Exception {
        // Given
        CompanyStatistics stats = new CompanyStatistics(5, 8000.0, "Jan Kowalski");
        when(employeeService.getCompanyStatistics("TechCorp")).thenReturn(stats);

        // POPRAWIONE: mockuj findHighestSalaryByCompany() z parametrem
        when(employeeService.findHighestSalaryByCompany("TechCorp")).thenReturn(15000.0);

        // When & Then
        mockMvc.perform(get("/api/statistics/company/TechCorp"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.companyName").value("TechCorp"))
                .andExpect(jsonPath("$.employeeCount").value(5))
                .andExpect(jsonPath("$.averageSalary").value(8000.0))
                .andExpect(jsonPath("$.highestSalary").value(15000.0)) // ✅ Teraz będzie działać
                .andExpect(jsonPath("$.topEarnerName").value("Jan Kowalski"));

        verify(employeeService, times(1)).getCompanyStatistics("TechCorp");
        verify(employeeService, times(1)).findHighestSalaryByCompany("TechCorp");
    }

    @Test
    void getCompanyStatistics_EmptyCompany_ShouldReturnEmptyStatistics() throws Exception {
        // Given
        CompanyStatistics emptyStats = new CompanyStatistics(0, 0.0, "None");
        when(employeeService.getCompanyStatistics("EmptyCorp")).thenReturn(emptyStats);

        // When & Then
        mockMvc.perform(get("/api/statistics/company/EmptyCorp"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.employeeCount").value(0))
                .andExpect(jsonPath("$.averageSalary").value(0.0))
                .andExpect(jsonPath("$.highestSalary").value(0.0));

        verify(employeeService, times(1)).getCompanyStatistics("EmptyCorp");
    }

    @Test
    void getPositionStatistics_ShouldReturnPositionCounts() throws Exception {
        // Given
        Map<Position, Long> positionCounts = new HashMap<>();
        positionCounts.put(Position.PROGRAMMER, 3L);
        positionCounts.put(Position.MANAGER, 1L);
        positionCounts.put(Position.INTERN, 2L);

        when(employeeService.countEmployeesByPosition()).thenReturn(positionCounts);

        // When & Then
        mockMvc.perform(get("/api/statistics/positions"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.PROGRAMMER").value(3))
                .andExpect(jsonPath("$.MANAGER").value(1))
                .andExpect(jsonPath("$.INTERN").value(2));

        verify(employeeService, times(1)).countEmployeesByPosition();
    }

    @Test
    void getPositionStatistics_EmptyDatabase_ShouldReturnEmptyMap() throws Exception {
        // Given
        when(employeeService.countEmployeesByPosition()).thenReturn(new HashMap<>());

        // When & Then
        mockMvc.perform(get("/api/statistics/positions"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isMap())
                .andExpect(jsonPath("$").isEmpty());

        verify(employeeService, times(1)).countEmployeesByPosition();
    }

    @Test
    void getEmploymentStatusStatistics_ShouldReturnStatusDistribution() throws Exception {
        // Given
        Map<EmploymentStatus, Long> statusDistribution = new HashMap<>();
        statusDistribution.put(EmploymentStatus.ACTIVE, 8L);
        statusDistribution.put(EmploymentStatus.ON_LEAVE, 2L);
        statusDistribution.put(EmploymentStatus.TERMINATED, 1L);

        when(employeeService.getEmploymentStatusDistribution()).thenReturn(statusDistribution);

        // When & Then
        mockMvc.perform(get("/api/statistics/status"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ACTIVE").value(8))
                .andExpect(jsonPath("$.ON_LEAVE").value(2))
                .andExpect(jsonPath("$.TERMINATED").value(1));

        verify(employeeService, times(1)).getEmploymentStatusDistribution();
    }

    @Test
    void getEmploymentStatusStatistics_EmptyDatabase_ShouldReturnEmptyMap() throws Exception {
        // Given
        when(employeeService.getEmploymentStatusDistribution()).thenReturn(new HashMap<>());

        // When & Then
        mockMvc.perform(get("/api/statistics/status"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isMap())
                .andExpect(jsonPath("$").isEmpty());

        verify(employeeService, times(1)).getEmploymentStatusDistribution();
    }

    @Test
    void getCompanyStatistics_WithSpecialCharacters_ShouldHandleCorrectly() throws Exception {
        // Given
        String companyWithSpecialChars = "Tech-Corp & Partners";
        CompanyStatistics stats = new CompanyStatistics(3, 9000.0, "Anna Nowak");
        when(employeeService.getCompanyStatistics(companyWithSpecialChars)).thenReturn(stats);

        // When & Then
        mockMvc.perform(get("/api/statistics/company/{companyName}", companyWithSpecialChars))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.companyName").value("Tech-Corp & Partners"))
                .andExpect(jsonPath("$.employeeCount").value(3));

        verify(employeeService, times(1)).getCompanyStatistics(companyWithSpecialChars);
    }


    @Test
    void getAverageSalary_WithCompany_ShouldCallCorrectServiceMethod() throws Exception {
        // Given
        when(employeeService.calculateAverageSalaryByCompany("TechCorp"))
                .thenReturn(OptionalDouble.of(8000.0));

        // When & Then
        mockMvc.perform(get("/api/statistics/salary/average")
                        .param("company", "TechCorp"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.averageSalary").value(8000.0));

        verify(employeeService, times(1)).calculateAverageSalaryByCompany("TechCorp");
        verify(employeeService, never()).calculateAverageSalary(); // to nie powinno być wywołane
    }

    @Test
    void getAverageSalary_WithoutCompany_ShouldCallGeneralMethod() throws Exception {
        // Given
        when(employeeService.calculateAverageSalary()).thenReturn(OptionalDouble.of(7500.0));

        // When & Then
        mockMvc.perform(get("/api/statistics/salary/average"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.averageSalary").value(7500.0));

        verify(employeeService, times(1)).calculateAverageSalary();
        verify(employeeService, never()).calculateAverageSalaryByCompany(anyString());
    }
}