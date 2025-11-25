//package service;
//
//import com.techcorp.employee.exception.DuplicateEmailException;
//import com.techcorp.employee.exception.EmployeeNotFoundException;
//import com.techcorp.employee.exception.InvalidDataException;
//import com.techcorp.employee.exception.InvalidFileException;
//import com.techcorp.employee.model.CompanyStatistics;
//import com.techcorp.employee.model.Employee;
//import com.techcorp.employee.model.EmploymentStatus;
//import com.techcorp.employee.model.Position;
//import com.techcorp.employee.service.EmployeeService;
//import com.techcorp.employee.service.FileStorageService;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//import org.junit.jupiter.api.extension.ExtendWith;
//import org.mockito.Mock;
//import org.mockito.junit.jupiter.MockitoExtension;
//import org.springframework.core.io.UrlResource;
//import org.springframework.http.ResponseEntity;
//import org.springframework.web.multipart.MultipartFile;
//
//import java.nio.file.Path;
//import java.util.*;
//
//import static org.junit.jupiter.api.Assertions.*;
//import static org.mockito.Mockito.*;
//
//@ExtendWith(MockitoExtension.class)
//class EmployeeServiceTest {
//
//    @Mock
//    private FileStorageService fileStorageService;
//
//    private EmployeeService employeeService;
//
//    @BeforeEach
//    void setUp() {
//        // Przekaż fileStorageService do konstruktora
//        employeeService = new EmployeeService(fileStorageService);
//    }
//
//    // ===== TESTY DODAWANIA PRACOWNIKÓW =====
//
//    @Test
//    void shouldAddEmployeeAndReturnTrue() throws InvalidDataException {
//        Employee employee = new Employee("John Doe", "john.doe@company.com", "TechCorp", Position.PROGRAMMER, 8000);
//        boolean result = employeeService.addEmployee(employee);
//        assertTrue(result);
//    }
//
//    @Test
//    void shouldIncreaseCountAfterAddingEmployee() throws InvalidDataException {
//        Employee employee = new Employee("John Doe", "john.doe@company.com", "TechCorp", Position.PROGRAMMER, 8000);
//        employeeService.addEmployee(employee);
//        assertEquals(1, employeeService.getEmployeeCount());
//    }
//
//    @Test
//    void shouldThrowDuplicateEmailExceptionForDuplicateEmployee() throws InvalidDataException {
//        Employee employee1 = new Employee("John Doe", "john.doe@company.com", "TechCorp", Position.PROGRAMMER, 8000);
//        Employee employee2 = new Employee("Jane Smith", "john.doe@company.com", "TechCorp", Position.MANAGER, 12000);
//        employeeService.addEmployee(employee1);
//        assertThrows(DuplicateEmailException.class, () -> employeeService.addEmployee(employee2));
//    }
//
//    @Test
//    void shouldThrowInvalidDataExceptionForNullEmployee() {
//        assertThrows(InvalidDataException.class, () -> employeeService.addEmployee(null));
//    }
//
//    // ===== TESTY WYSZUKIWANIA PRACOWNIKÓW =====
//
//    @Test
//    void shouldFindExistingEmployeeByEmail() throws InvalidDataException {
//        Employee employee = new Employee("John Doe", "john.doe@company.com", "TechCorp", Position.PROGRAMMER, 8000);
//        employeeService.addEmployee(employee);
//        Optional<Employee> found = employeeService.findEmployeeByEmail("john.doe@company.com");
//        assertTrue(found.isPresent());
//    }
//
//    @Test
//    void shouldReturnEmptyOptionalForNonExistingEmail() {
//        Optional<Employee> found = employeeService.findEmployeeByEmail("nonexistent@company.com");
//        assertTrue(found.isEmpty());
//    }
//
//    @Test
//    void shouldThrowExceptionForNullEmailInFind() {
//        assertThrows(IllegalArgumentException.class, () -> employeeService.findEmployeeByEmail(null));
//    }
//
//    @Test
//    void shouldThrowExceptionForEmptyEmailInFind() {
//        assertThrows(IllegalArgumentException.class, () -> employeeService.findEmployeeByEmail(""));
//    }
//
//    @Test
//    void shouldConfirmEmployeeExists() throws InvalidDataException {
//        Employee employee = new Employee("John Doe", "john.doe@company.com", "TechCorp", Position.PROGRAMMER, 8000);
//        employeeService.addEmployee(employee);
//        assertTrue(employeeService.employeeExists("john.doe@company.com"));
//    }
//
//    @Test
//    void shouldConfirmEmployeeDoesNotExist() {
//        assertFalse(employeeService.employeeExists("nonexistent@company.com"));
//    }
//
//    // ===== TESTY USUWANIA PRACOWNIKÓW =====
//
//    @Test
//    void shouldRemoveExistingEmployee() throws InvalidDataException {
//        Employee employee = new Employee("John Doe", "john.doe@company.com", "TechCorp", Position.PROGRAMMER, 8000);
//        employeeService.addEmployee(employee);
//        boolean result = employeeService.removeEmployee("john.doe@company.com");
//        assertTrue(result);
//    }
//
//    @Test
//    void shouldReturnFalseWhenRemovingNonExistingEmployee() {
//        boolean result = employeeService.removeEmployee("nonexistent@company.com");
//        assertFalse(result);
//    }
//
//    @Test
//    void shouldDecreaseCountAfterRemovingEmployee() throws InvalidDataException {
//        Employee employee = new Employee("John Doe", "john.doe@company.com", "TechCorp", Position.PROGRAMMER, 8000);
//        employeeService.addEmployee(employee);
//        employeeService.removeEmployee("john.doe@company.com");
//        assertEquals(0, employeeService.getEmployeeCount());
//    }
//
//    // ===== TESTY LIST I FILTRÓW =====
//
//    @Test
//    void shouldReturnAllEmployees() throws InvalidDataException {
//        Employee employee1 = new Employee("John Doe", "john.doe@company.com", "TechCorp", Position.PROGRAMMER, 8000);
//        Employee employee2 = new Employee("Jane Smith", "jane.smith@company.com", "TechCorp", Position.MANAGER, 12000);
//        employeeService.addEmployee(employee1);
//        employeeService.addEmployee(employee2);
//        List<Employee> employees = employeeService.getAllEmployees();
//        assertEquals(2, employees.size());
//    }
//
//    @Test
//    void shouldReturnEmptyListWhenNoEmployees() {
//        List<Employee> employees = employeeService.getAllEmployees();
//        assertTrue(employees.isEmpty());
//    }
//
//    @Test
//    void shouldFilterEmployeesByCompany() throws InvalidDataException {
//        Employee employee1 = new Employee("John Doe", "john.doe@techcorp.com", "TechCorp", Position.PROGRAMMER, 8000);
//        Employee employee2 = new Employee("Jane Smith", "jane.smith@softinc.com", "SoftInc", Position.MANAGER, 12000);
//        employeeService.addEmployee(employee1);
//        employeeService.addEmployee(employee2);
//        List<Employee> techCorpEmployees = employeeService.getEmployeesByCompany("TechCorp");
//        assertEquals(1, techCorpEmployees.size());
//    }
//
//    @Test
//    void shouldThrowExceptionForNullCompanyInFilter() {
//        assertThrows(IllegalArgumentException.class, () -> employeeService.getEmployeesByCompany(null));
//    }
//
//    @Test
//    void shouldReturnEmptyListForNonExistingCompany() throws InvalidDataException {
//        Employee employee = new Employee("John Doe", "john.doe@techcorp.com", "TechCorp", Position.PROGRAMMER, 8000);
//        employeeService.addEmployee(employee);
//        List<Employee> employees = employeeService.getEmployeesByCompany("NonExisting");
//        assertTrue(employees.isEmpty());
//    }
//
//    // ===== TESTY STATUSU ZATRUDNIENIA =====
//
//    @Test
//    void shouldUpdateEmployeeStatus() throws InvalidDataException {
//        Employee employee = new Employee("John Doe", "john.doe@company.com", "TechCorp", Position.PROGRAMMER, 8000);
//        employeeService.addEmployee(employee);
//        Employee updated = employeeService.updateEmployeeStatus("john.doe@company.com", EmploymentStatus.ON_LEAVE);
//        assertEquals(EmploymentStatus.ON_LEAVE, updated.getStatus());
//    }
//
//    @Test
//    void shouldThrowExceptionWhenUpdatingStatusOfNonExistingEmployee() {
//        assertThrows(EmployeeNotFoundException.class, () ->
//                employeeService.updateEmployeeStatus("nonexistent@company.com", EmploymentStatus.ON_LEAVE)
//        );
//    }
//
//    @Test
//    void shouldFilterEmployeesByStatus() throws InvalidDataException {
//        Employee employee1 = new Employee("John Doe", "john.doe@company.com", "TechCorp", Position.PROGRAMMER, 8000);
//        Employee employee2 = new Employee("Jane Smith", "jane.smith@company.com", "TechCorp", Position.MANAGER, 12000);
//        employeeService.addEmployee(employee1);
//        employeeService.addEmployee(employee2);
//        employeeService.updateEmployeeStatus("jane.smith@company.com", EmploymentStatus.ON_LEAVE);
//
//        List<Employee> activeEmployees = employeeService.getEmployeesByStatus(EmploymentStatus.ACTIVE);
//        List<Employee> onLeaveEmployees = employeeService.getEmployeesByStatus(EmploymentStatus.ON_LEAVE);
//
//        assertEquals(1, activeEmployees.size());
//        assertEquals(1, onLeaveEmployees.size());
//    }
//
//    @Test
//    void shouldThrowExceptionForNullStatusInFilter() {
//        assertThrows(IllegalArgumentException.class, () -> employeeService.getEmployeesByStatus(null));
//    }
//
//    @Test
//    void shouldCalculateEmploymentStatusDistribution() throws InvalidDataException {
//        Employee employee1 = new Employee("John Doe", "john.doe@company.com", "TechCorp", Position.PROGRAMMER, 8000);
//        Employee employee2 = new Employee("Jane Smith", "jane.smith@company.com", "TechCorp", Position.MANAGER, 12000);
//        employeeService.addEmployee(employee1);
//        employeeService.addEmployee(employee2);
//        employeeService.updateEmployeeStatus("jane.smith@company.com", EmploymentStatus.ON_LEAVE);
//
//        Map<EmploymentStatus, Long> distribution = employeeService.getEmploymentStatusDistribution();
//
//        assertEquals(1, distribution.get(EmploymentStatus.ACTIVE));
//        assertEquals(1, distribution.get(EmploymentStatus.ON_LEAVE));
//    }
//
//    // ===== TESTY SORTOWANIA I GRUPOWANIA =====
//
//    @Test
//    void shouldSortEmployeesByName() throws InvalidDataException {
//        Employee employee1 = new Employee("John Doe", "john.doe@company.com", "TechCorp", Position.PROGRAMMER, 8000);
//        Employee employee2 = new Employee("Alice Adams", "alice.adams@company.com", "TechCorp", Position.MANAGER, 12000);
//        employeeService.addEmployee(employee1);
//        employeeService.addEmployee(employee2);
//
//        List<Employee> sorted = employeeService.sortEmployeesByName();
//
//        assertEquals("Alice Adams", sorted.get(0).getName());
//        assertEquals("John Doe", sorted.get(1).getName());
//    }
//
//    @Test
//    void shouldGroupEmployeesByPosition() throws InvalidDataException {
//        Employee employee1 = new Employee("John Doe", "john.doe@company.com", "TechCorp", Position.PROGRAMMER, 8000);
//        Employee employee2 = new Employee("Jane Smith", "jane.smith@company.com", "TechCorp", Position.MANAGER, 12000);
//        employeeService.addEmployee(employee1);
//        employeeService.addEmployee(employee2);
//
//        Map<Position, List<Employee>> grouped = employeeService.groupEmployeesByPosition();
//
//        assertEquals(1, grouped.get(Position.PROGRAMMER).size());
//        assertEquals(1, grouped.get(Position.MANAGER).size());
//    }
//
//    @Test
//    void shouldCountEmployeesByPosition() throws InvalidDataException {
//        Employee employee1 = new Employee("John Doe", "john.doe@company.com", "TechCorp", Position.PROGRAMMER, 8000);
//        Employee employee2 = new Employee("Jane Smith", "jane.smith@company.com", "TechCorp", Position.MANAGER, 12000);
//        employeeService.addEmployee(employee1);
//        employeeService.addEmployee(employee2);
//
//        Map<Position, Long> counts = employeeService.countEmployeesByPosition();
//
//        assertEquals(1, counts.get(Position.PROGRAMMER));
//        assertEquals(1, counts.get(Position.MANAGER));
//    }
//
//    @Test
//    void shouldGroupEmployeesByCompany() throws InvalidDataException {
//        Employee employee1 = new Employee("John Doe", "john.doe@techcorp.com", "TechCorp", Position.PROGRAMMER, 8000);
//        Employee employee2 = new Employee("Jane Smith", "jane.smith@softinc.com", "SoftInc", Position.MANAGER, 12000);
//        employeeService.addEmployee(employee1);
//        employeeService.addEmployee(employee2);
//
//        Map<String, List<Employee>> grouped = employeeService.groupEmployeesByCompany();
//
//        assertEquals(1, grouped.get("TechCorp").size());
//        assertEquals(1, grouped.get("SoftInc").size());
//    }
//
//    // ===== TESTY STATYSTYK FINANSOWYCH =====
//
//    @Test
//    void shouldCalculateAverageSalary() throws InvalidDataException {
//        Employee employee1 = new Employee("John Doe", "john.doe@company.com", "TechCorp", Position.PROGRAMMER, 8000);
//        Employee employee2 = new Employee("Jane Smith", "jane.smith@company.com", "TechCorp", Position.MANAGER, 12000);
//        employeeService.addEmployee(employee1);
//        employeeService.addEmployee(employee2);
//
//        double averageSalary = employeeService.calculateAverageSalary().orElse(0.0);
//
//        assertEquals(10000.0, averageSalary, 0.001);
//    }
//
//    @Test
//    void shouldReturnEmptyOptionalForAverageSalaryWhenNoEmployees() {
//        OptionalDouble averageSalary = employeeService.calculateAverageSalary();
//        assertTrue(averageSalary.isEmpty());
//    }
//
//    @Test
//    void shouldCalculateAverageSalaryByCompany() throws InvalidDataException {
//        Employee employee1 = new Employee("John Doe", "john.doe@techcorp.com", "TechCorp", Position.PROGRAMMER, 8000);
//        Employee employee2 = new Employee("Jane Smith", "jane.smith@techcorp.com", "TechCorp", Position.MANAGER, 12000);
//        employeeService.addEmployee(employee1);
//        employeeService.addEmployee(employee2);
//
//        double averageSalary = employeeService.calculateAverageSalaryByCompany("TechCorp").orElse(0.0);
//
//        assertEquals(10000.0, averageSalary, 0.001);
//    }
//
//    @Test
//    void shouldReturnEmptyOptionalForAverageSalaryOfNonExistingCompany() throws InvalidDataException {
//        Employee employee = new Employee("John Doe", "john.doe@techcorp.com", "TechCorp", Position.PROGRAMMER, 8000);
//        employeeService.addEmployee(employee);
//
//        OptionalDouble averageSalary = employeeService.calculateAverageSalaryByCompany("NonExisting");
//
//        assertTrue(averageSalary.isEmpty());
//    }
//
//    @Test
//    void shouldFindHighestPaidEmployee() throws InvalidDataException {
//        Employee employee1 = new Employee("John Doe", "john.doe@company.com", "TechCorp", Position.PROGRAMMER, 8000);
//        Employee employee2 = new Employee("Jane Smith", "jane.smith@company.com", "TechCorp", Position.MANAGER, 12000);
//        employeeService.addEmployee(employee1);
//        employeeService.addEmployee(employee2);
//
//        Optional<Employee> highestPaid = employeeService.findHighestPaidEmployee();
//
//        assertTrue(highestPaid.isPresent());
//        assertEquals("Jane Smith", highestPaid.get().getName());
//    }
//
//    @Test
//    void shouldReturnEmptyOptionalForHighestPaidWhenNoEmployees() {
//        Optional<Employee> highestPaid = employeeService.findHighestPaidEmployee();
//        assertTrue(highestPaid.isEmpty());
//    }
//
//    @Test
//    void shouldFindLowestPaidEmployee() throws InvalidDataException {
//        Employee employee1 = new Employee("John Doe", "john.doe@company.com", "TechCorp", Position.PROGRAMMER, 8000);
//        Employee employee2 = new Employee("Jane Smith", "jane.smith@company.com", "TechCorp", Position.MANAGER, 12000);
//        employeeService.addEmployee(employee1);
//        employeeService.addEmployee(employee2);
//
//        Optional<Employee> lowestPaid = employeeService.findLowestPaidEmployee();
//
//        assertTrue(lowestPaid.isPresent());
//        assertEquals("John Doe", lowestPaid.get().getName());
//    }
//
//    @Test
//    void shouldCalculateTotalSalaryCost() throws InvalidDataException {
//        Employee employee1 = new Employee("John Doe", "john.doe@company.com", "TechCorp", Position.PROGRAMMER, 8000);
//        Employee employee2 = new Employee("Jane Smith", "jane.smith@company.com", "TechCorp", Position.MANAGER, 12000);
//        employeeService.addEmployee(employee1);
//        employeeService.addEmployee(employee2);
//
//        double totalCost = employeeService.calculateTotalSalaryCost();
//
//        assertEquals(20000.0, totalCost, 0.001);
//    }
//
//    @Test
//    void shouldCalculateTotalSalaryCostByCompany() throws InvalidDataException {
//        Employee employee1 = new Employee("John Doe", "john.doe@techcorp.com", "TechCorp", Position.PROGRAMMER, 8000);
//        Employee employee2 = new Employee("Jane Smith", "jane.smith@techcorp.com", "TechCorp", Position.MANAGER, 12000);
//        employeeService.addEmployee(employee1);
//        employeeService.addEmployee(employee2);
//
//        double totalCost = employeeService.calculateTotalSalaryCostByCompany("TechCorp");
//
//        assertEquals(20000.0, totalCost, 0.001);
//    }
//
//    @Test
//    void shouldReturnZeroTotalCostForNonExistingCompany() throws InvalidDataException {
//        Employee employee = new Employee("John Doe", "john.doe@techcorp.com", "TechCorp", Position.PROGRAMMER, 8000);
//        employeeService.addEmployee(employee);
//
//        double totalCost = employeeService.calculateTotalSalaryCostByCompany("NonExisting");
//
//        assertEquals(0.0, totalCost, 0.001);
//    }
//
//    // ===== TESTY STATYSTYK FIRM =====
//
//    @Test
//    void shouldGetCompanyStatistics() throws InvalidDataException {
//        Employee employee1 = new Employee("John Doe", "john.doe@techcorp.com", "TechCorp", Position.PROGRAMMER, 8000);
//        Employee employee2 = new Employee("Jane Smith", "jane.smith@techcorp.com", "TechCorp", Position.MANAGER, 12000);
//        employeeService.addEmployee(employee1);
//        employeeService.addEmployee(employee2);
//
//        CompanyStatistics stats = employeeService.getCompanyStatistics("TechCorp");
//
//        assertEquals(2, stats.getEmployeeCount());
//        assertEquals(10000.0, stats.getAverageSalary(), 0.001);
//        assertEquals("Jane Smith", stats.getHighestPaidEmployee());
//    }
//
//    @Test
//    void shouldReturnEmptyStatisticsForNonExistingCompany() {
//        CompanyStatistics stats = employeeService.getCompanyStatistics("NonExisting");
//
//        assertEquals(0, stats.getEmployeeCount());
//        assertEquals(0.0, stats.getAverageSalary(), 0.001);
//        assertEquals("None", stats.getHighestPaidEmployee());
//    }
//
//    @Test
//    void shouldGetAllCompanyStatistics() throws InvalidDataException {
//        Employee employee1 = new Employee("John Doe", "john.doe@techcorp.com", "TechCorp", Position.PROGRAMMER, 8000);
//        Employee employee2 = new Employee("Jane Smith", "jane.smith@softinc.com", "SoftInc", Position.MANAGER, 12000);
//        employeeService.addEmployee(employee1);
//        employeeService.addEmployee(employee2);
//
//        Map<String, CompanyStatistics> allStats = employeeService.getCompanyStatistics();
//
//        assertEquals(2, allStats.size());
//        assertTrue(allStats.containsKey("TechCorp"));
//        assertTrue(allStats.containsKey("SoftInc"));
//    }
//
//    // ===== TESTY WALIDACJI I POMOCNICZE =====
//
//    @Test
//    void shouldValidateSalaryConsistency() throws InvalidDataException {
//        Employee employee1 = new Employee("John Doe", "john.doe@company.com", "TechCorp", Position.PROGRAMMER, 6000);
//        Employee employee2 = new Employee("Jane Smith", "jane.smith@company.com", "TechCorp", Position.MANAGER, 12000);
//        employeeService.addEmployee(employee1);
//        employeeService.addEmployee(employee2);
//
//        List<Employee> underpaid = employeeService.validateSalaryConsistency(7000);
//
//        assertEquals(1, underpaid.size());
//        assertEquals("John Doe", underpaid.get(0).getName());
//    }
//
//    @Test
//    void shouldReturnEmptyListWhenNoUnderpaidEmployees() throws InvalidDataException {
//        Employee employee = new Employee("John Doe", "john.doe@company.com", "TechCorp", Position.PROGRAMMER, 8000);
//        employeeService.addEmployee(employee);
//
//        List<Employee> underpaid = employeeService.validateSalaryConsistency(7000);
//
//        assertTrue(underpaid.isEmpty());
//    }
//
//    @Test
//    void shouldGetEmployeeCountByCompany() throws InvalidDataException {
//        Employee employee1 = new Employee("John Doe", "john.doe@techcorp.com", "TechCorp", Position.PROGRAMMER, 8000);
//        Employee employee2 = new Employee("Jane Smith", "jane.smith@techcorp.com", "TechCorp", Position.MANAGER, 12000);
//        employeeService.addEmployee(employee1);
//        employeeService.addEmployee(employee2);
//
//        long count = employeeService.getEmployeeCountByCompany("TechCorp");
//
//        assertEquals(2, count);
//    }
//
//    @Test
//    void shouldReturnZeroCountForNonExistingCompany() {
//        long count = employeeService.getEmployeeCountByCompany("NonExisting");
//        assertEquals(0, count);
//    }
//
//    @Test
//    void shouldConfirmServiceIsEmptyInitially() {
//        assertTrue(employeeService.isEmpty());
//    }
//
//    @Test
//    void shouldConfirmServiceIsNotEmptyAfterAddingEmployee() throws InvalidDataException {
//        Employee employee = new Employee("John Doe", "john.doe@company.com", "TechCorp", Position.PROGRAMMER, 8000);
//        employeeService.addEmployee(employee);
//        assertFalse(employeeService.isEmpty());
//    }
//
//    @Test
//    void shouldAddAllEmployees() throws InvalidDataException {
//        Employee employee1 = new Employee("John Doe", "john.doe@company.com", "TechCorp", Position.PROGRAMMER, 8000);
//        Employee employee2 = new Employee("Jane Smith", "jane.smith@company.com", "TechCorp", Position.MANAGER, 12000);
//        List<Employee> employeesToAdd = List.of(employee1, employee2);
//
//        employeeService.addAllEmployees(employeesToAdd);
//
//        assertEquals(2, employeeService.getEmployeeCount());
//    }
//
//    @Test
//    void shouldUpdateEmployee() throws InvalidDataException {
//        Employee original = new Employee("John Doe", "john.doe@company.com", "TechCorp", Position.PROGRAMMER, 8000);
//        employeeService.addEmployee(original);
//
//        Employee updated = new Employee("John Smith", "john.doe@company.com", "NewCorp", Position.MANAGER, 15000);
//
//        Employee result = employeeService.updateEmployee(updated);
//
//        assertEquals("John Smith", result.getName());
//        assertEquals("NewCorp", result.getCompany());
//        assertEquals(Position.MANAGER, result.getPosition());
//    }
//
//    @Test
//    void shouldThrowExceptionWhenUpdatingNonExistingEmployee() throws InvalidDataException {
//        Employee nonExisting = new Employee("Non Existing", "nonexistent@company.com", "TechCorp", Position.PROGRAMMER, 8000);
//
//        assertThrows(EmployeeNotFoundException.class, () -> employeeService.updateEmployee(nonExisting));
//    }
//
//
//    // ===== TESTY ZARZĄDZANIA ZDJĘCIAMI =====
//
//
//
//
//    @Test
//    void shouldThrowInvalidFileExceptionWhenUploadingInvalidFile() throws InvalidDataException {
//        // Given
//        Employee employee = new Employee("John Doe", "john.doe@company.com", "TechCorp", Position.PROGRAMMER, 8000);
//        employeeService.addEmployee(employee);
//
//        MultipartFile mockFile = mock(MultipartFile.class);
//        doThrow(new InvalidFileException("Invalid file")).when(fileStorageService).validateFile(mockFile);
//
//        // When & Then - Oczekujemy że wyjątek zostanie rzucony
//        assertThrows(InvalidFileException.class,
//                () -> employeeService.uploadEmployeePhoto("john.doe@company.com", mockFile));
//
//        InvalidFileException exception = assertThrows(InvalidFileException.class,
//                () -> employeeService.uploadEmployeePhoto("john.doe@company.com", mockFile));
//        assertEquals("Invalid file", exception.getMessage());
//    }
//
//
//
//    @Test
//    void shouldReturnNotFoundWhenGettingPhotoForNonExistingEmployee() {
//        // When
//        ResponseEntity<org.springframework.core.io.Resource> response =
//                employeeService.getEmployeePhoto("nonexistent@company.com");
//
//        // Then
//        assertEquals(404, response.getStatusCodeValue());
//    }
//
//    @Test
//    void shouldReturnNotFoundWhenEmployeeHasNoPhoto() throws InvalidDataException {
//        // Given
//        Employee employee = new Employee("John Doe", "john.doe@company.com", "TechCorp", Position.PROGRAMMER, 8000);
//        employeeService.addEmployee(employee);
//
//        // When
//        ResponseEntity<org.springframework.core.io.Resource> response =
//                employeeService.getEmployeePhoto("john.doe@company.com");
//
//        // Then
//        assertEquals(404, response.getStatusCodeValue());
//    }
//
//    @Test
//    void shouldDeleteEmployeePhotoSuccessfully() throws InvalidDataException {
//        // Given
//        Employee employee = new Employee("John Doe", "john.doe@company.com", "TechCorp", Position.PROGRAMMER, 8000);
//        employee.setPhotoFileName("john_doe_photo.jpg");
//        employeeService.addEmployee(employee);
//
//        // When
//        employeeService.deleteEmployeePhoto("john.doe@company.com");
//
//        // Then
//        verify(fileStorageService, times(1)).deleteFile("john_doe_photo.jpg", "photos");
//        assertNull(employeeService.findEmployeeByEmail("john.doe@company.com").get().getPhotoFileName());
//    }
//
//    @Test
//    void shouldHandleDeletePhotoWhenEmployeeHasNoPhoto() throws InvalidDataException {
//        // Given
//        Employee employee = new Employee("John Doe", "john.doe@company.com", "TechCorp", Position.PROGRAMMER, 8000);
//        employeeService.addEmployee(employee);
//
//        // When & Then - Should not throw exception
//        assertDoesNotThrow(() -> employeeService.deleteEmployeePhoto("john.doe@company.com"));
//    }
//
//// ===== TESTY DODATKOWYCH METOD POMOCNICZYCH =====
//
//    @Test
//    void shouldCalculateAverageSalaryByPosition() throws InvalidDataException {
//        // Given
//        Employee employee1 = new Employee("John Doe", "john.doe@company.com", "TechCorp", Position.PROGRAMMER, 8000);
//        Employee employee2 = new Employee("Jane Smith", "jane.smith@company.com", "TechCorp", Position.PROGRAMMER, 9000);
//        Employee employee3 = new Employee("Bob Johnson", "bob.johnson@company.com", "TechCorp", Position.MANAGER, 12000);
//
//        employeeService.addEmployee(employee1);
//        employeeService.addEmployee(employee2);
//        employeeService.addEmployee(employee3);
//
//        // When
//        Map<Position, Double> avgSalaries = employeeService.getAverageSalaryByPosition();
//
//        // Then
//        assertEquals(2, avgSalaries.size());
//        assertEquals(8500.0, avgSalaries.get(Position.PROGRAMMER), 0.001);
//        assertEquals(12000.0, avgSalaries.get(Position.MANAGER), 0.001);
//    }
//
//    @Test
//    void shouldFindHighestSalary() throws InvalidDataException {
//        // Given
//        Employee employee1 = new Employee("John Doe", "john.doe@company.com", "TechCorp", Position.PROGRAMMER, 8000);
//        Employee employee2 = new Employee("Jane Smith", "jane.smith@company.com", "TechCorp", Position.MANAGER, 12000);
//        employeeService.addEmployee(employee1);
//        employeeService.addEmployee(employee2);
//
//        // When
//        double highestSalary = employeeService.findHighestSalary();
//
//        // Then
//        assertEquals(12000.0, highestSalary, 0.001);
//    }
//
//    @Test
//    void shouldReturnZeroForHighestSalaryWhenNoEmployees() {
//        // When
//        double highestSalary = employeeService.findHighestSalary();
//
//        // Then
//        assertEquals(0.0, highestSalary, 0.001);
//    }
//
//    @Test
//    void shouldFindHighestSalaryByCompany() throws InvalidDataException {
//        // Given
//        Employee employee1 = new Employee("John Doe", "john.doe@techcorp.com", "TechCorp", Position.PROGRAMMER, 8000);
//        Employee employee2 = new Employee("Jane Smith", "jane.smith@techcorp.com", "TechCorp", Position.MANAGER, 12000);
//        employeeService.addEmployee(employee1);
//        employeeService.addEmployee(employee2);
//
//        // When
//        double highestSalary = employeeService.findHighestSalaryByCompany("TechCorp");
//
//        // Then
//        assertEquals(12000.0, highestSalary, 0.001);
//    }
//
//    @Test
//    void shouldReturnZeroForHighestSalaryByNonExistingCompany() throws InvalidDataException {
//        // Given
//        Employee employee = new Employee("John Doe", "john.doe@techcorp.com", "TechCorp", Position.PROGRAMMER, 8000);
//        employeeService.addEmployee(employee);
//
//        // When
//        double highestSalary = employeeService.findHighestSalaryByCompany("NonExisting");
//
//        // Then
//        assertEquals(0.0, highestSalary, 0.001);
//    }
//
//// ===== TESTY DODATKOWYCH PRZYPADKÓW BRZEGOWYCH =====
//
//    @Test
//    void shouldHandleEmptyListInAddAllEmployees() {
//        // Given
//        List<Employee> emptyList = Collections.emptyList();
//
//        // When & Then - Should not throw exception
//        assertDoesNotThrow(() -> employeeService.addAllEmployees(emptyList));
//        assertEquals(0, employeeService.getEmployeeCount());
//    }
//
//
//
//    @Test
//    void shouldHandleUpdateEmployeeWithSameEmail() throws InvalidDataException {
//        // Given
//        Employee original = new Employee("John Doe", "john.doe@company.com", "TechCorp", Position.PROGRAMMER, 8000);
//        employeeService.addEmployee(original);
//
//        Employee updated = new Employee("John Smith", "john.doe@company.com", "NewCorp", Position.MANAGER, 15000);
//
//        // When
//        Employee result = employeeService.updateEmployee(updated);
//
//        // Then
//        assertEquals("John Smith", result.getName());
//        assertEquals("NewCorp", result.getCompany());
//        assertEquals(Position.MANAGER, result.getPosition());
//        assertEquals(1, employeeService.getEmployeeCount()); // Count should remain the same
//    }
//
//    @Test
//    void shouldHandleCaseInsensitiveEmailSearch() throws InvalidDataException {
//        // Given
//        Employee employee = new Employee("John Doe", "John.Doe@Company.COM", "TechCorp", Position.PROGRAMMER, 8000);
//        employeeService.addEmployee(employee);
//
//        // When
//        Optional<Employee> found1 = employeeService.findEmployeeByEmail("john.doe@company.com");
//        Optional<Employee> found2 = employeeService.findEmployeeByEmail("JOHN.DOE@COMPANY.COM");
//
//        // Then
//        assertTrue(found1.isPresent());
//        assertTrue(found2.isPresent());
//        assertEquals("John Doe", found1.get().getName());
//    }
//
//    @Test
//    void shouldHandleCaseInsensitiveCompanyFilter() throws InvalidDataException {
//        // Given
//        Employee employee = new Employee("John Doe", "john.doe@techcorp.com", "TechCorp", Position.PROGRAMMER, 8000);
//        employeeService.addEmployee(employee);
//
//        // When
//        List<Employee> result1 = employeeService.getEmployeesByCompany("techcorp");
//        List<Employee> result2 = employeeService.getEmployeesByCompany("TECHCORP");
//
//        // Then
//        assertEquals(1, result1.size());
//        assertEquals(1, result2.size());
//    }
//
//    @Test
//    void shouldHandleEmptyCollectionsInStatistics() {
//        // When
//        Map<EmploymentStatus, Long> statusDistribution = employeeService.getEmploymentStatusDistribution();
//        Map<Position, List<Employee>> positionGrouping = employeeService.groupEmployeesByPosition();
//        Map<Position, Long> positionCounts = employeeService.countEmployeesByPosition();
//        Map<String, List<Employee>> companyGrouping = employeeService.groupEmployeesByCompany();
//        Map<Position, Double> avgSalariesByPosition = employeeService.getAverageSalaryByPosition();
//
//        // Then - All should be empty but not null
//        assertNotNull(statusDistribution);
//        assertTrue(statusDistribution.isEmpty());
//        assertNotNull(positionGrouping);
//        assertTrue(positionGrouping.isEmpty());
//        assertNotNull(positionCounts);
//        assertTrue(positionCounts.isEmpty());
//        assertNotNull(companyGrouping);
//        assertTrue(companyGrouping.isEmpty());
//        assertNotNull(avgSalariesByPosition);
//        assertTrue(avgSalariesByPosition.isEmpty());
//    }
//
//// ===== TESTY DETERMINE IMAGE CONTENT TYPE =====
//
//    @Test
//    void shouldDetermineCorrectImageContentTypes() throws InvalidDataException {
//        // This tests the private method through public API
//        Employee employee = new Employee("John Doe", "john.doe@company.com", "TechCorp", Position.PROGRAMMER, 8000);
//        employee.setPhotoFileName("photo.png");
//        employeeService.addEmployee(employee);
//
//        Employee employee2 = new Employee("Jane Smith", "jane.smith@company.com", "TechCorp", Position.MANAGER, 12000);
//        employee2.setPhotoFileName("photo.gif");
//        employeeService.addEmployee(employee2);
//
//
//
//        assertEquals("photo.png", employee.getPhotoFileName());
//        assertEquals("photo.gif", employee2.getPhotoFileName());
//    }
//
//
//
//
//    // ===== TESTY DLA METOD DEPARTAMENTÓW =====
//
//    @Test
//    void shouldGetEmployeesWithoutDepartment() throws InvalidDataException {
//        // Given
//        Employee employee1 = new Employee("John Doe", "john.doe@company.com", "TechCorp", Position.PROGRAMMER, 8000);
//        Employee employee2 = new Employee("Jane Smith", "jane.smith@company.com", "TechCorp", Position.MANAGER, 12000);
//        employee2.setDepartmentId(1L);
//
//        employeeService.addEmployee(employee1);
//        employeeService.addEmployee(employee2);
//
//        // When
//        List<Employee> employeesWithoutDept = employeeService.getEmployeesWithoutDepartment();
//
//        // Then
//        assertEquals(1, employeesWithoutDept.size());
//        assertEquals("john.doe@company.com", employeesWithoutDept.get(0).getEmail());
//        assertNull(employeesWithoutDept.get(0).getDepartmentId());
//    }
//
//    @Test
//    void shouldAssignEmployeeToDepartment() throws InvalidDataException {
//        // Given
//        Employee employee = new Employee("John Doe", "john.doe@company.com", "TechCorp", Position.PROGRAMMER, 8000);
//        employeeService.addEmployee(employee);
//        Long departmentId = 1L;
//
//        // When
//        boolean result = employeeService.assignEmployeeToDepartment("john.doe@company.com", departmentId);
//
//        // Then
//        assertTrue(result);
//        assertEquals(departmentId, employee.getDepartmentId());
//    }
//
//    @Test
//    void shouldReturnFalseWhenAssigningNonExistingEmployeeToDepartment() {
//        // When
//        boolean result = employeeService.assignEmployeeToDepartment("nonexistent@company.com", 1L);
//
//        // Then
//        assertFalse(result);
//    }
//
//    @Test
//    void shouldRemoveEmployeeFromDepartment() throws InvalidDataException {
//        // Given
//        Employee employee = new Employee("John Doe", "john.doe@company.com", "TechCorp", Position.PROGRAMMER, 8000);
//        employee.setDepartmentId(1L);
//        employeeService.addEmployee(employee);
//
//        // When
//        boolean result = employeeService.removeEmployeeFromDepartment("john.doe@company.com");
//
//        // Then
//        assertTrue(result);
//        assertNull(employee.getDepartmentId());
//    }
//
//    @Test
//    void shouldReturnFalseWhenRemovingNonExistingEmployeeFromDepartment() {
//        // When
//        boolean result = employeeService.removeEmployeeFromDepartment("nonexistent@company.com");
//
//        // Then
//        assertFalse(result);
//    }
//
//    @Test
//    void shouldGetEmployeesByDepartment() throws InvalidDataException {
//        // Given
//        Long departmentId = 1L;
//        Employee employee1 = new Employee("John Doe", "john.doe@company.com", "TechCorp", Position.PROGRAMMER, 8000);
//        employee1.setDepartmentId(departmentId);
//        Employee employee2 = new Employee("Jane Smith", "jane.smith@company.com", "TechCorp", Position.MANAGER, 12000);
//        employee2.setDepartmentId(departmentId);
//        Employee employee3 = new Employee("Bob Johnson", "bob.johnson@company.com", "TechCorp", Position.MANAGER, 7000);
//
//        employeeService.addEmployee(employee1);
//        employeeService.addEmployee(employee2);
//        employeeService.addEmployee(employee3);
//
//        // When
//        List<Employee> departmentEmployees = employeeService.getEmployeesByDepartment(departmentId);
//
//        // Then
//        assertEquals(2, departmentEmployees.size());
//        assertTrue(departmentEmployees.stream().allMatch(emp -> departmentId.equals(emp.getDepartmentId())));
//    }
//
//    @Test
//    void shouldReturnEmptyListWhenGettingEmployeesByNonExistingDepartment() throws InvalidDataException {
//        // Given
//        Employee employee = new Employee("John Doe", "john.doe@company.com", "TechCorp", Position.PROGRAMMER, 8000);
//        employeeService.addEmployee(employee);
//
//        // When
//        List<Employee> departmentEmployees = employeeService.getEmployeesByDepartment(999L);
//
//        // Then
//        assertTrue(departmentEmployees.isEmpty());
//    }
//}














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
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

