package com.techcorp.employee.service;

import com.techcorp.employee.dto.CompanyStatisticsDTO;
import com.techcorp.employee.dto.EmployeeListView;
import com.techcorp.employee.exception.*;
import com.techcorp.employee.model.*;
import com.techcorp.employee.repository.EmployeeRepository;
import com.techcorp.employee.repository.DepartmentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.core.io.Resource;

import java.lang.reflect.Method;
import java.nio.file.Path;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmployeeServiceTest {

    @Mock
    private EmployeeRepository employeeRepository;

    @Mock
    private DepartmentRepository departmentRepository;

    @Mock
    private FileStorageService fileStorageService;

    @InjectMocks
    private EmployeeService employeeService;

    private Employee testEmployee;
    private Department testDepartment;
    private CompanyStatisticsDTO testStatisticsDTO;

    @BeforeEach
    void setUp() {
        testEmployee = new Employee(
                "Jan Kowalski",
                "jan@example.com",
                "TechCorp",
                Position.PROGRAMMER,
                8000.0,
                EmploymentStatus.ACTIVE
        );
        testEmployee.setId(1L);

        testDepartment = new Department("IT", "London", "Opis", "a@a.com", 100.0);
        testDepartment.setId(1L);

        testStatisticsDTO = new CompanyStatisticsDTO(
                "TechCorp",
                10L,
                7500.0,
                12000.0,
                "Jan Kowalski"
        );
    }

    // ===== TESTY OPERACJI MATEMATYCZNYCH =====

    @Test
    void calculateAverageSalary_ShouldReturnValue() {
        // Given
        when(employeeRepository.findAverageSalary()).thenReturn(7500.0);

        // When
        Double result = employeeService.calculateAverageSalary();

        // Then
        assertEquals(7500.0, result, 0.001);
        verify(employeeRepository, times(1)).findAverageSalary();
    }

    @Test
    void calculateAverageSalaryByCompany_ShouldReturnValue() {
        // Given
        when(employeeRepository.findAverageSalaryByCompany("TechCorp")).thenReturn(8000.0);

        // When
        Double result = employeeService.calculateAverageSalaryByCompany("TechCorp");

        // Then
        assertEquals(8000.0, result, 0.001);
        verify(employeeRepository, times(1)).findAverageSalaryByCompany("TechCorp");
    }

    @Test
    void calculateAverageSalaryByCompany_InvalidCompany_ShouldThrowException() {
        // When & Then
        assertThrows(IllegalArgumentException.class, () -> {
            employeeService.calculateAverageSalaryByCompany("");
        });
    }

    @Test
    void findMaxSalary_ShouldReturnValue() {
        // Given
        when(employeeRepository.findMaxSalary()).thenReturn(15000.0);

        // When
        Double result = employeeService.findMaxSalary();

        // Then
        assertEquals(15000.0, result, 0.001);
        verify(employeeRepository, times(1)).findMaxSalary();
    }

    @Test
    void findMaxSalaryByCompany_ShouldReturnValue() {
        // Given
        when(employeeRepository.findMaxSalaryByCompany("TechCorp")).thenReturn(12000.0);

        // When
        Double result = employeeService.findMaxSalaryByCompany("TechCorp");

        // Then
        assertEquals(12000.0, result, 0.001);
        verify(employeeRepository, times(1)).findMaxSalaryByCompany("TechCorp");
    }

    @Test
    void findMinSalary_ShouldReturnValue() {
        // Given
        when(employeeRepository.findMinSalary()).thenReturn(3000.0);

        // When
        Double result = employeeService.findMinSalary();

        // Then
        assertEquals(3000.0, result, 0.001);
        verify(employeeRepository, times(1)).findMinSalary();
    }

    @Test
    void calculateTotalSalaryCost_ShouldReturnValue() {
        // Given
        when(employeeRepository.findTotalSalaryCost()).thenReturn(150000.0);

        // When
        Double result = employeeService.calculateTotalSalaryCost();

        // Then
        assertEquals(150000.0, result, 0.001);
        verify(employeeRepository, times(1)).findTotalSalaryCost();
    }

    @Test
    void calculateTotalSalaryCostByCompany_ShouldReturnValue() {
        // Given
        when(employeeRepository.findTotalSalaryCostByCompany("TechCorp")).thenReturn(50000.0);

        // When
        Double result = employeeService.calculateTotalSalaryCostByCompany("TechCorp");

        // Then
        assertEquals(50000.0, result, 0.001);
        verify(employeeRepository, times(1)).findTotalSalaryCostByCompany("TechCorp");
    }

    @Test
    void getEmployeeCount_ShouldReturnCount() {
        // Given
        when(employeeRepository.countAllEmployees()).thenReturn(50L);

        // When
        Long result = employeeService.getEmployeeCount();

        // Then
        assertEquals(50L, result);
        verify(employeeRepository, times(1)).countAllEmployees();
    }

    @Test
    void getEmployeeCountByCompany_ShouldReturnCount() {
        // Given
        when(employeeRepository.countEmployeesByCompany("TechCorp")).thenReturn(10L);

        // When
        Long result = employeeService.getEmployeeCountByCompany("TechCorp");

        // Then
        assertEquals(10L, result);
        verify(employeeRepository, times(1)).countEmployeesByCompany("TechCorp");
    }

    @Test
    void getEmployeeCountByStatus_ShouldReturnCount() {
        // Given
        when(employeeRepository.countEmployeesByStatus(EmploymentStatus.ACTIVE)).thenReturn(8L);

        // When
        Long result = employeeService.getEmployeeCountByStatus(EmploymentStatus.ACTIVE);

        // Then
        assertEquals(8L, result);
        verify(employeeRepository, times(1)).countEmployeesByStatus(EmploymentStatus.ACTIVE);
    }

    @Test
    void getEmployeeCountByPosition_ShouldReturnCount() {
        // Given
        when(employeeRepository.countEmployeesByPosition(Position.PROGRAMMER)).thenReturn(5L);

        // When
        Long result = employeeService.getEmployeeCountByPosition(Position.PROGRAMMER);

        // Then
        assertEquals(5L, result);
        verify(employeeRepository, times(1)).countEmployeesByPosition(Position.PROGRAMMER);
    }

    @Test
    void getEmployeeCountByDepartment_ShouldReturnCount() {
        // Given
        when(employeeRepository.countEmployeesByDepartment(1L)).thenReturn(3L);

        // When
        Long result = employeeService.getEmployeeCountByDepartment(1L);

        // Then
        assertEquals(3L, result);
        verify(employeeRepository, times(1)).countEmployeesByDepartment(1L);
    }

    @Test
    void getEmployeeCountWithoutDepartment_ShouldReturnCount() {
        // Given
        when(employeeRepository.countEmployeesWithoutDepartment()).thenReturn(2L);

        // When
        Long result = employeeService.getEmployeeCountWithoutDepartment();

        // Then
        assertEquals(2L, result);
        verify(employeeRepository, times(1)).countEmployeesWithoutDepartment();
    }

    // ===== TESTY STATYSTYK FIRM =====

    @Test
    void getAllCompanyStatisticsDTO_ShouldReturnList() {
        // Given
        List<CompanyStatisticsDTO> dtos = Arrays.asList(testStatisticsDTO);
        when(employeeRepository.getCompanyStatisticsDTO()).thenReturn(dtos);

        // When
        List<CompanyStatisticsDTO> result = employeeService.getAllCompanyStatisticsDTO();

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("TechCorp", result.get(0).getCompanyName());
        verify(employeeRepository, times(1)).getCompanyStatisticsDTO();
    }

    @Test
    void getCompanyStatisticsDTO_CompanyExists_ShouldReturnOptional() {
        // Given
        when(employeeRepository.getCompanyStatisticsDTO("TechCorp")).thenReturn(Optional.of(testStatisticsDTO));

        // When
        Optional<CompanyStatisticsDTO> result = employeeService.getCompanyStatisticsDTO("TechCorp");

        // Then
        assertTrue(result.isPresent());
        assertEquals("TechCorp", result.get().getCompanyName());
        verify(employeeRepository, times(1)).getCompanyStatisticsDTO("TechCorp");
    }

    @Test
    void getCompanyStatistics_CompanyExists_ShouldReturnCompanyStatistics() {
        // Given
        when(employeeRepository.getCompanyStatisticsDTO("TechCorp")).thenReturn(Optional.of(testStatisticsDTO));

        // When
        CompanyStatistics result = employeeService.getCompanyStatistics("TechCorp");

        // Then
        assertNotNull(result);
        assertEquals("TechCorp", result.getCompanyName());
        assertEquals(10L, result.getEmployeeCount());
        assertEquals(7500.0, result.getAverageSalary(), 0.001);
        assertEquals("Jan Kowalski", result.getHighestPaidEmployee());
        verify(employeeRepository, times(1)).getCompanyStatisticsDTO("TechCorp");
    }

    @Test
    void getAllCompanyStatistics_ShouldReturnList() {
        // Given
        List<CompanyStatisticsDTO> dtos = Arrays.asList(testStatisticsDTO);
        when(employeeRepository.getCompanyStatisticsDTO()).thenReturn(dtos);

        // When
        List<CompanyStatistics> result = employeeService.getAllCompanyStatistics();

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("TechCorp", result.get(0).getCompanyName());
        verify(employeeRepository, times(1)).getCompanyStatisticsDTO();
    }

    @Test
    void getPositionStatistics_ShouldReturnMap() {
        // Given
        Object[] result1 = {Position.PROGRAMMER, 5L, 6000.0, 8000.0, 4000.0};
        Object[] result2 = {Position.MANAGER, 2L, 10000.0, 12000.0, 8000.0};
        List<Object[]> results = Arrays.asList(result1, result2);

        when(employeeRepository.getPositionStatistics()).thenReturn(results);

        // When
        Map<String, Object> result = employeeService.getPositionStatistics();

        // Then
        assertNotNull(result);
        assertEquals(2, result.size());
        assertTrue(result.containsKey("PROGRAMMER"));
        assertTrue(result.containsKey("MANAGER"));
        verify(employeeRepository, times(1)).getPositionStatistics();
    }

    @Test
    void getStatusStatistics_ShouldReturnMap() {
        // Given
        Object[] result1 = {EmploymentStatus.ACTIVE, 8L, 7500.0};
        Object[] result2 = {EmploymentStatus.ON_LEAVE, 2L, 6000.0};
        List<Object[]> results = Arrays.asList(result1, result2);

        when(employeeRepository.getStatusStatistics()).thenReturn(results);

        // When
        Map<String, Object> result = employeeService.getStatusStatistics();

        // Then
        assertNotNull(result);
        assertEquals(2, result.size());
        assertTrue(result.containsKey("ACTIVE"));
        assertTrue(result.containsKey("ON_LEAVE"));
        verify(employeeRepository, times(1)).getStatusStatistics();
    }

    @Test
    void getAverageSalaryByPosition_ShouldReturnMap() {
        // Given
        Object[] result1 = {Position.PROGRAMMER, 5L, 6000.0, 8000.0, 4000.0};
        Object[] result2 = {Position.MANAGER, 2L, 10000.0, 12000.0, 8000.0};
        List<Object[]> results = Arrays.asList(result1, result2);

        when(employeeRepository.getPositionStatistics()).thenReturn(results);

        // When
        Map<String, Double> result = employeeService.getAverageSalaryByPosition();

        // Then
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(6000.0, result.get("PROGRAMMER"), 0.001);
        assertEquals(10000.0, result.get("MANAGER"), 0.001);
        verify(employeeRepository, times(1)).getPositionStatistics();
    }

    // ===== TESTY ZAAWANSOWANYCH ZAPYTAŃ =====

    @Test
    void findHighestPaidEmployee_Exists_ShouldReturnEmployee() {
        // Given
        List<Employee> highestPaid = Arrays.asList(testEmployee);
        when(employeeRepository.findHighestPaidEmployees()).thenReturn(highestPaid);

        // When
        Optional<Employee> result = employeeService.findHighestPaidEmployee();

        // Then
        assertTrue(result.isPresent());
        assertEquals("jan@example.com", result.get().getEmail());
        verify(employeeRepository, times(1)).findHighestPaidEmployees();
    }

    @Test
    void findHighestPaidEmployeeByCompany_Exists_ShouldReturnEmployee() {
        // Given
        List<Employee> highestPaid = Arrays.asList(testEmployee);
        when(employeeRepository.findHighestPaidEmployeesByCompany("TechCorp")).thenReturn(highestPaid);

        // When
        Optional<Employee> result = employeeService.findHighestPaidEmployeeByCompany("TechCorp");

        // Then
        assertTrue(result.isPresent());
        assertEquals("jan@example.com", result.get().getEmail());
        verify(employeeRepository, times(1)).findHighestPaidEmployeesByCompany("TechCorp");
    }

    @Test
    void findEmployeesBelowAverageSalary_ShouldReturnList() {
        // Given
        List<Employee> employees = Arrays.asList(testEmployee);
        when(employeeRepository.findEmployeesBelowAverageSalary()).thenReturn(employees);

        // When
        List<Employee> result = employeeService.findEmployeesBelowAverageSalary();

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(employeeRepository, times(1)).findEmployeesBelowAverageSalary();
    }

    @Test
    void findTop10HighestPaidEmployees_ShouldReturnList() {
        // Given
        List<Employee> top10 = Arrays.asList(testEmployee);
        when(employeeRepository.findTop10HighestPaidEmployees(any(Pageable.class))).thenReturn(top10);

        // When
        List<Employee> result = employeeService.findTop10HighestPaidEmployees();

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(employeeRepository, times(1)).findTop10HighestPaidEmployees(any(Pageable.class));
    }

    @Test
    void getEmploymentStatusDistribution_ShouldReturnMap() {
        // Given
        Object[] result1 = {EmploymentStatus.ACTIVE, 8L};
        Object[] result2 = {EmploymentStatus.ON_LEAVE, 2L};
        List<Object[]> results = Arrays.asList(result1, result2);

        when(employeeRepository.getStatusStatistics()).thenReturn(results);

        // When
        Map<EmploymentStatus, Long> result = employeeService.getEmploymentStatusDistribution();

        // Then
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(8L, result.get(EmploymentStatus.ACTIVE));
        assertEquals(2L, result.get(EmploymentStatus.ON_LEAVE));
        verify(employeeRepository, times(1)).getStatusStatistics();
    }

    // ===== TESTY PAGINACJI =====

    @Test
    void getAllEmployees_WithPageable_ShouldReturnPagedResults() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);
        Page<Employee> page = new PageImpl<>(Arrays.asList(testEmployee), pageable, 1);
        when(employeeRepository.findAll(pageable)).thenReturn(page);

        // When
        Page<Employee> result = employeeService.getAllEmployees(pageable);

        // Then
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        verify(employeeRepository, times(1)).findAll(pageable);
    }

    @Test
    void getEmployeesByStatusProjection_ShouldReturnProjection() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);
        EmployeeListView view = new EmployeeListView() {
            @Override public String getName() { return "Jan Kowalski"; }
            @Override public String getEmail() { return "jan@example.com"; }
            @Override public String getCompany() { return "TechCorp"; }
            @Override public String getPosition() { return "PROGRAMMER"; }
            @Override public Double getSalary() { return 8000.0; }
            @Override public EmploymentStatus getStatus() { return EmploymentStatus.ACTIVE; }
            @Override public String getDepartmentName() { return "IT"; }
        };
        Page<EmployeeListView> page = new PageImpl<>(Collections.singletonList(view), pageable, 1);
        when(employeeRepository.findByStatusProjection(EmploymentStatus.ACTIVE, pageable)).thenReturn(page);

        // When
        Page<EmployeeListView> result = employeeService.getEmployeesByStatusProjection(EmploymentStatus.ACTIVE, pageable);

        // Then
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        verify(employeeRepository, times(1)).findByStatusProjection(EmploymentStatus.ACTIVE, pageable);
    }

    @Test
    void getEmployeesByCompanyProjection_ShouldReturnProjection() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);
        EmployeeListView view = new EmployeeListView() {
            @Override public String getName() { return "Jan Kowalski"; }
            @Override public String getEmail() { return "jan@example.com"; }
            @Override public String getCompany() { return "TechCorp"; }
            @Override public String getPosition() { return "PROGRAMMER"; }
            @Override public Double getSalary() { return 8000.0; }
            @Override public EmploymentStatus getStatus() { return EmploymentStatus.ACTIVE; }
            @Override public String getDepartmentName() { return "IT"; }
        };
        Page<EmployeeListView> page = new PageImpl<>(Collections.singletonList(view), pageable, 1);
        when(employeeRepository.findByCompanyProjection("TechCorp", pageable)).thenReturn(page);

        // When
        Page<EmployeeListView> result = employeeService.getEmployeesByCompanyProjection("TechCorp", pageable);

        // Then
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        verify(employeeRepository, times(1)).findByCompanyProjection("TechCorp", pageable);
    }

