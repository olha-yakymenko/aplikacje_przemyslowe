package com.techcorp.employee.service;

import com.techcorp.employee.dto.CompanyStatisticsDTO;
import com.techcorp.employee.model.CompanyStatistics;
import com.techcorp.employee.model.Employee;
import com.techcorp.employee.model.Department;
import com.techcorp.employee.model.EmploymentStatus;
import com.techcorp.employee.model.Position;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.OptionalDouble;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StatisticsServiceTest {

    @Mock
    private EmployeeService employeeService;

    @Mock
    private DepartmentService departmentService;

    @InjectMocks
    private StatisticsService statisticsService;


    @Test
    void getAllStatistics_WithEmptyData_ShouldReturnZeroValues() {
        // Given
        when(employeeService.getAllEmployees()).thenReturn(List.of());
        when(departmentService.getAllDepartments()).thenReturn(List.of());

        // When
        Map<String, Object> statistics = statisticsService.getAllStatistics();

        // Then
        assertNotNull(statistics);
        assertEquals(0, statistics.get("totalEmployees"));
        assertEquals(0.0, statistics.get("avgSalary"));
        assertEquals(0, statistics.get("totalDepartments"));
        assertEquals(0.0, statistics.get("totalBudget"));

        @SuppressWarnings("unchecked")
        Map<String, CompanyStatistics> companyStats = (Map<String, CompanyStatistics>) statistics.get("companyStats");
        assertTrue(companyStats.isEmpty());

        @SuppressWarnings("unchecked")
        Map<String, Long> positionDistribution = (Map<String, Long>) statistics.get("positionDistribution");
        assertTrue(positionDistribution.isEmpty());

        verify(employeeService, times(1)).getAllEmployees();
        verify(departmentService, times(1)).getAllDepartments();
    }

    @Test
    void getCompanyStatistics_WithValidCompany_ShouldReturnStatistics() {
        // Given
        CompanyStatistics expectedStats = new CompanyStatistics(10, 7000.0, "Jan Manager");
        when(employeeService.getCompanyStatistics("TechCorp")).thenReturn(expectedStats);

        // When
        CompanyStatistics result = statisticsService.getCompanyStatistics("TechCorp");

        // Then
        assertNotNull(result);
        assertEquals(10, result.getEmployeeCount());
        assertEquals(7000.0, result.getAverageSalary());
        assertEquals("Jan Manager", result.getHighestPaidEmployee());
        verify(employeeService, times(1)).getCompanyStatistics("TechCorp");
    }

    @Test
    void getEmployeesByCompany_WithValidCompany_ShouldReturnEmployees() {
        // Given
        Employee emp1 = createEmployee("Jan Kowalski", "jan@techcorp.com", "TechCorp",
                Position.PROGRAMMER, 5000.0, EmploymentStatus.ACTIVE);
        Employee emp2 = createEmployee("Anna Nowak", "anna@techcorp.com", "TechCorp",
                Position.MANAGER, 8000.0, EmploymentStatus.ACTIVE);
        List<Employee> expectedEmployees = Arrays.asList(emp1, emp2);

        when(employeeService.getEmployeesByCompany("TechCorp")).thenReturn(expectedEmployees);

        // When
        List<Employee> result = statisticsService.getEmployeesByCompany("TechCorp");

        // Then
        assertNotNull(result);
        assertEquals(2, result.size());
        verify(employeeService, times(1)).getEmployeesByCompany("TechCorp");
    }

    @Test
    void getAverageSalary_WithCompany_ShouldReturnCompanyAverage() {
        // Given
        when(employeeService.calculateAverageSalaryByCompany("TechCorp"))
                .thenReturn(OptionalDouble.of(7500.0));

        // When
        Map<String, Double> result = statisticsService.getAverageSalary("TechCorp");

        // Then
        assertNotNull(result);
        assertEquals(7500.0, result.get("averageSalary"));
        verify(employeeService, times(1)).calculateAverageSalaryByCompany("TechCorp");
        verify(employeeService, never()).calculateAverageSalary();
    }

    @Test
    void getAverageSalary_WithoutCompany_ShouldReturnOverallAverage() {
        // Given
        when(employeeService.calculateAverageSalary()).thenReturn(OptionalDouble.of(6500.0));

        // When
        Map<String, Double> result = statisticsService.getAverageSalary(null);

        // Then
        assertNotNull(result);
        assertEquals(6500.0, result.get("averageSalary"));
        verify(employeeService, times(1)).calculateAverageSalary();
        verify(employeeService, never()).calculateAverageSalaryByCompany(anyString());
    }

    @Test
    void getAverageSalary_WithEmptyCompany_ShouldReturnOverallAverage() {
        // Given
        when(employeeService.calculateAverageSalary()).thenReturn(OptionalDouble.of(6000.0));

        // When
        Map<String, Double> result = statisticsService.getAverageSalary("");

        // Then
        assertNotNull(result);
        assertEquals(6000.0, result.get("averageSalary"));
        verify(employeeService, times(1)).calculateAverageSalary();
        verify(employeeService, never()).calculateAverageSalaryByCompany(anyString());
    }

    @Test
    void getAverageSalary_WithNoEmployees_ShouldReturnZero() {
        // Given
        when(employeeService.calculateAverageSalary()).thenReturn(OptionalDouble.empty());

        // When
        Map<String, Double> result = statisticsService.getAverageSalary(null);

        // Then
        assertNotNull(result);
        assertEquals(0.0, result.get("averageSalary"));
        verify(employeeService, times(1)).calculateAverageSalary();
    }

    @Test
    void getCompanyStatisticsDTO_WithValidCompany_ShouldReturnDTO() {
        // Given
        CompanyStatistics companyStats = new CompanyStatistics(5, 8000.0, "Jan Manager");
        when(employeeService.getCompanyStatistics("TechCorp")).thenReturn(companyStats);
        when(employeeService.findHighestSalaryByCompany("TechCorp")).thenReturn(12000.0);

        // When
        CompanyStatisticsDTO result = statisticsService.getCompanyStatisticsDTO("TechCorp");

        // Then
        assertNotNull(result);
        assertEquals("TechCorp", result.getCompanyName());
        assertEquals(5, result.getEmployeeCount());
        assertEquals(8000.0, result.getAverageSalary());
        assertEquals(12000.0, result.getHighestSalary());
        assertEquals("Jan Manager", result.getTopEarnerName());

        verify(employeeService, times(1)).getCompanyStatistics("TechCorp");
        verify(employeeService, times(1)).findHighestSalaryByCompany("TechCorp");
    }

    @Test
    void getPositionStatistics_ShouldReturnPositionCounts() {
        // Given
        Map<Position, Long> positionCounts = Map.of(
                Position.PROGRAMMER, 3L,
                Position.MANAGER, 1L
        );
        when(employeeService.countEmployeesByPosition()).thenReturn(positionCounts);

        // When
        Map<String, Integer> result = statisticsService.getPositionStatistics();

        // Then
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(3, result.get("PROGRAMMER"));
        assertEquals(1, result.get("MANAGER"));
        verify(employeeService, times(1)).countEmployeesByPosition();
    }

    @Test
    void getPositionStatistics_WithNoEmployees_ShouldReturnEmptyMap() {
        // Given
        when(employeeService.countEmployeesByPosition()).thenReturn(Map.of());

        // When
        Map<String, Integer> result = statisticsService.getPositionStatistics();

        // Then
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(employeeService, times(1)).countEmployeesByPosition();
    }

    @Test
    void getEmploymentStatusStatistics_ShouldReturnStatusDistribution() {
        // Given
        Map<EmploymentStatus, Long> statusDistribution = Map.of(
                EmploymentStatus.ACTIVE, 8L,
                EmploymentStatus.ON_LEAVE, 2L
        );
        when(employeeService.getEmploymentStatusDistribution()).thenReturn(statusDistribution);

        // When
        Map<String, Integer> result = statisticsService.getEmploymentStatusStatistics();

        // Then
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(8, result.get("ACTIVE"));
        assertEquals(2, result.get("ON_LEAVE"));
        verify(employeeService, times(1)).getEmploymentStatusDistribution();
    }

    @Test
    void getEmploymentStatusStatistics_WithNoEmployees_ShouldReturnEmptyMap() {
        // Given
        when(employeeService.getEmploymentStatusDistribution()).thenReturn(Map.of());

        // When
        Map<String, Integer> result = statisticsService.getEmploymentStatusStatistics();

        // Then
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(employeeService, times(1)).getEmploymentStatusDistribution();
    }

    @Test
    void getAllStatistics_WithMultipleCompanies_ShouldReturnAllCompanyStats() {
        // Given
        Employee emp1 = createEmployee("Jan Kowalski", "jan@techcorp.com", "TechCorp",
                Position.PROGRAMMER, 5000.0, EmploymentStatus.ACTIVE);
        Employee emp2 = createEmployee("Anna Nowak", "anna@othercorp.com", "OtherCorp",
                Position.MANAGER, 8000.0, EmploymentStatus.ACTIVE);
        List<Employee> employees = Arrays.asList(emp1, emp2);

        Department dept = new Department(1L, "IT", "Warszawa", "Dział IT", "manager@techcorp.com", 100000.0);
        List<Department> departments = Arrays.asList(dept);

        CompanyStatistics techCorpStats = new CompanyStatistics(1, 5000.0, "Jan Kowalski");
        CompanyStatistics otherCorpStats = new CompanyStatistics(1, 8000.0, "Anna Nowak");

        when(employeeService.getAllEmployees()).thenReturn(employees);
        when(departmentService.getAllDepartments()).thenReturn(departments);
        when(employeeService.getCompanyStatistics("TechCorp")).thenReturn(techCorpStats);
        when(employeeService.getCompanyStatistics("OtherCorp")).thenReturn(otherCorpStats);

        // When
        Map<String, Object> statistics = statisticsService.getAllStatistics();

        // Then
        assertNotNull(statistics);

        @SuppressWarnings("unchecked")
        Map<String, CompanyStatistics> companyStats = (Map<String, CompanyStatistics>) statistics.get("companyStats");

        assertEquals(2, companyStats.size());
        assertTrue(companyStats.containsKey("TechCorp"));
        assertTrue(companyStats.containsKey("OtherCorp"));
        assertEquals(5000.0, companyStats.get("TechCorp").getAverageSalary());
        assertEquals(8000.0, companyStats.get("OtherCorp").getAverageSalary());

        verify(employeeService, times(1)).getCompanyStatistics("TechCorp");
        verify(employeeService, times(1)).getCompanyStatistics("OtherCorp");
    }

    @Test
    void getAllStatistics_WithZeroSalaries_ShouldCalculateCorrectly() {
        // Given
        Employee emp1 = createEmployee("Jan Kowalski", "jan@techcorp.com", "TechCorp",
                Position.PROGRAMMER, 0.0, EmploymentStatus.ACTIVE);
        Employee emp2 = createEmployee("Anna Nowak", "anna@techcorp.com", "TechCorp",
                Position.MANAGER, 0.0, EmploymentStatus.ACTIVE);
        List<Employee> employees = Arrays.asList(emp1, emp2);

        Department dept = new Department(1L, "IT", "Warszawa", "Dział IT", "manager@techcorp.com", 0.0);
        List<Department> departments = Arrays.asList(dept);

        CompanyStatistics companyStats = new CompanyStatistics(2, 0.0, "None");

        when(employeeService.getAllEmployees()).thenReturn(employees);
        when(departmentService.getAllDepartments()).thenReturn(departments);
        when(employeeService.getCompanyStatistics("TechCorp")).thenReturn(companyStats);

        // When
        Map<String, Object> statistics = statisticsService.getAllStatistics();

        // Then
        assertNotNull(statistics);
        assertEquals(2, statistics.get("totalEmployees"));
        assertEquals(0.0, statistics.get("avgSalary"));
        assertEquals(1, statistics.get("totalDepartments"));
        assertEquals(0.0, statistics.get("totalBudget"));
    }

    @Test
    void getAverageSalary_WithWhitespaceCompany_ShouldReturnOverallAverage() {
        // Given
        when(employeeService.calculateAverageSalary()).thenReturn(OptionalDouble.of(5500.0));

        // When
        Map<String, Double> result = statisticsService.getAverageSalary("   ");

        // Then
        assertNotNull(result);
        assertEquals(5500.0, result.get("averageSalary"));
        verify(employeeService, times(1)).calculateAverageSalary();
        verify(employeeService, never()).calculateAverageSalaryByCompany(anyString());
    }

    // Pomocnicza metoda do tworzenia pracowników
    private Employee createEmployee(String name, String email, String company,
                                    Position position, double salary, EmploymentStatus status) {
        try {
            return new Employee(name, email, company, position, salary, status);
        } catch (Exception e) {
            throw new RuntimeException("Error creating test employee", e);
        }
    }
}