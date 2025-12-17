package com.techcorp.employee.service;

import com.techcorp.employee.dto.EmployeeDTO;
import com.techcorp.employee.exception.InvalidDataException;
import com.techcorp.employee.model.Department;
import com.techcorp.employee.model.Employee;
import com.techcorp.employee.model.Position;
import com.techcorp.employee.model.EmploymentStatus;
import com.techcorp.employee.repository.DepartmentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static java.lang.Long.MAX_VALUE;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmployeeFormServiceTest {

    @Mock
    private EmployeeService employeeService;

    @Mock
    private DepartmentRepository departmentRepository;

    @InjectMocks
    private EmployeeFormService employeeFormService;

    private EmployeeDTO testEmployeeDTO;
    private Employee testEmployee;
    private Department testDepartment;

    @BeforeEach
    void setUp() {
        testDepartment = new Department("IT", "Warszawa", "Dział IT", "manager@example.com", 100000.0);
        testDepartment.setId(1L);

        testEmployeeDTO = new EmployeeDTO();
        testEmployeeDTO.setFirstName("Jan");
        testEmployeeDTO.setLastName("Kowalski");
        testEmployeeDTO.setEmail("jan@example.com");
        testEmployeeDTO.setCompany("TechCorp");
        testEmployeeDTO.setPosition(Position.PROGRAMMER);
        testEmployeeDTO.setSalary(new BigDecimal(5000));
        testEmployeeDTO.setStatus(EmploymentStatus.ACTIVE);
        testEmployeeDTO.setDepartmentId(null);

        testEmployee = new Employee(
                "Jan Kowalski",
                "jan@example.com",
                "TechCorp",
                Position.PROGRAMMER,
                new BigDecimal(5000),
                EmploymentStatus.ACTIVE
        );
        testEmployee.setId(1L);
        testEmployee.setDepartment(testDepartment);
    }

    // ===== TESTY GETFORMDATA =====

    @Test
    void getFormData_ShouldReturnAllPositionsAndStatuses() {
        // When
        EmployeeFormService.EmployeeFormData formData = employeeFormService.getFormData();

        // Then
        assertAll(
                () -> assertNotNull(formData),
                () -> assertNotNull(formData.getPositions()),
                () -> assertNotNull(formData.getStatuses()),
                () -> assertEquals(Arrays.asList(Position.values()), formData.getPositions()),
                () -> assertEquals(Arrays.asList(EmploymentStatus.values()), formData.getStatuses())
        );
    }

    // ===== TESTY CONVERTTOENTITY =====

    @Test
    void convertToEntity_ValidDTO_ShouldConvertSuccessfully() throws InvalidDataException {
        // Given
        testEmployeeDTO.setDepartmentId(1L);
        when(departmentRepository.findById(1L)).thenReturn(Optional.of(testDepartment));

        // When
        Employee result = employeeFormService.convertToEntity(testEmployeeDTO);

        // Then
        assertAll(
                () -> assertNotNull(result),
                () -> assertEquals("Jan Kowalski", result.getName()),
                () -> assertEquals("jan@example.com", result.getEmail()),
                () -> assertEquals("TechCorp", result.getCompany()),
                () -> assertEquals(Position.PROGRAMMER, result.getPosition()),
                () -> assertEquals(new BigDecimal(5000), result.getSalary(), String.valueOf(0.001)),
                () -> assertEquals(EmploymentStatus.ACTIVE, result.getStatus()),
                () -> assertEquals(testDepartment, result.getDepartment())
        );
        verify(departmentRepository, times(1)).findById(1L);
    }

    @Test
    void convertToEntity_DTONoDepartment_ShouldSetNullDepartment() throws InvalidDataException {
        // Given - departmentId już jest null w setup()

        // When
        Employee result = employeeFormService.convertToEntity(testEmployeeDTO);

        // Then
        assertAll(
                () -> assertNotNull(result),
                () -> assertEquals("Jan Kowalski", result.getName()),
                () -> assertNull(result.getDepartment())
        );
        verify(departmentRepository, never()).findById(anyLong());
    }

    @Test
    void convertToEntity_DepartmentNotFound_ShouldThrowException() {
        // Given
        testEmployeeDTO.setDepartmentId(999L);
        when(departmentRepository.findById(999L)).thenReturn(Optional.empty());

        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            employeeFormService.convertToEntity(testEmployeeDTO);
        });

        assertEquals("Department not found with ID: 999", exception.getMessage());
        verify(departmentRepository, times(1)).findById(999L);
    }

    @Test
    void convertToEntity_EmptyLastName_ShouldHandleCorrectly() throws InvalidDataException {
        // Given
        testEmployeeDTO.setFirstName("Jan");
        testEmployeeDTO.setLastName("");

        // When
        Employee result = employeeFormService.convertToEntity(testEmployeeDTO);

        // Then
        assertAll(
                () -> assertNotNull(result),
                () -> assertEquals("Jan", result.getName())
        );
    }

    @Test
    void convertToEntity_NullLastName_ShouldHandleCorrectly() throws InvalidDataException {
        // Given
        testEmployeeDTO.setFirstName("Jan");
        testEmployeeDTO.setLastName(null);

        // When
        Employee result = employeeFormService.convertToEntity(testEmployeeDTO);

        // Then
        assertAll(
                () -> assertNotNull(result),
                () -> assertEquals("Jan null", result.getName())
        );
    }

    // ===== TESTY CONVERTTODTO =====

    @Test
    void convertToDTO_ValidEmployee_ShouldConvertSuccessfully() {
        // When
        EmployeeDTO result = employeeFormService.convertToDTO(testEmployee);

        // Then
        assertAll(
                () -> assertNotNull(result),
                () -> assertEquals("Jan", result.getFirstName()),
                () -> assertEquals("Kowalski", result.getLastName()),
                () -> assertEquals("jan@example.com", result.getEmail()),
                () -> assertEquals("TechCorp", result.getCompany()),
                () -> assertEquals(Position.PROGRAMMER, result.getPosition()),
                () -> assertEquals(new BigDecimal(5000), result.getSalary(), String.valueOf(0.001)),
                () -> assertEquals(EmploymentStatus.ACTIVE, result.getStatus()),
                () -> assertEquals(1L, result.getDepartmentId())
        );
    }

    @Test
    void convertToDTO_EmployeeNoDepartment_ShouldSetNullDepartmentId() {
        // Given
        Employee employeeWithoutDept = new Employee(
                "Jan Kowalski",
                "jan@example.com",
                "TechCorp",
                Position.PROGRAMMER,
                new BigDecimal(5000),
                EmploymentStatus.ACTIVE
        );
        employeeWithoutDept.setId(1L);

        // When
        EmployeeDTO result = employeeFormService.convertToDTO(employeeWithoutDept);

        // Then
        assertAll(
                () -> assertNotNull(result),
                () -> assertNull(result.getDepartmentId())
        );
    }

    @Test
    void convertToDTO_EmployeeWithSingleName_ShouldSetOnlyFirstName() {
        // Given
        Employee employee = new Employee(
                "Jan",
                "jan@example.com",
                "TechCorp",
                Position.PROGRAMMER,
                new BigDecimal(5000),
                EmploymentStatus.ACTIVE
        );

        // When
        EmployeeDTO result = employeeFormService.convertToDTO(employee);

        // Then
        assertAll(
                () -> assertNotNull(result),
                () -> assertEquals("Jan", result.getFirstName()),
                () -> assertEquals("", result.getLastName())
        );
    }

    @Test
    void convertToDTO_EmployeeWithMultipleNames_ShouldSetCorrectly() {
        // Given
        Employee employee = new Employee(
                "Jan Maria Kowalski",
                "jan@example.com",
                "TechCorp",
                Position.PROGRAMMER,
                new BigDecimal(5000),
                EmploymentStatus.ACTIVE
        );

        // When
        EmployeeDTO result = employeeFormService.convertToDTO(employee);

        // Then
        assertAll(
                () -> assertNotNull(result),
                () -> assertEquals("Jan", result.getFirstName()),
                () -> assertEquals("Maria Kowalski", result.getLastName())
        );
    }

    @Test
    void convertToDTO_NullEmployee_ShouldThrowNullPointerException() {
        // When & Then
        assertThrows(NullPointerException.class, () -> {
            employeeFormService.convertToDTO(null);
        });
    }

    // ===== TESTY VALIDATEEMPLOYEE =====

    @Test
    void validateEmployee_ValidSalary_ShouldReturnValidResult() {
        // Given
        testEmployeeDTO.setSalary(new BigDecimal(5000));

        // When
        EmployeeFormService.FormValidationResult result = employeeFormService.validateEmployee(testEmployeeDTO);

        // Then
        assertAll(
                () -> assertNotNull(result),
                () -> assertTrue(result.isValid()),
                () -> assertNull(result.getField()),
                () -> assertNull(result.getMessage())
        );
    }

    @Test
    void validateEmployee_ZeroSalary_ShouldReturnError() {
        // Given
        testEmployeeDTO.setSalary(new BigDecimal(0));

        // When
        EmployeeFormService.FormValidationResult result = employeeFormService.validateEmployee(testEmployeeDTO);

        // Then
        assertAll(
                () -> assertNotNull(result),
                () -> assertFalse(result.isValid()),
                () -> assertEquals("salary", result.getField()),
                () -> assertEquals("Wynagrodzenie musi być większe niż 0", result.getMessage())
        );
    }

    @Test
    void validateEmployee_NegativeSalary_ShouldReturnError() {
        // Given
        testEmployeeDTO.setSalary(new BigDecimal(-100));

        // When
        EmployeeFormService.FormValidationResult result = employeeFormService.validateEmployee(testEmployeeDTO);

        // Then
        assertAll(
                () -> assertNotNull(result),
                () -> assertFalse(result.isValid()),
                () -> assertEquals("salary", result.getField()),
                () -> assertEquals("Wynagrodzenie musi być większe niż 0", result.getMessage())
        );
    }

    @Test
    void validateEmployee_NullSalary_ShouldReturnError() {
        // Given
        testEmployeeDTO.setSalary(null);

        // When
        EmployeeFormService.FormValidationResult result = employeeFormService.validateEmployee(testEmployeeDTO);

        // Then
        assertAll(
                () -> assertNotNull(result),
                () -> assertFalse(result.isValid()),
                () -> assertEquals("salary", result.getField())
//                () -> assertEquals("Wynagrodzenie musi być większe niż 0", result.getMessage())
        );
    }

    // ===== TESTY KLAS WEWNĘTRZNYCH =====

    @Test
    void employeeFormData_ShouldHaveCorrectGetters() {
        // Given
        List<Position> positions = Arrays.asList(Position.values());
        List<EmploymentStatus> statuses = Arrays.asList(EmploymentStatus.values());

        // When
        EmployeeFormService.EmployeeFormData formData =
                new EmployeeFormService.EmployeeFormData(positions, statuses);

        // Then
        assertAll(
                () -> assertEquals(positions, formData.getPositions()),
                () -> assertEquals(statuses, formData.getStatuses())
        );
    }

    @Test
    void formValidationResult_InitialState_ShouldBeValid() {
        // When
        EmployeeFormService.FormValidationResult result =
                new EmployeeFormService.FormValidationResult();

        // Then
        assertAll(
                () -> assertTrue(result.isValid()),
                () -> assertNull(result.getField()),
                () -> assertNull(result.getMessage())
        );
    }

    @Test
    void formValidationResult_AddError_ShouldSetInvalid() {
        // Given
        EmployeeFormService.FormValidationResult result =
                new EmployeeFormService.FormValidationResult();

        // When
        result.addError("salary", "Salary must be positive");

        // Then
        assertAll(
                () -> assertFalse(result.isValid()),
                () -> assertEquals("salary", result.getField()),
                () -> assertEquals("Salary must be positive", result.getMessage())
        );
    }

    @Test
    void formValidationResult_MultipleAddErrorCalls_ShouldKeepLastError() {
        // Given
        EmployeeFormService.FormValidationResult result =
                new EmployeeFormService.FormValidationResult();

        // When
        result.addError("salary", "First error");
        result.addError("email", "Second error");

        // Then
        assertAll(
                () -> assertFalse(result.isValid()),
                () -> assertEquals("email", result.getField()),
                () -> assertEquals("Second error", result.getMessage())
        );
    }

    // ===== TESTY INTEGRACYJNE =====

    @Test
    void convertToEntityAndBack_ShouldPreserveData() throws InvalidDataException {
        // Given - najpierw bez departamentu
        testEmployeeDTO.setDepartmentId(null);

        // When
        Employee entity = employeeFormService.convertToEntity(testEmployeeDTO);
        EmployeeDTO backToDTO = employeeFormService.convertToDTO(entity);

        // Then
        assertAll(
                () -> assertNotNull(backToDTO),
                () -> assertEquals(testEmployeeDTO.getFirstName(), backToDTO.getFirstName()),
                () -> assertEquals(testEmployeeDTO.getLastName(), backToDTO.getLastName()),
                () -> assertEquals(testEmployeeDTO.getEmail(), backToDTO.getEmail()),
                () -> assertEquals(testEmployeeDTO.getCompany(), backToDTO.getCompany()),
                () -> assertEquals(testEmployeeDTO.getPosition(), backToDTO.getPosition()),
                () -> assertEquals(testEmployeeDTO.getSalary(), backToDTO.getSalary()),
                () -> assertEquals(testEmployeeDTO.getStatus(), backToDTO.getStatus()),
                () -> assertEquals(testEmployeeDTO.getDepartmentId(), backToDTO.getDepartmentId())
        );
    }

    @Test
    void convertToEntityAndBack_WithDepartment_ShouldPreserveDepartment() throws InvalidDataException {
        // Given
        testEmployeeDTO.setDepartmentId(1L);
        when(departmentRepository.findById(1L)).thenReturn(Optional.of(testDepartment));

        // When
        Employee entity = employeeFormService.convertToEntity(testEmployeeDTO);
        EmployeeDTO backToDTO = employeeFormService.convertToDTO(entity);

        // Then
        assertAll(
                () -> assertNotNull(backToDTO),
                () -> assertEquals(1L, backToDTO.getDepartmentId())
        );
    }

    @Test
    void validateEmployee_AfterConversion_ShouldBeValid() throws InvalidDataException {
        // Given
        Employee entity = employeeFormService.convertToEntity(testEmployeeDTO);
        EmployeeDTO convertedDTO = employeeFormService.convertToDTO(entity);

        // When
        EmployeeFormService.FormValidationResult result =
                employeeFormService.validateEmployee(convertedDTO);

        // Then
        assertTrue(result.isValid());
    }

    // ===== TESTY GRANICZNE =====

    @Test
    void convertToEntity_MaxValues_ShouldHandleCorrectly() throws InvalidDataException {
        // Given
        testEmployeeDTO.setFirstName("A".repeat(50));
        testEmployeeDTO.setLastName("B".repeat(50));
        testEmployeeDTO.setSalary(new BigDecimal(MAX_VALUE));

        // When
        Employee result = employeeFormService.convertToEntity(testEmployeeDTO);

        // Then
        assertAll(
                () -> assertNotNull(result),
                () -> assertTrue(result.getName().startsWith("AAAAA"))
        );
    }

    @Test
    void convertToDTO_EmployeeWithMinimalData_ShouldHandleCorrectly() {
        // Given
        Employee minimalEmployee = new Employee(
                "J",
                "j@e.com",
                "C",
                Position.PROGRAMMER,
                new BigDecimal(0.01),
                EmploymentStatus.ACTIVE
        );

        // When
        EmployeeDTO result = employeeFormService.convertToDTO(minimalEmployee);

        // Then
        assertAll(
                () -> assertNotNull(result),
                () -> assertEquals("J", result.getFirstName()),
                () -> assertEquals("", result.getLastName())
        );
    }

    // ===== TESTY WYJĄTKÓW =====

    @Test
    void convertToEntity_DTONull_ShouldThrowException() {
        // When & Then
        assertThrows(NullPointerException.class, () -> {
            employeeFormService.convertToEntity(null);
        });
    }

    @Test
    void validateEmployee_DTONull_ShouldThrowException() {
        // When & Then
        assertThrows(NullPointerException.class, () -> {
            employeeFormService.validateEmployee(null);
        });
    }

    // ===== TESTY DODATKOWYCH SCENARIUSZY =====

    @Test
    void convertToEntity_WithDifferentPositionAndStatus_ShouldPreserveValues() throws InvalidDataException {
        // Given
        testEmployeeDTO.setPosition(Position.MANAGER);
        testEmployeeDTO.setStatus(EmploymentStatus.ON_LEAVE);

        // When
        Employee result = employeeFormService.convertToEntity(testEmployeeDTO);

        // Then
        assertAll(
                () -> assertNotNull(result),
                () -> assertEquals(Position.MANAGER, result.getPosition()),
                () -> assertEquals(EmploymentStatus.ON_LEAVE, result.getStatus())
        );
    }

    @Test
    void convertToDTO_EmployeeWithTerminatedStatus_ShouldPreserveStatus() {
        // Given
        testEmployee.setStatus(EmploymentStatus.TERMINATED);

        // When
        EmployeeDTO result = employeeFormService.convertToDTO(testEmployee);

        // Then
        assertAll(
                () -> assertNotNull(result),
                () -> assertEquals(EmploymentStatus.TERMINATED, result.getStatus())
        );
    }

    @Test
    void validateEmployee_SmallPositiveSalary_ShouldBeValid() {
        // Given
        testEmployeeDTO.setSalary( new BigDecimal(0.01));

        // When
        EmployeeFormService.FormValidationResult result = employeeFormService.validateEmployee(testEmployeeDTO);

        // Then
        assertTrue(result.isValid());
    }

    @Test
    void validateEmployee_VeryLargeSalary_ShouldBeValid() {
        // Given
        testEmployeeDTO.setSalary(BigDecimal.valueOf(Double.MAX_VALUE));

        // When
        EmployeeFormService.FormValidationResult result = employeeFormService.validateEmployee(testEmployeeDTO);

        // Then
        assertTrue(result.isValid());
    }

    // ===== TESTY INTERAKCJI Z DEPENDENCIES =====

    @Test
    void convertToEntity_CallsDepartmentRepository_ExactlyOnce() throws InvalidDataException {
        // Given
        testEmployeeDTO.setDepartmentId(1L);
        when(departmentRepository.findById(1L)).thenReturn(Optional.of(testDepartment));

        // When
        employeeFormService.convertToEntity(testEmployeeDTO);

        // Then
        verify(departmentRepository, times(1)).findById(1L);
        verifyNoMoreInteractions(departmentRepository);
        verifyNoInteractions(employeeService);
    }

    @Test
    void convertToEntity_NoDepartment_DoesNotCallRepository() throws InvalidDataException {
        // Given - departmentId jest null

        // When
        employeeFormService.convertToEntity(testEmployeeDTO);

        // Then
        verifyNoInteractions(departmentRepository);
    }

    // ===== TESTY NAZWY PRACOWNIKA =====

    @Test
    void convertToEntity_NameConcatenation_ShouldAddSpace() throws InvalidDataException {
        // Given
        testEmployeeDTO.setFirstName("Jan");
        testEmployeeDTO.setLastName("Kowalski");

        // When
        Employee result = employeeFormService.convertToEntity(testEmployeeDTO);

        // Then
        assertEquals("Jan Kowalski", result.getName());
    }

    @Test
    void convertToDTO_NameSplitting_WithMultipleMiddleNames() {
        // Given
        Employee employee = new Employee(
                "Jan Maria Rokita Kowalski",
                "jan@example.com",
                "TechCorp",
                Position.PROGRAMMER,
                new BigDecimal(5000),
                EmploymentStatus.ACTIVE
        );

        // When
        EmployeeDTO result = employeeFormService.convertToDTO(employee);

        // Then
        assertAll(
                () -> assertNotNull(result),
                () -> assertEquals("Jan", result.getFirstName()),
                () -> assertEquals("Maria Rokita Kowalski", result.getLastName())
        );
    }

    // ===== TESTY KONSTRUKTORA =====

    @Test
    void constructor_ShouldInitializeCorrectly() {
        // When
        EmployeeFormService service = new EmployeeFormService(employeeService, departmentRepository);

        // Then
        assertNotNull(service);
        EmployeeFormService.EmployeeFormData formData = service.getFormData();
        assertNotNull(formData);
    }

    // ===== TESTY ZWRACANYCH WARTOŚCI =====

    @Test
    void getFormData_ShouldReturnCorrectNumberOfPositions() {
        // When
        EmployeeFormService.EmployeeFormData formData = employeeFormService.getFormData();

        // Then
        assertEquals(Position.values().length, formData.getPositions().size());
    }

    @Test
    void getFormData_ShouldReturnCorrectNumberOfStatuses() {
        // When
        EmployeeFormService.EmployeeFormData formData = employeeFormService.getFormData();

        // Then
        assertEquals(EmploymentStatus.values().length, formData.getStatuses().size());
    }

    // ===== TESTY WALIDACJI FORMULARZA =====

    @Test
    void validateEmployee_ValidDTOWithAllFields_ShouldPass() {
        // Given - wszystkie pola są wypełnione w setup()

        // When
        EmployeeFormService.FormValidationResult result = employeeFormService.validateEmployee(testEmployeeDTO);

        // Then
        assertTrue(result.isValid());
    }

    @Test
    void validateEmployee_ValidDTOWithZeroDepartmentId_ShouldPass() {
        // Given
        testEmployeeDTO.setDepartmentId(0L);

        // When
        EmployeeFormService.FormValidationResult result = employeeFormService.validateEmployee(testEmployeeDTO);

        // Then
        assertTrue(result.isValid());
    }
}