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
import java.util.HashMap;

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
        CompanyStatistics expectedStats = new CompanyStatistics("TechCorp", 10, 7000.0, 10000.0);
        expectedStats.setHighestPaidEmployee("Jan Manager");
        when(employeeService.getCompanyStatistics("TechCorp")).thenReturn(expectedStats);

        // When
        CompanyStatistics result = statisticsService.getCompanyStatistics("TechCorp");

        // Then
        assertNotNull(result);
        assertEquals(10, result.getEmployeeCount());
        assertEquals(7000.0, result.getAverageSalary());
        assertEquals("Jan Manager", result.getHighestPaidEmployee());
        assertEquals("TechCorp", result.getCompanyName());
        assertEquals(10000.0, result.getMaxSalary());
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
        when(employeeService.calculateAverageSalaryByCompany("TechCorp")).thenReturn(7500.0);

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
        when(employeeService.calculateAverageSalary()).thenReturn(6500.0);

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
        when(employeeService.calculateAverageSalary()).thenReturn(6000.0);

        // When
        Map<String, Double> result = statisticsService.getAverageSalary("");

        // Then
        assertNotNull(result);
        assertEquals(6000.0, result.get("averageSalary"));
        verify(employeeService, times(1)).calculateAverageSalary();
        verify(employeeService, never()).calculateAverageSalaryByCompany(anyString());
    }

    @Test
    void getAverageSalary_WithWhitespaceCompany_ShouldReturnOverallAverage() {
        // Given
        when(employeeService.calculateAverageSalary()).thenReturn(5500.0);

        // When
        Map<String, Double> result = statisticsService.getAverageSalary("   ");

        // Then
        assertNotNull(result);
        assertEquals(5500.0, result.get("averageSalary"));
        verify(employeeService, times(1)).calculateAverageSalary();
        verify(employeeService, never()).calculateAverageSalaryByCompany(anyString());
    }

    @Test
    void getCompanyStatisticsDTO_WithValidCompany_ShouldReturnDTO() {
        // Given
        CompanyStatistics companyStats = new CompanyStatistics("TechCorp", 5, 8000.0, 12000.0);
        companyStats.setHighestPaidEmployee("Jan Manager");
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
        Map<Position, Long> positionCounts = new HashMap<>();
        positionCounts.put(Position.PROGRAMMER, 3L);
        positionCounts.put(Position.MANAGER, 1L);

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
        when(employeeService.countEmployeesByPosition()).thenReturn(new HashMap<>());

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
        Map<EmploymentStatus, Long> statusDistribution = new HashMap<>();
        statusDistribution.put(EmploymentStatus.ACTIVE, 8L);
        statusDistribution.put(EmploymentStatus.ON_LEAVE, 2L);

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
        when(employeeService.getEmploymentStatusDistribution()).thenReturn(new HashMap<>());

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

        Department dept = createDepartment(1L, "IT", "Warszawa", "Dział IT", "manager@techcorp.com", 100000.0);
        List<Department> departments = Arrays.asList(dept);

        CompanyStatistics techCorpStats = new CompanyStatistics("TechCorp", 1, 5000.0, 5000.0);
        techCorpStats.setHighestPaidEmployee("Jan Kowalski");

        CompanyStatistics otherCorpStats = new CompanyStatistics("OtherCorp", 1, 8000.0, 8000.0);
        otherCorpStats.setHighestPaidEmployee("Anna Nowak");

        when(employeeService.getAllEmployees()).thenReturn(employees);
        when(departmentService.getAllDepartments()).thenReturn(departments);
        when(employeeService.getCompanyStatistics("TechCorp")).thenReturn(techCorpStats);
        when(employeeService.getCompanyStatistics("OtherCorp")).thenReturn(otherCorpStats);

        // When
        Map<String, Object> statistics = statisticsService.getAllStatistics();

        // Then
        assertNotNull(statistics);
        assertEquals(2, statistics.get("totalEmployees"));
        assertEquals(6500.0, statistics.get("avgSalary")); // (5000 + 8000) / 2
        assertEquals(1, statistics.get("totalDepartments"));
        assertEquals(100000.0, statistics.get("totalBudget"));

        @SuppressWarnings("unchecked")
        Map<String, CompanyStatistics> companyStats = (Map<String, CompanyStatistics>) statistics.get("companyStats");

        assertEquals(2, companyStats.size());
        assertTrue(companyStats.containsKey("TechCorp"));
        assertTrue(companyStats.containsKey("OtherCorp"));
        assertEquals(5000.0, companyStats.get("TechCorp").getAverageSalary());
        assertEquals(8000.0, companyStats.get("OtherCorp").getAverageSalary());
        assertEquals("Jan Kowalski", companyStats.get("TechCorp").getHighestPaidEmployee());
        assertEquals("Anna Nowak", companyStats.get("OtherCorp").getHighestPaidEmployee());

        @SuppressWarnings("unchecked")
        Map<String, Long> positionDistribution = (Map<String, Long>) statistics.get("positionDistribution");
        assertEquals(1, positionDistribution.get("PROGRAMMER"));
        assertEquals(1, positionDistribution.get("MANAGER"));

        verify(employeeService, times(1)).getAllEmployees();
        verify(departmentService, times(1)).getAllDepartments();
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

        Department dept = createDepartment(1L, "IT", "Warszawa", "Dział IT", "manager@techcorp.com", 0.0);
        List<Department> departments = Arrays.asList(dept);

        CompanyStatistics companyStats = new CompanyStatistics("TechCorp", 2, 0.0, 0.0);

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

        @SuppressWarnings("unchecked")
        Map<String, CompanyStatistics> companyStatsResult = (Map<String, CompanyStatistics>) statistics.get("companyStats");
        assertEquals("", companyStatsResult.get("TechCorp").getHighestPaidEmployee());

        @SuppressWarnings("unchecked")
        Map<String, Long> positionDistribution = (Map<String, Long>) statistics.get("positionDistribution");
        assertEquals(1, positionDistribution.get("PROGRAMMER"));
        assertEquals(1, positionDistribution.get("MANAGER"));
    }

    @Test
    void getAllStatistics_WithSingleEmployee_ShouldCalculateCorrectly() {
        // Given
        Employee emp = createEmployee("Jan Kowalski", "jan@techcorp.com", "TechCorp",
                Position.PROGRAMMER, 5000.0, EmploymentStatus.ACTIVE);
        List<Employee> employees = Arrays.asList(emp);

        Department dept = createDepartment(1L, "IT", "Warszawa", "Dział IT", "manager@techcorp.com", 50000.0);
        List<Department> departments = Arrays.asList(dept);

        CompanyStatistics companyStats = new CompanyStatistics("TechCorp", 1, 5000.0, 5000.0);
        companyStats.setHighestPaidEmployee("Jan Kowalski");

        when(employeeService.getAllEmployees()).thenReturn(employees);
        when(departmentService.getAllDepartments()).thenReturn(departments);
        when(employeeService.getCompanyStatistics("TechCorp")).thenReturn(companyStats);

        // When
        Map<String, Object> statistics = statisticsService.getAllStatistics();

        // Then
        assertNotNull(statistics);
        assertEquals(1, statistics.get("totalEmployees"));
        assertEquals(5000.0, statistics.get("avgSalary"));
        assertEquals(1, statistics.get("totalDepartments"));
        assertEquals(50000.0, statistics.get("totalBudget"));

        @SuppressWarnings("unchecked")
        Map<String, CompanyStatistics> companyStatsResult = (Map<String, CompanyStatistics>) statistics.get("companyStats");
        assertEquals("TechCorp", companyStatsResult.get("TechCorp").getCompanyName());
        assertEquals("Jan Kowalski", companyStatsResult.get("TechCorp").getHighestPaidEmployee());

        @SuppressWarnings("unchecked")
        Map<String, Long> positionDistribution = (Map<String, Long>) statistics.get("positionDistribution");
        assertEquals(1, positionDistribution.get("PROGRAMMER"));
    }

    @Test
    void getAverageSalary_WithMultipleCompanies_ShouldHandleNullCompany() {
        // Given
        when(employeeService.calculateAverageSalary()).thenReturn(7000.0);

        // When
        Map<String, Double> result = statisticsService.getAverageSalary(null);

        // Then
        assertNotNull(result);
        assertEquals(7000.0, result.get("averageSalary"));
        verify(employeeService, times(1)).calculateAverageSalary();
        verify(employeeService, never()).calculateAverageSalaryByCompany(anyString());
    }

    @Test
    void getAverageSalary_WithMultipleCompanies_ShouldHandleEmptyString() {
        // Given
        when(employeeService.calculateAverageSalary()).thenReturn(7500.0);

        // When
        Map<String, Double> result = statisticsService.getAverageSalary("");

        // Then
        assertNotNull(result);
        assertEquals(7500.0, result.get("averageSalary"));
        verify(employeeService, times(1)).calculateAverageSalary();
        verify(employeeService, never()).calculateAverageSalaryByCompany(anyString());
    }

    @Test
    void getCompanyStatisticsDTO_WithNoHighestPaidEmployee_ShouldReturnNone() {
        // Given
        CompanyStatistics companyStats = new CompanyStatistics("TechCorp", 10, 7000.0, 10000.0);
        // highestPaidEmployee pozostaje puste
        when(employeeService.getCompanyStatistics("TechCorp")).thenReturn(companyStats);
        when(employeeService.findHighestSalaryByCompany("TechCorp")).thenReturn(10000.0);

        // When
        CompanyStatisticsDTO result = statisticsService.getCompanyStatisticsDTO("TechCorp");

        // Then
        assertNotNull(result);
        assertEquals("TechCorp", result.getCompanyName());
        assertEquals(10, result.getEmployeeCount());
        assertEquals(7000.0, result.getAverageSalary());
        assertEquals(10000.0, result.getHighestSalary());
        assertEquals("", result.getTopEarnerName()); // puste zamiast "None"
        verify(employeeService, times(1)).getCompanyStatistics("TechCorp");
        verify(employeeService, times(1)).findHighestSalaryByCompany("TechCorp");
    }

    @Test
    void getPositionStatistics_WithAllPositions_ShouldReturnCompleteMap() {
        // Given
        Map<Position, Long> positionCounts = new HashMap<>();
        for (Position position : Position.values()) {
            positionCounts.put(position, 2L); // 2 pracowników na każde stanowisko
        }

        when(employeeService.countEmployeesByPosition()).thenReturn(positionCounts);

        // When
        Map<String, Integer> result = statisticsService.getPositionStatistics();

        // Then
        assertNotNull(result);
        assertEquals(Position.values().length, result.size());
        for (Position position : Position.values()) {
            assertEquals(2, result.get(position.name()));
        }
        verify(employeeService, times(1)).countEmployeesByPosition();
    }

    @Test
    void getEmploymentStatusStatistics_WithAllStatuses_ShouldReturnCompleteMap() {
        // Given
        Map<EmploymentStatus, Long> statusDistribution = new HashMap<>();
        for (EmploymentStatus status : EmploymentStatus.values()) {
            statusDistribution.put(status, 3L); // 3 pracowników w każdym statusie
        }

        when(employeeService.getEmploymentStatusDistribution()).thenReturn(statusDistribution);

        // When
        Map<String, Integer> result = statisticsService.getEmploymentStatusStatistics();

        // Then
        assertNotNull(result);
        assertEquals(EmploymentStatus.values().length, result.size());
        for (EmploymentStatus status : EmploymentStatus.values()) {
            assertEquals(3, result.get(status.name()));
        }
        verify(employeeService, times(1)).getEmploymentStatusDistribution();
    }

    @Test
    void getAllStatistics_WithNoDepartments_ShouldCalculateCorrectly() {
        // Given
        Employee emp1 = createEmployee("Jan Kowalski", "jan@techcorp.com", "TechCorp",
                Position.PROGRAMMER, 5000.0, EmploymentStatus.ACTIVE);
        List<Employee> employees = Arrays.asList(emp1);

        List<Department> departments = List.of();

        CompanyStatistics companyStats = new CompanyStatistics("TechCorp", 1, 5000.0, 5000.0);
        companyStats.setHighestPaidEmployee("Jan Kowalski");

        when(employeeService.getAllEmployees()).thenReturn(employees);
        when(departmentService.getAllDepartments()).thenReturn(departments);
        when(employeeService.getCompanyStatistics("TechCorp")).thenReturn(companyStats);

        // When
        Map<String, Object> statistics = statisticsService.getAllStatistics();

        // Then
        assertNotNull(statistics);
        assertEquals(1, statistics.get("totalEmployees"));
        assertEquals(5000.0, statistics.get("avgSalary"));
        assertEquals(0, statistics.get("totalDepartments"));
        assertEquals(0.0, statistics.get("totalBudget"));

        @SuppressWarnings("unchecked")
        Map<String, CompanyStatistics> companyStatsResult = (Map<String, CompanyStatistics>) statistics.get("companyStats");
        assertEquals("Jan Kowalski", companyStatsResult.get("TechCorp").getHighestPaidEmployee());
    }

    @Test
    void getAllStatistics_WithMixedData_ShouldCalculateCorrectly() {
        // Given
        Employee emp1 = createEmployee("Jan", "jan@a.com", "CompanyA",
                Position.PROGRAMMER, 3000.0, EmploymentStatus.ACTIVE);
        Employee emp2 = createEmployee("Anna", "anna@a.com", "CompanyA",
                Position.MANAGER, 7000.0, EmploymentStatus.ON_LEAVE);
        Employee emp3 = createEmployee("Piotr", "piotr@b.com", "CompanyB",
                Position.VICE_PRESIDENT, 15000.0, EmploymentStatus.ACTIVE);
        List<Employee> employees = Arrays.asList(emp1, emp2, emp3);

        Department dept1 = createDepartment(1L, "IT", "Warszawa", "IT Dept", "manager@a.com", 50000.0);
        Department dept2 = createDepartment(2L, "HR", "Kraków", "HR Dept", "hr@a.com", 30000.0);
        List<Department> departments = Arrays.asList(dept1, dept2);

        CompanyStatistics companyAStats = new CompanyStatistics("CompanyA", 2, 5000.0, 7000.0);
        companyAStats.setHighestPaidEmployee("Anna");

        CompanyStatistics companyBStats = new CompanyStatistics("CompanyB", 1, 15000.0, 15000.0);
        companyBStats.setHighestPaidEmployee("Piotr");

        when(employeeService.getAllEmployees()).thenReturn(employees);
        when(departmentService.getAllDepartments()).thenReturn(departments);
        when(employeeService.getCompanyStatistics("CompanyA")).thenReturn(companyAStats);
        when(employeeService.getCompanyStatistics("CompanyB")).thenReturn(companyBStats);

        // When
        Map<String, Object> statistics = statisticsService.getAllStatistics();

        // Then
        assertNotNull(statistics);
        assertEquals(3, statistics.get("totalEmployees"));
        assertEquals(8333.33, (Double)statistics.get("avgSalary"), 0.01); // (3000+7000+15000)/3
        assertEquals(2, statistics.get("totalDepartments"));
        assertEquals(80000.0, statistics.get("totalBudget")); // 50000 + 30000

        @SuppressWarnings("unchecked")
        Map<String, CompanyStatistics> companyStats = (Map<String, CompanyStatistics>) statistics.get("companyStats");
        assertEquals(2, companyStats.size());
        assertEquals("Anna", companyStats.get("CompanyA").getHighestPaidEmployee());
        assertEquals("Piotr", companyStats.get("CompanyB").getHighestPaidEmployee());

        @SuppressWarnings("unchecked")
        Map<String, Long> positionDistribution = (Map<String, Long>) statistics.get("positionDistribution");
        assertEquals(1, positionDistribution.get("PROGRAMMER"));
        assertEquals(1, positionDistribution.get("MANAGER"));
        assertEquals(1, positionDistribution.get("VICE_PRESIDENT"));
    }

    // Pomocnicza metoda do tworzenia pracowników
    private Employee createEmployee(String name, String email, String company,
                                    Position position, double salary, EmploymentStatus status) {
        return new Employee(name, email, company, position, salary, status);
    }

    // Pomocnicza metoda do tworzenia departamentów
    private Department createDepartment(Long id, String name, String location,
                                        String description, String managerEmail, Double budget) {
        Department department = new Department(name, location, description, managerEmail, budget);
        department.setId(id);
        return department;
    }
}