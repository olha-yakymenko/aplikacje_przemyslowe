package com.techcorp.employee.controller;

import com.techcorp.employee.dto.DashboardStatisticsDTO;
import com.techcorp.employee.service.DashboardService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(HomeController.class)
class HomeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private DashboardService dashboardService;

    @Test
    @WithMockUser
    void home_ShouldReturnIndexPageWithStatistics() throws Exception {
        // Given
        DashboardStatisticsDTO stats = new DashboardStatisticsDTO(150L, 7500.50, 10L);
        when(dashboardService.getDashboardStatistics()).thenReturn(stats);

        // When & Then
        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(view().name("index"))
                .andExpect(model().attribute("totalEmployees", 150L))
                .andExpect(model().attribute("avgSalary", 7500.50))
                .andExpect(model().attribute("totalDepartments", 10L))
                .andExpect(model().attributeDoesNotExist("error"));
    }

    @Test
    @WithMockUser
    void home_WithServiceError_ShouldReturnIndexWithError() throws Exception {
        // Given
        when(dashboardService.getDashboardStatistics())
                .thenThrow(new RuntimeException("Database error"));

        // When & Then
        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(view().name("index"))
                .andExpect(model().attribute("totalEmployees", 0))
                .andExpect(model().attribute("avgSalary", 0.0))
                .andExpect(model().attribute("totalDepartments", 0))
                .andExpect(model().attribute("error", "Nie udało się załadować danych statystycznych"));
    }

    @Test
    @WithMockUser
    void home_WithNoData_ShouldReturnIndexWithZeroValues() throws Exception {
        // Given
        DashboardStatisticsDTO stats = new DashboardStatisticsDTO(0L, 0.0, 0L);
        when(dashboardService.getDashboardStatistics()).thenReturn(stats);

        // When & Then
        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(view().name("index"))
                .andExpect(model().attribute("totalEmployees", 0L))
                .andExpect(model().attribute("avgSalary", 0.0))
                .andExpect(model().attribute("totalDepartments", 0L))
                .andExpect(model().attributeDoesNotExist("error"));
    }

    @Test
    @WithMockUser
    void home_WithLargeNumbers_ShouldFormatCorrectly() throws Exception {
        // Given
        DashboardStatisticsDTO stats = new DashboardStatisticsDTO(1000000L, 999999.99, 500L);
        when(dashboardService.getDashboardStatistics()).thenReturn(stats);

        // When & Then
        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(view().name("index"))
                .andExpect(model().attribute("totalEmployees", 1000000L))
                .andExpect(model().attribute("avgSalary", 999999.99))
                .andExpect(model().attribute("totalDepartments", 500L));
    }

    @Test
    @WithMockUser
    void home_WithDecimalSalary_ShouldFormatCorrectly() throws Exception {
        // Given
        DashboardStatisticsDTO stats = new DashboardStatisticsDTO(100L, 12345.67, 5L);
        when(dashboardService.getDashboardStatistics()).thenReturn(stats);

        // When & Then
        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(view().name("index"))
                .andExpect(model().attribute("totalEmployees", 100L))
                .andExpect(model().attribute("avgSalary", 12345.67))
                .andExpect(model().attribute("totalDepartments", 5L));
    }

    @Test
    @WithMockUser
    void home_ShouldReturnCorrectViewName() throws Exception {
        // Given
        DashboardStatisticsDTO stats = new DashboardStatisticsDTO(10L, 5000.0, 2L);
        when(dashboardService.getDashboardStatistics()).thenReturn(stats);

        // When & Then
        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(view().name("index"))
                .andExpect(forwardedUrl(null)) // Sprawdź, że to nie jest forward
                .andExpect(redirectedUrl(null)); // Sprawdź, że to nie jest redirect
    }
}