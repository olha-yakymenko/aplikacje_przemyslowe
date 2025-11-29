package com.techcorp.employee.service;

import com.techcorp.employee.dao.EmployeeDAO;
import com.techcorp.employee.exception.*;
import com.techcorp.employee.model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmployeeServiceUnitTest {

    @Mock
    private EmployeeDAO employeeDAO;

    @Mock
    private FileStorageService fileStorageService;

    @InjectMocks
    private EmployeeService employeeService;

    @Captor
    private ArgumentCaptor<Employee> employeeCaptor;

    private Employee testEmployee;

    @BeforeEach
    void setUp() {
        testEmployee = new Employee();
        testEmployee.setId(1L);
        testEmployee.setName("Jan Kowalski");
        testEmployee.setEmail("jan.kowalski@techcorp.com");
        testEmployee.setCompany("TechCorp");
        testEmployee.setPosition(Position.PROGRAMMER);
        testEmployee.setSalary(8000.0);
        testEmployee.setStatus(EmploymentStatus.ACTIVE);
    }

    @Test
    void shouldAddEmployeeSuccessfully() throws InvalidDataException {
        // Given
        when(employeeDAO.existsByEmail("jan.kowalski@techcorp.com")).thenReturn(false);
        doNothing().when(employeeDAO).save(any(Employee.class));

        // When
        boolean result = employeeService.addEmployee(testEmployee);

        // Then
        assertTrue(result);
        verify(employeeDAO).save(testEmployee);
    }

    @Test
    void shouldThrowExceptionWhenAddingDuplicateEmployee() {
        // Given
        when(employeeDAO.existsByEmail("jan.kowalski@techcorp.com")).thenReturn(true);

        // When & Then
        assertThrows(DuplicateEmailException.class, () -> {
            employeeService.addEmployee(testEmployee);
        });
    }

    @Test
    void shouldFindEmployeeByEmail() {
        // Given
        when(employeeDAO.findByEmail("jan.kowalski@techcorp.com")).thenReturn(Optional.of(testEmployee));

        // When
        Optional<Employee> found = employeeService.findEmployeeByEmail("jan.kowalski@techcorp.com");

        // Then
        assertTrue(found.isPresent());
        assertEquals("jan.kowalski@techcorp.com", found.get().getEmail());
    }

    @Test
    void shouldRemoveEmployee() {
        // Given
        when(employeeDAO.existsByEmail("jan.kowalski@techcorp.com")).thenReturn(true);

        // When
        boolean result = employeeService.removeEmployee("jan.kowalski@techcorp.com");

        // Then
        assertTrue(result);
        verify(employeeDAO).deleteByEmail("jan.kowalski@techcorp.com");
    }

    @Test
    void shouldReturnFalseWhenRemovingNonExistentEmployee() {
        // Given
        when(employeeDAO.existsByEmail("nonexistent@techcorp.com")).thenReturn(false);

        // When
        boolean result = employeeService.removeEmployee("nonexistent@techcorp.com");

        // Then
        assertFalse(result);
        verify(employeeDAO, never()).deleteByEmail(anyString());
    }

    @Test
    void shouldGetAllEmployees() {
        // Given
        List<Employee> employees = Arrays.asList(testEmployee);
        when(employeeDAO.findAll()).thenReturn(employees);

        // When
        List<Employee> result = employeeService.getAllEmployees();

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getEmail()).isEqualTo("jan.kowalski@techcorp.com");
    }

    @Test
    void shouldUpdateEmployeeStatus() {
        // Given
        when(employeeDAO.findByEmail("jan.kowalski@techcorp.com")).thenReturn(Optional.of(testEmployee));
        doNothing().when(employeeDAO).save(any(Employee.class));

        // When
        Employee updated = employeeService.updateEmployeeStatus("jan.kowalski@techcorp.com", EmploymentStatus.ON_LEAVE);

        // Then
        verify(employeeDAO).save(employeeCaptor.capture());
        assertEquals(EmploymentStatus.ON_LEAVE, employeeCaptor.getValue().getStatus());
        assertEquals(testEmployee, updated); // Zwraca ten sam obiekt
    }

    @Test
    void shouldThrowExceptionWhenUpdatingNonExistentEmployeeStatus() {
        // Given
        when(employeeDAO.findByEmail("nonexistent@techcorp.com")).thenReturn(Optional.empty());

        // When & Then
        assertThrows(EmployeeNotFoundException.class, () -> {
            employeeService.updateEmployeeStatus("nonexistent@techcorp.com", EmploymentStatus.ACTIVE);
        });
    }

    @Test
    void shouldGetEmployeesByCompany() {
        // Given
        List<Employee> employees = Arrays.asList(testEmployee);
        when(employeeDAO.findByCompany("TechCorp")).thenReturn(employees);

        // When
        List<Employee> result = employeeService.getEmployeesByCompany("TechCorp");

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getCompany()).isEqualTo("TechCorp");
    }

    @Test
    void shouldGetEmployeesWithoutDepartment() {
        // Given
        List<Employee> employees = Arrays.asList(testEmployee);
        when(employeeDAO.findEmployeesWithoutDepartment()).thenReturn(employees);

        // When
        List<Employee> result = employeeService.getEmployeesWithoutDepartment();

        // Then
        assertThat(result).hasSize(1);
        verify(employeeDAO).findEmployeesWithoutDepartment();
    }

    @Test
    void shouldAssignEmployeeToDepartment() {
        // Given
        when(employeeDAO.findByEmail("jan.kowalski@techcorp.com")).thenReturn(Optional.of(testEmployee));
        doNothing().when(employeeDAO).save(any(Employee.class));

        // When
        boolean result = employeeService.assignEmployeeToDepartment("jan.kowalski@techcorp.com", 1L);

        // Then
        assertTrue(result);
        verify(employeeDAO).save(employeeCaptor.capture());
        assertEquals(1L, employeeCaptor.getValue().getDepartmentId());
    }

    @Test
    void shouldRemoveEmployeeFromDepartment() {
        // Given
        testEmployee.setDepartmentId(1L);
        when(employeeDAO.findByEmail("jan.kowalski@techcorp.com")).thenReturn(Optional.of(testEmployee));
        doNothing().when(employeeDAO).save(any(Employee.class));

        // When
        boolean result = employeeService.removeEmployeeFromDepartment("jan.kowalski@techcorp.com");

        // Then
        assertTrue(result);
        verify(employeeDAO).save(employeeCaptor.capture());
        assertNull(employeeCaptor.getValue().getDepartmentId());
    }

    @Test
    void shouldGetCompanyStatisticsFromDAO() {
        // Given
        CompanyStatistics stats1 = new CompanyStatistics("TechCorp", 5, 6000.0, 8000.0);
        stats1.setHighestPaidEmployee("Anna Nowak");

        CompanyStatistics stats2 = new CompanyStatistics("OtherCorp", 3, 4500.0, 6000.0);
        stats2.setHighestPaidEmployee("Piotr Kowalski");

        List<CompanyStatistics> mockStats = Arrays.asList(stats1, stats2);
        when(employeeDAO.getCompanyStatistics()).thenReturn(mockStats);

        // When
        Map<String, CompanyStatistics> result = employeeService.getCompanyStatistics();

        // Then
        assertThat(result).hasSize(2);
        assertThat(result).containsKeys("TechCorp", "OtherCorp");
        assertThat(result.get("TechCorp").getEmployeeCount()).isEqualTo(5);
        assertThat(result.get("OtherCorp").getEmployeeCount()).isEqualTo(3);
        verify(employeeDAO).getCompanyStatistics();
    }

    @Test
    void shouldGetSingleCompanyStatistics() {
        // Given
        Employee emp1 = new Employee();
        emp1.setName("Anna Nowak");
        emp1.setSalary(7000.0);

        Employee emp2 = new Employee();
        emp2.setName("Jan Kowalski");
        emp2.setSalary(5000.0);

        List<Employee> companyEmployees = Arrays.asList(emp1, emp2);
        when(employeeDAO.findByCompany("TechCorp")).thenReturn(companyEmployees);

        // When
        CompanyStatistics result = employeeService.getCompanyStatistics("TechCorp");

        // Then
        assertThat(result.getEmployeeCount()).isEqualTo(2);
        assertThat(result.getAverageSalary()).isEqualTo(6000.0);
        assertThat(result.getHighestPaidEmployee()).isEqualTo("Anna Nowak");
    }

    @Test
    void shouldUpdateEmployee() {
        // Given
        Employee updatedEmployee = new Employee();
        updatedEmployee.setEmail("jan.kowalski@techcorp.com");
        updatedEmployee.setName("Jan Nowak-Kowalski");
        updatedEmployee.setSalary(9000.0);

        when(employeeDAO.findByEmail("jan.kowalski@techcorp.com")).thenReturn(Optional.of(testEmployee));
        doNothing().when(employeeDAO).save(any(Employee.class));

        // When
        Employee result = employeeService.updateEmployee(updatedEmployee);

        // Then
        verify(employeeDAO).save(employeeCaptor.capture());
        assertEquals(1L, employeeCaptor.getValue().getId());
        assertEquals("Jan Nowak-Kowalski", employeeCaptor.getValue().getName());
        assertEquals(updatedEmployee, result); // Zwraca ten sam obiekt
    }

    @Test
    void shouldCalculateAverageSalary() {
        // Given
        Employee emp1 = new Employee();
        emp1.setSalary(4000.0);
        Employee emp2 = new Employee();
        emp2.setSalary(6000.0);

        List<Employee> employees = Arrays.asList(emp1, emp2);
        when(employeeDAO.findAll()).thenReturn(employees);

        // When
        OptionalDouble result = employeeService.calculateAverageSalary();

        // Then
        assertTrue(result.isPresent());
        assertEquals(5000.0, result.getAsDouble());
    }

    @Test
    void shouldReturnEmptyAverageSalaryForNoEmployees() {
        // Given
        when(employeeDAO.findAll()).thenReturn(List.of());

        // When
        OptionalDouble result = employeeService.calculateAverageSalary();

        // Then
        assertFalse(result.isPresent());
    }

    @Test
    void shouldFindHighestPaidEmployee() {
        // Given
        Employee lowPaid = new Employee();
        lowPaid.setSalary(3000.0);
        Employee highPaid = new Employee();
        highPaid.setSalary(7000.0);

        List<Employee> employees = Arrays.asList(lowPaid, highPaid);
        when(employeeDAO.findAll()).thenReturn(employees);

        // When
        Optional<Employee> result = employeeService.findHighestPaidEmployee();

        // Then
        assertTrue(result.isPresent());
        assertEquals(7000.0, result.get().getSalary());
    }

    @Test
    void shouldGetAvailableManagers() {
        // Given
        Employee manager = new Employee();
        manager.setPosition(Position.MANAGER);
        Employee programmer = new Employee();
        programmer.setPosition(Position.PROGRAMMER);
        Employee vp = new Employee();
        vp.setPosition(Position.VICE_PRESIDENT);
        Employee president = new Employee();
        president.setPosition(Position.PRESIDENT);
        Employee intern = new Employee();
        intern.setPosition(Position.INTERN);

        List<Employee> employees = Arrays.asList(manager, programmer, vp, president, intern);
        when(employeeDAO.findAll()).thenReturn(employees);

        // When
        List<Employee> result = employeeService.getAvailableManagers();

        // Then
        assertThat(result).hasSize(3);
        assertThat(result).extracting(Employee::getPosition)
                .containsExactlyInAnyOrder(Position.MANAGER, Position.VICE_PRESIDENT, Position.PRESIDENT);
    }

    @Test
    void shouldGetEmployeeCount() {
        // Given
        when(employeeDAO.findAll()).thenReturn(Arrays.asList(testEmployee, testEmployee));

        // When
        int count = employeeService.getEmployeeCount();

        // Then
        assertEquals(2, count);
    }

    @Test
    void shouldValidateEmail() {
        // When & Then
        assertThrows(IllegalArgumentException.class, () -> employeeService.findEmployeeByEmail(""));
        assertThrows(IllegalArgumentException.class, () -> employeeService.findEmployeeByEmail(null));
    }

    @Test
    void shouldValidateCompany() {
        // When & Then
        assertThrows(IllegalArgumentException.class, () -> employeeService.getEmployeesByCompany(""));
        assertThrows(IllegalArgumentException.class, () -> employeeService.getEmployeesByCompany(null));
    }

    @Test
    void shouldGetEmployeesByStatus() {
        // Given
        Employee active = new Employee();
        active.setStatus(EmploymentStatus.ACTIVE);
        Employee onLeave = new Employee();
        onLeave.setStatus(EmploymentStatus.ON_LEAVE);

        List<Employee> employees = Arrays.asList(active, onLeave);
        when(employeeDAO.findAll()).thenReturn(employees);

        // When
        List<Employee> result = employeeService.getEmployeesByStatus(EmploymentStatus.ACTIVE);

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getStatus()).isEqualTo(EmploymentStatus.ACTIVE);
    }

    @Test
    void shouldThrowExceptionForNullStatus() {
        assertThrows(IllegalArgumentException.class, () -> {
            employeeService.getEmployeesByStatus(null);
        });
    }

    @Test
    void shouldCalculateTotalSalaryCost() {
        // Given
        Employee emp1 = new Employee();
        emp1.setSalary(3000.0);
        Employee emp2 = new Employee();
        emp2.setSalary(5000.0);

        List<Employee> employees = Arrays.asList(emp1, emp2);
        when(employeeDAO.findAll()).thenReturn(employees);

        // When
        double result = employeeService.calculateTotalSalaryCost();

        // Then
        assertEquals(8000.0, result);
    }

    @Test
    void shouldGroupEmployeesByPosition() {
        // Given
        Employee programmer1 = new Employee();
        programmer1.setPosition(Position.PROGRAMMER);
        Employee programmer2 = new Employee();
        programmer2.setPosition(Position.PROGRAMMER);
        Employee manager = new Employee();
        manager.setPosition(Position.MANAGER);

        List<Employee> employees = Arrays.asList(programmer1, programmer2, manager);
        when(employeeDAO.findAll()).thenReturn(employees);

        // When
        Map<Position, List<Employee>> result = employeeService.groupEmployeesByPosition();

        // Then
        assertThat(result).hasSize(2);
        assertThat(result.get(Position.PROGRAMMER)).hasSize(2);
        assertThat(result.get(Position.MANAGER)).hasSize(1);
    }

    @Test
    void shouldCountEmployeesByPosition() {
        // Given
        Employee programmer1 = new Employee();
        programmer1.setPosition(Position.PROGRAMMER);
        Employee programmer2 = new Employee();
        programmer2.setPosition(Position.PROGRAMMER);
        Employee manager = new Employee();
        manager.setPosition(Position.MANAGER);

        List<Employee> employees = Arrays.asList(programmer1, programmer2, manager);
        when(employeeDAO.findAll()).thenReturn(employees);

        // When
        Map<Position, Long> result = employeeService.countEmployeesByPosition();

        // Then
        assertThat(result).hasSize(2);
        assertThat(result.get(Position.PROGRAMMER)).isEqualTo(2);
        assertThat(result.get(Position.MANAGER)).isEqualTo(1);
    }
}