// ===== TESTY JEDNOSTKOWE Z MOCKITO =====
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
        when(employeeDAO.save(any(Employee.class))).thenReturn(testEmployee);

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
        when(employeeDAO.save(any(Employee.class))).thenReturn(testEmployee);

        // When
        Employee updated = employeeService.updateEmployeeStatus("jan.kowalski@techcorp.com", EmploymentStatus.ON_LEAVE);

        // Then
        verify(employeeDAO).save(employeeCaptor.capture());
        assertEquals(EmploymentStatus.ON_LEAVE, employeeCaptor.getValue().getStatus());
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
        when(employeeDAO.save(any(Employee.class))).thenReturn(testEmployee);

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
        when(employeeDAO.save(any(Employee.class))).thenReturn(testEmployee);

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
        when(employeeDAO.save(any(Employee.class))).thenReturn(updatedEmployee);

        // When
        Employee result = employeeService.updateEmployee(updatedEmployee);

        // Then
        verify(employeeDAO).save(employeeCaptor.capture());
        assertEquals(1L, employeeCaptor.getValue().getId());
        assertEquals("Jan Nowak-Kowalski", employeeCaptor.getValue().getName());
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

// ===== TESTY INTEGRACYJNE =====
@SpringBootTest
@Transactional
@Sql(scripts = "/schema.sql")
class EmployeeServiceIntegrationTest {