//    @Test
//    void searchEmployeesAdvanced_ShouldReturnFilteredResults() {
//        // Given
//        Pageable pageable = PageRequest.of(0, 10);
//        EmployeeListView view = new EmployeeListView() {
//            @Override public String getName() { return "Jan Kowalski"; }
//            @Override public String getEmail() { return "jan@example.com"; }
//            @Override public String getCompany() { return "TechCorp"; }
//            @Override public String getPosition() { return "PROGRAMMER"; }
//            @Override public Double getSalary() { return 8000.0; }
//            @Override public EmploymentStatus getStatus() { return EmploymentStatus.ACTIVE; }
//            @Override public String getDepartmentName() { return "IT"; }
//        };
//        Page<EmployeeListView> page = new PageImpl<>(Collections.singletonList(view), pageable, 1);
//
//        when(employeeRepository.findEmployeesWithFiltersProjection(
//                any(), any(), any(), any(), any(), any(), any(), any(Pageable.class)))
//                .thenReturn(page);
//
//        // When
//        Page<EmployeeListView> result = employeeService.searchEmployeesAdvanced(
//                "Jan", "TechCorp", Position.PROGRAMMER, EmploymentStatus.ACTIVE,
//                5000.0, 10000.0, "IT", pageable);
//
//        // Then
//        assertNotNull(result);
//        assertEquals(1, result.getTotalElements());
//        verify(employeeRepository, times(1)).findEmployeesWithFiltersProjection(
//                any(), any(), any(), any(), any(), any(), any(), any(Pageable.class));
//    }

    // ===== TESTY PODSTAWOWE OPERACJE =====

    @Test
    void saveEmployee_ExistingEmployee_ShouldUpdateSuccessfully() throws InvalidDataException {
        // Given
        testEmployee.setId(1L);
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(testEmployee));
        when(employeeRepository.save(any(Employee.class))).thenReturn(testEmployee);

        // When
        Employee result = employeeService.saveEmployee(testEmployee);

        // Then
        assertNotNull(result);
        verify(employeeRepository, times(1)).findById(1L);
        verify(employeeRepository, times(1)).save(testEmployee);
    }

    @Test
    void createEmployee_ValidEmployee_ShouldSaveSuccessfully() throws InvalidDataException {
        // Given
        when(employeeRepository.existsByEmail("jan@example.com")).thenReturn(false);
        when(employeeRepository.save(any(Employee.class))).thenReturn(testEmployee);

        // When
        Employee result = employeeService.createEmployee(testEmployee);

        // Then
        assertNotNull(result);
        assertEquals("jan@example.com", result.getEmail());
        verify(employeeRepository, times(1)).existsByEmail("jan@example.com");
        verify(employeeRepository, times(1)).save(testEmployee);
    }

    @Test
    void createEmployee_DuplicateEmail_ShouldThrowException() {
        // Given
        when(employeeRepository.existsByEmail("jan@example.com")).thenReturn(true);

        // When & Then
        assertThrows(DuplicateEmailException.class, () -> {
            employeeService.createEmployee(testEmployee);
        });
        verify(employeeRepository, times(1)).existsByEmail("jan@example.com");
        verify(employeeRepository, never()).save(any(Employee.class));
    }

    @Test
    void deleteEmployeeByEmail_ShouldDeleteEmployee() {
        // Given
        String email = "jan@example.com";

        // When
        employeeService.deleteEmployeeByEmail(email);

        // Then
        verify(employeeRepository, times(1)).deleteByEmail(email);
    }

    @Test
    void findEmployeeByEmail_ShouldReturnEmployee() {
        // Given
        when(employeeRepository.findByEmail("jan@example.com")).thenReturn(Optional.of(testEmployee));

        // When
        Optional<Employee> result = employeeService.findEmployeeByEmail("jan@example.com");

        // Then
        assertTrue(result.isPresent());
        assertEquals("jan@example.com", result.get().getEmail());
        verify(employeeRepository, times(1)).findByEmail("jan@example.com");
    }

    @Test
    void updateEmployeeStatus_ShouldUpdateStatus() {
        // Given
        String email = "jan@example.com";
        EmploymentStatus newStatus = EmploymentStatus.ON_LEAVE;

        when(employeeRepository.findByEmail(email)).thenReturn(Optional.of(testEmployee));
        when(employeeRepository.save(any(Employee.class))).thenReturn(testEmployee);

        // When
        Employee result = employeeService.updateEmployeeStatus(email, newStatus);

        // Then
        assertNotNull(result);
        assertEquals(newStatus, result.getStatus());
        verify(employeeRepository, times(1)).findByEmail(email);
        verify(employeeRepository, times(1)).save(any(Employee.class));
    }

    @Test
    void updateEmployeeStatus_EmployeeNotFound_ShouldThrowException() {
        // Given
        String email = "nonexistent@example.com";
        when(employeeRepository.findByEmail(email)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(EmployeeNotFoundException.class, () -> {
            employeeService.updateEmployeeStatus(email, EmploymentStatus.ACTIVE);
        });
    }

    // ===== TESTY DEPARTAMENTÓW =====

    @Test
    void assignEmployeeToDepartment_ShouldAssignSuccessfully() {
        // Given
        String email = "jan@example.com";
        Long departmentId = 1L;

        when(employeeRepository.findByEmail(email)).thenReturn(Optional.of(testEmployee));
        when(departmentRepository.findById(departmentId)).thenReturn(Optional.of(testDepartment));
        when(employeeRepository.save(any(Employee.class))).thenReturn(testEmployee);

        // When
        boolean result = employeeService.assignEmployeeToDepartment(email, departmentId);

        // Then
        assertTrue(result);
        assertEquals(testDepartment, testEmployee.getDepartment());
        verify(employeeRepository, times(1)).findByEmail(email);
        verify(departmentRepository, times(1)).findById(departmentId);
        verify(employeeRepository, times(1)).save(testEmployee);
    }

    @Test
    void removeEmployeeFromDepartment_ShouldRemoveSuccessfully() {
        // Given
        String email = "jan@example.com";
        testEmployee.setDepartment(testDepartment);

        when(employeeRepository.findByEmail(email)).thenReturn(Optional.of(testEmployee));
        when(employeeRepository.save(any(Employee.class))).thenReturn(testEmployee);

        // When
        boolean result = employeeService.removeEmployeeFromDepartment(email);

        // Then
        assertTrue(result);
        assertNull(testEmployee.getDepartment());
        verify(employeeRepository, times(1)).findByEmail(email);
        verify(employeeRepository, times(1)).save(testEmployee);
    }

    // ===== TESTY ZDJĘĆ =====

    @Test
    void uploadEmployeePhoto_ValidFile_ShouldUploadSuccessfully() {
        // Given
        MockMultipartFile file = new MockMultipartFile(
                "photo", "test.jpg", "image/jpeg", "test image".getBytes());

        when(employeeRepository.existsByEmail("jan@example.com")).thenReturn(true);
        when(employeeRepository.findByEmail("jan@example.com")).thenReturn(Optional.of(testEmployee));
        when(fileStorageService.storeFileWithCustomName(any(), any(), any())).thenReturn("jan_example_com.jpg");
        when(employeeRepository.save(any(Employee.class))).thenReturn(testEmployee);

        // When
        ResponseEntity<String> result = employeeService.uploadEmployeePhoto("jan@example.com", file);

        // Then
        assertNotNull(result);
        assertEquals(200, result.getStatusCodeValue());
        assertTrue(result.getBody().contains("Photo uploaded successfully"));
        verify(employeeRepository, times(1)).existsByEmail("jan@example.com");
        verify(fileStorageService, times(1)).validateFile(file);
        verify(fileStorageService, times(1)).validateImageFile(file);
        verify(fileStorageService, times(1)).validateFileSize(eq(file), eq(2 * 1024 * 1024L));
        verify(fileStorageService, times(1)).storeFileWithCustomName(file, "photos", "jan_example_com.jpg");
        verify(employeeRepository, times(1)).save(testEmployee);
    }

    @Test
    void deleteEmployeePhoto_ShouldDeleteSuccessfully() {
        // Given
        String email = "jan@example.com";
        testEmployee.setPhotoFileName("photo.jpg");

        when(employeeRepository.findByEmail(email)).thenReturn(Optional.of(testEmployee));

        // When
        employeeService.deleteEmployeePhoto(email);

        // Then
        assertNull(testEmployee.getPhotoFileName());
        verify(employeeRepository, times(1)).findByEmail(email);
        verify(fileStorageService, times(1)).deleteFile("photo.jpg", "photos");
        verify(employeeRepository, times(1)).save(testEmployee);
    }

    // ===== TESTY METOD POMOCNICZYCH =====

    @Test
    void getAvailableManagers_ShouldReturnManagers() {
        // Given
        Employee manager = new Employee("Manager", "manager@example.com", "TechCorp",
                Position.MANAGER, 10000.0, EmploymentStatus.ACTIVE);
        Employee programmer = new Employee("Programmer", "programmer@example.com", "TechCorp",
                Position.PROGRAMMER, 5000.0, EmploymentStatus.ACTIVE);

        List<Employee> employees = Arrays.asList(manager, programmer);
        when(employeeRepository.findAll()).thenReturn(employees);

        // When
        List<Employee> result = employeeService.getAvailableManagers();

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(Position.MANAGER, result.get(0).getPosition());
        verify(employeeRepository, times(1)).findAll();
    }

    @Test
    void getAllUniqueCompanies_ShouldReturnCompanies() {
        // Given
        List<String> companies = Arrays.asList("TechCorp", "OtherCorp");
        when(employeeRepository.findDistinctCompanies()).thenReturn(companies);

        // When
        List<String> result = employeeService.getAllUniqueCompanies();

        // Then
        assertNotNull(result);
        assertEquals(2, result.size());
        verify(employeeRepository, times(1)).findDistinctCompanies();
    }

    // ===== TESTY WALIDACJI =====

    @Test
    void validateEmail_ValidEmail_ShouldNotThrowException() {
        // When & Then
        assertDoesNotThrow(() -> {
            invokePrivateMethod(employeeService, "validateEmail", "test@example.com");
        });
    }

    @Test
    void validateEmail_NullEmail_ShouldThrowException() {
        // When & Then
        assertThrows(RuntimeException.class, () -> {
            invokePrivateMethod(employeeService, "validateEmail", (Object) null);
        });
    }

    @Test
    void validateCompany_ValidCompany_ShouldNotThrowException() {
        // When & Then
        assertDoesNotThrow(() -> {
            invokePrivateMethod(employeeService, "validateCompany", "TechCorp");
        });
    }

    @Test
    void validateCompany_EmptyCompany_ShouldThrowException() {
        // When & Then
        assertThrows(RuntimeException.class, () -> {
            invokePrivateMethod(employeeService, "validateCompany", "");
        });
    }

    // ===== TESTY PRYWATNYCH METOD =====

    @Test
    void determineImageContentType_PngFile_ShouldReturnPngMimeType() {
        // When
        String result = invokePrivateMethod(employeeService, "determineImageContentType", "photo.png");

        // Then
        assertEquals("image/png", result);
    }

    @Test
    void getFileExtension_FileNameWithExtension_ShouldReturnExtension() {
        // When
        String result = invokePrivateMethod(employeeService, "getFileExtension", "photo.jpg");

        // Then
        assertEquals(".jpg", result);
    }

    // ===== POMOCNICZA METODA DO TESTOWANIA PRYWATNYCH METOD =====

    @SuppressWarnings("unchecked")
    private <T> T invokePrivateMethod(Object object, String methodName, Object... params) {
        try {
            Class<?>[] paramTypes = Arrays.stream(params)
                    .map(p -> p != null ? p.getClass() : Object.class)
                    .toArray(Class<?>[]::new);

            Method method = object.getClass().getDeclaredMethod(methodName, paramTypes);
            method.setAccessible(true);
            return (T) method.invoke(object, params);
        } catch (Exception e) {
            throw new RuntimeException("Failed to invoke private method", e);
        }
    }
}