@SpringBootTest
@Transactional
@Sql(scripts = "/schema.sql")
class EmployeeServiceIntegrationTest {

    @Autowired
    private EmployeeService employeeService;

    @Test
    void shouldPerformCompleteEmployeeLifecycle() throws Exception {
        // 1. CREATE
        Employee employee = new Employee();
        employee.setName("Jan Kowalski");
        employee.setEmail("jan.kowalski@techcorp.com");
        employee.setCompany("TechCorp");
        employee.setPosition(Position.PROGRAMMER);
        employee.setSalary(8000.0);
        employee.setStatus(EmploymentStatus.ACTIVE);

        boolean added = employeeService.addEmployee(employee);
        assertThat(added).isTrue();

        // 2. READ
        Optional<Employee> found = employeeService.findEmployeeByEmail("jan.kowalski@techcorp.com");
        assertThat(found).isPresent();
        assertThat(found.get().getName()).isEqualTo("Jan Kowalski");

        // 3. UPDATE
        Employee updatedEmployee = new Employee();
        updatedEmployee.setEmail("jan.kowalski@techcorp.com");
        updatedEmployee.setName("Jan Nowak-Kowalski");
        updatedEmployee.setCompany("TechCorp");
        updatedEmployee.setPosition(Position.PROGRAMMER);
        updatedEmployee.setSalary(9000.0);
        updatedEmployee.setStatus(EmploymentStatus.ACTIVE);

        Employee updated = employeeService.updateEmployee(updatedEmployee);
        assertThat(updated.getName()).isEqualTo("Jan Nowak-Kowalski");

        // 4. DELETE
        boolean deleted = employeeService.removeEmployee("jan.kowalski@techcorp.com");
        assertThat(deleted).isTrue();

        Optional<Employee> afterDelete = employeeService.findEmployeeByEmail("jan.kowalski@techcorp.com");
        assertThat(afterDelete).isEmpty();
    }

