//package com.techcorp.employee.service;
//
//import com.techcorp.employee.dto.CompanyStatisticsDTO;
//import com.techcorp.employee.dto.EmployeeListView;
//import com.techcorp.employee.exception.*;
//import com.techcorp.employee.model.*;
//import com.techcorp.employee.repository.EmployeeRepository;
//import com.techcorp.employee.repository.DepartmentRepository;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//import org.junit.jupiter.api.extension.ExtendWith;
//import org.mockito.InjectMocks;
//import org.mockito.Mock;
//import org.mockito.junit.jupiter.MockitoExtension;
//import org.springframework.data.domain.Page;
//import org.springframework.data.domain.PageImpl;
//import org.springframework.data.domain.PageRequest;
//import org.springframework.data.domain.Pageable;
//import org.springframework.http.ResponseEntity;
//import org.springframework.mock.web.MockMultipartFile;
//import org.springframework.core.io.Resource;
//
//import java.lang.reflect.Method;
//import java.nio.file.Path;
//import java.util.*;
//
//import static org.junit.jupiter.api.Assertions.*;
//import static org.mockito.ArgumentMatchers.*;
//import static org.mockito.Mockito.*;
//
//@ExtendWith(MockitoExtension.class)
//class EmployeeServiceTest {
//
//    @Mock
//    private EmployeeRepository employeeRepository;
//
//    @Mock
//    private DepartmentRepository departmentRepository;
//
//    @Mock
//    private FileStorageService fileStorageService;
//
//    @InjectMocks
//    private EmployeeService employeeService;
//
//    private Employee testEmployee;
//    private Department testDepartment;
//    private CompanyStatisticsDTO testStatisticsDTO;
//
//    @BeforeEach
//    void setUp() {
//        testEmployee = new Employee(
//                "Jan Kowalski",
//                "jan@example.com",
//                "TechCorp",
//                Position.PROGRAMMER,
//                8000.0,
//                EmploymentStatus.ACTIVE
//        );
//        testEmployee.setId(1L);
//
//        testDepartment = new Department("IT", "London", "Opis", "a@a.com", 100.0);
//        testDepartment.setId(1L);
//
//        testStatisticsDTO = new CompanyStatisticsDTO(
//                "TechCorp",
//                10L,
//                7500.0,
//                12000.0,
//                "Jan Kowalski"
//        );
//    }
//
//    // ===== DODATKOWE TESTY OPERACJI MATEMATYCZNYCH =====
//
//    @Test
//    void calculateAverageSalary_ShouldReturnNullWhenNoEmployees() {
//        // Given
//        when(employeeRepository.findAverageSalary()).thenReturn(null);
//
//        // When
//        Double result = employeeService.calculateAverageSalary();
//
//        // Then
//        assertNull(result);
//        verify(employeeRepository, times(1)).findAverageSalary();
//    }
//
//    @Test
//    void findMinSalary_ShouldReturnCorrectValue() {
//        // Given
//        when(employeeRepository.findMinSalary()).thenReturn(3000.0);
//
//        // When
//        Double result = employeeService.findMinSalary();
//
//        // Then
//        assertEquals(3000.0, result, 0.001);
//        verify(employeeRepository, times(1)).findMinSalary();
//    }
//
//    @Test
//    void calculateTotalSalaryCostByCompany_ShouldReturnCorrectValue() {
//        // Given
//        when(employeeRepository.findTotalSalaryCostByCompany("TechCorp")).thenReturn(50000.0);
//
//        // When
//        Double result = employeeService.calculateTotalSalaryCostByCompany("TechCorp");
//
//        // Then
//        assertEquals(50000.0, result, 0.001);
//        verify(employeeRepository, times(1)).findTotalSalaryCostByCompany("TechCorp");
//    }
//
//    @Test
//    void calculateTotalSalaryCostByCompany_InvalidCompany_ShouldThrowException() {
//        // When & Then
//        assertThrows(IllegalArgumentException.class, () -> {
//            employeeService.calculateTotalSalaryCostByCompany("");
//        });
//    }
//
//    @Test
//    void getEmployeeCountByStatus_ShouldReturnCorrectCount() {
//        // Given
//        when(employeeRepository.countEmployeesByStatus(EmploymentStatus.ACTIVE)).thenReturn(8L);
//
//        // When
//        Long result = employeeService.getEmployeeCountByStatus(EmploymentStatus.ACTIVE);
//
//        // Then
//        assertEquals(8L, result);
//        verify(employeeRepository, times(1)).countEmployeesByStatus(EmploymentStatus.ACTIVE);
//    }
//
//    @Test
//    void getEmployeeCountByPosition_ShouldReturnCorrectCount() {
//        // Given
//        when(employeeRepository.countEmployeesByPosition(Position.PROGRAMMER)).thenReturn(5L);
//
//        // When
//        Long result = employeeService.getEmployeeCountByPosition(Position.PROGRAMMER);
//
//        // Then
//        assertEquals(5L, result);
//        verify(employeeRepository, times(1)).countEmployeesByPosition(Position.PROGRAMMER);
//    }
//
//    @Test
//    void getEmployeeCountByDepartment_ShouldReturnCorrectCount() {
//        // Given
//        when(employeeRepository.countEmployeesByDepartment(1L)).thenReturn(3L);
//
//        // When
//        Long result = employeeService.getEmployeeCountByDepartment(1L);
//
//        // Then
//        assertEquals(3L, result);
//        verify(employeeRepository, times(1)).countEmployeesByDepartment(1L);
//    }
//
//    @Test
//    void getEmployeeCountWithoutDepartment_ShouldReturnCorrectCount() {
//        // Given
//        when(employeeRepository.countEmployeesWithoutDepartment()).thenReturn(2L);
//
//        // When
//        Long result = employeeService.getEmployeeCountWithoutDepartment();
//
//        // Then
//        assertEquals(2L, result);
//        verify(employeeRepository, times(1)).countEmployeesWithoutDepartment();
//    }
//
//    // ===== DODATKOWE TESTY STATYSTYK FIRM =====
//
//    @Test
//    void getAllCompanyStatistics_ShouldReturnList() {
//        // Given
//        List<CompanyStatisticsDTO> dtos = Arrays.asList(testStatisticsDTO);
//        when(employeeRepository.getCompanyStatisticsDTO()).thenReturn(dtos);
//
//        // When
//        List<CompanyStatistics> result = employeeService.getAllCompanyStatistics();
//
//        // Then
//        assertNotNull(result);
//        assertEquals(1, result.size());
//        assertEquals("TechCorp", result.get(0).getCompanyName());
//        assertEquals(10L, result.get(0).getEmployeeCount());
//        assertEquals(7500.0, result.get(0).getAverageSalary(), 0.001);
//        verify(employeeRepository, times(1)).getCompanyStatisticsDTO();
//    }
//
//    @Test
//    void getCompanyStatisticsMap_ShouldReturnSortedMap() {
//        // Given
//        CompanyStatisticsDTO dto1 = new CompanyStatisticsDTO("CompanyB", 5L, 6000.0, 8000.0, "John");
//        CompanyStatisticsDTO dto2 = new CompanyStatisticsDTO("CompanyA", 3L, 7000.0, 9000.0, "Anna");
//        List<CompanyStatisticsDTO> dtos = Arrays.asList(dto1, dto2);
//
//        when(employeeRepository.getCompanyStatisticsDTO()).thenReturn(dtos);
//
//        // When
//        Map<String, CompanyStatistics> result = employeeService.getCompanyStatisticsMap();
//
//        // Then
//        assertNotNull(result);
//        assertEquals(2, result.size());
//        // TreeMap powinien być posortowany
//        List<String> keys = new ArrayList<>(result.keySet());
//        assertEquals("CompanyA", keys.get(0));
//        assertEquals("CompanyB", keys.get(1));
//        verify(employeeRepository, times(1)).getCompanyStatisticsDTO();
//    }
//
//    @Test
//    void getPositionStatistics_ShouldReturnMap() {
//        // Given
//        Object[] result1 = {Position.PROGRAMMER, 5L, 6000.0, 8000.0, 4000.0};
//        Object[] result2 = {Position.MANAGER, 2L, 10000.0, 12000.0, 8000.0};
//        List<Object[]> results = Arrays.asList(result1, result2);
//
//        when(employeeRepository.getPositionStatistics()).thenReturn(results);
//
//        // When
//        Map<String, Object> result = employeeService.getPositionStatistics();
//
//        // Then
//        assertNotNull(result);
//        assertEquals(2, result.size());
//        assertTrue(result.containsKey("PROGRAMMER"));
//        assertTrue(result.containsKey("MANAGER"));
//
//        Map<String, Object> programmerStats = (Map<String, Object>) result.get("PROGRAMMER");
//        assertEquals(5L, programmerStats.get("count"));
//        assertEquals(6000.0, programmerStats.get("averageSalary"));
//        assertEquals(8000.0, programmerStats.get("maxSalary"));
//        assertEquals(4000.0, programmerStats.get("minSalary"));
//
//        verify(employeeRepository, times(1)).getPositionStatistics();
//    }
//
//    @Test
//    void getStatusStatistics_ShouldReturnMap() {
//        // Given
//        Object[] result1 = {EmploymentStatus.ACTIVE, 8L, 7500.0};
//        Object[] result2 = {EmploymentStatus.ON_LEAVE, 2L, 6000.0};
//        List<Object[]> results = Arrays.asList(result1, result2);
//
//        when(employeeRepository.getStatusStatistics()).thenReturn(results);
//
//        // When
//        Map<String, Object> result = employeeService.getStatusStatistics();
//
//        // Then
//        assertNotNull(result);
//        assertEquals(2, result.size());
//        assertTrue(result.containsKey("ACTIVE"));
//        assertTrue(result.containsKey("ON_LEAVE"));
//
//        Map<String, Object> activeStats = (Map<String, Object>) result.get("ACTIVE");
//        assertEquals(8L, activeStats.get("count"));
//        assertEquals(7500.0, activeStats.get("averageSalary"));
//
//        verify(employeeRepository, times(1)).getStatusStatistics();
//    }
//
//    @Test
//    void getAverageSalaryByPosition_ShouldReturnMap() {
//        // Given
//        Object[] result1 = {Position.PROGRAMMER, 5L, 6000.0, 8000.0, 4000.0};
//        Object[] result2 = {Position.MANAGER, 2L, 10000.0, 12000.0, 8000.0};
//        List<Object[]> results = Arrays.asList(result1, result2);
//
//        when(employeeRepository.getPositionStatistics()).thenReturn(results);
//
//        // When
//        Map<String, Double> result = employeeService.getAverageSalaryByPosition();
//
//        // Then
//        assertNotNull(result);
//        assertEquals(2, result.size());
//        assertEquals(6000.0, result.get("PROGRAMMER"), 0.001);
//        assertEquals(10000.0, result.get("MANAGER"), 0.001);
//        verify(employeeRepository, times(1)).getPositionStatistics();
//    }
//
//    // ===== DODATKOWE TESTY ZAAWANSOWANYCH ZAPYTAŃ =====
//
//    @Test
//    void findHighestPaidEmployeeByCompany_Exists_ShouldReturnEmployee() {
//        // Given
//        List<Employee> highestPaid = Arrays.asList(testEmployee);
//        when(employeeRepository.findHighestPaidEmployeesByCompany("TechCorp")).thenReturn(highestPaid);
//
//        // When
//        Optional<Employee> result = employeeService.findHighestPaidEmployeeByCompany("TechCorp");
//
//        // Then
//        assertTrue(result.isPresent());
//        assertEquals("jan@example.com", result.get().getEmail());
//        verify(employeeRepository, times(1)).findHighestPaidEmployeesByCompany("TechCorp");
//    }
//
//    @Test
//    void findHighestPaidEmployeeByCompany_NoEmployees_ShouldReturnEmpty() {
//        // Given
//        when(employeeRepository.findHighestPaidEmployeesByCompany("TechCorp")).thenReturn(Collections.emptyList());
//
//        // When
//        Optional<Employee> result = employeeService.findHighestPaidEmployeeByCompany("TechCorp");
//
//        // Then
//        assertFalse(result.isPresent());
//        verify(employeeRepository, times(1)).findHighestPaidEmployeesByCompany("TechCorp");
//    }
//
//    @Test
//    void findHighestPaidEmployeeByCompany_InvalidCompany_ShouldThrowException() {
//        // When & Then
//        assertThrows(IllegalArgumentException.class, () -> {
//            employeeService.findHighestPaidEmployeeByCompany("");
//        });
//    }
//
//    @Test
//    void getEmploymentStatusDistribution_ShouldReturnMap() {
//        // Given
//        Object[] result1 = {EmploymentStatus.ACTIVE, 8L};
//        Object[] result2 = {EmploymentStatus.ON_LEAVE, 2L};
//        List<Object[]> results = Arrays.asList(result1, result2);
//
//        when(employeeRepository.getStatusStatistics()).thenReturn(results);
//
//        // When
//        Map<EmploymentStatus, Long> result = employeeService.getEmploymentStatusDistribution();
//
//        // Then
//        assertNotNull(result);
//        assertEquals(2, result.size());
//        assertEquals(8L, result.get(EmploymentStatus.ACTIVE));
//        assertEquals(2L, result.get(EmploymentStatus.ON_LEAVE));
//        verify(employeeRepository, times(1)).getStatusStatistics();
//    }
//
//    // ===== DODATKOWE TESTY METOD Z PAGINACJĄ =====
//
//    @Test
//    void getAllEmployees_WithPageable_ShouldReturnPagedResults() {
//        // Given
//        Pageable pageable = PageRequest.of(0, 10);
//        Page<Employee> page = new PageImpl<>(Arrays.asList(testEmployee), pageable, 1);
//
//        when(employeeRepository.findAll(pageable)).thenReturn(page);
//
//        // When
//        Page<Employee> result = employeeService.getAllEmployees(pageable);
//
//        // Then
//        assertNotNull(result);
//        assertEquals(1, result.getTotalElements());
//        verify(employeeRepository, times(1)).findAll(pageable);
//    }
//
//    @Test
//    void searchEmployeesWithFilters_ShouldReturnFilteredResults() {
//        // Given
//        Pageable pageable = PageRequest.of(0, 20);
//        EmployeeListView view = mock(EmployeeListView.class);
//        Page<EmployeeListView> page = new PageImpl<>(Collections.singletonList(view), pageable, 1);
//
//        when(employeeRepository.findEmployeesWithFiltersProjection(
//                any(), any(), any(), any(), any(), any(), any(), any(Pageable.class)))
//                .thenReturn(page);
//
//        // When
//        Page<EmployeeListView> result = employeeService.searchEmployeesWithFilters(
//                "Jan", "TechCorp", Position.PROGRAMMER, EmploymentStatus.ACTIVE,
//                5000.0, 10000.0, "IT", pageable);
//
//        // Then
//        assertNotNull(result);
//        assertEquals(1, result.getTotalElements());
//        verify(employeeRepository, times(1)).findEmployeesWithFiltersProjection(
//                any(), any(), any(), any(), any(), any(), any(), any(Pageable.class));
//    }
//
//    @Test
//    void searchEmployeesWithFilters_NullParameters_ShouldHandleGracefully() {
//        // Given
//        Pageable pageable = PageRequest.of(0, 20);
//        Page<EmployeeListView> page = new PageImpl<>(Collections.emptyList(), pageable, 0);
//
//        when(employeeRepository.findEmployeesWithFiltersProjection(
//                any(), any(), any(), any(), any(), any(), any(), any(Pageable.class)))
//                .thenReturn(page);
//
//        // When
//        Page<EmployeeListView> result = employeeService.searchEmployeesWithFilters(
//                null, null, null, null, null, null, null, pageable);
//
//        // Then
//        assertNotNull(result);
//        assertEquals(0, result.getTotalElements());
//        verify(employeeRepository, times(1)).findEmployeesWithFiltersProjection(
//                any(), any(), any(), any(), any(), any(), any(), any(Pageable.class));
//    }
//
//    // ===== DODATKOWE TESTY PODSTAWOWYCH OPERACJI =====
//
//    @Test
//    void getEmployeesByDepartment_ShouldReturnList() {
//        // Given
//        List<Employee> employees = Arrays.asList(testEmployee);
//        when(employeeRepository.findByDepartmentId(1L)).thenReturn(employees);
//
//        // When
//        List<Employee> result = employeeService.getEmployeesByDepartment(1L);
//
//        // Then
//        assertNotNull(result);
//        assertEquals(1, result.size());
//        verify(employeeRepository, times(1)).findByDepartmentId(1L);
//    }
//
//    @Test
//    void getEmployeesByDepartmentId_ShouldReturnList() {
//        // Given
//        List<Employee> employees = Arrays.asList(testEmployee);
//        when(employeeRepository.findByDepartmentId(1L)).thenReturn(employees);
//
//        // When
//        List<Employee> result = employeeService.getEmployeesByDepartmentId(1L);
//
//        // Then
//        assertNotNull(result);
//        assertEquals(1, result.size());
//        verify(employeeRepository, times(1)).findByDepartmentId(1L);
//    }
//
//    @Test
//    void getEmployeesWithoutDepartment_ShouldReturnList() {
//        // Given
//        List<Employee> employees = Arrays.asList(testEmployee);
//        when(employeeRepository.findByDepartmentIsNull()).thenReturn(employees);
//
//        // When
//        List<Employee> result = employeeService.getEmployeesWithoutDepartment();
//
//        // Then
//        assertNotNull(result);
//        assertEquals(1, result.size());
//        verify(employeeRepository, times(1)).findByDepartmentIsNull();
//    }
//
//    // ===== DODATKOWE TESTY PRZYPISANIA DO DEPARTAMENTU =====
//
//    @Test
//    void assignEmployeeToDepartment_EmailCaseSensitive_ShouldHandleCorrectly() {
//        // Given
//        Employee employee = new Employee("Test", "TEST@EXAMPLE.COM", "TechCorp",
//                Position.PROGRAMMER, 5000.0, EmploymentStatus.ACTIVE);
//
//        when(employeeRepository.findByEmail("test@example.com")).thenReturn(Optional.of(employee));
//        when(departmentRepository.findById(1L)).thenReturn(Optional.of(testDepartment));
//        when(employeeRepository.save(any(Employee.class))).thenReturn(employee);
//
//        // When
//        boolean result = employeeService.assignEmployeeToDepartment("TEST@EXAMPLE.COM", 1L);
//
//        // Then
//        assertTrue(result);
//        verify(employeeRepository, times(1)).findByEmail("test@example.com");
//    }
//
//    @Test
//    void assignEmployeeToDepartment_EmployeeAlreadyInDepartment_ShouldStillReturnTrue() {
//        // Given
//        testEmployee.setDepartment(testDepartment);
//        when(employeeRepository.findByEmail("jan@example.com")).thenReturn(Optional.of(testEmployee));
//        when(departmentRepository.findById(1L)).thenReturn(Optional.of(testDepartment));
//        when(employeeRepository.save(any(Employee.class))).thenReturn(testEmployee);
//
//        // When
//        boolean result = employeeService.assignEmployeeToDepartment("jan@example.com", 1L);
//
//        // Then
//        assertTrue(result);
//        verify(employeeRepository, times(1)).save(any(Employee.class));
//    }
//
//    // ===== DODATKOWE TESTY OPERACJI ANALITYCZNYCH =====
//
//    @Test
//    void groupEmployeesByCompany_ShouldReturnSortedMap() {
//        // Given
//        Employee emp1 = new Employee("Jan", "jan@tech.com", "TechCorp",
//                Position.PROGRAMMER, 5000.0, EmploymentStatus.ACTIVE);
//        Employee emp2 = new Employee("Anna", "anna@abc.com", "AbcCorp",
//                Position.MANAGER, 8000.0, EmploymentStatus.ACTIVE);
//
//        List<Employee> employees = Arrays.asList(emp2, emp1); // w odwrotnej kolejności
//        when(employeeRepository.findAll()).thenReturn(employees);
//
//        // When
//        Map<String, List<Employee>> result = employeeService.groupEmployeesByCompany();
//
//        // Then
//        assertNotNull(result);
//        assertEquals(2, result.size());
//        // TreeMap powinien być posortowany
//        List<String> keys = new ArrayList<>(result.keySet());
//        assertEquals("AbcCorp", keys.get(0));
//        assertEquals("TechCorp", keys.get(1));
//        verify(employeeRepository, times(1)).findAll();
//    }
//
//    @Test
//    void validateSalaryConsistency_ShouldReturnEmployeesBelowBaseSalary() {
//        // Given
//        Employee emp1 = new Employee("Jan", "jan@example.com", "TechCorp",
//                Position.PROGRAMMER, 3000.0, EmploymentStatus.ACTIVE);
//        Employee emp2 = new Employee("Anna", "anna@example.com", "TechCorp",
//                Position.MANAGER, 7000.0, EmploymentStatus.ACTIVE);
//
//        List<Employee> employees = Arrays.asList(emp1, emp2);
//        when(employeeRepository.findAll()).thenReturn(employees);
//
//        // When
//        List<Employee> result = employeeService.validateSalaryConsistency(5000.0);
//
//        // Then
//        assertNotNull(result);
//        assertEquals(1, result.size());
//        assertEquals("Jan", result.get(0).getName());
//        verify(employeeRepository, times(1)).findAll();
//    }
//
//    @Test
//    void validateSalaryConsistency_NoEmployeesBelow_ShouldReturnEmptyList() {
//        // Given
//        Employee emp = new Employee("Jan", "jan@example.com", "TechCorp",
//                Position.PROGRAMMER, 6000.0, EmploymentStatus.ACTIVE);
//
//        List<Employee> employees = Arrays.asList(emp);
//        when(employeeRepository.findAll()).thenReturn(employees);
//
//        // When
//        List<Employee> result = employeeService.validateSalaryConsistency(5000.0);
//
//        // Then
//        assertNotNull(result);
//        assertTrue(result.isEmpty());
//        verify(employeeRepository, times(1)).findAll();
//    }
//
//    // ===== DODATKOWE TESTY METOD POMOCNICZYCH =====
//
//    @Test
//    void getAvailableManagers_ShouldIncludeAllManagementPositions() {
//        // Given
//        Employee manager = new Employee("Manager", "manager@example.com", "TechCorp",
//                Position.MANAGER, 10000.0, EmploymentStatus.ACTIVE);
//        Employee vp = new Employee("VP", "vp@example.com", "TechCorp",
//                Position.VICE_PRESIDENT, 15000.0, EmploymentStatus.ACTIVE);
//        Employee president = new Employee("President", "president@example.com", "TechCorp",
//                Position.PRESIDENT, 20000.0, EmploymentStatus.ACTIVE);
//        Employee programmer = new Employee("Programmer", "programmer@example.com", "TechCorp",
//                Position.PROGRAMMER, 5000.0, EmploymentStatus.ACTIVE);
//
//        List<Employee> employees = Arrays.asList(manager, vp, president, programmer);
//        when(employeeRepository.findAll()).thenReturn(employees);
//
//        // When
//        List<Employee> result = employeeService.getAvailableManagers();
//
//        // Then
//        assertNotNull(result);
//        assertEquals(3, result.size());
//        assertTrue(result.stream().anyMatch(e -> e.getPosition() == Position.MANAGER));
//        assertTrue(result.stream().anyMatch(e -> e.getPosition() == Position.VICE_PRESIDENT));
//        assertTrue(result.stream().anyMatch(e -> e.getPosition() == Position.PRESIDENT));
//        assertFalse(result.stream().anyMatch(e -> e.getPosition() == Position.PROGRAMMER));
//        verify(employeeRepository, times(1)).findAll();
//    }
//
//    @Test
//    void getAvailableManagers_NoManagers_ShouldReturnEmptyList() {
//        // Given
//        Employee programmer = new Employee("Programmer", "programmer@example.com", "TechCorp",
//                Position.PROGRAMMER, 5000.0, EmploymentStatus.ACTIVE);
//
//        List<Employee> employees = Arrays.asList(programmer);
//        when(employeeRepository.findAll()).thenReturn(employees);
//
//        // When
//        List<Employee> result = employeeService.getAvailableManagers();
//
//        // Then
//        assertNotNull(result);
//        assertTrue(result.isEmpty());
//        verify(employeeRepository, times(1)).findAll();
//    }
//
//    // ===== DODATKOWE TESTY ZDJĘĆ PRACOWNIKÓW =====
//
//    @Test
//    void uploadEmployeePhoto_InvalidFileType_ShouldThrowException() {
//        // Given
//        MockMultipartFile file = new MockMultipartFile(
//                "photo", "test.txt", "text/plain", "test content".getBytes());
//
//        when(employeeRepository.existsByEmail("jan@example.com")).thenReturn(true);
//        doThrow(new InvalidFileException("Invalid file type"))
//                .when(fileStorageService).validateImageFile(file);
//
//        // When & Then
//        assertThrows(InvalidFileException.class, () -> {
//            employeeService.uploadEmployeePhoto("jan@example.com", file);
//        });
//        verify(employeeRepository, times(1)).existsByEmail("jan@example.com");
//        verify(fileStorageService, times(1)).validateFile(file);
//        verify(fileStorageService, times(1)).validateImageFile(file);
//        verify(fileStorageService, never()).storeFileWithCustomName(any(), any(), any());
//    }
//
//    @Test
//    void uploadEmployeePhoto_FileTooLarge_ShouldThrowException() {
//        // Given
//        MockMultipartFile file = new MockMultipartFile(
//                "photo", "test.jpg", "image/jpeg", "test image".getBytes());
//
//        when(employeeRepository.existsByEmail("jan@example.com")).thenReturn(true);
//        doThrow(new MaxUploadSizeExceededException("File too large"))
//                .when(fileStorageService).validateFileSize(file, 2 * 1024 * 1024);
//
//        // When & Then
//        assertThrows(MaxUploadSizeExceededException.class, () -> {
//            employeeService.uploadEmployeePhoto("jan@example.com", file);
//        });
//        verify(employeeRepository, times(1)).existsByEmail("jan@example.com");
//        verify(fileStorageService, times(1)).validateFile(file);
//        verify(fileStorageService, times(1)).validateImageFile(file);
//        verify(fileStorageService, times(1)).validateFileSize(file, 2 * 1024 * 1024);
//        verify(fileStorageService, never()).storeFileWithCustomName(any(), any(), any());
//    }
//
//    @Test
//    void getEmployeePhoto_EmployeeWithPhoto_ShouldReturnResource() throws Exception {
//        // Given
//        testEmployee.setPhotoFileName("photo.jpg");
//        Path photoPath = mock(Path.class);
//        Resource mockResource = mock(Resource.class);
//
//        when(employeeRepository.findByEmail("jan@example.com")).thenReturn(Optional.of(testEmployee));
//        when(fileStorageService.getPhotosStorageLocation()).thenReturn(photoPath);
//        when(photoPath.resolve("photo.jpg")).thenReturn(photoPath);
//        // Mock UrlResource is complex, so we'll just verify the method calls
//
//        // When
//        ResponseEntity<Resource> result = employeeService.getEmployeePhoto("jan@example.com");
//
//        // Then
//        assertNotNull(result);
//        verify(employeeRepository, times(1)).findByEmail("jan@example.com");
//    }
//
//    @Test
//    void getEmployeePhoto_PhotoNotFound_ShouldReturnNotFound() {
//        // Given
//        testEmployee.setPhotoFileName("photo.jpg");
//        when(employeeRepository.findByEmail("jan@example.com")).thenReturn(Optional.of(testEmployee));
//
//        // Mock that resource doesn't exist
//        // This is complex due to UrlResource, so we'll just verify calls
//
//        // When
//        ResponseEntity<Resource> result = employeeService.getEmployeePhoto("jan@example.com");
//
//        // Then
//        assertNotNull(result);
//        verify(employeeRepository, times(1)).findByEmail("jan@example.com");
//    }
//
//    @Test
//    void getEmployeePhoto_EmployeeNotFound_ShouldReturnNotFound() {
//        // Given
//        when(employeeRepository.findByEmail("nonexistent@example.com")).thenReturn(Optional.empty());
//
//        // When
//        ResponseEntity<Resource> result = employeeService.getEmployeePhoto("nonexistent@example.com");
//
//        // Then
//        assertNotNull(result);
//        assertEquals(404, result.getStatusCodeValue());
//        verify(employeeRepository, times(1)).findByEmail("nonexistent@example.com");
//    }
//
//    // ===== DODATKOWE TESTY WALIDACJI I CRUD =====
//
//    @Test
//    void addAllEmployees_ShouldHandleDuplicatesGracefully() {
//        // Given
//        Employee emp1 = new Employee("Jan", "jan@example.com", "TechCorp",
//                Position.PROGRAMMER, 5000.0, EmploymentStatus.ACTIVE);
//        Employee emp2 = new Employee("Anna", "anna@example.com", "TechCorp",
//                Position.MANAGER, 8000.0, EmploymentStatus.ACTIVE);
//
//        List<Employee> newEmployees = Arrays.asList(emp1, emp2);
//
//        when(employeeRepository.existsByEmail("jan@example.com")).thenReturn(false);
//        when(employeeRepository.existsByEmail("anna@example.com")).thenReturn(true);
//        when(employeeRepository.findByEmail("anna@example.com")).thenReturn(Optional.of(emp2));
//        when(employeeRepository.save(any(Employee.class))).thenReturn(emp1);
//
//        // When
//        employeeService.addAllEmployees(newEmployees);
//
//        // Then
//        verify(employeeRepository, times(1)).existsByEmail("jan@example.com");
//        verify(employeeRepository, times(1)).existsByEmail("anna@example.com");
//        verify(employeeRepository, times(1)).save(emp1);
//        verify(employeeRepository, times(1)).save(emp2); // dla aktualizacji
//    }
//
//    @Test
//    void createEmployee_ValidEmployee_ShouldSaveSuccessfully() {
//        // Given
//        when(employeeRepository.existsByEmail("jan@example.com")).thenReturn(false);
//        when(employeeRepository.save(any(Employee.class))).thenReturn(testEmployee);
//
//        // When
//        Employee result = employeeService.createEmployee(testEmployee);
//
//        // Then
//        assertNotNull(result);
//        assertEquals("jan@example.com", result.getEmail());
//        verify(employeeRepository, times(1)).existsByEmail("jan@example.com");
//        verify(employeeRepository, times(1)).save(testEmployee);
//    }
//
//    @Test
//    void createEmployee_DuplicateEmail_ShouldThrowException() {
//        // Given
//        when(employeeRepository.existsByEmail("jan@example.com")).thenReturn(true);
//
//        // When & Then
//        assertThrows(DuplicateEmailException.class, () -> {
//            employeeService.createEmployee(testEmployee);
//        });
//        verify(employeeRepository, times(1)).existsByEmail("jan@example.com");
//        verify(employeeRepository, never()).save(any(Employee.class));
//    }
//
//    @Test
//    void createEmployee_NullEmployee_ShouldThrowException() {
//        // When & Then
//        assertThrows(InvalidDataException.class, () -> {
//            employeeService.createEmployee(null);
//        });
//    }
//
//    @Test
//    void saveEmployee_NewEmployee_ShouldCallCreateEmployee() {
//        // Given
//        Employee newEmployee = new Employee("Jan", "jan@example.com", "TechCorp",
//                Position.PROGRAMMER, 5000.0, EmploymentStatus.ACTIVE);
//        // ID jest null
//
//        when(employeeRepository.existsByEmail("jan@example.com")).thenReturn(false);
//        when(employeeRepository.save(any(Employee.class))).thenReturn(newEmployee);
//
//        // When
//        Employee result = employeeService.saveEmployee(newEmployee);
//
//        // Then
//        assertNotNull(result);
//        verify(employeeRepository, times(1)).existsByEmail("jan@example.com");
//        verify(employeeRepository, times(1)).save(newEmployee);
//    }
//
//    @Test
//    void saveEmployee_ExistingEmployee_ShouldCallUpdateEmployee() {
//        // Given
//        Employee existingEmployee = new Employee("Jan", "jan@example.com", "TechCorp",
//                Position.PROGRAMMER, 5000.0, EmploymentStatus.ACTIVE);
//        existingEmployee.setId(1L);
//
//        when(employeeRepository.findById(1L)).thenReturn(Optional.of(existingEmployee));
//        when(employeeRepository.save(any(Employee.class))).thenReturn(existingEmployee);
//
//        // When
//        Employee result = employeeService.saveEmployee(existingEmployee);
//
//        // Then
//        assertNotNull(result);
//        verify(employeeRepository, times(1)).findById(1L);
//        verify(employeeRepository, times(1)).save(existingEmployee);
//    }
//
//    // ===== DODATKOWE TESTY WALIDACJI =====
//
//    @Test
//    void findHighestSalaryByCompany_ShouldReturnMaxSalary() {
//        // Given
//        when(employeeRepository.findMaxSalaryByCompany("TechCorp")).thenReturn(12000.0);
//
//        // When
//        Double result = employeeService.findHighestSalaryByCompany("TechCorp");
//
//        // Then
//        assertEquals(12000.0, result, 0.001);
//        verify(employeeRepository, times(1)).findMaxSalaryByCompany("TechCorp");
//    }
//
//    @Test
//    void findHighestSalaryByCompany_InvalidCompany_ShouldThrowException() {
//        // When & Then
//        assertThrows(IllegalArgumentException.class, () -> {
//            employeeService.findHighestSalaryByCompany("");
//        });
//    }
//
//    @Test
//    void getEmployeesByStatus_DeprecatedMethod_ShouldStillWork() {
//        // Given
//        Pageable pageable = PageRequest.of(0, 10);
//        Page<Employee> page = new PageImpl<>(Arrays.asList(testEmployee), pageable, 1);
//
//        when(employeeRepository.findByStatus(EmploymentStatus.ACTIVE, pageable)).thenReturn(page);
//
//        // When
//        Page<Employee> result = employeeService.getEmployeesByStatus(EmploymentStatus.ACTIVE, pageable);
//
//        // Then
//        assertNotNull(result);
//        assertEquals(1, result.getTotalElements());
//        verify(employeeRepository, times(1)).findByStatus(EmploymentStatus.ACTIVE, pageable);
//    }
//
//    @Test
//    void getEmployeesByCompany_DeprecatedMethod_ShouldStillWork() {
//        // Given
//        Pageable pageable = PageRequest.of(0, 10);
//        Page<Employee> page = new PageImpl<>(Arrays.asList(testEmployee), pageable, 1);
//
//        when(employeeRepository.findByCompany("TechCorp", pageable)).thenReturn(page);
//
//        // When
//        Page<Employee> result = employeeService.getEmployeesByCompany("TechCorp", pageable);
//
//        // Then
//        assertNotNull(result);
//        assertEquals(1, result.getTotalElements());
//        verify(employeeRepository, times(1)).findByCompany("TechCorp", pageable);
//    }
//
//    // ===== TESTY BŁĘDÓW I EXCEPTIONS =====
//
//    @Test
//    void updateEmployee_EmployeeNotFound_ShouldThrowException() {
//        // Given
//        when(employeeRepository.findById(999L)).thenReturn(Optional.empty());
//
//        // When & Then
//        assertThrows(IllegalArgumentException.class, () -> {
//            testEmployee.setId(999L);
//            employeeService.updateEmployee(testEmployee);
//        });
//        verify(employeeRepository, times(1)).findById(999L);
//        verify(employeeRepository, never()).save(any(Employee.class));
//    }
//
//    @Test
//    void updateEmployee_EmailChangeToExisting_ShouldThrowException() {
//        // Given
//        Employee existingEmployee = new Employee("Jan", "jan@example.com", "TechCorp",
//                Position.PROGRAMMER, 5000.0, EmploymentStatus.ACTIVE);
//        existingEmployee.setId(1L);
//
//        Employee updatedEmployee = new Employee("Jan Updated", "newemail@example.com", "TechCorp",
//                Position.PROGRAMMER, 6000.0, EmploymentStatus.ACTIVE);
//        updatedEmployee.setId(1L);
//
//        when(employeeRepository.findById(1L)).thenReturn(Optional.of(existingEmployee));
//        when(employeeRepository.existsByEmail("newemail@example.com")).thenReturn(true);
//
//        // When & Then
//        assertThrows(DuplicateEmailException.class, () -> {
//            employeeService.updateEmployee(updatedEmployee);
//        });
//        verify(employeeRepository, times(1)).findById(1L);
//        verify(employeeRepository, times(1)).existsByEmail("newemail@example.com");
//        verify(employeeRepository, never()).save(any(Employee.class));
//    }
//
//    @Test
//    void addEmployee_WithException_ShouldRethrow() {
//        // Given
//        Employee invalidEmployee = null;
//
//        // When & Then
//        assertThrows(NullPointerException.class, () -> {
//            employeeService.addEmployee(invalidEmployee);
//        });
//        verify(employeeRepository, never()).existsByEmail(anyString());
//        verify(employeeRepository, never()).save(any(Employee.class));
//    }
//
//    // ===== TESTY GRANICZNE =====
//
//    @Test
//    void getAllEmployeesSummary_EmptyDatabase_ShouldReturnEmptyPage() {
//        // Given
//        Pageable pageable = PageRequest.of(0, 20);
//        Page<EmployeeListView> page = new PageImpl<>(Collections.emptyList(), pageable, 0);
//
//        when(employeeRepository.findAllEmployeesSummary(pageable)).thenReturn(page);
//
//        // When
//        Page<EmployeeListView> result = employeeService.getAllEmployeesSummary(pageable);
//
//        // Then
//        assertNotNull(result);
//        assertEquals(0, result.getTotalElements());
//        assertTrue(result.getContent().isEmpty());
//        verify(employeeRepository, times(1)).findAllEmployeesSummary(pageable);
//    }
//
//    @Test
//    void findEmployeesWithFiltersOptimized_AllNullParameters_ShouldReturnAll() {
//        // Given
//        Pageable pageable = PageRequest.of(0, 20);
//        EmployeeListView view = mock(EmployeeListView.class);
//        Page<EmployeeListView> page = new PageImpl<>(Collections.singletonList(view), pageable, 1);
//
//        when(employeeRepository.findEmployeesWithFiltersOptimized(
//                any(), any(), any(), any(), any(), any(Pageable.class)))
//                .thenReturn(page);
//
//        // When
//        Page<EmployeeListView> result = employeeService.findEmployeesWithFiltersOptimized(
//                null, null, null, null, null, pageable);
//
//        // Then
//        assertNotNull(result);
//        assertEquals(1, result.getTotalElements());
//        verify(employeeRepository, times(1)).findEmployeesWithFiltersOptimized(
//                any(), any(), any(), any(), any(), any(Pageable.class));
//    }
//
//    @Test
//    void findEmployeesWithFiltersOptimized_InvalidPositionString_ShouldHandleGracefully() {
//        // Given
//        Pageable pageable = PageRequest.of(0, 20);
//        EmployeeListView view = mock(EmployeeListView.class);
//        Page<EmployeeListView> page = new PageImpl<>(Collections.singletonList(view), pageable, 1);
//
//        when(employeeRepository.findEmployeesWithFiltersOptimized(
//                any(), any(), any(), any(), any(), any(Pageable.class)))
//                .thenReturn(page);
//
//        // When
//        Page<EmployeeListView> result = employeeService.findEmployeesWithFiltersOptimized(
//                "Jan", "TechCorp", "INVALID_POSITION", 5000.0, 10000.0, pageable);
//
//        // Then
//        assertNotNull(result);
//        assertEquals(1, result.getTotalElements());
//        // Position powinno być null gdy konwersja się nie uda
//        verify(employeeRepository, times(1)).findEmployeesWithFiltersOptimized(
//                eq("Jan"), eq("TechCorp"), isNull(), eq(5000.0), eq(10000.0), eq(pageable));
//    }
//
//    // ===== TESTY PERFORMANCE =====
//
//    @Test
//    void isEmpty_LargeDatabase_ShouldReturnFalseQuickly() {
//        // Given
//        List<Employee> largeList = new ArrayList<>();
//        for (int i = 0; i < 1000; i++) {
//            largeList.add(new Employee("Employee " + i, "emp" + i + "@example.com",
//                    "Company", Position.PROGRAMMER, 5000.0, EmploymentStatus.ACTIVE));
//        }
//        when(employeeRepository.findAll()).thenReturn(largeList);
//
//        // When
//        boolean result = employeeService.isEmpty();
//
//        // Then
//        assertFalse(result);
//        verify(employeeRepository, times(1)).findAll();
//    }
//
//    @Test
//    void sortEmployeesByName_LargeList_ShouldSortCorrectly() {
//        // Given
//        List<Employee> employees = new ArrayList<>();
//        employees.add(new Employee("Zebra", "zebra@example.com", "Zoo",
//                Position.PROGRAMMER, 5000.0, EmploymentStatus.ACTIVE));
//        employees.add(new Employee("Apple", "apple@example.com", "Zoo",
//                Position.PROGRAMMER, 5000.0, EmploymentStatus.ACTIVE));
//        employees.add(new Employee("Monkey", "monkey@example.com", "Zoo",
//                Position.PROGRAMMER, 5000.0, EmploymentStatus.ACTIVE));
//
//        when(employeeRepository.findAll()).thenReturn(employees);
//
//        // When
//        List<Employee> result = employeeService.sortEmployeesByName();
//
//        // Then
//        assertNotNull(result);
//        assertEquals(3, result.size());
//        assertEquals("Apple", result.get(0).getName());
//        assertEquals("Monkey", result.get(1).getName());
//        assertEquals("Zebra", result.get(2).getName());
//        verify(employeeRepository, times(1)).findAll();
//    }
//
//
//    // ===== TESTY DLA NOWYCH METOD Z EmployeeService.java =====
//
//    @Test
//    void calculateAverageSalaryByCompany_ShouldReturnCorrectValue() {
//        // Given
//        when(employeeRepository.findAverageSalaryByCompany("TechCorp")).thenReturn(7500.0);
//
//        // When
//        Double result = employeeService.calculateAverageSalaryByCompany("TechCorp");
//
//        // Then
//        assertEquals(7500.0, result, 0.001);
//        verify(employeeRepository, times(1)).findAverageSalaryByCompany("TechCorp");
//    }
//
//    @Test
//    void calculateAverageSalaryByCompany_InvalidCompany_ShouldThrowException() {
//        // When & Then
//        assertThrows(IllegalArgumentException.class, () -> {
//            employeeService.calculateAverageSalaryByCompany("");
//        });
//    }
//
//    @Test
//    void findMaxSalary_ShouldReturnCorrectValue() {
//        // Given
//        when(employeeRepository.findMaxSalary()).thenReturn(15000.0);
//
//        // When
//        Double result = employeeService.findMaxSalary();
//
//        // Then
//        assertEquals(15000.0, result, 0.001);
//        verify(employeeRepository, times(1)).findMaxSalary();
//    }
//
//    @Test
//    void findMaxSalaryByCompany_ShouldReturnCorrectValue() {
//        // Given
//        when(employeeRepository.findMaxSalaryByCompany("TechCorp")).thenReturn(12000.0);
//
//        // When
//        Double result = employeeService.findMaxSalaryByCompany("TechCorp");
//
//        // Then
//        assertEquals(12000.0, result, 0.001);
//        verify(employeeRepository, times(1)).findMaxSalaryByCompany("TechCorp");
//    }
//
//    @Test
//    void calculateTotalSalaryCost_ShouldReturnCorrectValue() {
//        // Given
//        when(employeeRepository.findTotalSalaryCost()).thenReturn(150000.0);
//
//        // When
//        Double result = employeeService.calculateTotalSalaryCost();
//
//        // Then
//        assertEquals(150000.0, result, 0.001);
//        verify(employeeRepository, times(1)).findTotalSalaryCost();
//    }
//
//    @Test
//    void getEmployeeCount_ShouldReturnCorrectCount() {
//        // Given
//        when(employeeRepository.countAllEmployees()).thenReturn(50L);
//
//        // When
//        Long result = employeeService.getEmployeeCount();
//
//        // Then
//        assertEquals(50L, result);
//        verify(employeeRepository, times(1)).countAllEmployees();
//    }
//
//    @Test
//    void getEmployeeCountByCompany_ShouldReturnCorrectCount() {
//        // Given
//        when(employeeRepository.countEmployeesByCompany("TechCorp")).thenReturn(10L);
//
//        // When
//        Long result = employeeService.getEmployeeCountByCompany("TechCorp");
//
//        // Then
//        assertEquals(10L, result);
//        verify(employeeRepository, times(1)).countEmployeesByCompany("TechCorp");
//    }
//
//    @Test
//    void getEmployeeCountByCompany_InvalidCompany_ShouldThrowException() {
//        // When & Then
//        assertThrows(IllegalArgumentException.class, () -> {
//            employeeService.getEmployeeCountByCompany("");
//        });
//    }
//
//// ===== TESTY DLA METOD ZWRACAJĄCYCH DTO =====
//
//    @Test
//    void getAllCompanyStatisticsDTO_ShouldReturnList() {
//        // Given
//        List<CompanyStatisticsDTO> dtos = Arrays.asList(testStatisticsDTO);
//        when(employeeRepository.getCompanyStatisticsDTO()).thenReturn(dtos);
//
//        // When
//        List<CompanyStatisticsDTO> result = employeeService.getAllCompanyStatisticsDTO();
//
//        // Then
//        assertNotNull(result);
//        assertEquals(1, result.size());
//        assertEquals("TechCorp", result.get(0).getCompanyName());
//        verify(employeeRepository, times(1)).getCompanyStatisticsDTO();
//    }
//
//    @Test
//    void getCompanyStatisticsDTO_CompanyExists_ShouldReturnOptional() {
//        // Given
//        when(employeeRepository.getCompanyStatisticsDTO("TechCorp")).thenReturn(Optional.of(testStatisticsDTO));
//
//        // When
//        Optional<CompanyStatisticsDTO> result = employeeService.getCompanyStatisticsDTO("TechCorp");
//
//        // Then
//        assertTrue(result.isPresent());
//        assertEquals("TechCorp", result.get().getCompanyName());
//        verify(employeeRepository, times(1)).getCompanyStatisticsDTO("TechCorp");
//    }
//
//    @Test
//    void getCompanyStatisticsDTO_CompanyNotExists_ShouldReturnEmpty() {
//        // Given
//        when(employeeRepository.getCompanyStatisticsDTO("NonExistent")).thenReturn(Optional.empty());
//
//        // When
//        Optional<CompanyStatisticsDTO> result = employeeService.getCompanyStatisticsDTO("NonExistent");
//
//        // Then
//        assertFalse(result.isPresent());
//        verify(employeeRepository, times(1)).getCompanyStatisticsDTO("NonExistent");
//    }
//
//    @Test
//    void getCompanyStatistics_CompanyExists_ShouldReturnCompanyStatistics() {
//        // Given
//        when(employeeRepository.getCompanyStatisticsDTO("TechCorp")).thenReturn(Optional.of(testStatisticsDTO));
//
//        // When
//        CompanyStatistics result = employeeService.getCompanyStatistics("TechCorp");
//
//        // Then
//        assertNotNull(result);
//        assertEquals("TechCorp", result.getCompanyName());
//        assertEquals(10L, result.getEmployeeCount());
//        assertEquals(7500.0, result.getAverageSalary(), 0.001);
//        assertEquals("Jan Kowalski", result.getHighestPaidEmployee());
//        verify(employeeRepository, times(1)).getCompanyStatisticsDTO("TechCorp");
//    }
//
//    @Test
//    void getCompanyStatistics_CompanyNotExists_ShouldReturnEmptyStats() {
//        // Given
//        when(employeeRepository.getCompanyStatisticsDTO("NonExistent")).thenReturn(Optional.empty());
//
//        // When
//        CompanyStatistics result = employeeService.getCompanyStatistics("NonExistent");
//
//        // Then
//        assertNotNull(result);
//        assertEquals("NonExistent", result.getCompanyName());
//        assertEquals(0, result.getEmployeeCount());
//        assertEquals(0.0, result.getAverageSalary(), 0.001);
//        verify(employeeRepository, times(1)).getCompanyStatisticsDTO("NonExistent");
//    }
//
//// ===== TESTY DLA ZAAWANSOWANYCH ZAPYTAŃ SQL =====
//
//    @Test
//    void findHighestPaidEmployee_Exists_ShouldReturnEmployee() {
//        // Given
//        List<Employee> highestPaid = Arrays.asList(testEmployee);
//        when(employeeRepository.findHighestPaidEmployees()).thenReturn(highestPaid);
//
//        // When
//        Optional<Employee> result = employeeService.findHighestPaidEmployee();
//
//        // Then
//        assertTrue(result.isPresent());
//        assertEquals("jan@example.com", result.get().getEmail());
//        verify(employeeRepository, times(1)).findHighestPaidEmployees();
//    }
//
//    @Test
//    void findHighestPaidEmployee_NoEmployees_ShouldReturnEmpty() {
//        // Given
//        when(employeeRepository.findHighestPaidEmployees()).thenReturn(Collections.emptyList());
//
//        // When
//        Optional<Employee> result = employeeService.findHighestPaidEmployee();
//
//        // Then
//        assertFalse(result.isPresent());
//        verify(employeeRepository, times(1)).findHighestPaidEmployees();
//    }
//
//    @Test
//    void findEmployeesBelowAverageSalary_ShouldReturnList() {
//        // Given
//        List<Employee> employees = Arrays.asList(testEmployee);
//        when(employeeRepository.findEmployeesBelowAverageSalary()).thenReturn(employees);
//
//        // When
//        List<Employee> result = employeeService.findEmployeesBelowAverageSalary();
//
//        // Then
//        assertNotNull(result);
//        assertEquals(1, result.size());
//        verify(employeeRepository, times(1)).findEmployeesBelowAverageSalary();
//    }
//
//    @Test
//    void findTop10HighestPaidEmployees_ShouldReturnLimitedList() {
//        // Given
//        List<Employee> top10 = new ArrayList<>();
//        for (int i = 0; i < 10; i++) {
//            Employee emp = new Employee("Emp " + i, "emp" + i + "@example.com",
//                    "Company", Position.PROGRAMMER, 10000.0 - i * 100, EmploymentStatus.ACTIVE);
//            top10.add(emp);
//        }
//        when(employeeRepository.findTop10HighestPaidEmployees(any(Pageable.class))).thenReturn(top10);
//
//        // When
//        List<Employee> result = employeeService.findTop10HighestPaidEmployees();
//
//        // Then
//        assertNotNull(result);
//        assertEquals(10, result.size());
//        verify(employeeRepository, times(1)).findTop10HighestPaidEmployees(any(Pageable.class));
//    }
//
//// ===== TESTY DLA METOD Z PROJEKCJĄ =====
//
//    @Test
//    void getEmployeesByStatusProjection_ShouldReturnProjection() {
//        // Given
//        Pageable pageable = PageRequest.of(0, 10);
//        EmployeeListView view = mock(EmployeeListView.class);
//        Page<EmployeeListView> page = new PageImpl<>(Collections.singletonList(view), pageable, 1);
//
//        when(employeeRepository.findByStatusProjection(EmploymentStatus.ACTIVE, pageable)).thenReturn(page);
//
//        // When
//        Page<EmployeeListView> result = employeeService.getEmployeesByStatusProjection(EmploymentStatus.ACTIVE, pageable);
//
//        // Then
//        assertNotNull(result);
//        assertEquals(1, result.getTotalElements());
//        verify(employeeRepository, times(1)).findByStatusProjection(EmploymentStatus.ACTIVE, pageable);
//    }
//
//    @Test
//    void getEmployeesByCompanyProjection_ShouldReturnProjection() {
//        // Given
//        Pageable pageable = PageRequest.of(0, 10);
//        EmployeeListView view = mock(EmployeeListView.class);
//        Page<EmployeeListView> page = new PageImpl<>(Collections.singletonList(view), pageable, 1);
//
//        when(employeeRepository.findByCompanyProjection("TechCorp", pageable)).thenReturn(page);
//
//        // When
//        Page<EmployeeListView> result = employeeService.getEmployeesByCompanyProjection("TechCorp", pageable);
//
//        // Then
//        assertNotNull(result);
//        assertEquals(1, result.getTotalElements());
//        verify(employeeRepository, times(1)).findByCompanyProjection("TechCorp", pageable);
//    }
//
//    @Test
//    void getEmployeesByCompanyProjection_InvalidCompany_ShouldThrowException() {
//        // Given
//        Pageable pageable = PageRequest.of(0, 10);
//
//        // When & Then
//        assertThrows(IllegalArgumentException.class, () -> {
//            employeeService.getEmployeesByCompanyProjection("", pageable);
//        });
//    }
//
//// ===== TESTY DLA OPERACJI CRUD =====
//
//    @Test
//    void deleteEmployeeByEmail_ExistingEmployee_ShouldDelete() {
//        // Given
//        String email = "jan@example.com";
//
//        // When
//        employeeService.deleteEmployeeByEmail(email);
//
//        // Then
//        verify(employeeRepository, times(1)).deleteByEmail(email);
//    }
//
//    @Test
//    void deleteEmployeeByEmail_InvalidEmail_ShouldThrowException() {
//        // When & Then
//        assertThrows(IllegalArgumentException.class, () -> {
//            employeeService.deleteEmployeeByEmail("");
//        });
//    }
//
//    @Test
//    void updateEmployeeStatus_ExistingEmployee_ShouldUpdateStatus() {
//        // Given
//        String email = "jan@example.com";
//        EmploymentStatus newStatus = EmploymentStatus.ON_LEAVE;
//
//        when(employeeRepository.findByEmail(email)).thenReturn(Optional.of(testEmployee));
//        when(employeeRepository.save(any(Employee.class))).thenReturn(testEmployee);
//
//        // When
//        Employee result = employeeService.updateEmployeeStatus(email, newStatus);
//
//        // Then
//        assertNotNull(result);
//        assertEquals(newStatus, result.getStatus());
//        verify(employeeRepository, times(1)).findByEmail(email);
//        verify(employeeRepository, times(1)).save(any(Employee.class));
//    }
//
//    @Test
//    void updateEmployeeStatus_EmployeeNotFound_ShouldThrowException() {
//        // Given
//        String email = "nonexistent@example.com";
//
//        when(employeeRepository.findByEmail(email)).thenReturn(Optional.empty());
//
//        // When & Then
//        assertThrows(EmployeeNotFoundException.class, () -> {
//            employeeService.updateEmployeeStatus(email, EmploymentStatus.ACTIVE);
//        });
//    }
//
//    @Test
//    void updateEmployeeStatus_InvalidEmail_ShouldThrowException() {
//        // When & Then
//        assertThrows(IllegalArgumentException.class, () -> {
//            employeeService.updateEmployeeStatus("", EmploymentStatus.ACTIVE);
//        });
//    }
//
//    @Test
//    void employeeExists_ExistingEmail_ShouldReturnTrue() {
//        // Given
//        when(employeeRepository.existsByEmail("jan@example.com")).thenReturn(true);
//
//        // When
//        boolean result = employeeService.employeeExists("jan@example.com");
//
//        // Then
//        assertTrue(result);
//        verify(employeeRepository, times(1)).existsByEmail("jan@example.com");
//    }
//
//    @Test
//    void employeeExists_NonExistingEmail_ShouldReturnFalse() {
//        // Given
//        when(employeeRepository.existsByEmail("nonexistent@example.com")).thenReturn(false);
//
//        // When
//        boolean result = employeeService.employeeExists("nonexistent@example.com");
//
//        // Then
//        assertFalse(result);
//        verify(employeeRepository, times(1)).existsByEmail("nonexistent@example.com");
//    }
//
//// ===== TESTY DLA PRZYPISANIA DO DEPARTAMENTU =====
//
//    @Test
//    void removeEmployeeFromDepartment_ExistingEmployee_ShouldRemoveDepartment() {
//        // Given
//        String email = "jan@example.com";
//        testEmployee.setDepartment(testDepartment);
//
//        when(employeeRepository.findByEmail(email)).thenReturn(Optional.of(testEmployee));
//        when(employeeRepository.save(any(Employee.class))).thenReturn(testEmployee);
//
//        // When
//        boolean result = employeeService.removeEmployeeFromDepartment(email);
//
//        // Then
//        assertTrue(result);
//        assertNull(testEmployee.getDepartment());
//        verify(employeeRepository, times(1)).findByEmail(email);
//        verify(employeeRepository, times(1)).save(testEmployee);
//    }
//
//    @Test
//    void removeEmployeeFromDepartment_EmployeeNotFound_ShouldReturnFalse() {
//        // Given
//        String email = "nonexistent@example.com";
//
//        when(employeeRepository.findByEmail(email)).thenReturn(Optional.empty());
//
//        // When
//        boolean result = employeeService.removeEmployeeFromDepartment(email);
//
//        // Then
//        assertFalse(result);
//        verify(employeeRepository, times(1)).findByEmail(email);
//        verify(employeeRepository, never()).save(any(Employee.class));
//    }
//
//// ===== TESTY DLA OPERACJI ANALITYCZNYCH =====
//
//    @Test
//    void groupEmployeesByPosition_ShouldReturnSortedMap() {
//        // Given
//        Employee programmer = new Employee("Programmer", "programmer@example.com", "TechCorp",
//                Position.PROGRAMMER, 5000.0, EmploymentStatus.ACTIVE);
//        Employee manager = new Employee("Manager", "manager@example.com", "TechCorp",
//                Position.MANAGER, 10000.0, EmploymentStatus.ACTIVE);
//
//        List<Employee> employees = Arrays.asList(manager, programmer); // w odwrotnej kolejności
//        when(employeeRepository.findAll()).thenReturn(employees);
//
//        // When
//        Map<Position, List<Employee>> result = employeeService.groupEmployeesByPosition();
//
//        // Then
//        assertNotNull(result);
//        assertEquals(2, result.size());
//        // TreeMap powinien być posortowany według enum order
//        List<Position> positions = new ArrayList<>(result.keySet());
//        assertEquals(Position.MANAGER, positions.get(0));
//        assertEquals(Position.PROGRAMMER, positions.get(1));
//        verify(employeeRepository, times(1)).findAll();
//    }
//
//    @Test
//    void countEmployeesByPosition_ShouldReturnCounts() {
//        // Given
//        Employee programmer1 = new Employee("Programmer1", "prog1@example.com", "TechCorp",
//                Position.PROGRAMMER, 5000.0, EmploymentStatus.ACTIVE);
//        Employee programmer2 = new Employee("Programmer2", "prog2@example.com", "TechCorp",
//                Position.PROGRAMMER, 6000.0, EmploymentStatus.ACTIVE);
//        Employee manager = new Employee("Manager", "manager@example.com", "TechCorp",
//                Position.MANAGER, 10000.0, EmploymentStatus.ACTIVE);
//
//        List<Employee> employees = Arrays.asList(programmer1, programmer2, manager);
//        when(employeeRepository.findAll()).thenReturn(employees);
//
//        // When
//        Map<Position, Long> result = employeeService.countEmployeesByPosition();
//
//        // Then
//        assertNotNull(result);
//        assertEquals(2, result.size());
//        assertEquals(2L, result.get(Position.PROGRAMMER));
//        assertEquals(1L, result.get(Position.MANAGER));
//        verify(employeeRepository, times(1)).findAll();
//    }
//
//    @Test
//    void isEmpty_EmptyDatabase_ShouldReturnTrue() {
//        // Given
//        when(employeeRepository.findAll()).thenReturn(Collections.emptyList());
//
//        // When
//        boolean result = employeeService.isEmpty();
//
//        // Then
//        assertTrue(result);
//        verify(employeeRepository, times(1)).findAll();
//    }
//
//// ===== TESTY DLA ZARZĄDZANIA ZDJĘCIAMI =====
//
//    @Test
//    void uploadEmployeePhoto_ValidFile_ShouldUploadSuccessfully() {
//        // Given
//        MockMultipartFile file = new MockMultipartFile(
//                "photo", "test.jpg", "image/jpeg", "test image".getBytes());
//
//        when(employeeRepository.existsByEmail("jan@example.com")).thenReturn(true);
//        when(employeeRepository.findByEmail("jan@example.com")).thenReturn(Optional.of(testEmployee));
//        when(fileStorageService.storeFileWithCustomName(any(), any(), any())).thenReturn("jan_example_com.jpg");
//        when(employeeRepository.save(any(Employee.class))).thenReturn(testEmployee);
//
//        // When
//        ResponseEntity<String> result = employeeService.uploadEmployeePhoto("jan@example.com", file);
//
//        // Then
//        assertNotNull(result);
//        assertEquals(200, result.getStatusCodeValue());
//        assertTrue(result.getBody().contains("Photo uploaded successfully"));
//        verify(employeeRepository, times(1)).existsByEmail("jan@example.com");
//        verify(fileStorageService, times(1)).validateFile(file);
//        verify(fileStorageService, times(1)).validateImageFile(file);
//        verify(fileStorageService, times(1)).validateFileSize(file, 2 * 1024 * 1024);
//        verify(fileStorageService, times(1)).storeFileWithCustomName(file, "photos", "jan_example_com.jpg");
//        verify(employeeRepository, times(1)).save(testEmployee);
//    }
//
//    @Test
//    void uploadEmployeePhoto_EmployeeNotFound_ShouldThrowException() {
//        // Given
//        MockMultipartFile file = new MockMultipartFile(
//                "photo", "test.jpg", "image/jpeg", "test image".getBytes());
//
//        when(employeeRepository.existsByEmail("nonexistent@example.com")).thenReturn(false);
//
//        // When & Then
//        assertThrows(EmployeeNotFoundException.class, () -> {
//            employeeService.uploadEmployeePhoto("nonexistent@example.com", file);
//        });
//
//        verify(employeeRepository, times(1)).existsByEmail("nonexistent@example.com");
//        verify(fileStorageService, never()).validateFile(any());
//        verify(fileStorageService, never()).storeFileWithCustomName(any(), any(), any());
//    }
//
////    @Test
////    void uploadEmployeePhoto_NullFile_ShouldThrowException() {
////        // Given
////        when(employeeRepository.existsByEmail("jan@example.com")).thenReturn(true);
////
////        // When & Then
////        assertThrows(NullPointerException.class, () -> {
////            employeeService.uploadEmployeePhoto("jan@example.com", null);
////        });
////
////        verify(employeeRepository, times(1)).existsByEmail("jan@example.com");
////        verify(fileStorageService, never()).validateFile(any());
////    }
//
//    @Test
//    void deleteEmployeePhoto_EmployeeWithPhoto_ShouldDeletePhoto() {
//        // Given
//        String email = "jan@example.com";
//        testEmployee.setPhotoFileName("photo.jpg");
//
//        when(employeeRepository.findByEmail(email)).thenReturn(Optional.of(testEmployee));
//
//        // When
//        employeeService.deleteEmployeePhoto(email);
//
//        // Then
//        verify(employeeRepository, times(1)).findByEmail(email);
//        verify(fileStorageService, times(1)).deleteFile("photo.jpg", "photos");
//        verify(employeeRepository, times(1)).save(testEmployee);
//        assertNull(testEmployee.getPhotoFileName());
//    }
//
//    @Test
//    void deleteEmployeePhoto_EmployeeWithoutPhoto_ShouldNotThrowException() {
//        // Given
//        String email = "jan@example.com";
//        testEmployee.setPhotoFileName(null);
//
//        when(employeeRepository.findByEmail(email)).thenReturn(Optional.of(testEmployee));
//
//        // When
//        employeeService.deleteEmployeePhoto(email);
//
//        // Then
//        verify(employeeRepository, times(1)).findByEmail(email);
//        verify(fileStorageService, never()).deleteFile(any(), any());
//    }
//
//    @Test
//    void deleteEmployeePhoto_FileStorageException_ShouldRethrow() {
//        // Given
//        String email = "jan@example.com";
//        testEmployee.setPhotoFileName("photo.jpg");
//
//        when(employeeRepository.findByEmail(email)).thenReturn(Optional.of(testEmployee));
//        doThrow(new FileStorageException("Error deleting file"))
//                .when(fileStorageService).deleteFile("photo.jpg", "photos");
//
//        // When & Then
//        assertThrows(FileStorageException.class, () -> {
//            employeeService.deleteEmployeePhoto(email);
//        });
//
//        verify(employeeRepository, times(1)).findByEmail(email);
//        verify(fileStorageService, times(1)).deleteFile("photo.jpg", "photos");
//    }
//
//// ===== TESTY DLA METOD POMOCNICZYCH =====
//
//    @Test
//    void removeEmployee_ExistingEmployee_ShouldReturnTrue() {
//        // Given
//        String email = "jan@example.com";
//
//        when(employeeRepository.existsByEmail(email)).thenReturn(true);
//
//        // When
//        boolean result = employeeService.removeEmployee(email);
//
//        // Then
//        assertTrue(result);
//        verify(employeeRepository, times(1)).existsByEmail(email);
//        verify(employeeRepository, times(1)).deleteByEmail(email);
//    }
//
//    @Test
//    void removeEmployee_NonExistingEmployee_ShouldReturnFalse() {
//        // Given
//        String email = "nonexistent@example.com";
//
//        when(employeeRepository.existsByEmail(email)).thenReturn(false);
//
//        // When
//        boolean result = employeeService.removeEmployee(email);
//
//        // Then
//        assertFalse(result);
//        verify(employeeRepository, times(1)).existsByEmail(email);
//        verify(employeeRepository, never()).deleteByEmail(email);
//    }
//
//// ===== TESTY WALIDACJI GRANICZNYCH =====
//
//    @Test
//    void validateSalaryConsistency_WithZeroBaseSalary_ShouldReturnAllEmployees() {
//        // Given
//        Employee emp1 = new Employee("Jan", "jan@example.com", "TechCorp",
//                Position.PROGRAMMER, 5000.0, EmploymentStatus.ACTIVE);
//
//        List<Employee> employees = Arrays.asList(emp1);
//        when(employeeRepository.findAll()).thenReturn(employees);
//
//        // When
//        List<Employee> result = employeeService.validateSalaryConsistency(0.0);
//
//        // Then
//        assertNotNull(result);
//        assertEquals(0, result.size()); // Nikt nie ma pensji poniżej 0
//        verify(employeeRepository, times(1)).findAll();
//    }
//
//    @Test
//    void validateSalaryConsistency_WithNegativeBaseSalary_ShouldReturnAllEmployees() {
//        // Given
//        Employee emp1 = new Employee("Jan", "jan@example.com", "TechCorp",
//                Position.PROGRAMMER, 5000.0, EmploymentStatus.ACTIVE);
//
//        List<Employee> employees = Arrays.asList(emp1);
//        when(employeeRepository.findAll()).thenReturn(employees);
//
//        // When
//        List<Employee> result = employeeService.validateSalaryConsistency(-1000.0);
//
//        // Then
//        assertNotNull(result);
//        assertEquals(0, result.size()); // Nikt nie ma pensji poniżej -1000
//        verify(employeeRepository, times(1)).findAll();
//    }
//
//    @Test
//    void getFileExtension_FileNameWithMultipleDots_ShouldReturnCorrectExtension() {
//        // Given
//        EmployeeService service = new EmployeeService(employeeRepository, departmentRepository, fileStorageService);
//
//        // When
//        String result = invokePrivateMethod(service, "getFileExtension", "photo.test.jpg");
//
//        // Then
//        assertEquals(".jpg", result);
//    }
//
//    @Test
//    void getFileExtension_FileNameWithoutExtension_ShouldReturnDefault() {
//        // Given
//        EmployeeService service = new EmployeeService(employeeRepository, departmentRepository, fileStorageService);
//
//        // When
//        String result = invokePrivateMethod(service, "getFileExtension", "photo");
//
//        // Then
//        assertEquals(".jpg", result);
//    }
//
////    @Test
////    void getFileExtension_NullFileName_ShouldReturnDefault() {
////        // Given
////        EmployeeService service = new EmployeeService(employeeRepository, departmentRepository, fileStorageService);
////
////        // When
////        String result = invokePrivateMethod(service, "getFileExtension", (Object) null);
////
////        // Then
////        assertEquals(".jpg", result);
////    }
//
//    @Test
//    void determineImageContentType_PngFile_ShouldReturnPngMimeType() {
//        // Given
//        EmployeeService service = new EmployeeService(employeeRepository, departmentRepository, fileStorageService);
//
//        // When
//        String result = invokePrivateMethod(service, "determineImageContentType", "photo.png");
//
//        // Then
//        assertEquals("image/png", result);
//    }
//
//    @Test
//    void determineImageContentType_GifFile_ShouldReturnGifMimeType() {
//        // Given
//        EmployeeService service = new EmployeeService(employeeRepository, departmentRepository, fileStorageService);
//
//        // When
//        String result = invokePrivateMethod(service, "determineImageContentType", "image.gif");
//
//        // Then
//        assertEquals("image/gif", result);
//    }
//
//    @Test
//    void determineImageContentType_JpgFile_ShouldReturnJpegMimeType() {
//        // Given
//        EmployeeService service = new EmployeeService(employeeRepository, departmentRepository, fileStorageService);
//
//        // When
//        String result = invokePrivateMethod(service, "determineImageContentType", "photo.jpg");
//
//        // Then
//        assertEquals("image/jpeg", result);
//    }
//
//    @Test
//    void determineImageContentType_JpegFile_ShouldReturnJpegMimeType() {
//        // Given
//        EmployeeService service = new EmployeeService(employeeRepository, departmentRepository, fileStorageService);
//
//        // When
//        String result = invokePrivateMethod(service, "determineImageContentType", "image.jpeg");
//
//        // Then
//        assertEquals("image/jpeg", result);
//    }
//
//    @Test
//    void determineImageContentType_UnknownExtension_ShouldReturnJpegMimeType() {
//        // Given
//        EmployeeService service = new EmployeeService(employeeRepository, departmentRepository, fileStorageService);
//
//        // When
//        String result = invokePrivateMethod(service, "determineImageContentType", "document.pdf");
//
//        // Then
//        assertEquals("image/jpeg", result);
//    }
//
//// ===== TESTY KONSTRUKTORA I ZALEŻNOŚCI =====
//
//
//    @Test
//    void constructor_AllDependenciesProvided_ShouldCreateInstance() {
//        // When
//        EmployeeService service = new EmployeeService(employeeRepository, departmentRepository, fileStorageService);
//
//        // Then
//        assertNotNull(service);
//    }
//
//// ===== TESTY DLA GETTERÓW I SETTERÓW MODELI =====
//
//    @Test
//    void employeeModel_SettersAndGetters_ShouldWorkCorrectly() {
//        // Given
//        Employee employee = new Employee();
//        Department department = new Department();
//
//        // When
//        employee.setId(1L);
//        employee.setName("John Doe");
//        employee.setEmail("john@example.com");
//        employee.setCompany("TechCorp");
//        employee.setPosition(Position.PROGRAMMER);
//        employee.setSalary(5000.0);
//        employee.setStatus(EmploymentStatus.ACTIVE);
//        employee.setDepartment(department);
//        employee.setPhotoFileName("photo.jpg");
//
//        // Then
//        assertEquals(1L, employee.getId());
//        assertEquals("John Doe", employee.getName());
//        assertEquals("john@example.com", employee.getEmail());
//        assertEquals("TechCorp", employee.getCompany());
//        assertEquals(Position.PROGRAMMER, employee.getPosition());
//        assertEquals(5000.0, employee.getSalary(), 0.001);
//        assertEquals(EmploymentStatus.ACTIVE, employee.getStatus());
//        assertEquals(department, employee.getDepartment());
//        assertEquals("photo.jpg", employee.getPhotoFileName());
//    }
//
//    @Test
//    void departmentModel_SettersAndGetters_ShouldWorkCorrectly() {
//        // Given
//        Department department = new Department();
//
//        // When
//        department.setId(1L);
//        department.setName("IT");
//        department.setLocation("London");
//        department.setDescription("IT Department");
//        department.setBudget(100000.0);
//
//        // Then
//        assertEquals(1L, department.getId());
//        assertEquals("IT", department.getName());
//        assertEquals("London", department.getLocation());
//        assertEquals("IT Department", department.getDescription());
//        assertEquals(100000.0, department.getBudget(), 0.001);
//    }
//
//// ===== TESTY DLA TO STRING I EQUALS/HASHCODE =====
//
//    @Test
//    void employeeToString_ShouldReturnStringRepresentation() {
//        // Given
//        Employee employee = new Employee("John Doe", "john@example.com", "TechCorp",
//                Position.PROGRAMMER, 5000.0, EmploymentStatus.ACTIVE);
//        employee.setId(1L);
//
//        // When
//        String result = employee.toString();
//
//        // Then
//        assertNotNull(result);
//        assertTrue(result.contains("John Doe"));
//        assertTrue(result.contains("john@example.com"));
//    }
//
//    @Test
//    void employeeEquals_SameId_ShouldBeEqual() {
//        // Given
//        Employee emp1 = new Employee();
//        emp1.setId(1L);
//
//        Employee emp2 = new Employee();
//        emp2.setId(1L);
//
//        // Then
//        assertEquals(emp1, emp2);
//        assertEquals(emp1.hashCode(), emp2.hashCode());
//    }
//
////    @Test
////    void employeeEquals_DifferentId_ShouldNotBeEqual() {
////        // Given
////        Employee emp1 = new Employee();
////        emp1.setId(1L);
////
////        Employee emp2 = new Employee();
////        emp2.setId(2L);
////
////        // Then
////        assertNotEquals(emp1, emp2);
////    }
//
//    @Test
//    void employeeEquals_SameInstance_ShouldBeEqual() {
//        // Given
//        Employee emp1 = new Employee();
//        emp1.setId(1L);
//
//        // Then
//        assertEquals(emp1, emp1);
//    }
//
//    @Test
//    void employeeEquals_Null_ShouldNotBeEqual() {
//        // Given
//        Employee emp1 = new Employee();
//        emp1.setId(1L);
//
//        // Then
//        assertNotEquals(emp1, null);
//    }
//
//    @Test
//    void employeeEquals_DifferentClass_ShouldNotBeEqual() {
//        // Given
//        Employee emp1 = new Employee();
//        emp1.setId(1L);
//
//        // Then
//        assertNotEquals(emp1, new Object());
//    }
//
//// ===== POMOCNICZE METODY DO TESTOWANIA PRYWATNYCH METOD =====
//
//    @SuppressWarnings("unchecked")
//    private <T> T invokePrivateMethod(Object object, String methodName, Object... params) {
//        try {
//            Class<?>[] paramTypes = Arrays.stream(params)
//                    .map(Object::getClass)
//                    .toArray(Class<?>[]::new);
//
//            Method method = object.getClass().getDeclaredMethod(methodName, paramTypes);
//            method.setAccessible(true);
//            return (T) method.invoke(object, params);
//        } catch (Exception e) {
//            throw new RuntimeException("Failed to invoke private method", e);
//        }
//    }
//
//// ===== TESTY DLA RÓŻNYCH SCENARIUSZY BŁĘDÓW =====
//
//    @Test
//    void updateEmployee_WithNullIdAndNonExistingEmail_ShouldThrowException() {
//        // Given
//        Employee employee = new Employee("Test", "new@example.com", "TechCorp",
//                Position.PROGRAMMER, 5000.0, EmploymentStatus.ACTIVE);
//        employee.setId(null); // ID jest null
//
//        when(employeeRepository.findByEmail("new@example.com")).thenReturn(Optional.empty());
//
//        // When & Then
//        assertThrows(IllegalArgumentException.class, () -> {
//            employeeService.updateEmployee(employee);
//        });
//
//        verify(employeeRepository, times(1)).findByEmail("new@example.com");
//        verify(employeeRepository, never()).save(any(Employee.class));
//    }
//
//    @Test
//    void saveEmployee_ExistingEmployeeWithDifferentEmail_ShouldUpdateSuccessfully() {
//        // Given
//        Employee existingEmployee = new Employee("Jan", "old@example.com", "TechCorp",
//                Position.PROGRAMMER, 5000.0, EmploymentStatus.ACTIVE);
//        existingEmployee.setId(1L);
//
//        Employee updatedEmployee = new Employee("Jan Updated", "new@example.com", "TechCorp",
//                Position.MANAGER, 6000.0, EmploymentStatus.ACTIVE);
//        updatedEmployee.setId(1L);
//
//        when(employeeRepository.findById(1L)).thenReturn(Optional.of(existingEmployee));
//        when(employeeRepository.existsByEmail("new@example.com")).thenReturn(false);
//        when(employeeRepository.save(any(Employee.class))).thenReturn(updatedEmployee);
//
//        // When
//        Employee result = employeeService.saveEmployee(updatedEmployee);
//
//        // Then
//        assertNotNull(result);
//        assertEquals("Jan Updated", result.getName());
//        assertEquals("new@example.com", result.getEmail());
//        assertEquals(Position.MANAGER, result.getPosition());
//        verify(employeeRepository, times(1)).findById(1L);
//        verify(employeeRepository, times(1)).existsByEmail("new@example.com");
//        verify(employeeRepository, times(1)).save(any(Employee.class));
//    }
//
//// ===== TESTY DLA SYTUACJI Z DUPLIKATAMI =====
//
//    @Test
//    void addAllEmployees_WithMixedNewAndExisting_ShouldHandleCorrectly() {
//        // Given
//        Employee newEmployee = new Employee("New", "new@example.com", "TechCorp",
//                Position.PROGRAMMER, 5000.0, EmploymentStatus.ACTIVE);
//
//        Employee existingEmployee = new Employee("Existing", "existing@example.com", "TechCorp",
//                Position.MANAGER, 8000.0, EmploymentStatus.ACTIVE);
//
//        List<Employee> employees = Arrays.asList(newEmployee, existingEmployee);
//
//        when(employeeRepository.existsByEmail("new@example.com")).thenReturn(false);
//        when(employeeRepository.existsByEmail("existing@example.com")).thenReturn(true);
//        when(employeeRepository.findByEmail("existing@example.com")).thenReturn(Optional.of(existingEmployee));
//        when(employeeRepository.save(any(Employee.class))).thenAnswer(invocation -> invocation.getArgument(0));
//
//        // When
//        employeeService.addAllEmployees(employees);
//
//        // Then
//        verify(employeeRepository, times(1)).existsByEmail("new@example.com");
//        verify(employeeRepository, times(1)).existsByEmail("existing@example.com");
//        verify(employeeRepository, times(1)).save(newEmployee);
//        verify(employeeRepository, times(1)).save(existingEmployee); // Aktualizacja
//    }
//
//// ===== TESTY DLA KONWERSJI POZYCJI Z STRINGA =====
//
//    @Test
//    void findEmployeesWithFiltersOptimized_ValidPositionString_ShouldConvertCorrectly() {
//        // Given
//        Pageable pageable = PageRequest.of(0, 20);
//        EmployeeListView view = mock(EmployeeListView.class);
//        Page<EmployeeListView> page = new PageImpl<>(Collections.singletonList(view), pageable, 1);
//
//        when(employeeRepository.findEmployeesWithFiltersOptimized(
//                any(), any(), any(), any(), any(), any(Pageable.class)))
//                .thenReturn(page);
//
//        // When
//        Page<EmployeeListView> result = employeeService.findEmployeesWithFiltersOptimized(
//                "Jan", "TechCorp", "PROGRAMMER", 5000.0, 10000.0, pageable);
//
//        // Then
//        assertNotNull(result);
//        verify(employeeRepository, times(1)).findEmployeesWithFiltersOptimized(
//                eq("Jan"), eq("TechCorp"), eq(Position.PROGRAMMER), eq(5000.0), eq(10000.0), eq(pageable));
//    }
//
//    @Test
//    void findEmployeesWithFiltersOptimized_LowercasePositionString_ShouldConvertCorrectly() {
//        // Given
//        Pageable pageable = PageRequest.of(0, 20);
//        EmployeeListView view = mock(EmployeeListView.class);
//        Page<EmployeeListView> page = new PageImpl<>(Collections.singletonList(view), pageable, 1);
//
//        when(employeeRepository.findEmployeesWithFiltersOptimized(
//                any(), any(), any(), any(), any(), any(Pageable.class)))
//                .thenReturn(page);
//
//        // When
//        Page<EmployeeListView> result = employeeService.findEmployeesWithFiltersOptimized(
//                "Jan", "TechCorp", "programmer", 5000.0, 10000.0, pageable);
//
//        // Then
//        assertNotNull(result);
//        verify(employeeRepository, times(1)).findEmployeesWithFiltersOptimized(
//                eq("Jan"), eq("TechCorp"), eq(Position.PROGRAMMER), eq(5000.0), eq(10000.0), eq(pageable));
//    }
//
//    @Test
//    void findEmployeesWithFiltersOptimized_MixedCasePositionString_ShouldConvertCorrectly() {
//        // Given
//        Pageable pageable = PageRequest.of(0, 20);
//        EmployeeListView view = mock(EmployeeListView.class);
//        Page<EmployeeListView> page = new PageImpl<>(Collections.singletonList(view), pageable, 1);
//
//        when(employeeRepository.findEmployeesWithFiltersOptimized(
//                any(), any(), any(), any(), any(), any(Pageable.class)))
//                .thenReturn(page);
//
//        // When
//        Page<EmployeeListView> result = employeeService.findEmployeesWithFiltersOptimized(
//                "Jan", "TechCorp", "ProGrammer", 5000.0, 10000.0, pageable);
//
//        // Then
//        assertNotNull(result);
//        verify(employeeRepository, times(1)).findEmployeesWithFiltersOptimized(
//                eq("Jan"), eq("TechCorp"), eq(Position.PROGRAMMER), eq(5000.0), eq(10000.0), eq(pageable));
//    }
//}