package com.techcorp.employee.service;

import com.techcorp.employee.dto.CompanyStatisticsDTO;
import com.techcorp.employee.model.*;
import com.techcorp.employee.repository.EmployeeRepository;
import com.techcorp.employee.repository.DepartmentRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StatisticsServiceTest {

    @Mock
    private EmployeeRepository employeeRepository;

    @Mock
    private DepartmentRepository departmentRepository;

    @InjectMocks
    private StatisticsService statisticsService;

    @Test
    void getAllStatistics_WithEmptyData_ShouldReturnZeroValues() {
        // Given
        when(employeeRepository.countAllEmployees()).thenReturn(0L);
        when(employeeRepository.findAverageSalary()).thenReturn(0.0);
        when(employeeRepository.findMaxSalary()).thenReturn(0.0);
        when(employeeRepository.findMinSalary()).thenReturn(0.0);
        when(employeeRepository.findTotalSalaryCost()).thenReturn(0.0);
        when(employeeRepository.getCompanyStatisticsDTO()).thenReturn(List.of());
        when(employeeRepository.getPositionStatistics()).thenReturn(List.of());
        when(employeeRepository.getStatusStatistics()).thenReturn(List.of());
        when(departmentRepository.count()).thenReturn(0L);
        when(employeeRepository.countEmployeesWithoutDepartment()).thenReturn(0L);

        // When
        Map<String, Object> statistics = statisticsService.getAllStatistics();

        // Then - zgrupowanie wszystkich asercji dla pustych statystyk
        assertAll("Empty statistics verification",
                () -> assertNotNull(statistics, "Statistics map should not be null"),
                () -> assertEquals(0L, statistics.get("totalEmployees"), "Total employees should be 0"),
                () -> assertEquals(0.0, statistics.get("avgSalary"), "Average salary should be 0"),
                () -> assertEquals(0.0, statistics.get("maxSalary"), "Max salary should be 0"),
                () -> assertEquals(0.0, statistics.get("minSalary"), "Min salary should be 0"),
                () -> assertEquals(0.0, statistics.get("totalSalaryCost"), "Total salary cost should be 0"),
                () -> assertEquals(0L, statistics.get("totalDepartments"), "Total departments should be 0"),
                () -> assertEquals(0L, statistics.get("employeesWithoutDept"), "Employees without department should be 0"),
                () -> {
                    @SuppressWarnings("unchecked")
                    Map<String, CompanyStatistics> companyStats = (Map<String, CompanyStatistics>) statistics.get("companyStats");
                    assertTrue(companyStats.isEmpty(), "Company stats should be empty");
                },
                () -> {
                    @SuppressWarnings("unchecked")
                    Map<String, Long> positionDistribution = (Map<String, Long>) statistics.get("positionDistribution");
                    assertTrue(positionDistribution.isEmpty(), "Position distribution should be empty");
                },
                () -> {
                    @SuppressWarnings("unchecked")
                    Map<String, Long> statusDistribution = (Map<String, Long>) statistics.get("statusDistribution");
                    assertTrue(statusDistribution.isEmpty(), "Status distribution should be empty");
                }
        );
    }

    @Test
    void getCompanyStatistics_WithValidCompany_ShouldReturnStatistics() {
        // Given
        CompanyStatisticsDTO dto = new CompanyStatisticsDTO(
                "TechCorp", 10L, 7000.0, 10000.0, "Jan Manager"
        );

        when(employeeRepository.getCompanyStatisticsDTO("TechCorp"))
                .thenReturn(Optional.of(dto));

        // When
        CompanyStatistics result = statisticsService.getCompanyStatistics("TechCorp");

        // Then
        assertAll("Company statistics verification",
                () -> assertNotNull(result, "Company statistics should not be null"),
                () -> assertEquals("TechCorp", result.getCompanyName(), "Company name should match"),
                () -> assertEquals(10, result.getEmployeeCount(), "Employee count should be 10"),
                () -> assertEquals(0, new BigDecimal("7000.0").compareTo(result.getAverageSalary()),
                        "Average salary should be 7000.0"),
                () -> assertEquals(0, new BigDecimal("10000.0").compareTo(result.getMaxSalary()),
                        "Max salary should be 10000.0"),
                () -> assertEquals("Jan Manager", result.getTopEarnerName(), "Top earner name should match")
        );
    }

    @Test
    void getCompanyStatistics_WithNonExistingCompany_ShouldReturnEmpty() {
        // Given
        when(employeeRepository.getCompanyStatisticsDTO("NonExisting"))
                .thenReturn(Optional.empty());

        // When
        CompanyStatistics result = statisticsService.getCompanyStatistics("NonExisting");

        // Then
        assertAll("Non-existing company statistics",
                () -> assertNotNull(result, "Statistics should not be null even for non-existing company"),
                () -> assertEquals("NonExisting", result.getCompanyName(), "Company name should be preserved"),
                () -> assertEquals(0, result.getEmployeeCount(), "Employee count should be 0"),
                () -> assertEquals(0, BigDecimal.ZERO.compareTo(result.getAverageSalary()),
                        "Average salary should be 0"),
                () -> assertEquals(0, BigDecimal.ZERO.compareTo(result.getMaxSalary()),
                        "Max salary should be 0"),
                () -> assertEquals("", result.getTopEarnerName(), "Top earner name should be empty")
        );
    }

    @Test
    void getEmployeesByCompany_WithValidCompany_ShouldReturnEmployees() {
        // Given
        Employee emp1 = createEmployee("Jan Kowalski", "jan@techcorp.com", "TechCorp",
                Position.PROGRAMMER, new BigDecimal(5000), EmploymentStatus.ACTIVE);
        Employee emp2 = createEmployee("Anna Nowak", "anna@techcorp.com", "TechCorp",
                Position.MANAGER, new BigDecimal(8000), EmploymentStatus.ACTIVE);
        List<Employee> employees = Arrays.asList(emp1, emp2);

        Page<Employee> page = new PageImpl<>(employees);
        when(employeeRepository.findByCompany(eq("TechCorp"), any(PageRequest.class)))
                .thenReturn(page);

        // When
        List<Employee> result = statisticsService.getEmployeesByCompany("TechCorp");

        // Then
        assertAll("Employees by company verification",
                () -> assertNotNull(result, "Result list should not be null"),
                () -> assertEquals(2, result.size(), "Should return 2 employees")
        );
    }

    @Test
    void getAverageSalary_WithCompany_ShouldReturnCompanyAverage() {
        // Given
        when(employeeRepository.findAverageSalaryByCompany("TechCorp")).thenReturn(7500.0);

        // When
        Map<String, Double> result = statisticsService.getAverageSalary("TechCorp");

        // Then - asercje dla średniego wynagrodzenia w firmie
        assertAll("Company average salary verification",
                () -> assertNotNull(result, "Result map should not be null"),
                () -> assertEquals(7500.0, result.get("averageSalary"), "Average salary should be 7500.0"),
                () -> verify(employeeRepository, times(1)).findAverageSalaryByCompany("TechCorp"),
                () -> verify(employeeRepository, never()).findAverageSalary()
        );
    }

    @Test
    void getAverageSalary_WithoutCompany_ShouldReturnOverallAverage() {
        // Given
        when(employeeRepository.findAverageSalary()).thenReturn(6500.0);

        // When
        Map<String, Double> result = statisticsService.getAverageSalary(null);

        // Then - asercje dla ogólnej średniej
        assertAll("Overall average salary verification",
                () -> assertNotNull(result, "Result map should not be null"),
                () -> assertEquals(6500.0, result.get("averageSalary"), "Average salary should be 6500.0"),
                () -> verify(employeeRepository, times(1)).findAverageSalary(),
                () -> verify(employeeRepository, never()).findAverageSalaryByCompany(anyString())
        );
    }

    @Test
    void getAverageSalary_WithNullValueFromDB_ShouldReturnZero() {
        // Given
        when(employeeRepository.findAverageSalary()).thenReturn(null);

        // When
        Map<String, Double> result = statisticsService.getAverageSalary(null);

        // Then
        assertAll("Null average salary handling",
                () -> assertNotNull(result, "Result map should not be null"),
                () -> assertEquals(0.0, result.get("averageSalary"), "Should return 0.0 for null from DB")
        );
    }

    @Test
    void getCompanyStatisticsDTO_WithValidCompany_ShouldReturnDTO() {
        // Given
        CompanyStatisticsDTO dto = new CompanyStatisticsDTO(
                "TechCorp", 5L, 8000.0, 12000.0, "Jan Manager"
        );

        when(employeeRepository.getCompanyStatisticsDTO("TechCorp"))
                .thenReturn(Optional.of(dto));

        // When
        CompanyStatisticsDTO result = statisticsService.getCompanyStatisticsDTO("TechCorp");

        // Then - asercje dla DTO statystyk firmy
        assertAll("Company statistics DTO verification",
                () -> assertNotNull(result, "DTO should not be null"),
                () -> assertEquals("TechCorp", result.getCompanyName(), "Company name should match"),
                () -> assertEquals(5, result.getEmployeeCount(), "Employee count should be 5"),
                () -> assertEquals(BigDecimal.valueOf(8000.0), result.getAverageSalary(), "Average salary should be 8000.0"),
                () -> assertEquals(BigDecimal.valueOf(12000.0), result.getHighestSalary(), "Highest salary should be 12000.0"),
                () -> assertEquals("Jan Manager", result.getTopEarnerName(), "Top earner name should match")
        );
    }

    @Test
    void getPositionStatistics_ShouldReturnPositionCounts() {
        // Given
        List<Object[]> positionStats = Arrays.asList(
                new Object[]{Position.PROGRAMMER, 3L},
                new Object[]{Position.MANAGER, 1L}
        );

        when(employeeRepository.getPositionStatistics()).thenReturn(positionStats);

        // When
        Map<String, Integer> result = statisticsService.getPositionStatistics();

        // Then - asercje dla statystyk pozycji
        assertAll("Position statistics verification",
                () -> assertNotNull(result, "Result map should not be null"),
                () -> assertEquals(2, result.size(), "Should have 2 position entries"),
                () -> assertEquals(3, result.get("PROGRAMMER"), "Should have 3 PROGRAMMER positions"),
                () -> assertEquals(1, result.get("MANAGER"), "Should have 1 MANAGER position")
        );
    }

    @Test
    void getEmploymentStatusStatistics_ShouldReturnStatusDistribution() {
        // Given
        List<Object[]> statusStats = Arrays.asList(
                new Object[]{EmploymentStatus.ACTIVE, 8L},
                new Object[]{EmploymentStatus.ON_LEAVE, 2L}
        );

        when(employeeRepository.getStatusStatistics()).thenReturn(statusStats);

        // When
        Map<String, Integer> result = statisticsService.getEmploymentStatusStatistics();

        // Then - asercje dla statystyk statusu zatrudnienia
        assertAll("Employment status statistics verification",
                () -> assertNotNull(result, "Result map should not be null"),
                () -> assertEquals(2, result.size(), "Should have 2 status entries"),
                () -> assertEquals(8, result.get("ACTIVE"), "Should have 8 ACTIVE employees"),
                () -> assertEquals(2, result.get("ON_LEAVE"), "Should have 2 ON_LEAVE employees")
        );
    }

    @Test
    void getAdvancedStatistics_ShouldReturnAllAdvancedStats() {
        // Given
        Employee emp1 = createEmployee("Jan", "jan@tech.com", "TechCorp",
                Position.PRESIDENT, new BigDecimal(15000), EmploymentStatus.ACTIVE);
        List<Employee> topEarners = Arrays.asList(emp1);

        Page<Employee> topEarnersPage = new PageImpl<>(topEarners);
        when(employeeRepository.findTop10HighestPaidEmployees(any(PageRequest.class)))
                .thenReturn(topEarners);

        List<Employee> belowAvg = Arrays.asList(
                createEmployee("Anna", "anna@tech.com", "TechCorp",
                        Position.PROGRAMMER, new BigDecimal(3000), EmploymentStatus.ACTIVE)
        );
        when(employeeRepository.findEmployeesBelowAverageSalary()).thenReturn(belowAvg);

        Department dept = new Department("IT", "Warsaw", "IT Department", "manager@tech.com", 100000.0);
        dept.setId(1L);
        when(departmentRepository.findAll()).thenReturn(Arrays.asList(dept));

        when(employeeRepository.countEmployeesByDepartment(1L)).thenReturn(5L);
        when(employeeRepository.findByDepartmentId(1L)).thenReturn(belowAvg);

        // When
        Map<String, Object> result = statisticsService.getAdvancedStatistics();

        // Then - asercje dla zaawansowanych statystyk
        assertAll("Advanced statistics verification",
                () -> assertNotNull(result, "Advanced statistics should not be null"),
                () -> assertEquals(1, ((List<?>) result.get("topEarners")).size(),
                        "Should have 1 top earner"),
                () -> assertEquals(1, result.get("belowAverageCount"),
                        "Should have 1 employee below average"),
                () -> {
                    @SuppressWarnings("unchecked")
                    Map<String, Object> deptStats = (Map<String, Object>) result.get("departmentStats");
                    assertAll("Department stats verification",
                            () -> assertNotNull(deptStats, "Department stats should not be null"),
                            () -> assertNotNull(deptStats.get("IT"), "IT department should be in stats")
                    );
                }
        );
    }

    // Pomocnicza metoda do tworzenia pracowników
    private Employee createEmployee(String name, String email, String company,
                                    Position position, BigDecimal salary, EmploymentStatus status) {
        return new Employee(name, email, company, position, salary, status);
    }
}