    @Test
    void shouldCalculateStatisticsFromDatabase() throws Exception {
        // Given
        createTestEmployees();

        // When
        Map<String, CompanyStatistics> statistics = employeeService.getCompanyStatistics();

        // Then
        assertThat(statistics).hasSize(2);

        CompanyStatistics techcorpStats = statistics.get("TechCorp");
        assertThat(techcorpStats.getEmployeeCount()).isEqualTo(2);
        assertThat(techcorpStats.getAverageSalary()).isEqualTo(7500.0);
        assertThat(techcorpStats.getMaxSalary()).isEqualTo(9000.0);

        CompanyStatistics othercorpStats = statistics.get("OtherCorp");
        assertThat(othercorpStats.getEmployeeCount()).isEqualTo(1);
        assertThat(othercorpStats.getAverageSalary()).isEqualTo(4000.0);
    }

    @Test
    void shouldHandleDepartmentOperations() throws Exception {
        // Given
        Employee employee = new Employee();
        employee.setName("Test Employee");
        employee.setEmail("test.department@techcorp.com");
        employee.setCompany("TechCorp");
        employee.setPosition(Position.PROGRAMMER);
        employee.setSalary(8000.0);
        employee.setStatus(EmploymentStatus.ACTIVE);
        employeeService.addEmployee(employee);

        // When - Assign to department
        boolean assigned = employeeService.assignEmployeeToDepartment("test.department@techcorp.com", 1L);
        assertThat(assigned).isTrue();

        // Then - Check department assignment
        List<Employee> departmentEmployees = employeeService.getEmployeesByDepartment(1L);
        assertThat(departmentEmployees).isNotEmpty();

        // When - Remove from department
        boolean removed = employeeService.removeEmployeeFromDepartment("test.department@techcorp.com");
        assertThat(removed).isTrue();

        // Then - Check removal
        List<Employee> withoutDepartment = employeeService.getEmployeesWithoutDepartment();
        assertThat(withoutDepartment).extracting(Employee::getEmail)
                .contains("test.department@techcorp.com");
    }

