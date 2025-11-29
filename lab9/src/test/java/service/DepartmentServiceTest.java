package com.techcorp.employee.service;

import com.techcorp.employee.dto.DepartmentDTO;
import com.techcorp.employee.exception.InvalidDataException;
import com.techcorp.employee.model.Department;
import com.techcorp.employee.model.Employee;
import com.techcorp.employee.model.Position;
import com.techcorp.employee.model.EmploymentStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class DepartmentServiceTest {

    @Mock
    private EmployeeService employeeService;

    private DepartmentService departmentService;

    @BeforeEach
    public void setUp() {
        departmentService = new DepartmentService(employeeService);
    }

    @Test
    public void testCreateDepartment() {
        // Given
        Department department = new Department(null, "IT", "Warszawa", "Dział IT", "manager@techcorp.com", 100000.0);

        // When
        Department result = departmentService.createDepartment(department);

        // Then
        assertNotNull(result);
        assertNotNull(result.getId());
        assertEquals("IT", result.getName());
        assertEquals("Warszawa", result.getLocation());
        assertEquals("Dział IT", result.getDescription());
        assertEquals("manager@techcorp.com", result.getManagerEmail());
        assertEquals(100000.0, result.getBudget());
    }

    @Test
    public void testGetAllDepartments() {
        // Given
        Department dept1 = new Department(1L, "IT", "Warszawa", "Dział IT", "manager@techcorp.com", 100000.0);
        Department dept2 = new Department(2L, "HR", "Kraków", "Dział HR", "hr@techcorp.com", 50000.0);

        departmentService.createDepartment(dept1);
        departmentService.createDepartment(dept2);

        // When
        List<Department> result = departmentService.getAllDepartments();

        // Then
        assertNotNull(result);
        assertEquals(2, result.size());
        assertTrue(result.stream().anyMatch(dept -> "IT".equals(dept.getName())));
        assertTrue(result.stream().anyMatch(dept -> "HR".equals(dept.getName())));
    }

    @Test
    public void testGetDepartmentById_Found() {
        // Given
        Department department = new Department(null, "IT", "Warszawa", "Dział IT", "manager@techcorp.com", 100000.0);
        Department created = departmentService.createDepartment(department);
        Long departmentId = created.getId();

        // When
        Optional<Department> result = departmentService.getDepartmentById(departmentId);

        // Then
        assertTrue(result.isPresent());
        assertEquals("IT", result.get().getName());
        assertEquals(departmentId, result.get().getId());
    }

    @Test
    public void testGetDepartmentById_NotFound() {
        // When
        Optional<Department> result = departmentService.getDepartmentById(999L);

        // Then
        assertFalse(result.isPresent());
    }

    @Test
    public void testUpdateDepartment_Success() {
        // Given
        Department department = new Department(null, "IT", "Warszawa", "Dział IT", "manager@techcorp.com", 100000.0);
        Department created = departmentService.createDepartment(department);
        Long departmentId = created.getId();

        Department updatedData = new Department(null, "IT Updated", "Kraków", "Zaktualizowany dział", "newmanager@techcorp.com", 120000.0);

        // When
        Department result = departmentService.updateDepartment(departmentId, updatedData);

        // Then
        assertNotNull(result);
        assertEquals(departmentId, result.getId());
        assertEquals("IT Updated", result.getName());
        assertEquals("Kraków", result.getLocation());
        assertEquals("Zaktualizowany dział", result.getDescription());
        assertEquals("newmanager@techcorp.com", result.getManagerEmail());
        assertEquals(120000.0, result.getBudget());
    }

    @Test
    public void testUpdateDepartment_NotFound() {
        // Given
        Department updatedData = new Department(null, "IT Updated", "Kraków", "Zaktualizowany dział", "newmanager@techcorp.com", 120000.0);

        // When
        Department result = departmentService.updateDepartment(999L, updatedData);

        // Then
        assertNull(result);
    }

    @Test
    public void testDeleteDepartment_Success() {
        // Given
        Department department = new Department(null, "IT", "Warszawa", "Dział IT", "manager@techcorp.com", 100000.0);
        Department created = departmentService.createDepartment(department);
        Long departmentId = created.getId();

        // When
        boolean result = departmentService.deleteDepartment(departmentId);

        // Then
        assertTrue(result);
        assertFalse(departmentService.getDepartmentById(departmentId).isPresent());
    }

    @Test
    public void testDeleteDepartment_NotFound() {
        // When
        boolean result = departmentService.deleteDepartment(999L);

        // Then
        assertFalse(result);
    }

    @Test
    public void testGetDepartmentCount() {
        // Given
        departmentService.createDepartment(new Department(null, "IT", "Warszawa", "Dział IT", "manager@techcorp.com", 100000.0));
        departmentService.createDepartment(new Department(null, "HR", "Kraków", "Dział HR", "hr@techcorp.com", 50000.0));

        // When
        int count = departmentService.getDepartmentCount();

        // Then
        assertEquals(2, count);
    }

    @Test
    public void testGetDepartmentsByManager() {
        // Given
        Department dept1 = new Department(null, "IT", "Warszawa", "Dział IT", "manager@techcorp.com", 100000.0);
        Department dept2 = new Department(null, "HR", "Kraków", "Dział HR", "hr@techcorp.com", 50000.0);
        Department dept3 = new Department(null, "Finance", "Warszawa", "Dział Finansów", "manager@techcorp.com", 80000.0);

        departmentService.createDepartment(dept1);
        departmentService.createDepartment(dept2);
        departmentService.createDepartment(dept3);

        // When
        List<Department> result = departmentService.getDepartmentsByManager("manager@techcorp.com");

        // Then
        assertNotNull(result);
        assertEquals(2, result.size());
        assertTrue(result.stream().allMatch(dept -> "manager@techcorp.com".equals(dept.getManagerEmail())));
    }

    @Test
    public void testGetDepartmentDetails_Success() throws InvalidDataException {
        // Given
        Department department = new Department(null, "IT", "Warszawa", "Dział IT", "manager@techcorp.com", 100000.0);
        Department created = departmentService.createDepartment(department);
        Long departmentId = created.getId();

        Employee manager = new Employee("Jan Manager", "manager@techcorp.com", "TechCorp",
                Position.MANAGER, 8000.0, EmploymentStatus.ACTIVE);
        Employee employee1 = new Employee("Jan Developer", "dev1@techcorp.com", "TechCorp",
                Position.PROGRAMMER, 5000.0, EmploymentStatus.ACTIVE);
        employee1.setDepartmentId(departmentId);
        Employee employee2 = new Employee("Anna Tester", "tester@techcorp.com", "TechCorp",
                Position.MANAGER, 4500.0, EmploymentStatus.ACTIVE);
        employee2.setDepartmentId(departmentId);

        List<Employee> departmentEmployees = Arrays.asList(employee1, employee2);

        when(employeeService.getEmployeesByDepartmentId(departmentId)).thenReturn(departmentEmployees);
        when(employeeService.findEmployeeByEmail("manager@techcorp.com")).thenReturn(Optional.of(manager));

        // When
        DepartmentDTO result = departmentService.getDepartmentDetails(departmentId);

        // Then
        assertNotNull(result);
        assertEquals(departmentId, result.getDepartment().getId());
        assertEquals("IT", result.getDepartment().getName());
        assertEquals(2, result.getEmployees().size());
        assertTrue(result.getManager().isPresent());
        assertEquals("Jan Manager", result.getManager().get().getName());

        verify(employeeService, times(1)).getEmployeesByDepartmentId(departmentId);
        verify(employeeService, times(1)).findEmployeeByEmail("manager@techcorp.com");
    }

    @Test
    public void testGetDepartmentDetails_DepartmentNotFound() {
        // When
        DepartmentDTO result = departmentService.getDepartmentDetails(999L);

        // Then
        assertNull(result);
        verify(employeeService, never()).getEmployeesByDepartmentId(anyLong());
        verify(employeeService, never()).findEmployeeByEmail(anyString());
    }

    @Test
    public void testGetDepartmentDetails_ManagerNotFound() throws InvalidDataException {
        // Given
        Department department = new Department(null, "IT", "Warszawa", "Dział IT", "nonexistent@techcorp.com", 100000.0);
        Department created = departmentService.createDepartment(department);
        Long departmentId = created.getId();

        Employee employee = new Employee("Jan Developer", "dev@techcorp.com", "TechCorp",
                Position.PROGRAMMER, 5000.0, EmploymentStatus.ACTIVE);
        employee.setDepartmentId(departmentId);

        when(employeeService.getEmployeesByDepartmentId(departmentId)).thenReturn(List.of(employee));
        when(employeeService.findEmployeeByEmail("nonexistent@techcorp.com")).thenReturn(Optional.empty());

        // When
        DepartmentDTO result = departmentService.getDepartmentDetails(departmentId);

        // Then
        assertNotNull(result);
        assertFalse(result.getManager().isPresent());
        assertEquals(1, result.getEmployees().size());

        verify(employeeService, times(1)).getEmployeesByDepartmentId(departmentId);
        verify(employeeService, times(1)).findEmployeeByEmail("nonexistent@techcorp.com");
    }

    @Test
    public void testGetDepartmentDetails_NoManagerEmail() throws InvalidDataException {
        // Given
        Department department = new Department(null, "IT", "Warszawa", "Dział IT", null, 100000.0);
        Department created = departmentService.createDepartment(department);
        Long departmentId = created.getId();

        Employee employee = new Employee("Jan Developer", "dev@techcorp.com", "TechCorp",
                Position.PROGRAMMER, 5000.0, EmploymentStatus.ACTIVE);
        employee.setDepartmentId(departmentId);

        when(employeeService.getEmployeesByDepartmentId(departmentId)).thenReturn(List.of(employee));

        // When
        DepartmentDTO result = departmentService.getDepartmentDetails(departmentId);

        // Then
        assertNotNull(result);
        assertFalse(result.getManager().isPresent());
        assertEquals(1, result.getEmployees().size());

        verify(employeeService, times(1)).getEmployeesByDepartmentId(departmentId);
        verify(employeeService, never()).findEmployeeByEmail(anyString());
    }
}