package com.techcorp.employee.service;

import com.techcorp.employee.dto.EmployeeDTO;
import com.techcorp.employee.exception.InvalidDataException;
import com.techcorp.employee.model.Employee;
import com.techcorp.employee.model.Position;
import com.techcorp.employee.model.EmploymentStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
public class EmployeeFormServiceTest {

    @Mock
    private EmployeeService employeeService;

    private EmployeeFormService employeeFormService;

    @BeforeEach
    public void setUp() {
        employeeFormService = new EmployeeFormService(employeeService);
    }

    @Test
    public void testGetFormData() {
        // When
        EmployeeFormService.EmployeeFormData formData = employeeFormService.getFormData();

        // Then
        assertNotNull(formData);
        assertNotNull(formData.getPositions());
        assertNotNull(formData.getStatuses());
        assertEquals(Position.values().length, formData.getPositions().size());
        assertEquals(EmploymentStatus.values().length, formData.getStatuses().size());
    }

    @Test
    public void testConvertToEntity() throws InvalidDataException {
        // Given
        EmployeeDTO dto = new EmployeeDTO();
        dto.setFirstName("Jan");
        dto.setLastName("Kowalski");
        dto.setEmail("jan.kowalski@techcorp.com");
        dto.setCompany("TechCorp");
        dto.setPosition(Position.PROGRAMMER);
        dto.setSalary(5000.0);
        dto.setStatus(EmploymentStatus.ACTIVE);
        dto.setDepartmentId(1L);

        // When
        Employee employee = employeeFormService.convertToEntity(dto);

        // Then
        assertNotNull(employee);
        assertEquals("Jan Kowalski", employee.getName());
        assertEquals("jan.kowalski@techcorp.com", employee.getEmail());
        assertEquals("TechCorp", employee.getCompany());
        assertEquals(Position.PROGRAMMER, employee.getPosition());
        assertEquals(5000.0, employee.getSalary());
        assertEquals(EmploymentStatus.ACTIVE, employee.getStatus());
        assertEquals(1L, employee.getDepartmentId());
    }

    @Test
    public void testConvertToEntity_NoLastName() throws InvalidDataException {
        // Given
        EmployeeDTO dto = new EmployeeDTO();
        dto.setFirstName("Jan");
        dto.setLastName("");
        dto.setEmail("jan@techcorp.com");
        dto.setCompany("TechCorp");
        dto.setPosition(Position.PROGRAMMER);
        dto.setSalary(5000.0);
        dto.setStatus(EmploymentStatus.ACTIVE);

        // When
        Employee employee = employeeFormService.convertToEntity(dto);

        // Then
        assertNotNull(employee);
        assertEquals("Jan", employee.getName());
        assertEquals("jan@techcorp.com", employee.getEmail());
    }

    @Test
    public void testConvertToDTO() throws InvalidDataException {
        // Given
        Employee employee = new Employee("Jan Kowalski", "jan.kowalski@techcorp.com",
                "TechCorp", Position.PROGRAMMER, 5000.0, EmploymentStatus.ACTIVE);
        employee.setDepartmentId(1L);

        // When
        EmployeeDTO dto = employeeFormService.convertToDTO(employee);

        // Then
        assertNotNull(dto);
        assertEquals("Jan", dto.getFirstName());
        assertEquals("Kowalski", dto.getLastName());
        assertEquals("jan.kowalski@techcorp.com", dto.getEmail());
        assertEquals("TechCorp", dto.getCompany());
        assertEquals(Position.PROGRAMMER, dto.getPosition());
        assertEquals(5000.0, dto.getSalary());
        assertEquals(EmploymentStatus.ACTIVE, dto.getStatus());
        assertEquals(1L, dto.getDepartmentId());
    }

    @Test
    public void testConvertToDTO_SingleName() throws InvalidDataException {
        // Given
        Employee employee = new Employee("Jan", "jan@techcorp.com",
                "TechCorp", Position.PROGRAMMER, 5000.0, EmploymentStatus.ACTIVE);

        // When
        EmployeeDTO dto = employeeFormService.convertToDTO(employee);

        // Then
        assertNotNull(dto);
        assertEquals("Jan", dto.getFirstName());
        assertEquals("", dto.getLastName());
    }

    @Test
    public void testValidateEmployee_Valid() {
        // Given
        EmployeeDTO dto = new EmployeeDTO();
        dto.setSalary(5000.0);

        // When
        EmployeeFormService.FormValidationResult result = employeeFormService.validateEmployee(dto);

        // Then
        assertTrue(result.isValid());
        assertNull(result.getField());
        assertNull(result.getMessage());
    }

    @Test
    public void testValidateEmployee_InvalidSalary() {
        // Given
        EmployeeDTO dto = new EmployeeDTO();
        dto.setSalary(0.0);

        // When
        EmployeeFormService.FormValidationResult result = employeeFormService.validateEmployee(dto);

        // Then
        assertFalse(result.isValid());
        assertEquals("salary", result.getField());
        assertEquals("Wynagrodzenie musi być większe niż 0", result.getMessage());
    }

    @Test
    public void testValidateEmployee_NullSalary() {
        // Given
        EmployeeDTO dto = new EmployeeDTO();
        dto.setSalary(null);

        // When
        EmployeeFormService.FormValidationResult result = employeeFormService.validateEmployee(dto);

        // Then
        assertFalse(result.isValid());
        assertEquals("salary", result.getField());
        assertEquals("Wynagrodzenie musi być większe niż 0", result.getMessage());
    }

    @Test
    public void testValidateEmployee_NegativeSalary() {
        // Given
        EmployeeDTO dto = new EmployeeDTO();
        dto.setSalary(-1000.0);

        // When
        EmployeeFormService.FormValidationResult result = employeeFormService.validateEmployee(dto);

        // Then
        assertFalse(result.isValid());
        assertEquals("salary", result.getField());
        assertEquals("Wynagrodzenie musi być większe niż 0", result.getMessage());
    }
}