    @Test
    void shouldSortAndGroupEmployees() throws Exception {
        // Given
        createTestEmployees();

        // When
        List<Employee> sorted = employeeService.sortEmployeesByName();
        Map<Position, List<Employee>> byPosition = employeeService.groupEmployeesByPosition();
        Map<String, List<Employee>> byCompany = employeeService.groupEmployeesByCompany();

        // Then
        assertThat(sorted).isNotEmpty();

        assertThat(byPosition).containsKey(Position.PROGRAMMER);
        assertThat(byPosition).containsKey(Position.MANAGER);
        assertThat(byCompany).containsKeys("TechCorp", "OtherCorp");
    }

    @Test
    void shouldGetAvailableManagersFromDatabase() throws Exception {
        // Given
        Employee president = new Employee();
        president.setName("Prezes");
        president.setEmail("prezes@techcorp.com");
        president.setCompany("TechCorp");
        president.setPosition(Position.PRESIDENT);
        president.setSalary(25000.0);
        president.setStatus(EmploymentStatus.ACTIVE);
        employeeService.addEmployee(president);

        Employee vp = new Employee();
        vp.setName("Wiceprezes");
        vp.setEmail("wiceprezes@techcorp.com");
        vp.setCompany("TechCorp");
        vp.setPosition(Position.VICE_PRESIDENT);
        vp.setSalary(18000.0);
        vp.setStatus(EmploymentStatus.ACTIVE);
        employeeService.addEmployee(vp);

        Employee manager = new Employee();
        manager.setName("Manager");
        manager.setEmail("manager@techcorp.com");
        manager.setCompany("TechCorp");
        manager.setPosition(Position.MANAGER);
        manager.setSalary(12000.0);
        manager.setStatus(EmploymentStatus.ACTIVE);
        employeeService.addEmployee(manager);

        Employee programmer = new Employee();
        programmer.setName("Programista");
        programmer.setEmail("programista@techcorp.com");
        programmer.setCompany("TechCorp");
        programmer.setPosition(Position.PROGRAMMER);
        programmer.setSalary(8000.0);
        programmer.setStatus(EmploymentStatus.ACTIVE);
        employeeService.addEmployee(programmer);

        // When
        List<Employee> managers = employeeService.getAvailableManagers();

        // Then
        assertThat(managers).hasSize(3);
        assertThat(managers).extracting(Employee::getPosition)
                .containsExactlyInAnyOrder(Position.PRESIDENT, Position.VICE_PRESIDENT, Position.MANAGER);
    }