    @Autowired
    private EmployeeService employeeService;

    @Test
    void shouldPerformCompleteEmployeeLifecycle() throws Exception {
        // 1. CREATE
        Employee employee = new Employee("Jan Kowalski", "jan.kowalski@techcorp.com",
                "TechCorp", Position.PROGRAMMER, 8000.0, EmploymentStatus.ACTIVE);

        boolean added = employeeService.addEmployee(employee);
        assertThat(added).isTrue();

        // 2. READ
        Optional<Employee> found = employeeService.findEmployeeByEmail("jan.kowalski@techcorp.com");
        assertThat(found).isPresent();
        assertThat(found.get().getName()).isEqualTo("Jan Kowalski");

        // 3. UPDATE
        Employee updatedEmployee = new Employee("Jan Nowak-Kowalski", "jan.kowalski@techcorp.com",
                "TechCorp", Position.PROGRAMMER, 9000.0, EmploymentStatus.ACTIVE);
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
        Employee employee = new Employee("Test Employee", "test.department@techcorp.com",
                "TechCorp", Position.PROGRAMMER, 8000.0, EmploymentStatus.ACTIVE);
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
        employeeService.addEmployee(new Employee("Prezes", "prezes@techcorp.com",
                "TechCorp", Position.PRESIDENT, 25000.0, EmploymentStatus.ACTIVE));
        employeeService.addEmployee(new Employee("Wiceprezes", "wiceprezes@techcorp.com",
                "TechCorp", Position.VICE_PRESIDENT, 18000.0, EmploymentStatus.ACTIVE));
        employeeService.addEmployee(new Employee("Manager", "manager@techcorp.com",
                "TechCorp", Position.MANAGER, 12000.0, EmploymentStatus.ACTIVE));
        employeeService.addEmployee(new Employee("Programista", "programista@techcorp.com",
                "TechCorp", Position.PROGRAMMER, 8000.0, EmploymentStatus.ACTIVE));
        employeeService.addEmployee(new Employee("Stażysta", "stazysta@techcorp.com",
                "TechCorp", Position.INTERN, 3000.0, EmploymentStatus.ACTIVE));

        // When
        List<Employee> managers = employeeService.getAvailableManagers();

        // Then
        assertThat(managers).hasSize(3);
        assertThat(managers).extracting(Employee::getPosition)
                .containsExactlyInAnyOrder(Position.PRESIDENT, Position.VICE_PRESIDENT, Position.MANAGER);
    }

    private void createTestEmployees() throws Exception {
        employeeService.addEmployee(new Employee("Anna Nowak", "anna@techcorp.com",
                "TechCorp", Position.MANAGER, 9000.0, EmploymentStatus.ACTIVE));
        employeeService.addEmployee(new Employee("Jan Kowalski", "jan@techcorp.com",
                "TechCorp", Position.PROGRAMMER, 6000.0, EmploymentStatus.ACTIVE));
        employeeService.addEmployee(new Employee("Piotr Wiśniewski", "piotr@othercorp.com",
                "OtherCorp", Position.PROGRAMMER, 4000.0, EmploymentStatus.ACTIVE));
    }
}

// ===== TESTY WYJĄTKÓW =====
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