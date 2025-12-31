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
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.*;

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
    @WithMockUser  // Dodaj to!
    public void testShowStatistics() throws Exception {
        // Given
        Map<String, Object> statistics = new HashMap<>();
        statistics.put("totalEmployees", 100L);
        statistics.put("avgSalary", BigDecimal.valueOf(7500.0));
        statistics.put("totalDepartments", 5L);
        statistics.put("totalBudget", BigDecimal.valueOf(500000.0));

        // CompanyStatistics z BigDecimal
        Map<String, CompanyStatistics> companyStats = new HashMap<>();
        CompanyStatistics techCorpStats = new CompanyStatistics(
                "TechCorp",
                50,
                BigDecimal.valueOf(8000.0),
                BigDecimal.valueOf(10000.0),
                "Jan Kowalski"
        );
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
                .andExpect(model().attribute("avgSalary", BigDecimal.valueOf(7500.0)));

        verify(statisticsService, times(1)).getAllStatistics();
    }

    @Test
    @WithMockUser  // Dodaj to!
    public void testShowStatistics_EmptyData() throws Exception {
        // Given
        Map<String, Object> statistics = new HashMap<>();
        statistics.put("totalEmployees", 0L);
        statistics.put("avgSalary", BigDecimal.ZERO);
        statistics.put("totalDepartments", 0L);
        statistics.put("totalBudget", BigDecimal.ZERO);
        statistics.put("companyStats", new HashMap<>());
        statistics.put("positionDistribution", new HashMap<>());

        when(statisticsService.getAllStatistics()).thenReturn(statistics);

        // When & Then
        mockMvc.perform(get("/statistics"))
                .andExpect(status().isOk())
                .andExpect(view().name("statistics/index"))
                .andExpect(model().attribute("totalEmployees", 0L))
                .andExpect(model().attribute("avgSalary", BigDecimal.ZERO))
                .andExpect(model().attribute("totalDepartments", 0L))
                .andExpect(model().attribute("totalBudget", BigDecimal.ZERO));

        verify(statisticsService, times(1)).getAllStatistics();
    }

    @Test
    @WithMockUser  // Dodaj to!
    public void testShowCompanyStatistics() throws Exception {
        // Given
        String companyName = "TechCorp";

        // CompanyStatisticsDTO z BigDecimal
        CompanyStatisticsDTO statsDTO = new CompanyStatisticsDTO(
                companyName,
                25L,
                BigDecimal.valueOf(8500.0),
                BigDecimal.valueOf(15000.0),
                "Anna Nowak"
        );

        Employee employee1 = new Employee("Anna Nowak", "anna@techcorp.com", "TechCorp",
                Position.MANAGER, BigDecimal.valueOf(10000), EmploymentStatus.ACTIVE);
        Employee employee2 = new Employee("Jan Kowalski", "jan@techcorp.com", "TechCorp",
                Position.PROGRAMMER, BigDecimal.valueOf(5000), EmploymentStatus.ACTIVE);
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
    @WithMockUser  // Dodaj to!
    public void testShowCompanyStatistics_NoEmployees() throws Exception {
        // Given
        String companyName = "EmptyCorp";

        // CompanyStatisticsDTO z BigDecimal
        CompanyStatisticsDTO statsDTO = new CompanyStatisticsDTO(
                companyName,
                0L,
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                ""
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
    @WithMockUser  // Dodaj to!
    public void testShowCompanyStatistics_CompanyNotFound() throws Exception {
        // Given
        String companyName = "NonExistentCorp";

        // CompanyStatisticsDTO z BigDecimal
        CompanyStatisticsDTO statsDTO = new CompanyStatisticsDTO(
                companyName,
                0L,
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                ""
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
    @WithMockUser  // Dodaj to!
    public void testShowStatistics_MultipleCompanies() throws Exception {
        // Given
        Map<String, Object> statistics = new HashMap<>();
        statistics.put("totalEmployees", 150L);
        statistics.put("avgSalary", BigDecimal.valueOf(8200.50));
        statistics.put("totalDepartments", 8L);
        statistics.put("totalBudget", BigDecimal.valueOf(1230750.00));

        // Wiele firm z BigDecimal
        Map<String, CompanyStatistics> companyStats = new HashMap<>();

        CompanyStatistics techCorpStats = new CompanyStatistics(
                "TechCorp",
                75,
                BigDecimal.valueOf(9000.75),
                BigDecimal.valueOf(12000.50),
                "Anna Nowak"
        );

        CompanyStatistics softIncStats = new CompanyStatistics(
                "SoftInc",
                50,
                BigDecimal.valueOf(7500.25),
                BigDecimal.valueOf(9500.00),
                "Jan Kowalski"
        );

        CompanyStatistics dataCorpStats = new CompanyStatistics(
                "DataCorp",
                25,
                BigDecimal.valueOf(6500.00),
                BigDecimal.valueOf(8000.00),
                "Piotr Wiśniewski"
        );

        companyStats.put("TechCorp", techCorpStats);
        companyStats.put("SoftInc", softIncStats);
        companyStats.put("DataCorp", dataCorpStats);

        statistics.put("companyStats", companyStats);

        Map<String, Long> positionDistribution = new HashMap<>();
        positionDistribution.put("DEVELOPER", 80L);
        positionDistribution.put("MANAGER", 30L);
        positionDistribution.put("ANALYST", 25L);
        positionDistribution.put("TESTER", 15L);
        statistics.put("positionDistribution", positionDistribution);

        when(statisticsService.getAllStatistics()).thenReturn(statistics);

        // When & Then
        mockMvc.perform(get("/statistics"))
                .andExpect(status().isOk())
                .andExpect(view().name("statistics/index"))
                .andExpect(model().attribute("totalEmployees", 150L))
                .andExpect(model().attribute("avgSalary", BigDecimal.valueOf(8200.50)))
                .andExpect(model().attribute("totalDepartments", 8L))
                .andExpect(model().attribute("totalBudget", BigDecimal.valueOf(1230750.00)))
                .andExpect(model().attribute("companyStats", companyStats))
                .andExpect(model().attribute("positionDistribution", positionDistribution));

        verify(statisticsService, times(1)).getAllStatistics();
    }

    @Test
    @WithMockUser  // Dodaj to!
    public void testShowCompanyStatistics_LargeSalaries() throws Exception {
        // Given
        String companyName = "BigMoneyCorp";

        // Duże wartości z BigDecimal
        CompanyStatisticsDTO statsDTO = new CompanyStatisticsDTO(
                companyName,
                10L,
                BigDecimal.valueOf(125000.50),
                BigDecimal.valueOf(250000.75),
                "CEO Executive"
        );

        Employee ceo = new Employee("CEO Executive", "ceo@bigmoney.com", "BigMoneyCorp",
                Position.MANAGER, BigDecimal.valueOf(250000.75), EmploymentStatus.ACTIVE);
        Employee cto = new Employee("CTO Tech", "cto@bigmoney.com", "BigMoneyCorp",
                Position.MANAGER, BigDecimal.valueOf(180000.25), EmploymentStatus.ACTIVE);
        List<Employee> companyEmployees = Arrays.asList(ceo, cto);

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
    @WithMockUser  // Dodaj to!
    public void testShowStatistics_NullValues() throws Exception {
        // Given - symulacja null w statystykach
        Map<String, Object> statistics = new HashMap<>();
        statistics.put("totalEmployees", null);
        statistics.put("avgSalary", null);
        statistics.put("totalDepartments", null);
        statistics.put("totalBudget", null);
        statistics.put("companyStats", null);
        statistics.put("positionDistribution", null);

        when(statisticsService.getAllStatistics()).thenReturn(statistics);

        // When & Then - aplikacja powinna obsłużyć null
        mockMvc.perform(get("/statistics"))
                .andExpect(status().isOk())
                .andExpect(view().name("statistics/index"));

        verify(statisticsService, times(1)).getAllStatistics();
    }

    @Test
    @WithMockUser(roles = "ADMIN")  // Test z rolą ADMIN
    public void testShowStatistics_WithAdminRole() throws Exception {
        // Given
        Map<String, Object> statistics = new HashMap<>();
        statistics.put("totalEmployees", 200L);
        statistics.put("avgSalary", BigDecimal.valueOf(8500.0));
        statistics.put("totalDepartments", 10L);

        when(statisticsService.getAllStatistics()).thenReturn(statistics);

        // When & Then
        mockMvc.perform(get("/statistics"))
                .andExpect(status().isOk())
                .andExpect(view().name("statistics/index"))
                .andExpect(model().attribute("totalEmployees", 200L));

        verify(statisticsService, times(1)).getAllStatistics();
    }

    @Test
    @WithMockUser(username = "stat_user")  // Test z konkretną nazwą użytkownika
    public void testShowCompanyStatistics_WithSpecificUser() throws Exception {
        // Given
        String companyName = "TechCorp";
        CompanyStatisticsDTO statsDTO = new CompanyStatisticsDTO(
                companyName,
                30L,
                BigDecimal.valueOf(7500.0),
                BigDecimal.valueOf(12000.0),
                "Test User"
        );

        when(statisticsService.getCompanyStatisticsDTO(companyName)).thenReturn(statsDTO);
        when(statisticsService.getEmployeesByCompany(companyName)).thenReturn(Collections.emptyList());

        // When & Then
        mockMvc.perform(get("/statistics/company/{companyName}", companyName))
                .andExpect(status().isOk())
                .andExpect(view().name("statistics/company-details"));

        verify(statisticsService, times(1)).getCompanyStatisticsDTO(companyName);
        verify(statisticsService, times(1)).getEmployeesByCompany(companyName);
    }

    @Test
    @WithMockUser  // Test dla URL z encodowanymi znakami
    public void testShowCompanyStatistics_WithSpecialCharacters() throws Exception {
        // Given
        String companyName = "Tech-Corp & Partners";
        CompanyStatisticsDTO statsDTO = new CompanyStatisticsDTO(
                companyName,
                15L,
                BigDecimal.valueOf(6500.0),
                BigDecimal.valueOf(9000.0),
                "Test"
        );

        when(statisticsService.getCompanyStatisticsDTO(companyName)).thenReturn(statsDTO);
        when(statisticsService.getEmployeesByCompany(companyName)).thenReturn(Collections.emptyList());

        // When & Then
        mockMvc.perform(get("/statistics/company/{companyName}", companyName))
                .andExpect(status().isOk())
                .andExpect(view().name("statistics/company-details"));

        verify(statisticsService, times(1)).getCompanyStatisticsDTO(companyName);
        verify(statisticsService, times(1)).getEmployeesByCompany(companyName);
    }

    @Test
    @WithMockUser  // Test dla długiej nazwy firmy
    public void testShowCompanyStatistics_WithLongCompanyName() throws Exception {
        // Given
        String companyName = "VeryLongCompanyNameThatExceedsNormalLengthButShouldStillWorkInURL";
        CompanyStatisticsDTO statsDTO = new CompanyStatisticsDTO(
                companyName,
                5L,
                BigDecimal.valueOf(5000.0),
                BigDecimal.valueOf(6000.0),
                "Test"
        );

        when(statisticsService.getCompanyStatisticsDTO(companyName)).thenReturn(statsDTO);
        when(statisticsService.getEmployeesByCompany(companyName)).thenReturn(Collections.emptyList());

        // When & Then
        mockMvc.perform(get("/statistics/company/{companyName}", companyName))
                .andExpect(status().isOk())
                .andExpect(view().name("statistics/company-details"));

        verify(statisticsService, times(1)).getCompanyStatisticsDTO(companyName);
        verify(statisticsService, times(1)).getEmployeesByCompany(companyName);
    }
}