    private void createTestEmployees() throws Exception {
        Employee anna = new Employee();
        anna.setName("Anna Nowak");
        anna.setEmail("anna@techcorp.com");
        anna.setCompany("TechCorp");
        anna.setPosition(Position.MANAGER);
        anna.setSalary(9000.0);
        anna.setStatus(EmploymentStatus.ACTIVE);
        employeeService.addEmployee(anna);

        Employee jan = new Employee();
        jan.setName("Jan Kowalski");
        jan.setEmail("jan@techcorp.com");
        jan.setCompany("TechCorp");
        jan.setPosition(Position.PROGRAMMER);
        jan.setSalary(6000.0);
        jan.setStatus(EmploymentStatus.ACTIVE);
        employeeService.addEmployee(jan);

        Employee piotr = new Employee();
        piotr.setName("Piotr Wiśniewski");
        piotr.setEmail("piotr@othercorp.com");
        piotr.setCompany("OtherCorp");
        piotr.setPosition(Position.PROGRAMMER);
        piotr.setSalary(4000.0);
        piotr.setStatus(EmploymentStatus.ACTIVE);
        employeeService.addEmployee(piotr);
    }
}

@ExtendWith(MockitoExtension.class)
class EmployeeServiceExceptionTest {

    @Mock
    private EmployeeDAO employeeDAO;

