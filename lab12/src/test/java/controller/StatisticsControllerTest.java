package com.techcorp.employee.controller;

import com.techcorp.employee.dto.CompanyStatisticsDTO;
import com.techcorp.employee.service.StatisticsService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.HashMap;
import java.util.Map;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(StatisticsController.class)
public class StatisticsControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private StatisticsService statisticsService;

    @Test
    @WithMockUser  // Dodaj to!
    public void testGetAverageSalary_WithoutCompany() throws Exception {
        // Given
        Map<String, Double> response = new HashMap<>();
        response.put("averageSalary", 7500.0);

        when(statisticsService.getAverageSalary(null)).thenReturn(response);

        // When & Then
        mockMvc.perform(get("/api/statistics/salary/average")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.averageSalary").value(7500.0));

        verify(statisticsService, times(1)).getAverageSalary(null);
    }

    @Test
    @WithMockUser  // Dodaj to!
    public void testGetAverageSalary_WithCompany() throws Exception {
        // Given
        Map<String, Double> response = new HashMap<>();
        response.put("averageSalary", 8000.0);

        when(statisticsService.getAverageSalary("TechCorp")).thenReturn(response);

        // When & Then
        mockMvc.perform(get("/api/statistics/salary/average")
                        .param("company", "TechCorp")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.averageSalary").value(8000.0));

        verify(statisticsService, times(1)).getAverageSalary("TechCorp");
    }

    @Test
    @WithMockUser  // Dodaj to!
    public void testGetCompanyStatistics() throws Exception {
        // Given
        CompanyStatisticsDTO dto = new CompanyStatisticsDTO(
                "TechCorp",
                50L,
                7500.0,
                15000.0,
                "Jan Kowalski"
        );

        when(statisticsService.getCompanyStatisticsDTO("TechCorp")).thenReturn(dto);

        // When & Then
        mockMvc.perform(get("/api/statistics/company/TechCorp")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.companyName").value("TechCorp"))
                .andExpect(jsonPath("$.employeeCount").value(50))
                .andExpect(jsonPath("$.averageSalary").value(7500.0))
                .andExpect(jsonPath("$.highestSalary").value(15000.0))
                .andExpect(jsonPath("$.topEarnerName").value("Jan Kowalski"));

        verify(statisticsService, times(1)).getCompanyStatisticsDTO("TechCorp");
    }

    @Test
    @WithMockUser  // Dodaj to!
    public void testGetPositionStatistics() throws Exception {
        // Given
        Map<String, Integer> response = new HashMap<>();
        response.put("PROGRAMMER", 20);
        response.put("MANAGER", 5);
        response.put("PRESIDENT", 1);

        when(statisticsService.getPositionStatistics()).thenReturn(response);

        // When & Then
        mockMvc.perform(get("/api/statistics/positions")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.PROGRAMMER").value(20))
                .andExpect(jsonPath("$.MANAGER").value(5))
                .andExpect(jsonPath("$.PRESIDENT").value(1));

        verify(statisticsService, times(1)).getPositionStatistics();
    }

    @Test
    @WithMockUser  // Dodaj to!
    public void testGetEmploymentStatusStatistics() throws Exception {
        // Given
        Map<String, Integer> response = new HashMap<>();
        response.put("ACTIVE", 45);
        response.put("ON_LEAVE", 3);
        response.put("TERMINATED", 2);

        when(statisticsService.getEmploymentStatusStatistics()).thenReturn(response);

        // When & Then
        mockMvc.perform(get("/api/statistics/status")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ACTIVE").value(45))
                .andExpect(jsonPath("$.ON_LEAVE").value(3))
                .andExpect(jsonPath("$.TERMINATED").value(2));

        verify(statisticsService, times(1)).getEmploymentStatusStatistics();
    }

    @Test
    @WithMockUser  // Dodaj to!
    public void testGetCompanyStatistics_NotFound() throws Exception {
        // Given
        CompanyStatisticsDTO emptyStats = new CompanyStatisticsDTO(
                "NonExistentCorp", 0L, 0.0, 0.0, null
        );
        when(statisticsService.getCompanyStatisticsDTO("NonExistentCorp"))
                .thenReturn(emptyStats);

        // When & Then
        mockMvc.perform(get("/api/statistics/company/NonExistentCorp")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.companyName").value("NonExistentCorp"))
                .andExpect(jsonPath("$.employeeCount").value(0))
                .andExpect(jsonPath("$.averageSalary").value(0.0))
                .andExpect(jsonPath("$.highestSalary").value(0.0));

        verify(statisticsService, times(1)).getCompanyStatisticsDTO("NonExistentCorp");
    }

    @Test
    @WithMockUser  // Dodaj to!
    public void testGetAverageSalary_EmptyCompany() throws Exception {
        // Given
        Map<String, Double> response = new HashMap<>();
        response.put("averageSalary", 7000.0);

        when(statisticsService.getAverageSalary("")).thenReturn(response);

        // When & Then
        mockMvc.perform(get("/api/statistics/salary/average")
                        .param("company", "")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.averageSalary").value(7000.0));

        verify(statisticsService, times(1)).getAverageSalary("");
    }

    @Test
    @WithMockUser  // Dodaj to!
    public void testGetPositionStatistics_Empty() throws Exception {
        // Given
        Map<String, Integer> response = new HashMap<>();

        when(statisticsService.getPositionStatistics()).thenReturn(response);

        // When & Then
        mockMvc.perform(get("/api/statistics/positions")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isMap())
                .andExpect(jsonPath("$").isEmpty());

        verify(statisticsService, times(1)).getPositionStatistics();
    }

    @Test
    @WithMockUser  // Dodaj to!
    public void testGetEmploymentStatusStatistics_Empty() throws Exception {
        // Given
        Map<String, Integer> response = new HashMap<>();

        when(statisticsService.getEmploymentStatusStatistics()).thenReturn(response);

        // When & Then
        mockMvc.perform(get("/api/statistics/status")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isMap())
                .andExpect(jsonPath("$").isEmpty());

        verify(statisticsService, times(1)).getEmploymentStatusStatistics();
    }

    @Test
    @WithMockUser(roles = "ADMIN")  // Możesz też testować różne role
    public void testGetCompanyStatistics_WithAdminRole() throws Exception {
        // Given
        CompanyStatisticsDTO dto = new CompanyStatisticsDTO(
                "TechCorp", 100L, 8500.0, 20000.0, "CEO"
        );
        when(statisticsService.getCompanyStatisticsDTO("TechCorp")).thenReturn(dto);

        // When & Then
        mockMvc.perform(get("/api/statistics/company/TechCorp"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.companyName").value("TechCorp"));
    }

    @Test
    @WithMockUser(username = "testuser")  // Możesz ustawić konkretną nazwę użytkownika
    public void testGetAverageSalary_WithSpecificUser() throws Exception {
        // Given
        Map<String, Double> response = new HashMap<>();
        response.put("averageSalary", 6500.0);
        when(statisticsService.getAverageSalary(null)).thenReturn(response);

        // When & Then
        mockMvc.perform(get("/api/statistics/salary/average"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.averageSalary").value(6500.0));
    }

    @Test
    @WithMockUser  // Test dla walidacji pustej nazwy firmy
    public void testGetCompanyStatistics_BlankCompanyName() throws Exception {
        // When & Then - powinno zwrócić 400 Bad Request
        mockMvc.perform(get("/api/statistics/company/ "))
                .andExpect(status().isBadRequest());  // @NotBlank w kontrolerze

        verify(statisticsService, never()).getCompanyStatisticsDTO(anyString());
    }

    @Test
    @WithMockUser  // Test dla długiej nazwy firmy
    public void testGetCompanyStatistics_LongCompanyName() throws Exception {
        // Given
        String longCompanyName = "VeryLongCompanyNameThatExceedsNormalLengthButShouldStillWork";
        CompanyStatisticsDTO dto = new CompanyStatisticsDTO(
                longCompanyName, 10L, 5000.0, 8000.0, "Test"
        );
        when(statisticsService.getCompanyStatisticsDTO(longCompanyName)).thenReturn(dto);

        // When & Then
        mockMvc.perform(get("/api/statistics/company/{companyName}", longCompanyName))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.companyName").value(longCompanyName));

        verify(statisticsService, times(1)).getCompanyStatisticsDTO(longCompanyName);
    }

    @Test
    @WithMockUser  // Test dla wartości ujemnych
    public void testGetCompanyStatistics_NegativeValues() throws Exception {
        // Given
        CompanyStatisticsDTO dto = new CompanyStatisticsDTO(
                "StrugglingCorp", -5L, -1000.0, -2000.0, null
        );
        when(statisticsService.getCompanyStatisticsDTO("StrugglingCorp")).thenReturn(dto);

        // When & Then
        mockMvc.perform(get("/api/statistics/company/StrugglingCorp"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.employeeCount").value(-5))
                .andExpect(jsonPath("$.averageSalary").value(-1000.0))
                .andExpect(jsonPath("$.highestSalary").value(-2000.0));

        verify(statisticsService, times(1)).getCompanyStatisticsDTO("StrugglingCorp");
    }

    @Test
    @WithMockUser  // Test dla bardzo dużych wartości
    public void testGetAverageSalary_VeryLargeValues() throws Exception {
        // Given
        Map<String, Double> response = new HashMap<>();
        response.put("averageSalary", 9999999.99);
        when(statisticsService.getAverageSalary("BigCorp")).thenReturn(response);

        // When & Then
        mockMvc.perform(get("/api/statistics/salary/average")
                        .param("company", "BigCorp"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.averageSalary").value(9999999.99));

        verify(statisticsService, times(1)).getAverageSalary("BigCorp");
    }
}