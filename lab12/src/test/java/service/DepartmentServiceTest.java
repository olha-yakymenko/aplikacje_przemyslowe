package com.techcorp.employee.service;

import com.techcorp.employee.dto.DepartmentDTO;
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
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DepartmentServiceTest {

    @Mock
    private DepartmentRepository departmentRepository;

    @Mock
    private EmployeeService employeeService;

    @InjectMocks
    private DepartmentService departmentService;

    private Department testDepartment;
    private Employee testEmployee;
    private Employee testManager;

    @BeforeEach
    void setUp() {
        // Używamy konstruktora z 5 parametrami
        testDepartment = new Department(
                "IT",                      // name
                "Warsaw",                 // location
                "Information Technology", // description
                "anna@example.com",       // managerEmail
                100000.0                  // budget
        );
        testDepartment.setId(1L);

        testEmployee = new Employee(
                "Jan Kowalski",
                "jan@example.com",
                "TechCorp",
                Position.PROGRAMMER,
                new BigDecimal(8000),
                EmploymentStatus.ACTIVE
        );
        testEmployee.setId(1L);

        testManager = new Employee(
                "Anna Nowak",
                "anna@example.com",
                "TechCorp",
                Position.MANAGER,
                new BigDecimal(12000),
                EmploymentStatus.ACTIVE
        );
        testManager.setId(2L);
        // managerEmail jest już ustawiony w konstruktorze
    }

    // ===== TESTY PODSTAWOWYCH OPERACJI =====

    @Test
    void getAllDepartments_ShouldReturnList() {
        // Given
        List<Department> departments = Arrays.asList(testDepartment);
        when(departmentRepository.findAll()).thenReturn(departments);

        // When
        List<Department> result = departmentService.getAllDepartments();

        // Then
        assertAll(
                () -> assertNotNull(result),
                () -> assertEquals(1, result.size()),
                () -> assertEquals("IT", result.get(0).getName())
        );
        verify(departmentRepository, times(1)).findAll();
    }

    @Test
    void getDepartmentCount_ShouldReturnCorrectCount() {
        // Given
        when(departmentRepository.count()).thenReturn(5L);

        // When
        int result = departmentService.getDepartmentCount();

        // Then
        assertEquals(5, result);
        verify(departmentRepository, times(1)).count();
    }

    @Test
    void getDepartmentById_ExistingId_ShouldReturnDepartment() {
        // Given
        when(departmentRepository.findById(1L)).thenReturn(Optional.of(testDepartment));

        // When
        Optional<Department> result = departmentService.getDepartmentById(1L);

        // Then
        assertAll(
                () -> assertTrue(result.isPresent()),
                () -> assertEquals("IT", result.get().getName())
        );
        verify(departmentRepository, times(1)).findById(1L);
    }

    @Test
    void getDepartmentById_NonExistingId_ShouldReturnEmpty() {
        // Given
        when(departmentRepository.findById(999L)).thenReturn(Optional.empty());

        // When
        Optional<Department> result = departmentService.getDepartmentById(999L);

        // Then
        assertFalse(result.isPresent());
        verify(departmentRepository, times(1)).findById(999L);
    }

    // ===== TESTY TWORZENIA DEPARTAMENTU =====

    @Test
    void createDepartment_ValidDepartment_ShouldSaveSuccessfully() {
        // Given
        Department newDepartment = new Department(
                "HR",                    // name
                "New York",             // location
                "Human Resources",      // description
                "hr@example.com",       // managerEmail
                50000.0                 // budget
        );

        when(departmentRepository.existsByName("HR")).thenReturn(false);
        when(departmentRepository.save(newDepartment)).thenReturn(newDepartment);

        // When
        Department result = departmentService.createDepartment(newDepartment);

        // Then
        assertAll(
                () -> assertNotNull(result),
                () -> assertEquals("HR", result.getName()),
                () -> assertEquals("New York", result.getLocation()),
                () -> assertEquals("Human Resources", result.getDescription()),
                () -> assertEquals("hr@example.com", result.getManagerEmail()),
                () -> assertEquals(50000.0, result.getBudget(), 0.001)
        );
        verify(departmentRepository, times(1)).existsByName("HR");
        verify(departmentRepository, times(1)).save(newDepartment);
    }

    @Test
    void createDepartment_DuplicateName_ShouldThrowException() {
        // Given
        Department duplicateDepartment = new Department(
                "IT",                    // name - duplikat
                "London",               // location
                "Duplicate IT",         // description
                "it2@example.com",      // managerEmail
                75000.0                 // budget
        );

        when(departmentRepository.existsByName("IT")).thenReturn(true);

        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            departmentService.createDepartment(duplicateDepartment);
        });

        assertEquals("Department with name 'IT' already exists", exception.getMessage());
        verify(departmentRepository, times(1)).existsByName("IT");
        verify(departmentRepository, never()).save(any());
    }

    @Test
    void createDepartment_NullDepartment_ShouldThrowException() {
        // When & Then
        assertThrows(NullPointerException.class, () -> {
            departmentService.createDepartment(null);
        });
    }

    // ===== TESTY AKTUALIZACJI DEPARTAMENTU =====

    @Test
    void updateDepartment_ValidUpdate_ShouldUpdateSuccessfully() {
        // Given
        Long departmentId = 1L;

        // Tworzymy zaktualizowany departament
        Department updatedDepartment = new Department(
                "IT Updated",           // name - zmienione
                "Krakow",              // location - zmienione
                "Updated Description",  // description
                "manager@example.com",  // managerEmail - zmienione
                150000.0               // budget - zmienione
        );

        when(departmentRepository.findById(departmentId)).thenReturn(Optional.of(testDepartment));
        when(departmentRepository.existsByName("IT Updated")).thenReturn(false);
        when(departmentRepository.save(any(Department.class))).thenReturn(updatedDepartment);

        // When
        Department result = departmentService.updateDepartment(departmentId, updatedDepartment);

        // Then
        assertAll(
                () -> assertNotNull(result),
                () -> assertEquals("IT Updated", result.getName()),
                () -> assertEquals("Krakow", result.getLocation()),
                () -> assertEquals("Updated Description", result.getDescription()),
                () -> assertEquals("manager@example.com", result.getManagerEmail()),
                () -> assertEquals(150000.0, result.getBudget(), 0.001)
        );
        verify(departmentRepository, times(1)).findById(departmentId);
        verify(departmentRepository, times(1)).existsByName("IT Updated");
        verify(departmentRepository, times(1)).save(any(Department.class));
    }

    @Test
    void updateDepartment_SameName_ShouldUpdateSuccessfully() {
        // Given
        Long departmentId = 1L;

        // Tworzymy zaktualizowany departament z tą samą nazwą
        Department updatedDepartment = new Department(
                "IT",                    // name - bez zmian
                "Krakow",              // location - zmienione
                "Updated Description",  // description
                "newmanager@example.com", // managerEmail - zmienione
                120000.0               // budget - zmienione
        );

        when(departmentRepository.findById(departmentId)).thenReturn(Optional.of(testDepartment));
        when(departmentRepository.save(any(Department.class))).thenReturn(updatedDepartment);

        // When
        Department result = departmentService.updateDepartment(departmentId, updatedDepartment);

        // Then
        assertAll(
                () -> assertNotNull(result),
                () -> assertEquals("IT", result.getName()),
                () -> assertEquals("Krakow", result.getLocation())
        );
        verify(departmentRepository, times(1)).findById(departmentId);
        verify(departmentRepository, never()).existsByName(anyString());
        verify(departmentRepository, times(1)).save(any(Department.class));
    }

    @Test
    void updateDepartment_DuplicateName_ShouldThrowException() {
        // Given
        Long departmentId = 1L;
        Department updatedDepartment = new Department(
                "HR",                    // name - nowa nazwa
                "London",               // location
                "New Name",             // description
                "hr@example.com",       // managerEmail
                80000.0                 // budget
        );

        when(departmentRepository.findById(departmentId)).thenReturn(Optional.of(testDepartment));
        when(departmentRepository.existsByName("HR")).thenReturn(true);

        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            departmentService.updateDepartment(departmentId, updatedDepartment);
        });

        assertEquals("Department with name 'HR' already exists", exception.getMessage());
        verify(departmentRepository, times(1)).findById(departmentId);
        verify(departmentRepository, times(1)).existsByName("HR");
        verify(departmentRepository, never()).save(any());
    }

    @Test
    void updateDepartment_NonExistingDepartment_ShouldThrowException() {
        // Given
        Long nonExistingId = 999L;
        Department updatedDepartment = new Department(
                "IT",                    // name
                "Warsaw",               // location
                "Updated",              // description
                "email@example.com",    // managerEmail
                100000.0                // budget
        );

        when(departmentRepository.findById(nonExistingId)).thenReturn(Optional.empty());

        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            departmentService.updateDepartment(nonExistingId, updatedDepartment);
        });

        assertEquals("Department not found with id: " + nonExistingId, exception.getMessage());
        verify(departmentRepository, times(1)).findById(nonExistingId);
        verify(departmentRepository, never()).existsByName(anyString());
        verify(departmentRepository, never()).save(any());
    }

    // ===== TESTY USUWANIA DEPARTAMENTU =====

    @Test
    void deleteDepartment_ExistingDepartment_ShouldDeleteSuccessfully() {
        // Given
        Long departmentId = 1L;
        testEmployee.setDepartment(testDepartment);
        List<Employee> employeesInDepartment = Arrays.asList(testEmployee);

        when(departmentRepository.findById(departmentId)).thenReturn(Optional.of(testDepartment));
        when(employeeService.getEmployeesByDepartmentId(departmentId)).thenReturn(employeesInDepartment);
        when(employeeService.saveEmployee(any(Employee.class))).thenReturn(testEmployee);
        doNothing().when(departmentRepository).delete(testDepartment);

        // When
        departmentService.deleteDepartment(departmentId);

        // Then
        assertNull(testEmployee.getDepartment());
        verify(departmentRepository, times(1)).findById(departmentId);
        verify(employeeService, times(1)).getEmployeesByDepartmentId(departmentId);
        verify(employeeService, times(1)).saveEmployee(testEmployee);
        verify(departmentRepository, times(1)).delete(testDepartment);
    }

    @Test
    void deleteDepartment_NoEmployees_ShouldDeleteSuccessfully() {
        // Given
        Long departmentId = 1L;
        List<Employee> emptyList = Collections.emptyList();

        when(departmentRepository.findById(departmentId)).thenReturn(Optional.of(testDepartment));
        when(employeeService.getEmployeesByDepartmentId(departmentId)).thenReturn(emptyList);
        doNothing().when(departmentRepository).delete(testDepartment);

        // When
        departmentService.deleteDepartment(departmentId);

        // Then
        verify(departmentRepository, times(1)).findById(departmentId);
        verify(employeeService, times(1)).getEmployeesByDepartmentId(departmentId);
        verify(employeeService, never()).saveEmployee(any());
        verify(departmentRepository, times(1)).delete(testDepartment);
    }

    @Test
    void deleteDepartment_NonExistingDepartment_ShouldThrowException() {
        // Given
        Long nonExistingId = 999L;
        when(departmentRepository.findById(nonExistingId)).thenReturn(Optional.empty());

        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            departmentService.deleteDepartment(nonExistingId);
        });

        assertEquals("Department not found with id: " + nonExistingId, exception.getMessage());
        verify(departmentRepository, times(1)).findById(nonExistingId);
        verify(employeeService, never()).getEmployeesByDepartmentId(anyLong());
        verify(departmentRepository, never()).delete(any());
    }

    // ===== TESTY PRZYPISYWANIA PRACOWNIKÓW =====

    @Test
    void assignEmployeeToDepartment_ValidData_ShouldAssignSuccessfully() {
        // Given
        String employeeEmail = "jan@example.com";
        Long departmentId = 1L;

        when(departmentRepository.findById(departmentId)).thenReturn(Optional.of(testDepartment));
        when(employeeService.findEmployeeByEmail(employeeEmail)).thenReturn(Optional.of(testEmployee));
        when(employeeService.saveEmployee(any(Employee.class))).thenReturn(testEmployee);

        // When
        departmentService.assignEmployeeToDepartment(employeeEmail, departmentId);

        // Then
        assertEquals(testDepartment, testEmployee.getDepartment());
        verify(departmentRepository, times(1)).findById(departmentId);
        verify(employeeService, times(1)).findEmployeeByEmail(employeeEmail);
        verify(employeeService, times(1)).saveEmployee(testEmployee);
    }

    @Test
    void assignEmployeeToDepartment_DepartmentNotFound_ShouldThrowException() {
        // Given
        String employeeEmail = "jan@example.com";
        Long nonExistingDepartmentId = 999L;

        when(departmentRepository.findById(nonExistingDepartmentId)).thenReturn(Optional.empty());

        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            departmentService.assignEmployeeToDepartment(employeeEmail, nonExistingDepartmentId);
        });

        assertEquals("Department not found with id: " + nonExistingDepartmentId, exception.getMessage());
        verify(departmentRepository, times(1)).findById(nonExistingDepartmentId);
        verify(employeeService, never()).findEmployeeByEmail(anyString());
        verify(employeeService, never()).saveEmployee(any());
    }

    @Test
    void assignEmployeeToDepartment_EmployeeNotFound_ShouldThrowException() {
        // Given
        String nonExistingEmail = "nonexistent@example.com";
        Long departmentId = 1L;

        when(departmentRepository.findById(departmentId)).thenReturn(Optional.of(testDepartment));
        when(employeeService.findEmployeeByEmail(nonExistingEmail)).thenReturn(Optional.empty());

        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            departmentService.assignEmployeeToDepartment(nonExistingEmail, departmentId);
        });

        assertEquals("Employee not found with email: " + nonExistingEmail, exception.getMessage());
        verify(departmentRepository, times(1)).findById(departmentId);
        verify(employeeService, times(1)).findEmployeeByEmail(nonExistingEmail);
        verify(employeeService, never()).saveEmployee(any());
    }

    @Test
    void removeEmployeeFromDepartment_ValidEmployee_ShouldRemoveSuccessfully() {
        // Given
        String employeeEmail = "jan@example.com";
        testEmployee.setDepartment(testDepartment);

        when(employeeService.findEmployeeByEmail(employeeEmail)).thenReturn(Optional.of(testEmployee));
        when(employeeService.saveEmployee(any(Employee.class))).thenReturn(testEmployee);

        // When
        departmentService.removeEmployeeFromDepartment(employeeEmail);

        // Then
        assertNull(testEmployee.getDepartment());
        verify(employeeService, times(1)).findEmployeeByEmail(employeeEmail);
        verify(employeeService, times(1)).saveEmployee(testEmployee);
    }

    @Test
    void removeEmployeeFromDepartment_EmployeeNotFound_ShouldThrowException() {
        // Given
        String nonExistingEmail = "nonexistent@example.com";

        when(employeeService.findEmployeeByEmail(nonExistingEmail)).thenReturn(Optional.empty());

        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            departmentService.removeEmployeeFromDepartment(nonExistingEmail);
        });

        assertEquals("Employee not found with email: " + nonExistingEmail, exception.getMessage());
        verify(employeeService, times(1)).findEmployeeByEmail(nonExistingEmail);
        verify(employeeService, never()).saveEmployee(any());
    }

    @Test
    void removeEmployeeFromDepartment_EmployeeWithoutDepartment_ShouldHandleGracefully() {
        // Given
        String employeeEmail = "jan@example.com";
        testEmployee.setDepartment(null);

        when(employeeService.findEmployeeByEmail(employeeEmail)).thenReturn(Optional.of(testEmployee));
        when(employeeService.saveEmployee(any(Employee.class))).thenReturn(testEmployee);

        // When
        departmentService.removeEmployeeFromDepartment(employeeEmail);

        // Then
        assertNull(testEmployee.getDepartment());
        verify(employeeService, times(1)).findEmployeeByEmail(employeeEmail);
        verify(employeeService, times(1)).saveEmployee(testEmployee);
    }

    // ===== TESTY SZCZEGÓŁÓW DEPARTAMENTU =====

    @Test
    void getDepartmentDetails_ExistingDepartment_ShouldReturnDTO() {
        // Given
        Long departmentId = 1L;

        testEmployee.setDepartment(testDepartment);
        List<Employee> departmentEmployees = Arrays.asList(testEmployee);
        testDepartment.setEmployees(departmentEmployees);

        when(departmentRepository.findByIdWithEmployees(departmentId))
                .thenReturn(Optional.of(testDepartment));
        when(employeeService.findEmployeeByEmail("anna@example.com"))
                .thenReturn(Optional.of(testManager));

        // When
        DepartmentDTO result = departmentService.getDepartmentDetails(departmentId);

        // Then
        assertAll(
                () -> assertNotNull(result),
                () -> assertNotNull(result.getEmployees()),
                () -> assertEquals(1, result.getEmployees().size()),
                () -> assertTrue(result.getManager().isPresent()),
                () -> assertEquals("Anna Nowak", result.getManager().get().getName())
        );
        verify(departmentRepository, times(1)).findByIdWithEmployees(departmentId);
        verify(employeeService, times(1)).findEmployeeByEmail("anna@example.com");
    }

    @Test
    void getDepartmentDetails_NonExistingDepartment_ShouldReturnNull() {
        // Given
        Long nonExistingId = 999L;
        when(departmentRepository.findByIdWithEmployees(nonExistingId))
                .thenReturn(Optional.empty());

        // When
        DepartmentDTO result = departmentService.getDepartmentDetails(nonExistingId);

        // Then
        assertNull(result);
        verify(departmentRepository, times(1)).findByIdWithEmployees(nonExistingId);
        verify(employeeService, never()).findEmployeeByEmail(anyString());
    }

    @Test
    void getDepartmentDetails_DepartmentWithoutManager_ShouldReturnDTOWithoutManager() {
        // Given
        Long departmentId = 1L;

        Department departmentWithoutManager = new Department(
                "IT", "Warsaw", "IT Department", null, 100000.0
        );
        departmentWithoutManager.setId(1L);

        testEmployee.setDepartment(departmentWithoutManager);
        List<Employee> departmentEmployees = Arrays.asList(testEmployee);
        departmentWithoutManager.setEmployees(departmentEmployees);

        when(departmentRepository.findByIdWithEmployees(departmentId))
                .thenReturn(Optional.of(departmentWithoutManager));

        // When
        DepartmentDTO result = departmentService.getDepartmentDetails(departmentId);

        // Then
        assertAll(
                () -> assertNotNull(result),
                () -> assertFalse(result.getManager().isPresent())
        );
        verify(departmentRepository, times(1)).findByIdWithEmployees(departmentId);
        verify(employeeService, never()).findEmployeeByEmail(anyString());
    }

    @Test
    void getDepartmentDetails_DepartmentWithNonExistingManager_ShouldReturnDTOWithoutManager() {
        // Given
        Long departmentId = 1L;
        testDepartment.setManagerEmail("nonexistent@example.com");

        testEmployee.setDepartment(testDepartment);
        List<Employee> departmentEmployees = Arrays.asList(testEmployee);
        testDepartment.setEmployees(departmentEmployees);

        when(departmentRepository.findByIdWithEmployees(departmentId))
                .thenReturn(Optional.of(testDepartment));
        when(employeeService.findEmployeeByEmail("nonexistent@example.com"))
                .thenReturn(Optional.empty());

        // When
        DepartmentDTO result = departmentService.getDepartmentDetails(departmentId);

        // Then
        assertAll(
                () -> assertNotNull(result),
                () -> assertFalse(result.getManager().isPresent())
        );
        verify(departmentRepository, times(1)).findByIdWithEmployees(departmentId);
        verify(employeeService, times(1)).findEmployeeByEmail("nonexistent@example.com");
    }

    // ===== TESTY POMOCNICZYCH METOD =====

    @Test
    void getEmployeesWithoutDepartment_ShouldReturnList() {
        // Given
        List<Employee> employeesWithoutDept = Arrays.asList(testEmployee);
        when(employeeService.getEmployeesWithoutDepartment()).thenReturn(employeesWithoutDept);

        // When
        List<Employee> result = departmentService.getEmployeesWithoutDepartment();

        // Then
        assertAll(
                () -> assertNotNull(result),
                () -> assertEquals(1, result.size())
        );
        verify(employeeService, times(1)).getEmployeesWithoutDepartment();
    }

    @Test
    void getDepartmentsByManager_ShouldReturnList() {
        // Given
        String managerEmail = "manager@example.com";
        List<Department> departments = Arrays.asList(testDepartment);

        when(departmentRepository.findByLocation(managerEmail)).thenReturn(departments);

        // When
        List<Department> result = departmentService.getDepartmentsByManager(managerEmail);

        // Then
        assertAll(
                () -> assertNotNull(result),
                () -> assertEquals(1, result.size())
        );
        verify(departmentRepository, times(1)).findByLocation(managerEmail);
    }

    // ===== TESTY KRAŃCOWYCH PRZYPADKÓW =====

    @Test
    void createDepartment_EmptyName_ShouldBeHandledByRepository() {
        // Given
        Department emptyNameDepartment = new Department(
                "",                    // pusta nazwa
                "Location",           // location
                "Description",        // description
                "email@example.com",  // managerEmail
                1000.0                // budget
        );

        when(departmentRepository.existsByName("")).thenReturn(false);
        when(departmentRepository.save(emptyNameDepartment)).thenReturn(emptyNameDepartment);

        // When
        Department result = departmentService.createDepartment(emptyNameDepartment);

        // Then
        assertAll(
                () -> assertNotNull(result),
                () -> assertEquals("", result.getName())
        );
        verify(departmentRepository, times(1)).existsByName("");
        verify(departmentRepository, times(1)).save(emptyNameDepartment);
    }

    @Test
    void updateDepartment_NullUpdateData_ShouldThrowException() {
        // Given
        Long departmentId = 1L;
        when(departmentRepository.findById(departmentId)).thenReturn(Optional.of(testDepartment));

        // When & Then
        assertThrows(NullPointerException.class, () -> {
            departmentService.updateDepartment(departmentId, null);
        });
        verify(departmentRepository, times(1)).findById(departmentId);
        verify(departmentRepository, never()).existsByName(anyString());
        verify(departmentRepository, never()).save(any());
    }

    @Test
    void deleteDepartment_AlreadyDeleted_ShouldThrowException() {
        // Given
        Long departmentId = 1L;

        when(departmentRepository.findById(departmentId)).thenReturn(Optional.of(testDepartment));
        doThrow(new RuntimeException("Department already deleted"))
                .when(departmentRepository).delete(testDepartment);

        // When & Then
        assertThrows(RuntimeException.class, () -> {
            departmentService.deleteDepartment(departmentId);
        });

        verify(departmentRepository, times(1)).findById(departmentId);
        verify(employeeService, times(1)).getEmployeesByDepartmentId(departmentId);
        verify(departmentRepository, times(1)).delete(testDepartment);
    }

    @Test
    void assignEmployeeToDepartment_EmployeeAlreadyInDepartment_ShouldReassign() {
        // Given
        String employeeEmail = "jan@example.com";
        Long newDepartmentId = 2L;

        Department newDepartment = new Department(
                "HR",                    // name
                "New York",             // location
                "Human Resources",      // description
                "hr@example.com",       // managerEmail
                50000.0                 // budget
        );
        newDepartment.setId(2L);

        testEmployee.setDepartment(testDepartment);

        when(departmentRepository.findById(newDepartmentId)).thenReturn(Optional.of(newDepartment));
        when(employeeService.findEmployeeByEmail(employeeEmail)).thenReturn(Optional.of(testEmployee));
        when(employeeService.saveEmployee(any(Employee.class))).thenReturn(testEmployee);

        // When
        departmentService.assignEmployeeToDepartment(employeeEmail, newDepartmentId);

        // Then
        assertEquals(newDepartment, testEmployee.getDepartment());
        verify(departmentRepository, times(1)).findById(newDepartmentId);
        verify(employeeService, times(1)).findEmployeeByEmail(employeeEmail);
        verify(employeeService, times(1)).saveEmployee(testEmployee);
    }
}