    @Mock
    private FileStorageService fileStorageService;

    @InjectMocks
    private EmployeeService employeeService;

    @Test
    void shouldThrowInvalidDataExceptionForNullEmployee() {
        assertThrows(InvalidDataException.class, () -> {
            employeeService.addEmployee(null);
        });
    }

    @Test
    void shouldThrowDuplicateEmailException() {
        // Given
        Employee employee = new Employee();
        employee.setEmail("duplicate@techcorp.com");
        when(employeeDAO.existsByEmail("duplicate@techcorp.com")).thenReturn(true);

        // When & Then
        assertThrows(DuplicateEmailException.class, () -> {
            employeeService.addEmployee(employee);
        });
    }

    @Test
    void shouldThrowEmployeeNotFoundExceptionForNonExistentEmail() {
        // Given
        when(employeeDAO.findByEmail("nonexistent@techcorp.com")).thenReturn(Optional.empty());

        // When & Then
        assertThrows(EmployeeNotFoundException.class, () -> {
            employeeService.updateEmployeeStatus("nonexistent@techcorp.com", EmploymentStatus.ACTIVE);
        });
    }

    @Test
    void shouldThrowIllegalArgumentExceptionForInvalidEmail() {
        assertThrows(IllegalArgumentException.class, () -> {
            employeeService.findEmployeeByEmail("");
        });

        assertThrows(IllegalArgumentException.class, () -> {
            employeeService.findEmployeeByEmail(null);
        });
    }

    @Test
    void shouldThrowIllegalArgumentExceptionForInvalidCompany() {
        assertThrows(IllegalArgumentException.class, () -> {
            employeeService.getEmployeesByCompany("");
        });

        assertThrows(IllegalArgumentException.class, () -> {
            employeeService.getEmployeesByCompany(null);
        });
    }

    @Test
    void shouldThrowIllegalArgumentExceptionForNullStatus() {
        assertThrows(IllegalArgumentException.class, () -> {
            employeeService.getEmployeesByStatus(null);
        });
    }
}