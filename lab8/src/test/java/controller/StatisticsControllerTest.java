package com.techcorp.employee.controller;

import com.techcorp.employee.dto.CompanyStatisticsDTO;
import com.techcorp.employee.service.StatisticsService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.HashMap;
import java.util.Map;

import static org.mockito.ArgumentMatchers.anyString;
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
    public void testGetCompanyStatistics() throws Exception {
        // Given
        CompanyStatisticsDTO dto = new CompanyStatisticsDTO(
                "TechCorp",
                50,
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
                .andExpect(jsonPath("$.highestSalary").value(15000.0));

        verify(statisticsService, times(1)).getCompanyStatisticsDTO("TechCorp");
    }

    @Test
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
    public void testGetCompanyStatistics_NotFound() throws Exception {
        // Given
        when(statisticsService.getCompanyStatisticsDTO("NonExistentCorp"))
                .thenReturn(new CompanyStatisticsDTO("NonExistentCorp", 0, 0.0, 0.0, null));

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
}