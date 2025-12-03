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

        // Then
        assertNotNull(statistics);
        assertEquals(0L, statistics.get("totalEmployees"));
        assertEquals(0.0, statistics.get("avgSalary"));
        assertEquals(0.0, statistics.get("maxSalary"));
        assertEquals(0.0, statistics.get("minSalary"));
        assertEquals(0.0, statistics.get("totalSalaryCost"));
        assertEquals(0L, statistics.get("totalDepartments"));
        assertEquals(0L, statistics.get("employeesWithoutDept"));

        @SuppressWarnings("unchecked")
        Map<String, CompanyStatistics> companyStats = (Map<String, CompanyStatistics>) statistics.get("companyStats");
        assertTrue(companyStats.isEmpty());

        @SuppressWarnings("unchecked")
        Map<String, Long> positionDistribution = (Map<String, Long>) statistics.get("positionDistribution");
        assertTrue(positionDistribution.isEmpty());

        @SuppressWarnings("unchecked")
        Map<String, Long> statusDistribution = (Map<String, Long>) statistics.get("statusDistribution");
        assertTrue(statusDistribution.isEmpty());
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
        assertNotNull(result);
        assertEquals("TechCorp", result.getCompanyName());
        assertEquals(10, result.getEmployeeCount());
        assertEquals(7000.0, result.getAverageSalary());
        assertEquals(10000.0, result.getMaxSalary());
        assertEquals("Jan Manager", result.getTopEarnerName());
    }

    @Test
    void getCompanyStatistics_WithNonExistingCompany_ShouldReturnEmpty() {
        // Given
        when(employeeRepository.getCompanyStatisticsDTO("NonExisting"))
                .thenReturn(Optional.empty());

        // When
        CompanyStatistics result = statisticsService.getCompanyStatistics("NonExisting");

        // Then
        assertNotNull(result);
        assertEquals("NonExisting", result.getCompanyName());
        assertEquals(0, result.getEmployeeCount());
        assertEquals(0.0, result.getAverageSalary());
        assertEquals(0.0, result.getMaxSalary());
        assertEquals("", result.getTopEarnerName());
    }

    @Test
    void getEmployeesByCompany_WithValidCompany_ShouldReturnEmployees() {
        // Given
        Employee emp1 = createEmployee("Jan Kowalski", "jan@techcorp.com", "TechCorp",
                Position.PROGRAMMER, 5000.0, EmploymentStatus.ACTIVE);
        Employee emp2 = createEmployee("Anna Nowak", "anna@techcorp.com", "TechCorp",
                Position.MANAGER, 8000.0, EmploymentStatus.ACTIVE);
        List<Employee> employees = Arrays.asList(emp1, emp2);

        Page<Employee> page = new PageImpl<>(employees);
        when(employeeRepository.findByCompany(eq("TechCorp"), any(PageRequest.class)))
                .thenReturn(page);

        // When
        List<Employee> result = statisticsService.getEmployeesByCompany("TechCorp");

        // Then
        assertNotNull(result);
        assertEquals(2, result.size());
    }

    @Test
    void getAverageSalary_WithCompany_ShouldReturnCompanyAverage() {
        // Given
        when(employeeRepository.findAverageSalaryByCompany("TechCorp")).thenReturn(7500.0);

        // When
        Map<String, Double> result = statisticsService.getAverageSalary("TechCorp");

        // Then
        assertNotNull(result);
        assertEquals(7500.0, result.get("averageSalary"));
        verify(employeeRepository, times(1)).findAverageSalaryByCompany("TechCorp");
        verify(employeeRepository, never()).findAverageSalary();
    }

    @Test
    void getAverageSalary_WithoutCompany_ShouldReturnOverallAverage() {
        // Given
        when(employeeRepository.findAverageSalary()).thenReturn(6500.0);

        // When
        Map<String, Double> result = statisticsService.getAverageSalary(null);

        // Then
        assertNotNull(result);
        assertEquals(6500.0, result.get("averageSalary"));
        verify(employeeRepository, times(1)).findAverageSalary();
        verify(employeeRepository, never()).findAverageSalaryByCompany(anyString());
    }

    @Test
    void getAverageSalary_WithNullValueFromDB_ShouldReturnZero() {
        // Given
        when(employeeRepository.findAverageSalary()).thenReturn(null);

        // When
        Map<String, Double> result = statisticsService.getAverageSalary(null);

        // Then
        assertNotNull(result);
        assertEquals(0.0, result.get("averageSalary"));
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

        // Then
        assertNotNull(result);
        assertEquals("TechCorp", result.getCompanyName());
        assertEquals(5, result.getEmployeeCount());
        assertEquals(8000.0, result.getAverageSalary());
        assertEquals(12000.0, result.getHighestSalary());
        assertEquals("Jan Manager", result.getTopEarnerName());
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

        // Then
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(3, result.get("PROGRAMMER"));
        assertEquals(1, result.get("MANAGER"));
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

        // Then
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(8, result.get("ACTIVE"));
        assertEquals(2, result.get("ON_LEAVE"));
    }

    @Test
    void getAdvancedStatistics_ShouldReturnAllAdvancedStats() {
        // Given
        Employee emp1 = createEmployee("Jan", "jan@tech.com", "TechCorp",
                Position.PRESIDENT, 15000.0, EmploymentStatus.ACTIVE);
        List<Employee> topEarners = Arrays.asList(emp1);

        Page<Employee> topEarnersPage = new PageImpl<>(topEarners);
        when(employeeRepository.findTop10HighestPaidEmployees(any(PageRequest.class)))
                .thenReturn(topEarners);

        List<Employee> belowAvg = Arrays.asList(
                createEmployee("Anna", "anna@tech.com", "TechCorp",
                        Position.PROGRAMMER, 3000.0, EmploymentStatus.ACTIVE)
        );
        when(employeeRepository.findEmployeesBelowAverageSalary()).thenReturn(belowAvg);

        Department dept = new Department("IT", "Warsaw", "IT Department", "manager@tech.com", 100000.0);
        dept.setId(1L);
        when(departmentRepository.findAll()).thenReturn(Arrays.asList(dept));

        when(employeeRepository.countEmployeesByDepartment(1L)).thenReturn(5L);
        when(employeeRepository.findByDepartmentId(1L)).thenReturn(belowAvg);

        // When
        Map<String, Object> result = statisticsService.getAdvancedStatistics();

        // Then
        assertNotNull(result);
        assertEquals(1, ((List<?>) result.get("topEarners")).size());
        assertEquals(1, result.get("belowAverageCount"));

        @SuppressWarnings("unchecked")
        Map<String, Object> deptStats = (Map<String, Object>) result.get("departmentStats");
        assertNotNull(deptStats.get("IT"));
    }

    // Pomocnicza metoda do tworzenia pracowników
    private Employee createEmployee(String name, String email, String company,
                                    Position position, double salary, EmploymentStatus status) {
        return new Employee(name, email, company, position, salary, status);
    }
}